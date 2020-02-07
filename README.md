# hello-disruptor
[GITHUB - Disruptor](https://github.com/LMAX-Exchange/disruptor)
## Disruptor Quick Start
1. 建立一个工厂"Event类， 用于创建Event类实例对象
2. 需要有一个监听事件类，用于处理数据(Event类)
3. 实例化Disruptor实例，配置-系列参数，编写Disruptor核心组件
4. 编写生产者组件，向Disruptor容器中去投递数据

## Disruptor核心原理 - RingBuffer
[GITHUB - Disruptor - Introduction](https://github.com/LMAX-Exchange/disruptor/wiki/Introduction)
- 初看Disruptor，给人的印象就是RingBuffer是其核心，生产者向RingBuffer中写入元素，消费者从RingBuffer中消费元素

### RingBuffer到底是啥
- 正如名字所说的一样，它是一个环(首尾相接的环)
- 它用做在不同上下文(线程)间传递数据的buffer!
- RingBuffer拥有一个序号，这个序号指向数组中下一个可用元素

### RingBuffer数据结构深入探究
- 随着你不停地填充这个buffer (可能也会有相应的读取)， 这个序号会一直增长，直到绕过这个环
- 要找到数组中当前序号指向的元素，可以通过mod操作: sequence mod array length = array index (取模操作)以上面的RingBuffer的size 10 为例(java的mod语法) : 12 % 10= 2
- 如果槽的个数是2的N次方更有利于基于二进制的计算机进行计算

> RingBuffer：基于数组的缓存实现，也是创建sequencer与定义WaitStrategy的入口

> Disruptor：持有RingBuffer、消费者线程池Executor、消费者集合ConsumerRepository等引用


### Disruptor核心 - Sequence
- 通过顺序递增的序号来编号，管理进行交换的数据(事件)
- 对数据(事件)的处理过程总是沿着序号逐个递增处理
- 一个Sequence用于跟踪标识某个特定的事件处理者(RingBuffer/Producer/Consumer)的处理进度
- Sequence可以看成是一个AtomicLong用于标识进度
- 还有另外一个目的就是防止不同Sequence之间CPU缓存伪共享(Flase Sharing)的问题

### Disruptor核心 - Sequencer
- Sequencer是Disruptor的真正核心
- 此接口有两个实现类
    - SingleProducerSequencer
    - MultiProducerSequencer
- 主要实现生产者和消费者之间快速、正确地传递数据的并发算法

### Disruptor核心 - Sequence Barrier
- 用于保持对RingBuffer的Main Published Sequence(Producer)和Consumer之间的平衡关系; Sequence Barrier还定义了决定Consumer是否还有可处理的事件的逻辑。

### Disruptor核心-WaitStrategy
> **BlockingWaitStrategy**Disruptor使用的默认等待策略是BlockingWaitStrategy。内部BlockingWaitStrategy使用典型的锁和条件变量来处理线程唤醒。BlockingWaitStrategy是可用的等待策略中最慢的，但是在CPU使用率方面是最保守的，它将在最广泛的部署选项中提供最一致的行为。但是，再次了解已部署系统可以提高性能。

> **SleepingWaitStrategy** 尝试通过使用简单的繁忙等待循环来保守CPU使用率像BlockingWaitStrategy一样，SleepingWaitStrategy尝试通过使用简单的繁忙等待循环来保守CPU使用率，但在循环中间使用对LockSupport.parkNanos（1）的调用。在典型的Linux系统上，这会将线程暂停大约60µs。但是，这样做的好处是，生产线程不需要采取任何其他行动就可以增加适当的计数器，也不需要花费信号通知条件变量。但是，在生产者线程和使用者线程之间移动事件的平均等待时间会更长。它在不需要低延迟但对生产线程的影响较小的情况下效果最佳。一个常见的用例是异步日志记录。

> **YieldingWaitStrategy**是可在低延迟系统中使用的2种等待策略之一，在该系统中，可以选择刻录CPU周期以提高延迟。 YieldingWaitStrategy将忙于旋转以等待序列增加到适当的值。在循环体内，将调用`Thread.yield()`，以允许其他排队的线程运行。 当需要非常高的性能并且事件处理程序线程的数量少于逻辑核心的总数时，这是推荐的等待策略。注：启用超线程。

> **BusySpinWaitStrategy**是性能最高的“等待策略”，但对部署环境设置了最高的约束。 仅当事件处理程序线程的数量小于包装盒上的物理核心数量时，才应使用此等待策略。 注：超线程应该被禁用。

- `BlockingWaitStrategy`是最低效的策略，但其对CPU的消耗最小并且在各种不同部署环境中能提供更加一致的性能表现
- `SleepingWaitStrategy`的性能表现跟`BlockingWaitStrategy`差不多，对CPU的消耗也类似，但其对生产者线程的影响最小，适合用于异步日志类似的场景
- `YieldingWaitStrategy`的性能是最好的，适合用于低延迟的系统。在要求极高性能且事件处理线数小于CPU逻辑核心数的场景中，推荐使用此策略;例如，CPU开启超线程的特性

### Disruptor核心-Event
- `Event`从生产者到消费者过程中所处理的数据单元
- `Disruptor`中没有代码表示`Event`,因为它完全是由用户定义的

### Disruptor核心-EventProcessor
- `EventProcessor`:主要事件循环，处理`Disruptor`中的`Event`,拥有消费者的Sequence
- 它有一个实现类是`BatchEventProcessor`,包含了`event loop`有效的实现，并且将回调到一个`EventHandler`接口的实现对象

### Disruptor核心- EventHandler
- `EventHandler`:由用户实现并且代表了`Disruptor`中的-一个消费者的接口，也就是我们的消费者逻辑都需要写在这里

### Disruptor核心-WorkProcessor
- `WorkProcessor`: 确保每个`sequence`只被一个`processor`消费，在同一个`WorkPool`中处理多个`WorkProcessor`不会消费同样的`sequence` 


## Disruptor高级操作
### 并行计算
- EventHandlerGroup<T> handleEventsWith(final EventHandler<? super T>... handlers)
#### 串行操作:使用链式调用的方式
```java
public class Main {
    public static void main(String[] args) throws InterruptedException {
        //构建一个线程池用于提交任务
        ExecutorService es1 = Executors.newFixedThreadPool(1);
        ExecutorService es = Executors.newFixedThreadPool(5);
        //1 构建Disruptor
        Disruptor<Trade> disruptor = new Disruptor<Trade>(
                Trade::new,
                1024 * 1024,
                es,
                ProducerType.SINGLE,
                new BusySpinWaitStrategy());

        //2 把消费者设置到Disruptor中 handleEventsWith
        //2.1 串行操作：
        disruptor
                .handleEventsWith(new Handler1())
                .handleEventsWith(new Handler2())
                .handleEventsWith(new Handler3());

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
```
#### 并行操作:使用单独调用的方式，可以有两种方式去进行
1. handleEventsWith方法 添加多个handler实现即可
2. handleEventsWith方法 分别进行调用
```java
disruptor.handleEventsWith(new Handler1(), new Handler2(), new Handler3());
```
```java
disruptor.handleEventsWith(new Handler1());
disruptor.handleEventsWith(new Handler2());
disruptor.handleEventsWith(new Handler3());
```
```java
public class Main {
    public static void main(String[] args) throws InterruptedException {
        //构建一个线程池用于提交任务
        ExecutorService es1 = Executors.newFixedThreadPool(1);
        ExecutorService es2 = Executors.newFixedThreadPool(5);
        //1 构建Disruptor
        Disruptor<Trade> disruptor = new Disruptor<Trade>(
                Trade::new,
                1024 * 1024,
                es2,
                ProducerType.SINGLE,
                new BusySpinWaitStrategy());

        //2 把消费者设置到Disruptor中 handleEventsWith
        //2.2 并行操作: 可以有两种方式去进行
        //1. handleEventsWith方法 添加多个handler实现即可
        //2. handleEventsWith方法 分别进行调用
        disruptor.handleEventsWith(new Handler1(), new Handler2(), new Handler3());
        //		disruptor.handleEventsWith(new Handler2());
        //		disruptor.handleEventsWith(new Handler3());

        //3 启动disruptor
        RingBuffer<Trade> ringBuffer = disruptor.start();
        CountDownLatch latch = new CountDownLatch(1);
        long begin = System.currentTimeMillis();
        es1.submit(new TradePushlisher(latch, disruptor));
        latch.await();    //进行向下
        disruptor.shutdown();
        es2.shutdown();
        System.err.println("总耗时: " + (System.currentTimeMillis() - begin));
    }
}
```

#### 并行计算-多边形高端操作
- Disruptor可以实现串并行同时编码
```
c1-> c2 -> c4
c1-> c3 -> c4
```

```java
/**2.3 菱形操作 (一)*/
disruptor.handleEventsWith(new Handler1(), new Handler2()).handleEventsWith(new Handler3());

/**2.3 菱形操作 (二)*/
EventHandlerGroup<Trade> group = disruptor.handleEventsWith(new Handler1(), new Handler2());
group.then(new Handler3());
```
- 六边形操作
```
s -> h1 -> h2 -> h3
s -> h4 -> h5 -> h3
```
```java
public class Main {
    public static void main(String[] args) throws InterruptedException {
        //构建一个线程池用于提交任务
        ExecutorService es1 = Executors.newFixedThreadPool(1);
        //因为是但消费者模式，有5个handler，所以此处线程池至少要5个
        ExecutorService es = Executors.newFixedThreadPool(5);
        //1 构建Disruptor
        Disruptor<Trade> disruptor = new Disruptor<Trade>(
                Trade::new,
                1024 * 1024,
                es,
                ProducerType.SINGLE,
                new BusySpinWaitStrategy());

        //2 把消费者设置到Disruptor中 handleEventsWith
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
```


### Disruptor - 多消费者模型讲解
- 依赖WorkerPoo|实现多消费者:
```java
workerPool(final RingBuffer<T> ringBuffer,
           final SequenceBarrier sequenceBarrier,
           final ExceptionHandler<? super T> exceptionHandler,
           final WorkHandler<? super T>... workHandlers)
```
```java
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
```