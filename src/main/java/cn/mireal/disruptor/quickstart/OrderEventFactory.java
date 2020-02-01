package cn.mireal.disruptor.quickstart;

import com.lmax.disruptor.EventFactory;

/**
 * @author Mireal
 */
public class OrderEventFactory implements EventFactory<OrderEvent> {
    @Override
    public OrderEvent newInstance() {
        //这个方法就是为了返回空的数据对象（Event）
        return new OrderEvent();
    }
}
