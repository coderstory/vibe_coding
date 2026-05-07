package cn.coderstory.springboot.mapper.user;

import cn.coderstory.springboot.dto.user.UserVO;
import cn.coderstory.springboot.entity.user.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    UserVO selectUserWithRoleName(@Param("id") Long id);

    User findByUsername(@Param("username") String username);
}
