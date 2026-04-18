package cn.coderstory.springboot.service.impl;

import cn.coderstory.springboot.entity.User;
import cn.coderstory.springboot.exception.BusinessException;
import cn.coderstory.springboot.mapper.UserMapper;
import cn.coderstory.springboot.security.PasswordEncoder;
import cn.coderstory.springboot.service.UserService;
import cn.coderstory.springboot.vo.UserVO;
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
    public IPage<User> getUserPage(Page<User> page, String username, String name, String department, Integer enabled, String phone) {
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
        if (phone != null && !phone.isEmpty()) {
            wrapper.like(User::getPhone, phone);
        }
        wrapper.orderByDesc(User::getCreateTime);
        return userMapper.selectPage(page, wrapper);
    }

    @Override
    public UserVO getUserById(Long id) {
        UserVO user = userMapper.selectUserWithRoleName(id);
        if (user == null) {
            throw BusinessException.notFound("用户不存在");
        }
        return user;
    }

    @Override
    public boolean saveUser(User user, String rawPassword) {
        if (rawPassword == null || rawPassword.isEmpty()) {
            throw BusinessException.badRequest("密码不能为空");
        }
        if (rawPassword.length() < 6) {
            throw BusinessException.badRequest("密码长度不能少于6位");
        }
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, user.getUsername());
        if (userMapper.selectCount(wrapper) > 0) {
            throw BusinessException.conflict("用户名已存在");
        }
        String encodedPassword = passwordEncoder.encode(rawPassword);
        user.setPassword(encodedPassword);
        return userMapper.insert(user) > 0;
    }

    @Override
    public boolean updateUser(User user) {
        if (user.getId() == null) {
            throw BusinessException.badRequest("用户ID不能为空");
        }
        User existing = userMapper.selectById(user.getId());
        if (existing == null) {
            throw BusinessException.notFound("用户不存在");
        }
        return userMapper.updateById(user) > 0;
    }

    @Override
    public boolean deleteUser(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw BusinessException.notFound("用户不存在");
        }
        return userMapper.deleteById(id) > 0;
    }

    @Override
    public boolean resetPassword(Long id, String newPassword) {
        if (newPassword == null || newPassword.isEmpty()) {
            throw BusinessException.badRequest("密码不能为空");
        }
        if (newPassword.length() < 6) {
            throw BusinessException.badRequest("密码长度不能少于6位");
        }
        User user = userMapper.selectById(id);
        if (user == null) {
            throw BusinessException.notFound("用户不存在");
        }
        String encodedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encodedPassword);
        return userMapper.updateById(user) > 0;
    }

    @Override
    public boolean updateUserStatus(Long id, Integer enabled) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw BusinessException.notFound("用户不存在");
        }
        user.setEnabled(enabled);
        return userMapper.updateById(user) > 0;
    }
}
