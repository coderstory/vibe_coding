package cn.coderstory.springboot.stock.service.impl;

import cn.coderstory.springboot.seckill.entity.SeckillGoods;
import cn.coderstory.springboot.seckill.mapper.SeckillGoodsMapper;
import cn.coderstory.springboot.stock.entity.Stock;
import cn.coderstory.springboot.stock.mapper.StockMapper;
import cn.coderstory.springboot.stock.service.ReconcileService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReconcileServiceImpl implements ReconcileService {
    private final StockMapper stockMapper;
    private final SeckillGoodsMapper goodsMapper;

    @Override
    @Scheduled(cron = "0 0 2 * * ?")
    public void reconcileStock() {
        log.info("开始库存对账...");

        List<Stock> stocks = stockMapper.selectList(new LambdaQueryWrapper<Stock>());
        for (Stock stock : stocks) {
            SeckillGoods goods = goodsMapper.selectById(stock.getGoodsId());
            if (goods == null) {
                continue;
            }

            int expectedStock = goods.getStock() - goods.getSold();
            if (stock.getAvailableStock() + stock.getLockedStock() != expectedStock) {
                log.warn("库存不一致: goodsId={}, 预期={}, 实际={}+{}={}",
                    stock.getGoodsId(), expectedStock,
                    stock.getAvailableStock(), stock.getLockedStock(),
                    stock.getAvailableStock() + stock.getLockedStock());
            }
        }

        log.info("库存对账完成");
    }
}