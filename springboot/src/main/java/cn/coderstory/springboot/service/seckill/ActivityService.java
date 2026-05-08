package cn.coderstory.springboot.service.seckill;

import cn.coderstory.springboot.entity.seckill.SeckillActivity;
import cn.coderstory.springboot.vo.seckill.ActivityDetailVO;
import com.baomidou.mybatisplus.core.metadata.IPage;

/**
 * 秒杀活动管理服务接口。
 * <p>
 * 提供活动的 CRUD 操作、状态管理（启动/结束/发布）和库存查询功能。
 * 活动状态的变更受状态机约束，非法的状态转换将被拒绝。
 *
 * @since 1.7.0
 */
public interface ActivityService {

    /**
     * 根据活动 ID 获取活动信息。
     *
     * @param activityId 活动 ID
     * @return 活动实体，不存在返回 null
     */
    SeckillActivity getActivity(Long activityId);

    /**
     * 获取活动详情（包含商品列表）
     * <p>
     * 用于秒杀详情页，展示活动信息及关联商品
     * 一个活动可以关联多个商品
     *
     * @param activityId 活动ID
     * @return 活动详情（含商品列表）
     */
    ActivityDetailVO getActivityDetail(Long activityId);

    /**
     * 分页查询活动列表。
     *
     * @param page 页码（从 1 开始）
     * @param size 每页条数
     * @return 活动分页结果
     */
    IPage<SeckillActivity> getActivityPage(int page, int size);

    /**
     * 创建秒杀活动。
     *
     * @param activity 活动信息（不含 ID）
     * @return 已创建的活动（含生成的 ID）
     */
    SeckillActivity createActivity(SeckillActivity activity);

    /**
     * 更新活动信息。
     *
     * @param id       活动 ID
     * @param activity 更新的活动字段
     * @return 更新后的活动
     */
    SeckillActivity updateActivity(Long id, SeckillActivity activity);

    /**
     * 删除指定活动。
     *
     * @param id 活动 ID
     * @return 是否删除成功
     */
    boolean deleteActivity(Long id);

    /**
     * 启动指定活动，状态变更为进行中。
     *
     * @param activityId 活动 ID
     * @return 是否启动成功
     */
    boolean startActivity(Long activityId);

    /**
     * 结束指定活动，状态变更为已结束。
     *
     * @param activityId 活动 ID
     * @return 是否结束成功
     */
    boolean endActivity(Long activityId);

    /**
     * 发布指定活动，状态变更为已发布。
     * <p>
     * 发布后活动对用户可见，但未到开始时间不可抢购。
     *
     * @param activityId 活动 ID
     * @return 是否发布成功
     */
    boolean publishActivity(Long activityId);

    /**
     * 获取活动总库存（所有关联商品的库存之和）
     *
     * @param activityId 活动ID
     * @return 总库存数量
     */
    int getActivityStock(Long activityId);
}
