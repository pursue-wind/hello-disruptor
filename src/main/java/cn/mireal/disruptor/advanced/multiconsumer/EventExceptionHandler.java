package cn.mireal.disruptor.advanced.multiconsumer;

import com.lmax.disruptor.ExceptionHandler;
/**
 * @author Mireal
 * @version V1.0
 * @date 2020/2/5 14:17
 */
public class EventExceptionHandler implements ExceptionHandler<Order> {
    @Override
    public void handleEventException(Throwable ex, long sequence, Order event) {
    }

    @Override
    public void handleOnStartException(Throwable ex) {
    }

    @Override
    public void handleOnShutdownException(Throwable ex) {
    }
}