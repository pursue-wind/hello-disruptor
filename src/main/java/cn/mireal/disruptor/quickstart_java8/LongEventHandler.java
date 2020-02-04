package cn.mireal.disruptor.quickstart_java8;

import com.lmax.disruptor.EventHandler;

/**
 * 定义事件后，我们需要创建一个处理这些事件的使用者。在我们的例子中，我们要做的就是在控制台上打印该值。
 *
 * @author Mireal
 */
public class LongEventHandler implements EventHandler<LongEvent> {
    @Override
    public void onEvent(LongEvent event, long sequence, boolean endOfBatch) {
        System.out.println("Event: " + event);
    }
}