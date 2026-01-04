package com.run.runsocialplatform.module.auth.mapper;



import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.run.runsocialplatform.module.auth.model.entity.UserEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper extends BaseMapper<UserEntity> {
    @Select("SELECT * FROM user WHERE username = #{username} AND status = 1")
    UserEntity selectByUsername(@Param("username") String username);

    @Select("SELECT * FROM user WHERE email = #{email} AND status = 1")
    UserEntity selectByEmail(@Param("email") String email);

    @Select("SELECT * FROM user WHERE phone = #{phone} AND status = 1")
    UserEntity selectByPhone(@Param("phone") String phone);

    @Select("SELECT COUNT(*) FROM user WHERE username = #{username}")
    Long countByUsername(@Param("username") String username);

    @Select("SELECT COUNT(*) FROM user WHERE email = #{email}")
    Long countByEmail(@Param("email") String email);

    @Select("SELECT COUNT(*) FROM user WHERE phone = #{phone}")
    Long countByPhone(@Param("phone") String phone);
}