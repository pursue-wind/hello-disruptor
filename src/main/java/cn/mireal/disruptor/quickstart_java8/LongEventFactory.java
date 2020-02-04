package cn.mireal.disruptor.quickstart_java8;

import com.lmax.disruptor.EventFactory;

/**
 * 为了允许Disruptor为我们预分配这些事件，我们需要一个EventFactory来执行构造
 *
 * @author Mireal
 */
public class LongEventFactory implements EventFactory<LongEvent> {
    @Override
    public LongEvent newInstance() {
        return new LongEvent();
    }
}