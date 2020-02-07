package cn.mireal.disruptor.advanced.multiconsumer;

import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.ProducerType;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;
/**
 * @author Mireal
 * @version V1.0
 * @date 2020/2/5 13:22
 */
public class Main {
    public static void main(String[] args) throws InterruptedException {
        //1 创建RingBuffer
        RingBuffer<Order> ringBuffer =
                RingBuffer.create(ProducerType.MULTI,
                        Order::new,
                        1024 * 1024,
                        new YieldingWaitStrategy());

        //2 通过ringBuffer 创建一个屏障
        SequenceBarrier sequenceBarrier = ringBuffer.newBarrier();

        //3 创建多个消费者数组:
        Consumer[] consumers = new Consumer[10];
        IntStream.range(0, consumers.length).forEach(i -> {
            consumers[i] = new Consumer("C" + i);
        });

        //4 构建多消费者工作池
        WorkerPool<Order> workerPool = new WorkerPool<Order>(
                ringBuffer,
                sequenceBarrier,
                new EventExceptionHandler(),
                consumers);

        //5 设置多个消费者的sequence序号 用于单独统计消费进度, 并且设置到ringbuffer中
        ringBuffer.addGatingSequences(workerPool.getWorkerSequences());

        //6 启动workerPool
        workerPool.start(Executors.newFixedThreadPool(5));

        final CountDownLatch latch = new CountDownLatch(1);
        IntStream.range(0, 100).forEach(i -> {
            final Producer producer = new Producer(ringBuffer);
            CompletableFuture.runAsync(() -> {
                try {
                    latch.await();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                IntStream.range(0, 100).forEach(j -> {
                    producer.sendData(UUID.randomUUID().toString());
                });
            });
        });

        Thread.sleep(2000);
        System.err.println("----------线程创建完毕，开始生产数据----------");
        latch.countDown();

        Thread.sleep(8000);

        System.err.println("任务总数:" + consumers[2].getCount());
    }
}
