package cn.mireal.disruptor.quickstart_java8;

import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.util.DaemonThreadFactory;

import java.nio.ByteBuffer;

/**
 * @author Mireal
 */
public class LongEventMain {
    public static void main(String[] args) throws Exception {
        //指定RingBuffer的大小，必须为2的幂。
        int bufferSize = 1024;

        // 构造Disruptor
        Disruptor<LongEvent> disruptor = new Disruptor<>(LongEvent::new, bufferSize, DaemonThreadFactory.INSTANCE);

        // 连接处理程序
        disruptor.handleEventsWith((event, sequence, endOfBatch) -> System.out.println("Event: " + event));

        // 启动Disruptor，启动所有正在运行的线程
        disruptor.start();

        // 从Disruptor获取RingBuffer以用于发布。
        RingBuffer<LongEvent> ringBuffer = disruptor.getRingBuffer();

        ByteBuffer bb = ByteBuffer.allocate(8);
        for (long l = 0; true; l++) {
            bb.putLong(0, l);
            ringBuffer.publishEvent((event, sequence, buffer) -> event.set(buffer.getLong(0)), bb);
            Thread.sleep(1000);
        }
    }
}