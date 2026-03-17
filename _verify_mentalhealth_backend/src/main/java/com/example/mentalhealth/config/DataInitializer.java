package com.example.mentalhealth.config;

import com.example.mentalhealth.entity.CounselorProfile;
import com.example.mentalhealth.entity.User;
import com.example.mentalhealth.mapper.CounselorProfileMapper;
import com.example.mentalhealth.mapper.UserMapper;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataInitializer implements ApplicationRunner {

    private final UserMapper userMapper;
    private final CounselorProfileMapper counselorProfileMapper;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserMapper userMapper, CounselorProfileMapper counselorProfileMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.counselorProfileMapper = counselorProfileMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void run(ApplicationArguments args) {
        initDemoUser();
        initCounselors();
        initBulkStudents();
        initBulkCounselors();
        initAdmins();
    }

    private void initDemoUser() {
        User existing = userMapper.selectByUsername("demo");
        if (existing != null) {
            return;
        }
        User user = new User();
        user.setUsername("demo");
        user.setPasswordHash(passwordEncoder.encode("demo1234"));
        user.setRole("STUDENT");
        user.setStatus("ENABLED");
        user.setNickname("测试学生");
        user.setAvatarUrl("https://api.dicebear.com/7.x/avataaars/svg?seed=demo");
        userMapper.insert(user);
    }

    private void initCounselors() {
        createCounselor("counselor1", "李医生", "国家二级心理咨询师", "情绪管理, 焦虑", "擅长CBT认知行为疗法，帮助来访者解决焦虑、抑郁等情绪问题。");
        createCounselor("counselor2", "王老师", "临床心理师", "亲密关系, 睡眠", "拥有丰富的临床经验，专注于解决家庭关系、情感困扰以及睡眠障碍。");
        createCounselor("counselor3", "张博士", "心理治疗师", "抑郁, 压力", "心理学博士，擅长压力管理、个人成长以及深度心理咨询。");
    }

    private void initBulkStudents() {
        for (int i = 1; i <= 100; i++) {
            String username = String.format("student%03d", i);
            String nickname = String.format("学生%03d", i);
            createUserIfAbsent(username, "STUDENT", nickname);
        }
    }

    private void initBulkCounselors() {
        String[] titles = new String[] {"心理咨询师", "临床心理师", "心理治疗师", "国家二级心理咨询师"};
        String[] expertises = new String[] {"情绪管理", "压力管理", "亲密关系", "学习与成长", "睡眠问题", "焦虑", "抑郁"};

        for (int i = 1; i <= 50; i++) {
            String username = String.format("counselor%03d", i);
            String realName = String.format("咨询师%03d", i);
            String title = titles[i % titles.length];
            String expertise = expertises[i % expertises.length];
            String intro = "欢迎预约咨询，我会尽力为你提供帮助。";
            createCounselor(username, realName, title, expertise, intro);
        }
    }

    private void initAdmins() {
        for (int i = 1; i <= 3; i++) {
            String username = String.format("admin%02d", i);
            String nickname = String.format("管理员%02d", i);
            createUserIfAbsent(username, "ADMIN", nickname);
        }
    }

    private User createUserIfAbsent(String username, String role, String nickname) {
        User existing = userMapper.selectByUsername(username);
        if (existing != null) {
            return existing;
        }

        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode("123456"));
        user.setRole(role);
        user.setStatus("ENABLED");
        user.setNickname(nickname);
        user.setAvatarUrl("https://api.dicebear.com/7.x/avataaars/svg?seed=" + username);
        userMapper.insert(user);
        return user;
    }

    private void createCounselor(String username, String realName, String title, String expertise, String intro) {
        User user = createUserIfAbsent(username, "CONSULTANT", realName);
        CounselorProfile exists = counselorProfileMapper.selectByUserId(user.getId());
        if (exists != null) {
            return;
        }

        CounselorProfile profile = new CounselorProfile();
        profile.setUserId(user.getId());
        profile.setRealName(realName);
        profile.setTitle(title);
        profile.setExpertise(expertise);
        profile.setIntro(intro);
        counselorProfileMapper.insert(profile);
    }
}
