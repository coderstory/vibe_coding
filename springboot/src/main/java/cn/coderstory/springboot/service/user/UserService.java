package cn.coderstory.springboot.service.user;

import cn.coderstory.springboot.dto.user.UserVO;
import cn.coderstory.springboot.entity.user.User;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * 用户管理服务接口。
 * <p>
 * 提供用户的 CRUD 操作、密码重置和状态管理功能。
 *
 * @since 1.7.0
 */
public interface UserService {

    /**
     * 分页查询用户列表。
     *
     * @param page       分页参数
     * @param username   用户名（可选，模糊匹配）
     * @param name       姓名（可选，模糊匹配）
     * @param department 部门（可选，模糊匹配）
     * @param enabled    启用状态（可选）
     * @param phone      手机号（可选，模糊匹配）
     * @return 用户分页结果
     */
    IPage<User> getUserPage(Page<User> page, String username, String name, String department, Integer enabled, String phone);

    /**
     * 根据 ID 获取用户详情。
     *
     * @param id 用户 ID
     * @return 用户视图对象
     */
    UserVO getUserById(Long id);

    /**
     * 新增用户。
     *
     * @param user        用户实体
     * @param rawPassword 明文密码
     * @return 新增是否成功
     */
    boolean saveUser(User user, String rawPassword);

    /**
     * 更新用户信息。
     *
     * @param user 用户更新数据
     * @return 更新是否成功
     */
    boolean updateUser(User user);

    /**
     * 删除用户（逻辑删除）。
     *
     * @param id 用户 ID
     * @return 删除是否成功
     */
    boolean deleteUser(Long id);

    /**
     * 重置用户密码。
     *
     * @param id          用户 ID
     * @param newPassword 新密码（明文）
     * @return 重置是否成功
     */
    boolean resetPassword(Long id, String newPassword);

    /**
     * 更新用户启用状态。
     *
     * @param id      用户 ID
     * @param enabled 启用状态（1 启用，0 禁用）
     * @return 更新是否成功
     */
    boolean updateUserStatus(Long id, Integer enabled);
}
