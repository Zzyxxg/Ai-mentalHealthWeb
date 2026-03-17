package com.example.mentalhealth.service.impl;

import com.example.mentalhealth.common.BizException;
import com.example.mentalhealth.common.ErrorCode;
import com.example.mentalhealth.common.PageResp;
import com.example.mentalhealth.dto.req.ConsultAppointmentCreateReq;
import com.example.mentalhealth.dto.req.ConsultMessageCreateReq;
import com.example.mentalhealth.dto.req.ConsultThreadCreateReq;
import com.example.mentalhealth.dto.req.ScheduleSlotCreateReq;
import com.example.mentalhealth.dto.resp.ConsultAppointmentResp;
import com.example.mentalhealth.dto.resp.ConsultMessageResp;
import com.example.mentalhealth.dto.resp.ConsultThreadResp;
import com.example.mentalhealth.dto.resp.CounselorResp;
import com.example.mentalhealth.dto.resp.ScheduleSlotResp;
import com.example.mentalhealth.entity.ConsultAppointment;
import com.example.mentalhealth.entity.ConsultMessage;
import com.example.mentalhealth.entity.ConsultThread;
import com.example.mentalhealth.entity.CounselorProfile;
import com.example.mentalhealth.entity.ScheduleSlot;
import com.example.mentalhealth.enums.AppointmentStatus;
import com.example.mentalhealth.enums.ConsultThreadStatus;
import com.example.mentalhealth.enums.NotificationType;
import com.example.mentalhealth.enums.ScheduleSlotStatus;
import com.example.mentalhealth.mapper.ConsultAppointmentMapper;
import com.example.mentalhealth.mapper.ConsultMessageMapper;
import com.example.mentalhealth.mapper.ConsultThreadMapper;
import com.example.mentalhealth.mapper.CounselorProfileMapper;
import com.example.mentalhealth.mapper.NotificationMapper;
import com.example.mentalhealth.mapper.ScheduleSlotMapper;
import com.example.mentalhealth.service.ConsultService;
import com.example.mentalhealth.service.AuditLogService;
import com.example.mentalhealth.security.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
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
    private final ConsultThreadMapper consultThreadMapper;
    private final ConsultMessageMapper consultMessageMapper;
    private final ScheduleSlotMapper scheduleSlotMapper;
    private final NotificationMapper notificationMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RedissonClient redissonClient;
    private final AuditLogService auditLogService;

    public ConsultServiceImpl(CounselorProfileMapper counselorProfileMapper,
                              ConsultAppointmentMapper consultAppointmentMapper,
                              ConsultThreadMapper consultThreadMapper,
                              ConsultMessageMapper consultMessageMapper,
                              ScheduleSlotMapper scheduleSlotMapper,
                              NotificationMapper notificationMapper,
                              RedisTemplate<String, Object> redisTemplate,
                              RedissonClient redissonClient,
                              AuditLogService auditLogService) {
        this.counselorProfileMapper = counselorProfileMapper;
        this.consultAppointmentMapper = consultAppointmentMapper;
        this.consultThreadMapper = consultThreadMapper;
        this.consultMessageMapper = consultMessageMapper;
        this.scheduleSlotMapper = scheduleSlotMapper;
        this.notificationMapper = notificationMapper;
        this.redisTemplate = redisTemplate;
        this.redissonClient = redissonClient;
        this.auditLogService = auditLogService;
    }

    private UserPrincipal getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal) {
            return (UserPrincipal) auth.getPrincipal();
        }
        return null;
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

        // 转换时间并校验未来时间
        LocalDateTime startTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(req.getStartTime()), ZoneId.systemDefault());
        if (startTime.isBefore(LocalDateTime.now())) {
            throw new BizException(ErrorCode.PARAM_ERROR, "开始时间必须在未来");
        }
        LocalDate slotDate = startTime.toLocalDate();
        LocalTime slotStartTime = startTime.toLocalTime();
        LocalTime slotEndTime = slotStartTime.plusMinutes(req.getDurationMinutes());

        String normalizedKey = idempotencyKey == null ? null : idempotencyKey.trim();
        if (normalizedKey != null && !normalizedKey.isEmpty()) {
            String redisKey = "mh:idempotency:consult-appointment:user:" + userId + ":" + normalizedKey;
            Object cachedId = redisTemplate.opsForValue().get(redisKey);
            if (cachedId instanceof Number) {
                return getAppointment(userId, ((Number) cachedId).longValue());
            }
        }

        String lockKey = "mh:lock:consult:slot:" + req.getCounselorUserId() + ":" + slotDate;
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
            List<ScheduleSlot> slots = scheduleSlotMapper.selectAvailableByCounselorAndTime(req.getCounselorUserId(), slotDate, slotStartTime, slotEndTime);
            if (slots == null || slots.isEmpty()) {
                throw new BizException(ErrorCode.NOT_FOUND, "该时段未配置、已预约或不存在");
            }
            
            // 挑选最合适的时段（目前逻辑是只要包含就行，取第一个）
            ScheduleSlot slot = slots.get(0);
            
            // 准备时段拆分
            List<ScheduleSlot> toInsert = new ArrayList<>();
            
            // 1. 如果预约开始时间晚于时段开始时间，拆分出前半段
            if (slot.getStartTime().isBefore(slotStartTime)) {
                ScheduleSlot pre = new ScheduleSlot();
                pre.setCounselorUserId(slot.getCounselorUserId());
                pre.setDate(slot.getDate());
                pre.setStartTime(slot.getStartTime());
                pre.setEndTime(slotStartTime);
                pre.setStatus(ScheduleSlotStatus.AVAILABLE);
                toInsert.add(pre);
            }
            
            // 2. 如果预约结束时间早于时段结束时间，拆分出后半段
            if (slot.getEndTime().isAfter(slotEndTime)) {
                ScheduleSlot post = new ScheduleSlot();
                post.setCounselorUserId(slot.getCounselorUserId());
                post.setDate(slot.getDate());
                post.setStartTime(slotEndTime);
                post.setEndTime(slot.getEndTime());
                post.setStatus(ScheduleSlotStatus.AVAILABLE);
                toInsert.add(post);
            }
            
            // 3. 更新当前时段为预约时段并设为 OCCUPIED
            scheduleSlotMapper.updateTimeAndStatus(slot.getId(), slotStartTime, slotEndTime, ScheduleSlotStatus.OCCUPIED.name());
            
            // 4. 插入拆分出来的 AVAILABLE 时段
            if (!toInsert.isEmpty()) {
                scheduleSlotMapper.batchInsert(toInsert);
            }

            ConsultAppointment appointment = new ConsultAppointment();
            appointment.setUserId(userId);
            appointment.setCounselorUserId(req.getCounselorUserId());
            appointment.setSlotId(slot.getId());
            appointment.setStartTime(startTime);
            appointment.setDurationMinutes(req.getDurationMinutes());
            appointment.setStatus(AppointmentStatus.CONFIRMED);
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
            writeAppointmentNotification(userId, appointment.getCounselorUserId(), appointment.getId(), true);
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
    public PageResp<ConsultAppointmentResp> pageAppointments(Long userId, String role, int pageNum, int pageSize, String status) {
        int pn = Math.max(pageNum, 1);
        int ps = Math.min(Math.max(pageSize, 1), MAX_PAGE_SIZE);
        Integer statusCode = parseStatusCode(status);

        PageHelper.startPage(pn, ps);
        List<ConsultAppointment> list;
        if ("CONSULTANT".equals(role)) {
            list = consultAppointmentMapper.selectByCounselor(userId, statusCode);
        } else {
            // Default to STUDENT view
            list = consultAppointmentMapper.selectByUser(userId, statusCode);
        }
        
        List<ConsultAppointmentResp> respList = list.stream().map(this::toResp).toList();
        PageInfo<ConsultAppointment> pageInfo = new PageInfo<>(list);
        return new PageResp<>(pageInfo.getTotal(), pn, ps, respList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ConsultAppointmentResp completeAppointment(Long userId, Long appointmentId, String note) {
        ConsultAppointment appt = consultAppointmentMapper.selectById(appointmentId);
        if (appt == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "预约不存在");
        }
        if (!Objects.equals(appt.getCounselorUserId(), userId)) {
            throw new BizException(ErrorCode.FORBIDDEN, "无权限操作该预约");
        }
        if (appt.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new BizException(ErrorCode.PARAM_ERROR, "仅确认状态的预约可标记完成");
        }

        consultAppointmentMapper.updateStatusAndNote(appointmentId, AppointmentStatus.COMPLETED.getCode(), note);
        redisTemplate.delete("mh:consult:appointment:id:" + appointmentId);
        ConsultAppointment latest = consultAppointmentMapper.selectById(appointmentId);
        cacheAppointment(latest);
        return toResp(latest);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createScheduleSlots(Long counselorUserId, List<ScheduleSlotCreateReq> reqs) {
        if (reqs == null || reqs.isEmpty()) {
            return;
        }
        
        List<ScheduleSlot> slots = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (ScheduleSlotCreateReq req : reqs) {
            String key = req.getDate() + "|" + req.getStartTime() + "|" + req.getEndTime();
            if (!seen.add(key)) {
                throw new BizException(ErrorCode.PARAM_ERROR, "存在重复时段: " + key);
            }
            if (!req.getEndTime().isAfter(req.getStartTime())) {
                throw new BizException(ErrorCode.PARAM_ERROR, "结束时间必须晚于开始时间");
            }
            ScheduleSlot slot = new ScheduleSlot();
            slot.setCounselorUserId(counselorUserId);
            slot.setDate(req.getDate());
            slot.setStartTime(req.getStartTime());
            slot.setEndTime(req.getEndTime());
            slot.setStatus(ScheduleSlotStatus.AVAILABLE);
            slots.add(slot);
        }
        
        try {
            scheduleSlotMapper.batchInsert(slots);
        } catch (DuplicateKeyException e) {
            throw new BizException(ErrorCode.CONFLICT, "排班时段重复（同一咨询师同一时间段唯一）");
        }
    }

    @Override
    public List<ScheduleSlotResp> listScheduleSlots(Long counselorUserId, Long startDate, Long endDate) {
        LocalDate start = LocalDate.ofInstant(Instant.ofEpochMilli(startDate), ZoneId.systemDefault());
        LocalDate end = LocalDate.ofInstant(Instant.ofEpochMilli(endDate), ZoneId.systemDefault());
        
        List<ScheduleSlot> slots = scheduleSlotMapper.selectByCounselorAndDateRange(counselorUserId, start, end);
        return slots.stream().map(this::toResp).toList();
    }

    private ScheduleSlotResp toResp(ScheduleSlot slot) {
        ScheduleSlotResp resp = new ScheduleSlotResp();
        resp.setId(slot.getId());
        resp.setCounselorUserId(slot.getCounselorUserId());
        resp.setDate(slot.getDate());
        resp.setStartTime(slot.getStartTime());
        resp.setEndTime(slot.getEndTime());
        resp.setStatus(slot.getStatus());
        return resp;
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
        if (appt.getStatus() == AppointmentStatus.COMPLETED) {
            throw new BizException(ErrorCode.PARAM_ERROR, "已完成的预约不可取消");
        }

        consultAppointmentMapper.updateStatus(appointmentId, userId, AppointmentStatus.CANCELED.getCode());
        if (appt.getSlotId() != null) {
            scheduleSlotMapper.updateStatusIfCurrent(appt.getSlotId(), ScheduleSlotStatus.OCCUPIED.name(), ScheduleSlotStatus.AVAILABLE.name());
        }
        writeAppointmentNotification(appt.getUserId(), appt.getCounselorUserId(), appt.getId(), false);
        redisTemplate.delete("mh:consult:appointment:id:" + appointmentId);
        ConsultAppointment latest = consultAppointmentMapper.selectById(appointmentId);
        if (latest == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "预约不存在");
        }
        cacheAppointment(latest);
        
        UserPrincipal currentUser = getCurrentUser();
        if (currentUser != null) {
            auditLogService.record(currentUser.getUserId(), currentUser.getUsername(), "APPOINTMENT_CANCEL", "APPOINTMENT", appointmentId, "Canceled appointment");
        }
        
        return toResp(latest);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateScheduleSlotStatus(Long counselorUserId, Long slotId, String status) {
        if (status == null || status.isBlank()) {
            throw new BizException(ErrorCode.PARAM_ERROR, "status不能为空");
        }
        String target = status.trim().toUpperCase();
        if (!ScheduleSlotStatus.AVAILABLE.name().equals(target) && !ScheduleSlotStatus.UNAVAILABLE.name().equals(target)) {
            throw new BizException(ErrorCode.PARAM_ERROR, "status仅支持 AVAILABLE/UNAVAILABLE");
        }

        ScheduleSlot slot = scheduleSlotMapper.selectById(slotId);
        if (slot == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "排班时段不存在");
        }
        if (!Objects.equals(slot.getCounselorUserId(), counselorUserId)) {
            throw new BizException(ErrorCode.FORBIDDEN, "无权限操作该排班时段");
        }
        
        LocalDateTime slotStart = LocalDateTime.of(slot.getDate(), slot.getStartTime());
        // 允许修改过去时段，以应对特殊场景
        // if (slotStart.isBefore(LocalDateTime.now())) {
        //     throw new BizException(ErrorCode.PARAM_ERROR, "仅允许修改未来时段");
        // }

        scheduleSlotMapper.updateStatus(slotId, target);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteScheduleSlot(Long counselorUserId, Long slotId) {
        ScheduleSlot slot = scheduleSlotMapper.selectById(slotId);
        if (slot == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "排班时段不存在");
        }
        if (!Objects.equals(slot.getCounselorUserId(), counselorUserId)) {
            throw new BizException(ErrorCode.FORBIDDEN, "无权限操作该排班时段");
        }
        
        LocalDateTime slotStart = LocalDateTime.of(slot.getDate(), slot.getStartTime());
        // 允许删除过去时段，以应对特殊场景
        // if (slotStart.isBefore(LocalDateTime.now())) {
        //     throw new BizException(ErrorCode.PARAM_ERROR, "仅允许删除未来时段");
        // }
        scheduleSlotMapper.deleteById(slotId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ConsultThreadResp createThread(Long studentUserId, ConsultThreadCreateReq req) {
        CounselorProfile profile = counselorProfileMapper.selectByUserId(req.getCounselorUserId());
        if (profile == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "咨询师不存在");
        }

        ConsultThread thread = new ConsultThread();
        thread.setStudentUserId(studentUserId);
        thread.setCounselorUserId(req.getCounselorUserId());
        thread.setStatus(ConsultThreadStatus.UNPROCESSED);
        thread.setTopic(req.getTopic());
        thread.setHidden(false);

        consultThreadMapper.insert(thread);

        ConsultMessage message = new ConsultMessage();
        message.setThreadId(thread.getId());
        message.setSenderRole("STUDENT");
        message.setContent(req.getContent());
        message.setHidden(false);

        consultMessageMapper.insert(message);

        // 通知咨询师：收到新的咨询消息
        writeStudentMessageNotification(thread.getCounselorUserId(), thread.getId());

        ConsultThreadResp resp = toResp(thread);
        resp.setMessages(Collections.singletonList(toResp(message)));
        resp.setCounselorName(profile.getRealName());
        return resp;
    }

    @Override
    public ConsultThreadResp getThread(Long userId, Long threadId) {
        ConsultThread thread = consultThreadMapper.selectById(threadId);
        if (thread == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "咨询会话不存在");
        }
        // 鉴权：只有参与者或管理员可以查看
        if (!Objects.equals(thread.getStudentUserId(), userId) && !Objects.equals(thread.getCounselorUserId(), userId)) {
            // 这里简化处理，如果是管理员也可以看，后续可以根据角色判断
            // throw new BizException(ErrorCode.FORBIDDEN, "无权限访问该会话");
        }

        List<ConsultMessage> messages = consultMessageMapper.selectByThreadId(threadId);
        ConsultThreadResp resp = toResp(thread);
        resp.setMessages(messages.stream().map(this::toResp).toList());

        CounselorProfile profile = counselorProfileMapper.selectByUserId(thread.getCounselorUserId());
        if (profile != null) {
            resp.setCounselorName(profile.getRealName());
        }

        return resp;
    }

    @Override
    public PageResp<ConsultThreadResp> listThreads(Long userId, String role, int pageNum, int pageSize, String status) {
        int pn = Math.max(pageNum, 1);
        int ps = Math.min(Math.max(pageSize, 1), MAX_PAGE_SIZE);

        PageHelper.startPage(pn, ps);
        List<ConsultThread> threads;
        if ("STUDENT".equals(role)) {
            threads = consultThreadMapper.selectByStudent(userId);
        } else if ("CONSULTANT".equals(role)) {
            threads = consultThreadMapper.selectByCounselor(userId, status);
        } else {
            // ADMIN or others
            threads = Collections.emptyList();
        }

        List<ConsultThreadResp> respList = threads.stream().map(t -> {
            ConsultThreadResp resp = toResp(t);
            CounselorProfile profile = counselorProfileMapper.selectByUserId(t.getCounselorUserId());
            if (profile != null) {
                resp.setCounselorName(profile.getRealName());
            }
            return resp;
        }).toList();

        PageInfo<ConsultThread> pageInfo = new PageInfo<>(threads);
        return new PageResp<>(pageInfo.getTotal(), pn, ps, respList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ConsultMessageResp sendMessage(Long userId, String role, ConsultMessageCreateReq req) {
        ConsultThread thread = consultThreadMapper.selectById(req.getThreadId());
        if (thread == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "咨询会话不存在");
        }

        // 校验权限
        if ("STUDENT".equals(role) && !Objects.equals(thread.getStudentUserId(), userId)) {
            throw new BizException(ErrorCode.FORBIDDEN, "无权限在该会话发送消息");
        }
        if ("CONSULTANT".equals(role) && !Objects.equals(thread.getCounselorUserId(), userId)) {
            throw new BizException(ErrorCode.FORBIDDEN, "无权限在该会话发送消息");
        }

        ConsultMessage message = new ConsultMessage();
        message.setThreadId(req.getThreadId());
        message.setSenderRole(role);
        message.setContent(req.getContent());
        message.setHidden(false);

        consultMessageMapper.insert(message);

        // 学生发送消息：通知咨询师
        if ("STUDENT".equals(role)) {
            writeStudentMessageNotification(thread.getCounselorUserId(), thread.getId());
        }

        // 如果是咨询师回复，且状态是 UNPROCESSED，则改为 PROCESSING
        if ("CONSULTANT".equals(role) && thread.getStatus() == ConsultThreadStatus.UNPROCESSED) {
            consultThreadMapper.updateStatus(thread.getId(), ConsultThreadStatus.PROCESSING.name());
            writeConsultantRepliedNotification(thread.getStudentUserId(), thread.getId());
        }

        return toResp(message);
    }

    private void writeAppointmentNotification(Long studentUserId, Long counselorUserId, Long appointmentId, boolean created) {
        String type = created ? NotificationType.APPOINTMENT_CREATED.name() : NotificationType.APPOINTMENT_CANCELED.name();
        String title = created ? "预约成功" : "预约已取消";
        String content = created ? ("你的预约已确认（ID=" + appointmentId + "）") : ("你的预约已取消（ID=" + appointmentId + "）");

        com.example.mentalhealth.entity.Notification n1 = new com.example.mentalhealth.entity.Notification();
        n1.setReceiverUserId(studentUserId);
        n1.setType(NotificationType.valueOf(type));
        n1.setTitle(title);
        n1.setContent(content);
        n1.setReadFlag(false);
        notificationMapper.insert(n1);

        com.example.mentalhealth.entity.Notification n2 = new com.example.mentalhealth.entity.Notification();
        n2.setReceiverUserId(counselorUserId);
        n2.setType(NotificationType.valueOf(type));
        n2.setTitle(title);
        n2.setContent((created ? "有新的预约（" : "预约已取消（") + "ID=" + appointmentId + "）");
        n2.setReadFlag(false);
        notificationMapper.insert(n2);
    }

    private void writeConsultantRepliedNotification(Long studentUserId, Long threadId) {
        com.example.mentalhealth.entity.Notification n = new com.example.mentalhealth.entity.Notification();
        n.setReceiverUserId(studentUserId);
        n.setType(NotificationType.CONSULTANT_REPLIED);
        n.setTitle("咨询师已回复");
        n.setContent("你的咨询已收到回复（threadId=" + threadId + "）");
        n.setReadFlag(false);
        notificationMapper.insert(n);
    }

    private void writeStudentMessageNotification(Long counselorUserId, Long threadId) {
        com.example.mentalhealth.entity.Notification n = new com.example.mentalhealth.entity.Notification();
        n.setReceiverUserId(counselorUserId);
        n.setType(NotificationType.STUDENT_MESSAGE);
        n.setTitle("新的咨询消息");
        n.setContent("你有新的咨询消息（threadId=" + threadId + "）");
        n.setReadFlag(false);
        notificationMapper.insert(n);
    }

    @Override
    public void closeThread(Long userId, Long threadId) {
        ConsultThread thread = consultThreadMapper.selectById(threadId);
        if (thread == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "咨询会话不存在");
        }
        if (!Objects.equals(thread.getCounselorUserId(), userId)) {
            throw new BizException(ErrorCode.FORBIDDEN, "无权限结束该会话");
        }
        if (thread.getStatus() == ConsultThreadStatus.CLOSED) {
            return;
        }
        consultThreadMapper.updateStatus(threadId, ConsultThreadStatus.CLOSED.name());
        
        UserPrincipal currentUser = getCurrentUser();
        if (currentUser != null) {
            auditLogService.record(currentUser.getUserId(), currentUser.getUsername(), "THREAD_CLOSE", "THREAD", threadId, "Closed thread");
        }
    }

    @Override
    public void hideThread(Long threadId, String reason) {
        consultThreadMapper.updateHidden(threadId, true, reason);
    }

    @Override
    public void hideMessage(Long messageId, String reason) {
        consultMessageMapper.updateHidden(messageId, true, reason);
    }

    private ConsultThreadResp toResp(ConsultThread thread) {
        ConsultThreadResp resp = new ConsultThreadResp();
        resp.setId(thread.getId());
        resp.setStudentUserId(thread.getStudentUserId());
        resp.setCounselorUserId(thread.getCounselorUserId());
        resp.setStatus(thread.getStatus().name());
        resp.setTopic(thread.getTopic());
        
        // 尝试获取最新的一条消息内容作为预览
        List<ConsultMessage> msgs = consultMessageMapper.selectByThreadId(thread.getId());
        if (msgs != null && !msgs.isEmpty()) {
            resp.setContent(msgs.get(msgs.size() - 1).getContent());
        }
        
        resp.setHidden(thread.getHidden());
        resp.setHiddenReason(thread.getHiddenReason());
        resp.setCreateTime(thread.getCreateTime() == null ? null : thread.getCreateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        resp.setUpdateTime(thread.getUpdateTime() == null ? null : thread.getUpdateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        return resp;
    }

    private ConsultMessageResp toResp(ConsultMessage message) {
        ConsultMessageResp resp = new ConsultMessageResp();
        resp.setId(message.getId());
        resp.setThreadId(message.getThreadId());
        resp.setSenderRole(message.getSenderRole());
        resp.setContent(message.getContent());
        resp.setHidden(message.getHidden());
        resp.setHiddenReason(message.getHiddenReason());
        resp.setCreateTime(message.getCreateTime() == null ? null : message.getCreateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        return resp;
    }

    private ConsultAppointmentResp toResp(ConsultAppointment appt) {
        return new ConsultAppointmentResp(
                appt.getId(),
                appt.getUserId(),
                appt.getCounselorUserId(),
                appt.getSlotId(),
                appt.getStartTime() == null ? null : appt.getStartTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
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
