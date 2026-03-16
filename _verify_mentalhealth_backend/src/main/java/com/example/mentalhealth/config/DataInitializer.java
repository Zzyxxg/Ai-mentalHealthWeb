package com.example.mentalhealth.config;

import com.example.mentalhealth.entity.CounselorProfile;
import com.example.mentalhealth.entity.User;
import com.example.mentalhealth.mapper.CounselorProfileMapper;
import com.example.mentalhealth.mapper.UserMapper;
import java.util.List;
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
        user.setNickname("测试学生");
        user.setAvatarUrl("https://api.dicebear.com/7.x/avataaars/svg?seed=demo");
        userMapper.insert(user);
    }

    private void initCounselors() {
        if (counselorProfileMapper.countAll() > 0) {
            return;
        }

        createCounselor("counselor1", "李医生", "国家二级心理咨询师", "情绪管理, 焦虑", "擅长CBT认知行为疗法，帮助来访者解决焦虑、抑郁等情绪问题。");
        createCounselor("counselor2", "王老师", "临床心理师", "亲密关系, 睡眠", "拥有丰富的临床经验，专注于解决家庭关系、情感困扰以及睡眠障碍。");
        createCounselor("counselor3", "张博士", "心理治疗师", "抑郁, 压力", "心理学博士，擅长压力管理、个人成长以及深度心理咨询。");
    }

    private void createCounselor(String username, String realName, String title, String expertise, String intro) {
        User user = userMapper.selectByUsername(username);
        if (user == null) {
            user = new User();
            user.setUsername(username);
            user.setPasswordHash(passwordEncoder.encode("123456"));
            user.setRole("CONSULTANT");
            user.setNickname(realName);
            user.setAvatarUrl("https://api.dicebear.com/7.x/avataaars/svg?seed=" + username);
            userMapper.insert(user);
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
