package cn.mireal.disruptor.advanced.multiconsumer;

import com.lmax.disruptor.WorkHandler;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Mireal
 * @version V1.0
 * @date 2020/2/5 14:16
 */
public class Consumer implements WorkHandler<Order> {

    private String comsumerId;

    private static AtomicInteger count = new AtomicInteger(0);

    private Random random = new Random();

    public Consumer(String comsumerId) {
        this.comsumerId = comsumerId;
    }

    @Override
    public void onEvent(Order event) throws Exception {
        Thread.sleep(random.nextInt(5));
        System.err.println("当前消费者: " + this.comsumerId + ", 消费信息ID: " + event.getId());
        count.incrementAndGet();
    }

    public int getCount() {
        return count.get();
    }
}
