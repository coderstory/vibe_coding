package cn.coderstory.springboot.mapper.order;

import cn.coderstory.springboot.entity.order.Cart;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CartMapper extends BaseMapper<Cart> {
}