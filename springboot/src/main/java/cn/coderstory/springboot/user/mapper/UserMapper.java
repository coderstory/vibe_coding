package cn.coderstory.springboot.user.mapper;

import cn.coderstory.springboot.user.dto.UserVO;
import cn.coderstory.springboot.user.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    UserVO selectUserWithRoleName(@Param("id") Long id);

    User findByUsername(@Param("username") String username);
}
