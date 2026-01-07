package com.run.runsocialplatform.module.alumni.service;



import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.run.runsocialplatform.module.alumni.model.entity.AlumniInfo;
import com.run.runsocialplatform.module.alumni.model.dto.AlumniProfileDTO;
import com.run.runsocialplatform.module.alumni.model.dto.AlumniSearchDTO;
import com.run.runsocialplatform.module.alumni.model.vo.AlumniListItemVO;
import com.run.runsocialplatform.module.alumni.model.vo.AlumniProfileVO;


public interface AlumniProfileService extends IService<AlumniInfo> {

    /**
     * 获取当前用户的校友档案
     */
    AlumniProfileVO getCurrentUserProfile();

    /**
     * 更新当前用户的校友档案
     */
    void updateCurrentUserProfile(AlumniProfileDTO profileDTO);

    /**
     * 获取校友详情
     */
    AlumniProfileVO getAlumniProfile(Long alumniId);

    /**
     * 搜索校友
     */
    Page<AlumniListItemVO> searchAlumni(AlumniSearchDTO searchDTO);

    /**
     * 获取校友列表（分页）
     */
    Page<AlumniListItemVO> getAlumniList(Page<AlumniListItemVO> page);

    /**
     * 验证校友信息是否完整
     */
    boolean validateProfileComplete(Long userId);

    /**
     * 获取热门校友（推荐）
     */
    Page<AlumniListItemVO> getHotAlumni(Page<AlumniListItemVO> page);
}