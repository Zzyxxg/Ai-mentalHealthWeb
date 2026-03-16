package com.example.mentalhealth.mapper;

import com.example.mentalhealth.entity.CounselorProfile;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CounselorProfileMapper {

    CounselorProfile selectById(@Param("id") Long id);

    CounselorProfile selectByUserId(@Param("userId") Long userId);

    List<CounselorProfile> selectList(@Param("keyword") String keyword);

    int countAll();

    int insert(CounselorProfile profile);

    int insertBatch(@Param("list") List<CounselorProfile> list);
}
