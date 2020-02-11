# hello-disruptor
[GITHUB - Disruptor](https://github.com/LMAX-Exchange/disruptor)
## 并发编程基础
### Atomic系列类 & UnSafe类
#### Atmoic系列类提供了原子性操作，保障多线程下的安全
**UnSafe类的四大作用:**
- 内存操作
- 字段的定位与修改
- 挂起与恢复
- CAS操作(乐观锁)
### Volatile
- 作用一：多线程间的可见性
- 作用二：阻止指令重排序
### J.U.C工具类
- CountDownLatch & CyclicBarrier
- Future模式与Caller接口
- Exchanger线程数据交换器
- ForkJoin并行计算框架
- Semaphore信号量
### AQS锁
- ReentrantLock重入锁
- ReentrantReadWriteLock读写锁
- Condition条件判断
- LockSupport基于线程的锁
### 线程池核心
- Executors工厂类
- ThreadPoolExecutor自定义线程池
- 计算机密集型与IO密集型
  - 计算机密集型：核心线程数取`cpu核心数量 + 1`或者`cpu核心数量 * 2`
  - IO密集型：核心线程数取`cpu核心数量 / (1 - 阻塞系数)`，阻塞系数一般为0.8 - 0.9
```java
ThreadPoolExecutor pool = new ThreadPoolExecutor(
        核心线程数,
        最大线程数,
        存活时间,
        TimeUnit.SECONDS,
        new ArrayBlockingQueue<>(200),
        new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new
                        Thread(r);
                t.setName("order- thread");
                if (t.isDaemon()) {
                    t.setDaemon(false);
                }
                if (Thread.NORM_PRIORITY != t.getPriority()) {
                    t.setPriority(Thread.NORM_PRIORITY);
                }
                return t;
            }
        },
        new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                System.out.println("拒绝策略：" + r + " executor：" + executor);
            }
        }
);
```
### AQS架构
```java
      +------+  prev +-----+       +-----+
 head |      | <---- |     | <---- |     |  tail
      +------+       +-----+       +-----+
```
- AQS维护了-个volatile int state (代表共享资源)和一-个FIFO
- 线程等待队列(多线程争用资源被阻塞时会进入此队列)
- AQS定义两种资源共享方式: Exclusive, Share
    - isHeldExclusively方法:该线程是否正在独占资源
    - tryAcquire / tryRelease :独占的方式尝试获取和释放资源（成功返回true）
    - tryAcquireShared / tryReleaseShared :共享方式尝试获取和释放资源
      - `tryAcquireShared` 尝试以共享模式进行获取。该方法应查询对象的状态是否允许以共享模式获取对象，如果允许，则查询该对象。此方法始终由执行获取的线程调用。如果此方法报告失败，则获取方法可能会将线程排队（如果尚未排队），直到被其他线程释放发出信号为止。
      - 返回负值说明获取失败，返回零则表示没有可用的资源，正数则表示获取成功

### AQS - ReentrantLock

- **state初始化为0**，表示未锁定状态
- A线程`lock()`时，会调用`tryAcquire()`独占该锁并将**state+ 1**
- 此后，其他线程再`tryAcquire()`时就会失败，直到A线程**unlock()**到**state=0** (即释放锁)为止，其它线程才有机会获取该锁

- 当然，释放锁之前，A线程自己是可以重复获取此锁的(**state会累加**)，这就是**可重入**的概念
- 但要注意，获取多少次就要释放多么次，这样才能保证state是能回到零态的



### AQS - CountDownLatch

- 任务分为N个子线程去执行，**state**也初始化为N (注意N要与线程个数一致)
- 这N个子线程是并行执行的，每个子线程执行完后**countDown()**一次，**state会CAS减1**
- 等到所有子线程都执行完后**(即state=0)**，会**unpark()**调用线程
- 然后主调用线程就会从**await()**函数返回，继续后余动作

## Disruptor Quick Start
1. 建立一个工厂"Event类， 用于创建Event类实例对象
2. 需要有一个监听事件类，用于处理数据(Event类)
3. 实例化Disruptor实例，配置-系列参数，编写Disruptor核心组件
4. 编写生产者组件，向Disruptor容器中去投递数据

## Disruptor核心原理
### Disruptor核心 - RingBuffer
[GITHUB - Disruptor - Introduction](https://github.com/LMAX-Exchange/disruptor/wiki/Introduction)
- 初看Disruptor，给人的印象就是RingBuffer是其核心，生产者向RingBuffer中写入元素，消费者从RingBuffer中消费元素

#### RingBuffer到底是啥
- 正如名字所说的一样，它是一个环(首尾相接的环)
- 它用做在不同上下文(线程)间传递数据的buffer!
- RingBuffer拥有一个序号，这个序号指向数组中下一个可用元素

#### RingBuffer数据结构深入探究
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
- 依赖`WorkerPool`实现多消费者:
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

## Disruptor源码分析

### Disruptor为何底层性能如此牛?

- 数据结构层面:使用环形结构、数组、内存预加载
- 使用单线程写方式、内存屏障
- 消除伪共享(填充缓存行)
- 序号栅栏和序号配合使用来消除锁和CAS

#### 高性能之道 - 数据结构 - 内存预加载机制

- RingBuffer使用数组Object[] entries作为存储元素
- 预加载即RingBuffer中的fill方法对数组的每一个位置创建一个空的event对象

#### 高性能之道 - 内核 - 使用单线程写

- Disruptor的RingBuffer，之所以可以做到完全无锁，也是因为"单线程写"，这是所有前提的前提
- 离了这个前提条件，没有任何技术可以做到完全无锁
- Redis、Netty等等高性能技术框架的设计都是这个核心思想

#### 高性能之道 - 系统内存优化 - 内存屏障

- 要正确的实现无锁，还需要另外一个关键技术：内存屏障。
- 对应到Java语言，就是valotile变量与happens before语义。
- 内存屏障 - Linux的`smp _wmb()/smp_rmb()`
- 系统内核:拿Linux的kfifo来举例：smp_ wmb()，无论是底层的读写都是使用了Linux的smp_wmb

> Linux内核源码 - https://github.com/opennetworklinux/linux-3.8.13/blob/master/kernel/kfifo.c

#### 高性能之道 - 系统缓存优化 - 消除伪共享

- 缓存系统中是以**缓存行(cache line)**为单位存储的
- 缓存行是2的整数幂个连续字节，一般为32 - 256个字节
- 最常见的缓存行大小是64个字节
- 当多线程修改互相独立的变量时，如果这些变量共享同一个缓存行，就会无意中影响彼此的性能，这就是伪共享
- Sequence消除伪共享原理，（左边填充7个long）value（右边填充7个long）使得存储的变量不会和其他变量在同一个缓存行，采用的是空间换时间的原理

```java
class LhsPadding {
    protected long p1, p2, p3, p4, p5, p6, p7;
}

class Value extends LhsPadding {
    protected volatile long value;
}

class RhsPadding extends Value {
    protected long p9, p10, p11, p12, p13, p14, p15;
}

public class Sequence extends RhsPadding { ... }
```

> **JDK1.8中增加了Contended注解方式来解决缓存伪共享问题。**
>
> 在JDK1.8中，新增了一种注解@sun.misc.Contended，来使各个变量在Cache line中分隔开。注意，jvm需要添加参数-XX:-RestrictContended才能开启此功能 

#### 