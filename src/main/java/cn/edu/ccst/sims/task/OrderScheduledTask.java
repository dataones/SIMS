package cn.edu.ccst.sims.task;

import cn.edu.ccst.sims.service.TbOrderSettlementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 订单定时任务
 * 处理未支付订单的自动取消
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "order.auto-cancel.enabled", havingValue = "true", matchIfMissing = true)
public class OrderScheduledTask {

    @Autowired
    private TbOrderSettlementService orderService;

    @Value("${order.auto-cancel.timeout-minutes:30}")
    private double timeoutMinutes;

    @Value("${order.auto-cancel.check-interval-minutes:5}")
    private double checkIntervalMinutes;

    /**
     * 定时检查未支付订单
     * 执行间隔由配置文件决定
     */
    @Scheduled(fixedRate = 60000) // 1分钟固定间隔
    public void cancelUnpaidOrders() {
        try {
            log.info("开始执行未支付订单自动取消任务，超时时间：{}分钟", timeoutMinutes);

            // 调用服务层方法处理未支付订单
            int cancelledCount = orderService.cancelUnpaidOrders((int) Math.round(timeoutMinutes));

            if (cancelledCount > 0) {
                log.info("自动取消了{}个超时未支付订单", cancelledCount);
            } else {
                log.debug("本次检查没有需要取消的未支付订单");
            }

        } catch (Exception e) {
            log.error("未支付订单自动取消任务执行失败", e);
        }
    }
}
