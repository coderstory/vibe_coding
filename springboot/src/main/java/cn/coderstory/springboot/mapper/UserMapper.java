package cn.coderstory.springboot.mapper;

import cn.coderstory.springboot.entity.User;
import cn.coderstory.springboot.vo.UserVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    UserVO selectUserWithRoleName(@Param("id") Long id);

    User findByUsername(@Param("username") String username);
}
