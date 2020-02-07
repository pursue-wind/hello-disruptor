package cn.mireal.disruptor.advanced.multiconsumer;

import com.lmax.disruptor.RingBuffer;

/**
 * Disruptor中的 Event
 *
 * @author Mireal
 * @version V1.0
 * @date 2020/2/5 14:17
 */
public class Producer {

    private RingBuffer<Order> ringBuffer;

    public Producer(RingBuffer<Order> ringBuffer) {
        this.ringBuffer = ringBuffer;
    }

    public void sendData(String uuid) {
        long sequence = ringBuffer.next();
        try {
            Order order = ringBuffer.get(sequence);
            order.setId(uuid);
        } finally {
            ringBuffer.publish(sequence);
        }
    }

}
