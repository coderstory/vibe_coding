package cn.coderstory.springboot.mapper.seckill.stock;

import cn.coderstory.springboot.entity.seckill.stock.Stock;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StockMapper extends BaseMapper<Stock> {
}