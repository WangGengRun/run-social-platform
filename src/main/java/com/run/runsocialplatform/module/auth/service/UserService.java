package com.run.runsocialplatform.module.auth.service;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.IService;
import com.run.runsocialplatform.module.auth.model.dto.LoginDTO;
import com.run.runsocialplatform.module.auth.model.dto.RegisterDTO;
import com.run.runsocialplatform.module.auth.model.entity.UserEntity;
import com.run.runsocialplatform.module.auth.model.vo.LoginResultVO;

public interface UserService extends IService<UserEntity> {

    /**
     * 用户注册
     */
    Long register(RegisterDTO registerDTO);

    /**
     * 用户登录
     */
    LoginResultVO login(LoginDTO loginDTO);

    /**
     * 通过用户名获取用户
     */
    UserEntity getByUsername(String username);

    /**
     * 通过邮箱获取用户
     */
    UserEntity getByEmail(String email);

    /**
     * 通过手机号获取用户
     */
    UserEntity getByPhone(String phone);

    /**
     * 检查用户名是否存在
     */
    boolean checkUsernameExists(String username);

    /**
     * 检查邮箱是否存在
     */
    boolean checkEmailExists(String email);

    /**
     * 检查手机号是否存在
     */
    boolean checkPhoneExists(String phone);

    /**
     * 修改密码
     */
    void changePassword(Long userId, String oldPassword, String newPassword);

    /**
     * 重置密码
     */
    void resetPassword(String email, String newPassword, String captcha);

    /**
     * 更新最后登录时间
     */
    void updateLastLoginTime(Long userId);
}