package com.example.mentalhealth.service.impl;

import com.example.mentalhealth.common.BizException;
import com.example.mentalhealth.common.ErrorCode;
import com.example.mentalhealth.common.PageResp;
import com.example.mentalhealth.dto.req.AssessmentSubmitReq;
import com.example.mentalhealth.dto.resp.AssessmentResp;
import com.example.mentalhealth.dto.resp.AssessmentScaleResp;
import com.example.mentalhealth.entity.Assessment;
import com.example.mentalhealth.enums.AssessmentScaleType;
import com.example.mentalhealth.mapper.AssessmentMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AssessmentServiceImpl implements com.example.mentalhealth.service.AssessmentService {

    private static final int MAX_PAGE_SIZE = 100;

    private static final List<AssessmentScaleResp.Option> DEFAULT_OPTIONS = List.of(
            option(0, "没有"),
            option(1, "几天"),
            option(2, "一半以上的天数"),
            option(3, "几乎每天")
    );

    private final AssessmentMapper assessmentMapper;

    public AssessmentServiceImpl(AssessmentMapper assessmentMapper) {
        this.assessmentMapper = assessmentMapper;
    }

    @Override
    public List<AssessmentScaleResp> listScales() {
        return List.of(buildScale(AssessmentScaleType.PHQ9), buildScale(AssessmentScaleType.GAD7));
    }

    @Override
    public AssessmentScaleResp getScale(String type) {
        AssessmentScaleType t = parseType(type);
        return buildScale(t);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AssessmentResp submit(Long userId, AssessmentSubmitReq req) {
        AssessmentScaleType type = parseType(req.getScaleType());
        AssessmentScaleResp scale = buildScale(type);
        int expected = scale.getQuestions().size();
        if (req.getAnswers() == null || req.getAnswers().size() != expected) {
            throw new BizException(ErrorCode.PARAM_ERROR, "answers数量不正确，期望 " + expected);
        }
        int total = 0;
        for (Integer a : req.getAnswers()) {
            if (a == null || a < 0 || a > 3) {
                throw new BizException(ErrorCode.PARAM_ERROR, "答案分值仅支持 0~3");
            }
            total += a;
        }

        String level = calcLevel(type, total);
        String suggestion = buildSuggestion(type, level);

        Assessment entity = new Assessment();
        entity.setUserId(userId);
        entity.setScaleType(type);
        entity.setTotalScore(total);
        entity.setLevel(level);
        entity.setSuggestion(suggestion);
        assessmentMapper.insert(entity);

        return toResp(entity);
    }

    @Override
    public PageResp<AssessmentResp> pageMy(Long userId, int pageNum, int pageSize, String scaleType) {
        int pn = Math.max(pageNum, 1);
        int ps = Math.min(Math.max(pageSize, 1), MAX_PAGE_SIZE);

        PageHelper.startPage(pn, ps);
        List<Assessment> list;
        if (scaleType != null && !scaleType.isBlank()) {
            AssessmentScaleType t = parseType(scaleType);
            list = assessmentMapper.selectByUserAndType(userId, t.name());
        } else {
            list = assessmentMapper.selectByUser(userId);
        }
        List<AssessmentResp> respList = list.stream().map(this::toResp).toList();
        PageInfo<Assessment> pageInfo = new PageInfo<>(list);
        return new PageResp<>(pageInfo.getTotal(), pn, ps, respList);
    }

    private AssessmentResp toResp(Assessment a) {
        AssessmentResp resp = new AssessmentResp();
        resp.setId(a.getId());
        resp.setUserId(a.getUserId());
        resp.setScaleType(a.getScaleType() == null ? null : a.getScaleType().name());
        resp.setTotalScore(a.getTotalScore());
        resp.setLevel(a.getLevel());
        resp.setSuggestion(a.getSuggestion());
        resp.setCreateTime(a.getCreateTime() == null ? null : a.getCreateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        return resp;
    }

    private AssessmentScaleType parseType(String type) {
        if (type == null || type.isBlank()) {
            throw new BizException(ErrorCode.PARAM_ERROR, "scaleType不能为空");
        }
        String t = type.trim().toUpperCase();
        try {
            return AssessmentScaleType.valueOf(t);
        } catch (Exception e) {
            throw new BizException(ErrorCode.PARAM_ERROR, "scaleType仅支持 PHQ9/GAD7");
        }
    }

    private AssessmentScaleResp buildScale(AssessmentScaleType type) {
        AssessmentScaleResp resp = new AssessmentScaleResp();
        resp.setType(type.name());
        resp.setName(type == AssessmentScaleType.PHQ9 ? "PHQ-9 抑郁筛查量表" : "GAD-7 焦虑筛查量表");
        List<String> questions = type == AssessmentScaleType.PHQ9 ? phq9Questions() : gad7Questions();
        List<AssessmentScaleResp.Question> qs = new ArrayList<>();
        for (int i = 0; i < questions.size(); i++) {
            AssessmentScaleResp.Question q = new AssessmentScaleResp.Question();
            q.setIndex(i + 1);
            q.setTitle(questions.get(i));
            q.setOptions(DEFAULT_OPTIONS);
            qs.add(q);
        }
        resp.setQuestions(qs);
        return resp;
    }

    private String calcLevel(AssessmentScaleType type, int score) {
        if (type == AssessmentScaleType.PHQ9) {
            if (score <= 4) return "MINIMAL";
            if (score <= 9) return "MILD";
            if (score <= 14) return "MODERATE";
            if (score <= 19) return "MODERATELY_SEVERE";
            return "SEVERE";
        }
        // GAD7
        if (score <= 4) return "MINIMAL";
        if (score <= 9) return "MILD";
        if (score <= 14) return "MODERATE";
        return "SEVERE";
    }

    private String buildSuggestion(AssessmentScaleType type, String level) {
        if ("SEVERE".equals(level) || "MODERATELY_SEVERE".equals(level)) {
            return "建议尽快联系专业心理咨询师或校内心理中心进行进一步评估与支持。";
        }
        if ("MODERATE".equals(level)) {
            return "建议关注近期情绪变化，保持规律作息，并可考虑预约咨询师进行进一步沟通。";
        }
        if ("MILD".equals(level)) {
            return "建议保持良好作息与运动，可尝试放松训练；若症状持续可寻求帮助。";
        }
        return "当前风险较低，建议继续保持健康的作息与情绪管理习惯。";
    }

    private static AssessmentScaleResp.Option option(int score, String label) {
        AssessmentScaleResp.Option o = new AssessmentScaleResp.Option();
        o.setScore(score);
        o.setLabel(label);
        return o;
    }

    private static List<String> phq9Questions() {
        return List.of(
                "做事时提不起劲或没有兴趣",
                "感到心情低落、沮丧或绝望",
                "入睡困难、睡不安或睡得太多",
                "感觉疲倦或没有精力",
                "食欲不振或暴饮暴食",
                "觉得自己很糟——或觉得自己让自己或家人失望",
                "对事物难以专注，例如阅读报纸或看电视",
                "动作或说话速度慢到别人注意到；或相反，坐立不安、比平时更烦躁",
                "觉得不如死了或想伤害自己"
        );
    }

    private static List<String> gad7Questions() {
        return List.of(
                "感到紧张、焦虑或坐立不安",
                "无法停止或控制担心",
                "对各种各样的事情担心过多",
                "很难放松",
                "坐立不安以至于难以静坐",
                "变得容易烦恼或易怒",
                "感到害怕，好像要发生可怕的事情"
        );
    }
}

