package cn.mireal.disruptor.test;

import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;


public class DisruptorSingle4Test {

    public static void main(String[] args) {
        int ringBufferSize = 65536;
        final Disruptor<User> disruptor = new Disruptor<User>(User::new,
                ringBufferSize,
                Executors.newSingleThreadExecutor(),
                ProducerType.SINGLE,
                //new BlockingWaitStrategy()
                new YieldingWaitStrategy()
        );

        Consumer consumer = new Consumer();
        //消费数据
        disruptor.handleEventsWith(consumer);
        disruptor.start();
        CompletableFuture.runAsync(() -> {
            RingBuffer<User> ringBuffer = disruptor.getRingBuffer();
            for (long i = 0; i < 10000000; i++) {
                long seq = ringBuffer.next();
                User User = ringBuffer.get(seq);
                User.setId(i);
                User.setName("c" + i);
                ringBuffer.publish(seq);
            }
        });
    }
}