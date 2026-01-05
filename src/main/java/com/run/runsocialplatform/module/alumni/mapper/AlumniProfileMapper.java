package com.run.runsocialplatform.module.alumni.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.run.runsocialplatform.module.alumni.model.AlumniInfo;
import com.run.runsocialplatform.module.alumni.model.dto.AlumniSearchDTO;
import com.run.runsocialplatform.module.alumni.model.vo.AlumniListItemVO;
import com.run.runsocialplatform.module.alumni.model.vo.AlumniProfileVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface AlumniProfileMapper extends BaseMapper<AlumniInfo> {

    /**
     * 根据用户ID查询校友信息
     */
    @Select("SELECT a.*, u.avatar, u.username, u.email " +
            "FROM alumni_info a " +
            "LEFT JOIN user u ON a.user_id = u.id " +
            "WHERE a.user_id = #{userId}")
    AlumniInfo selectWithUserInfo(@Param("userId") Long userId);

    /**
     * 搜索校友列表
     */
    IPage<AlumniListItemVO> searchAlumniList(Page<AlumniListItemVO> page,
                                             @Param("searchDTO") AlumniSearchDTO searchDTO,
                                             @Param("currentUserId") Long currentUserId);

    /**
     * 根据ID查询校友详情（包含用户信息）
     */
    AlumniProfileVO selectAlumniDetail(@Param("alumniId") Long alumniId,
                                       @Param("currentUserId") Long currentUserId);

    /**
     * 检查学号是否已存在
     */
    @Select("SELECT COUNT(*) FROM alumni_info WHERE student_id = #{studentId} " +
            "AND user_id != #{userId}")
    int countByStudentIdExcludeUser(@Param("studentId") String studentId,
                                    @Param("userId") Long userId);
}