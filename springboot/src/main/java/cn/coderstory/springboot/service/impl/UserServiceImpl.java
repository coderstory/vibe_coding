package cn.coderstory.springboot.service.impl;

import cn.coderstory.springboot.entity.User;
import cn.coderstory.springboot.mapper.UserMapper;
import cn.coderstory.springboot.security.PasswordEncoder;
import cn.coderstory.springboot.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    public IPage<User> getUserPage(Page<User> page, String username, String name, String department, Integer enabled) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (username != null && !username.isEmpty()) {
            wrapper.like(User::getUsername, username);
        }
        if (name != null && !name.isEmpty()) {
            wrapper.like(User::getName, name);
        }
        if (department != null && !department.isEmpty()) {
            wrapper.eq(User::getDepartment, department);
        }
        if (enabled != null) {
            wrapper.eq(User::getEnabled, enabled);
        }
        wrapper.orderByDesc(User::getCreateTime);
        return userMapper.selectPage(page, wrapper);
    }
    
    @Override
    public User getUserWithRole(Long id) {
        return userMapper.selectById(id);
    }
    
    @Override
    public boolean saveUser(User user, String rawPassword) {
        String encodedPassword = passwordEncoder.encode(rawPassword);
        user.setPassword(encodedPassword);
        return userMapper.insert(user) > 0;
    }
    
    @Override
    public boolean updateUser(User user) {
        return userMapper.updateById(user) > 0;
    }
    
    @Override
    public boolean deleteUser(Long id) {
        return userMapper.deleteById(id) > 0;
    }
    
    @Override
    public boolean resetPassword(Long id, String newPassword) {
        String encodedPassword = passwordEncoder.encode(newPassword);
        User user = new User();
        user.setId(id);
        user.setPassword(encodedPassword);
        return userMapper.updateById(user) > 0;
    }
}
