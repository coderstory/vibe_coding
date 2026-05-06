package cn.coderstory.springboot.user.service;

import cn.coderstory.springboot.user.entity.User;
import cn.coderstory.springboot.user.dto.UserVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

public interface UserService {
    
    IPage<User> getUserPage(Page<User> page, String username, String name, String department, Integer enabled, String phone);

    UserVO getUserById(Long id);
    
    boolean saveUser(User user, String rawPassword);
    
    boolean updateUser(User user);
    
    boolean deleteUser(Long id);
    
    boolean resetPassword(Long id, String newPassword);

    boolean updateUserStatus(Long id, Integer enabled);
}
