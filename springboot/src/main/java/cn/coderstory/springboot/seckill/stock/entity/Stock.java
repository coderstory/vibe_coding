package cn.coderstory.springboot.seckill.stock.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("stock")
public class Stock {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long goodsId;
    private Integer totalStock;
    private Integer availableStock;
    private Integer lockedStock;
    private Integer version;
    private Integer mqDeductCount;
    private Integer mqRollbackCount;
    private LocalDateTime updateTime;
}
