package cn.mireal.disruptor.advanced;

import com.lmax.disruptor.BusySpinWaitStrategy;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.EventHandlerGroup;
import com.lmax.disruptor.dsl.ProducerType;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Mireal
 * @version V1.0
 * @date 2020/2/3 17:15
 */
public class Main {
    public static void main(String[] args) throws InterruptedException {
        //构建一个线程池用于提交任务
        ExecutorService es1 = Executors.newFixedThreadPool(1);

        ExecutorService es = Executors.newFixedThreadPool(4);
        //1 构建Disruptor
        Disruptor<Trade> disruptor = new Disruptor<Trade>(
                Trade::new,
                1024 * 1024,
                es,
                ProducerType.SINGLE,
                new BusySpinWaitStrategy());

        //2 把消费者设置到Disruptor中 handleEventsWith
        /**2.1 串行操作：*/
/*        disruptor
                .handleEventsWith(new Handler1())
                .handleEventsWith(new Handler2())
                .handleEventsWith(new Handler3());*/
        /**2.2 并行操作: 可以有两种方式去进行*/
        //1 handleEventsWith方法 添加多个handler实现即可
        //2 handleEventsWith方法 分别进行调用
//        disruptor.handleEventsWith(new Handler1(), new Handler2(), new Handler3());
        //		disruptor.handleEventsWith(new Handler2());
        //		disruptor.handleEventsWith(new Handler3());
        /**2.3 菱形操作 (一)*/
        /*
         disruptor.handleEventsWith(new Handler1(), new Handler2())
         .handleEventsWith(new Handler3());
         */

        /**2.3 菱形操作 (二)*/
/*        EventHandlerGroup<Trade> ehGroup = disruptor.handleEventsWith(new Handler1(), new Handler2());
        ehGroup.then(new Handler3());*/

        /**2.4 六边形操作 (二)*/
        Handler1 h1 = new Handler1();
        Handler2 h2 = new Handler2();
        Handler3 h3 = new Handler3();
        Handler4 h4 = new Handler4();
        Handler5 h5 = new Handler5();
        disruptor.handleEventsWith(h1, h4);
        disruptor.after(h1).handleEventsWith(h2);
        disruptor.after(h4).handleEventsWith(h5);
        disruptor.after(h2, h5).handleEventsWith(h3);

        //3 启动disruptor
        RingBuffer<Trade> ringBuffer = disruptor.start();

        CountDownLatch latch = new CountDownLatch(1);

        long begin = System.currentTimeMillis();

        es1.submit(new TradePushlisher(latch, disruptor));


        latch.await();    //进行向下

        disruptor.shutdown();
        es.shutdown();
        System.err.println("总耗时: " + (System.currentTimeMillis() - begin));
    }
}
