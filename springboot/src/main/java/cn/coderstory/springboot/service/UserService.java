package cn.coderstory.springboot.service;

import cn.coderstory.springboot.entity.User;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

public interface UserService {
    
    IPage<User> getUserPage(Page<User> page, String username, String name, String department, Integer enabled);
    
    User getUserWithRole(Long id);
    
    boolean saveUser(User user, String rawPassword);
    
    boolean updateUser(User user);
    
    boolean deleteUser(Long id);
    
    boolean resetPassword(Long id, String newPassword);
}
