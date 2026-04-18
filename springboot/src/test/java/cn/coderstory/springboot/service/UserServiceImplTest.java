package cn.coderstory.springboot.service;

import cn.coderstory.springboot.entity.User;
import cn.coderstory.springboot.exception.BusinessException;
import cn.coderstory.springboot.mapper.UserMapper;
import cn.coderstory.springboot.security.PasswordEncoder;
import cn.coderstory.springboot.service.impl.UserServiceImpl;
import cn.coderstory.springboot.vo.UserVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService 单元测试")
class UserServiceImplTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setName("测试用户");
        testUser.setGender(1);
        testUser.setEmail("test@example.com");
        testUser.setPhone("13800138000");
        testUser.setDepartment("技术部");
        testUser.setPosition("工程师");
        testUser.setRoleId(1L);
        testUser.setEnabled(1);
    }

    @Nested
    @DisplayName("getUserById")
    class GetUserByIdTests {

        @Test
        @DisplayName("用户存在时返回用户信息")
        void whenUserExists_returnsUser() {
            UserVO userVO = new UserVO();
            userVO.setId(1L);
            userVO.setUsername("testuser");
            userVO.setRoleName("管理员");

            when(userMapper.selectUserWithRoleName(1L)).thenReturn(userVO);

            UserVO result = userService.getUserById(1L);

            assertNotNull(result);
            assertEquals("testuser", result.getUsername());
            verify(userMapper).selectUserWithRoleName(1L);
        }

        @Test
        @DisplayName("用户不存在时抛出 BusinessException")
        void whenUserNotExists_throwsException() {
            when(userMapper.selectUserWithRoleName(999L)).thenReturn(null);

            BusinessException exception = assertThrows(BusinessException.class,
                () -> userService.getUserById(999L));

            assertEquals(404, exception.getCode());
            assertEquals("用户不存在", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("saveUser")
    class SaveUserTests {

        @Test
        @DisplayName("密码为空时抛出异常")
        void whenPasswordEmpty_throwsException() {
            BusinessException exception = assertThrows(BusinessException.class,
                () -> userService.saveUser(testUser, null));

            assertEquals(400, exception.getCode());
            assertEquals("密码不能为空", exception.getMessage());
        }

        @Test
        @DisplayName("密码太短时抛出异常")
        void whenPasswordTooShort_throwsException() {
            BusinessException exception = assertThrows(BusinessException.class,
                () -> userService.saveUser(testUser, "123"));

            assertEquals(400, exception.getCode());
            assertEquals("密码长度不能少于6位", exception.getMessage());
        }

        @Test
        @DisplayName("用户名已存在时抛出异常")
        void whenUsernameExists_throwsException() {
            when(userMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

            BusinessException exception = assertThrows(BusinessException.class,
                () -> userService.saveUser(testUser, "password123"));

            assertEquals(409, exception.getCode());
            assertEquals("用户名已存在", exception.getMessage());
        }

        @Test
        @DisplayName("创建用户成功返回true")
        void whenValid_returnsTrue() {
            when(userMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
            when(passwordEncoder.encode("password123")).thenReturn("encoded_password");
            when(userMapper.insert(any(User.class))).thenReturn(1);

            boolean result = userService.saveUser(testUser, "password123");

            assertTrue(result);
            verify(passwordEncoder).encode("password123");
            verify(userMapper).insert(any(User.class));
        }
    }

    @Nested
    @DisplayName("updateUser")
    class UpdateUserTests {

        @Test
        @DisplayName("用户不存在时抛出异常")
        void whenUserNotExists_throwsException() {
            User nonExistentUser = new User();
            nonExistentUser.setId(999L);
            when(userMapper.selectById(999L)).thenReturn(null);

            BusinessException exception = assertThrows(BusinessException.class,
                () -> userService.updateUser(nonExistentUser));

            assertEquals(404, exception.getCode());
            assertEquals("用户不存在", exception.getMessage());
        }

        @Test
        @DisplayName("更新用户成功返回true")
        void whenUserExists_returnsTrue() {
            when(userMapper.selectById(1L)).thenReturn(testUser);
            when(userMapper.updateById(any(User.class))).thenReturn(1);

            boolean result = userService.updateUser(testUser);

            assertTrue(result);
            verify(userMapper).updateById(any(User.class));
        }
    }

    @Nested
    @DisplayName("deleteUser")
    class DeleteUserTests {

        @Test
        @DisplayName("用户不存在时抛出异常")
        void whenUserNotExists_throwsException() {
            when(userMapper.selectById(999L)).thenReturn(null);

            BusinessException exception = assertThrows(BusinessException.class,
                () -> userService.deleteUser(999L));

            assertEquals(404, exception.getCode());
            assertEquals("用户不存在", exception.getMessage());
        }

        @Test
        @DisplayName("删除用户成功返回true")
        void whenUserExists_returnsTrue() {
            when(userMapper.selectById(1L)).thenReturn(testUser);
            when(userMapper.deleteById(1L)).thenReturn(1);

            boolean result = userService.deleteUser(1L);

            assertTrue(result);
            verify(userMapper).deleteById(1L);
        }
    }

    @Nested
    @DisplayName("resetPassword")
    class ResetPasswordTests {

        @Test
        @DisplayName("密码为空时抛出异常")
        void whenPasswordEmpty_throwsException() {
            BusinessException exception = assertThrows(BusinessException.class,
                () -> userService.resetPassword(1L, null));

            assertEquals(400, exception.getCode());
            assertEquals("密码不能为空", exception.getMessage());
        }

        @Test
        @DisplayName("密码太短时抛出异常")
        void whenPasswordTooShort_throwsException() {
            BusinessException exception = assertThrows(BusinessException.class,
                () -> userService.resetPassword(1L, "12345"));

            assertEquals(400, exception.getCode());
            assertEquals("密码长度不能少于6位", exception.getMessage());
        }

        @Test
        @DisplayName("用户不存在时抛出异常")
        void whenUserNotExists_throwsException() {
            when(userMapper.selectById(999L)).thenReturn(null);

            BusinessException exception = assertThrows(BusinessException.class,
                () -> userService.resetPassword(999L, "newpassword123"));

            assertEquals(404, exception.getCode());
            assertEquals("用户不存在", exception.getMessage());
        }

        @Test
        @DisplayName("重置密码成功返回true")
        void whenValid_returnsTrue() {
            when(userMapper.selectById(1L)).thenReturn(testUser);
            when(passwordEncoder.encode("newpassword123")).thenReturn("encoded_new_password");
            when(userMapper.updateById(any(User.class))).thenReturn(1);

            boolean result = userService.resetPassword(1L, "newpassword123");

            assertTrue(result);
            verify(passwordEncoder).encode("newpassword123");
            verify(userMapper).updateById(any(User.class));
        }
    }

    @Nested
    @DisplayName("updateUserStatus")
    class UpdateUserStatusTests {

        @Test
        @DisplayName("用户不存在时抛出异常")
        void whenUserNotExists_throwsException() {
            when(userMapper.selectById(999L)).thenReturn(null);

            BusinessException exception = assertThrows(BusinessException.class,
                () -> userService.updateUserStatus(999L, 0));

            assertEquals(404, exception.getCode());
            assertEquals("用户不存在", exception.getMessage());
        }

        @Test
        @DisplayName("更新状态成功返回true")
        void whenUserExists_returnsTrue() {
            when(userMapper.selectById(1L)).thenReturn(testUser);
            when(userMapper.updateById(any(User.class))).thenReturn(1);

            boolean result = userService.updateUserStatus(1L, 0);

            assertTrue(result);
            verify(userMapper).updateById(any(User.class));
        }
    }

    @Nested
    @DisplayName("getUserPage")
    class GetUserPageTests {

        @Test
        @DisplayName("分页查询返回结果")
        void returnsPageResult() {
            Page<User> pageParam = new Page<>(1, 20);
            Page<User> resultPage = new Page<>();
            resultPage.setRecords(java.util.List.of(testUser));
            resultPage.setTotal(1);

            when(userMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(resultPage);

            IPage<User> result = userService.getUserPage(pageParam, "test", null, null, null, null);

            assertNotNull(result);
            assertEquals(1, result.getRecords().size());
            verify(userMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
        }
    }
}