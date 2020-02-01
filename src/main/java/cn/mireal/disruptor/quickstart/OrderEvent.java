package cn.mireal.disruptor.quickstart;

import lombok.Data;

/**
 * @author Mireal
 */
@Data
public class OrderEvent {
    /**
     * 订单的价格
     */
    private long value;
}
