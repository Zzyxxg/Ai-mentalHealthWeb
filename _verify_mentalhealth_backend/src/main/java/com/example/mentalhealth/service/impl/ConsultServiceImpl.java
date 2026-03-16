package com.example.mentalhealth.service.impl;

import com.example.mentalhealth.common.BizException;
import com.example.mentalhealth.common.ErrorCode;
import com.example.mentalhealth.common.PageResp;
import com.example.mentalhealth.dto.req.ConsultAppointmentCreateReq;
import com.example.mentalhealth.dto.resp.ConsultAppointmentResp;
import com.example.mentalhealth.dto.resp.CounselorResp;
import com.example.mentalhealth.entity.ConsultAppointment;
import com.example.mentalhealth.entity.CounselorProfile;
import com.example.mentalhealth.enums.AppointmentStatus;
import com.example.mentalhealth.mapper.ConsultAppointmentMapper;
import com.example.mentalhealth.mapper.CounselorProfileMapper;
import com.example.mentalhealth.service.ConsultService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConsultServiceImpl implements ConsultService {

    private static final int MAX_PAGE_SIZE = 100;
    private static final long CACHE_CONSULTANT_TTL_SECONDS = 900;
    private static final long CACHE_APPOINTMENT_TTL_SECONDS = 1800;
    private static final long IDEMPOTENCY_TTL_SECONDS = 86400;

    private final CounselorProfileMapper counselorProfileMapper;
    private final ConsultAppointmentMapper consultAppointmentMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RedissonClient redissonClient;

    public ConsultServiceImpl(CounselorProfileMapper counselorProfileMapper,
                              ConsultAppointmentMapper consultAppointmentMapper,
                              RedisTemplate<String, Object> redisTemplate,
                              RedissonClient redissonClient) {
        this.counselorProfileMapper = counselorProfileMapper;
        this.consultAppointmentMapper = consultAppointmentMapper;
        this.redisTemplate = redisTemplate;
        this.redissonClient = redissonClient;
    }

    @Override
    public List<CounselorResp> listConsultants(String keyword) {
        String normalized = keyword == null ? "" : keyword.trim();
        String cacheKey = "mh:consultants:keyword:" + (normalized.isEmpty() ? "all" : normalized);
        
        Object cached = null;
        try {
            cached = redisTemplate.opsForValue().get(cacheKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        if (cached instanceof List) {
            // 需要进行类型检查，确保 list 中的元素是 CounselorResp
            List<?> list = (List<?>) cached;
            if (!list.isEmpty() && list.get(0) instanceof CounselorResp) {
                return (List<CounselorResp>) cached;
            }
        }

        try {
            List<CounselorProfile> list = counselorProfileMapper.selectList(normalized.isEmpty() ? null : normalized);
            List<CounselorResp> resp = list.stream().map(this::toResp).toList();
            try {
                redisTemplate.opsForValue().set(cacheKey, resp, ttlSecondsWithJitter(CACHE_CONSULTANT_TTL_SECONDS), TimeUnit.SECONDS);
            } catch (Exception e) {
                // Redis 错误不影响业务
                e.printStackTrace();
            }
            return resp;
        } catch (Exception e) {
            e.printStackTrace();
            throw new BizException(ErrorCode.INTERNAL_ERROR, "查询咨询师列表失败: " + e.getMessage());
        }
    }

    @Override
    public CounselorResp getConsultant(Long consultantId) {
        String cacheKey = "mh:consultant:id:" + consultantId;
        Object cached = null;
        try {
            cached = redisTemplate.opsForValue().get(cacheKey);
        } catch (Exception e) {
            // Redis 错误不影响业务
            e.printStackTrace();
        }
        if (cached instanceof CounselorResp) {
            return (CounselorResp) cached;
        }

        CounselorProfile profile = counselorProfileMapper.selectById(consultantId);
        if (profile == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "咨询师不存在");
        }
        CounselorResp resp = toResp(profile);
        redisTemplate.opsForValue().set(cacheKey, resp, ttlSecondsWithJitter(CACHE_CONSULTANT_TTL_SECONDS), TimeUnit.SECONDS);
        return resp;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ConsultAppointmentResp createAppointment(Long userId, ConsultAppointmentCreateReq req, String idempotencyKey) {
        CounselorProfile profile = counselorProfileMapper.selectByUserId(req.getCounselorUserId());
        if (profile == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "咨询师不存在");
        }

        String normalizedKey = idempotencyKey == null ? null : idempotencyKey.trim();
        if (normalizedKey != null && !normalizedKey.isEmpty()) {
            String redisKey = "mh:idempotency:consult-appointment:user:" + userId + ":" + normalizedKey;
            Object cachedId = redisTemplate.opsForValue().get(redisKey);
            if (cachedId instanceof Number) {
                return getAppointment(userId, ((Number) cachedId).longValue());
            }
        }

        String lockKey = "mh:lock:consult:slot:" + req.getCounselorUserId() + ":" + req.getStartTime();
        RLock lock = redissonClient.getLock(lockKey);
        boolean locked;
        try {
            locked = lock.tryLock(3, 30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BizException(ErrorCode.INTERNAL_ERROR, "系统繁忙，请稍后重试");
        }
        if (!locked) {
            throw new BizException(ErrorCode.CONFLICT, "当前时段预约繁忙，请稍后重试");
        }

        try {
            ConsultAppointment appointment = new ConsultAppointment();
            appointment.setUserId(userId);
            appointment.setCounselorUserId(req.getCounselorUserId());
            appointment.setStartTime(req.getStartTime());
            appointment.setDurationMinutes(req.getDurationMinutes());
            appointment.setStatus(AppointmentStatus.CREATED);
            appointment.setNote(req.getNote());
            appointment.setIdempotencyKey((normalizedKey == null || normalizedKey.isEmpty()) ? null : normalizedKey);

            try {
                consultAppointmentMapper.insert(appointment);
            } catch (DuplicateKeyException e) {
                if (normalizedKey == null || normalizedKey.isEmpty()) {
                    throw e;
                }
                ConsultAppointment existing = consultAppointmentMapper.selectByUserAndIdempotencyKey(userId, normalizedKey);
                if (existing == null) {
                    throw new BizException(ErrorCode.CONFLICT, "重复提交，请稍后重试");
                }
                return toResp(existing);
            }

            if (normalizedKey != null && !normalizedKey.isEmpty()) {
                String redisKey = "mh:idempotency:consult-appointment:user:" + userId + ":" + normalizedKey;
                redisTemplate.opsForValue().set(redisKey, appointment.getId(), IDEMPOTENCY_TTL_SECONDS, TimeUnit.SECONDS);
            }
            cacheAppointment(appointment);
            return toResp(appointment);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public ConsultAppointmentResp getAppointment(Long userId, Long appointmentId) {
        ConsultAppointment appt = getAppointmentEntityById(appointmentId);
        if (appt == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "预约不存在");
        }
        if (!Objects.equals(appt.getUserId(), userId)) {
            throw new BizException(ErrorCode.FORBIDDEN, "无权限访问该预约");
        }
        return toResp(appt);
    }

    @Override
    public PageResp<ConsultAppointmentResp> pageAppointments(Long userId, int pageNum, int pageSize, String status) {
        int pn = Math.max(pageNum, 1);
        int ps = Math.min(Math.max(pageSize, 1), MAX_PAGE_SIZE);
        Integer statusCode = parseStatusCode(status);

        PageHelper.startPage(pn, ps);
        List<ConsultAppointment> list = consultAppointmentMapper.selectByUser(userId, statusCode);
        List<ConsultAppointmentResp> respList = list.stream().map(this::toResp).toList();
        PageInfo<ConsultAppointment> pageInfo = new PageInfo<>(list);
        return new PageResp<>(pageInfo.getTotal(), pn, ps, respList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ConsultAppointmentResp cancelAppointment(Long userId, Long appointmentId) {
        ConsultAppointment appt = consultAppointmentMapper.selectById(appointmentId);
        if (appt == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "预约不存在");
        }
        if (!Objects.equals(appt.getUserId(), userId)) {
            throw new BizException(ErrorCode.FORBIDDEN, "无权限访问该预约");
        }
        if (appt.getStatus() == AppointmentStatus.CANCELED) {
            return toResp(appt);
        }

        consultAppointmentMapper.updateStatus(appointmentId, userId, AppointmentStatus.CANCELED.getCode());
        redisTemplate.delete("mh:consult:appointment:id:" + appointmentId);
        ConsultAppointment latest = consultAppointmentMapper.selectById(appointmentId);
        if (latest == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "预约不存在");
        }
        cacheAppointment(latest);
        return toResp(latest);
    }

    private ConsultAppointmentResp toResp(ConsultAppointment appt) {
        return new ConsultAppointmentResp(
                appt.getId(),
                appt.getUserId(),
                appt.getCounselorUserId(),
                appt.getStartTime(),
                appt.getDurationMinutes(),
                appt.getStatus().name(),
                appt.getNote()
        );
    }

    private CounselorResp toResp(CounselorProfile profile) {
        return new CounselorResp(profile.getId(), profile.getUserId(), profile.getRealName(), profile.getTitle(), profile.getExpertise(), profile.getIntro());
    }

    private ConsultAppointment getAppointmentEntityById(Long appointmentId) {
        String cacheKey = "mh:consult:appointment:id:" + appointmentId;
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached instanceof ConsultAppointment) {
            return (ConsultAppointment) cached;
        }
        ConsultAppointment appt = consultAppointmentMapper.selectById(appointmentId);
        if (appt != null) {
            cacheAppointment(appt);
        }
        return appt;
    }

    private void cacheAppointment(ConsultAppointment appt) {
        String cacheKey = "mh:consult:appointment:id:" + appt.getId();
        redisTemplate.opsForValue().set(cacheKey, appt, ttlSecondsWithJitter(CACHE_APPOINTMENT_TTL_SECONDS), TimeUnit.SECONDS);
    }

    private long ttlSecondsWithJitter(long baseSeconds) {
        long jitter = ThreadLocalRandom.current().nextLong(0, 300);
        return baseSeconds + jitter;
    }

    private Integer parseStatusCode(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        String s = status.trim().toUpperCase();
        if ("CREATED".equals(s)) {
            return AppointmentStatus.CREATED.getCode();
        }
        if ("CANCELED".equals(s)) {
            return AppointmentStatus.CANCELED.getCode();
        }
        if ("CONFIRMED".equals(s)) {
            return AppointmentStatus.CONFIRMED.getCode();
        }
        if ("COMPLETED".equals(s)) {
            return AppointmentStatus.COMPLETED.getCode();
        }
        throw new BizException(ErrorCode.PARAM_ERROR, "status 仅支持 CREATED/CANCELED/CONFIRMED/COMPLETED");
    }
}
