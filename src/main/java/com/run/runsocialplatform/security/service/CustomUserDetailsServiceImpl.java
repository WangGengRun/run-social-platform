package com.run.runsocialplatform.security.service;



import com.run.runsocialplatform.module.auth.model.entity.UserEntity;
import com.run.runsocialplatform.module.auth.service.UserService;
import com.run.runsocialplatform.security.model.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * 实现自定义UserDetailsService
 */
@Slf4j
@Service
@RequiredArgsConstructor
/**
 * 这个注解主要是制定这个实现类为主实现，不然注册两个bean，security会报错
 * 最终版本可以把不用的实现类删掉
 */
@Primary
public class CustomUserDetailsServiceImpl implements UserDetailsService {

    private final UserService userService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 根据用户名查找用户
        UserEntity user = userService.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }

        log.debug("加载用户详情: {}", username);
        return new CustomUserDetails(user);
    }
}