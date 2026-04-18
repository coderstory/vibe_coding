package cn.coderstory.springboot.mapper;

import cn.coderstory.springboot.entity.User;
import cn.coderstory.springboot.vo.UserVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Select("SELECT u.*, r.role_name as roleName FROM sys_user u LEFT JOIN sys_role r ON u.role_id = r.id WHERE u.id = #{id} AND u.deleted = 0")
    UserVO selectUserWithRoleName(@Param("id") Long id);

    @Select("SELECT * FROM sys_user WHERE username = #{username} AND deleted = 0")
    User findByUsername(String username);
}
