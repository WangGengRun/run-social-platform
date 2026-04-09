package com.run.runsocialplatform.security.model;


import com.run.runsocialplatform.module.auth.model.entity.UserEntity;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Arrays;

@Data
public class CustomUserDetails implements UserDetails {

    private UserEntity user;

    public CustomUserDetails(UserEntity user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 兼容 hasAuthority('ALUMNI') 与 hasRole('ALUMNI') 两种写法
        return Arrays.asList(
                new SimpleGrantedAuthority(user.getRole()),
                new SimpleGrantedAuthority("ROLE_" + user.getRole())
        );
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return user.getStatus() != 0; // 0表示禁用
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user.getStatus() == 1; // 1表示启用
    }

    public Long getUserId() {
        return user.getId();
    }
}