package cn.coderstory.springboot.seckill.stock.mapper;

import cn.coderstory.springboot.seckill.stock.entity.Stock;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StockMapper extends BaseMapper<Stock> {
}