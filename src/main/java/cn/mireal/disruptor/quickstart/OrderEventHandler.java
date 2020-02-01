package cn.mireal.disruptor.quickstart;

import com.lmax.disruptor.EventHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Mireal
 */
@Slf4j
public class OrderEventHandler implements EventHandler<OrderEvent> {

    @Override
    public void onEvent(OrderEvent event, long sequence, boolean endOfBatch) throws Exception {
        //Thread.sleep(Integer.MAX_VALUE);
        log.info("消费者: {}", event.getValue());
    }
}
