# hello-disruptor


### Disruptor Quick Start
1. 建立一个工厂"Event类， 用于创建Event类实例对象
2. 需要有一个监听事件类，用于处理数据(Event类)
3. 实例化Disruptor实例，配置-系列参数，编写Disruptor核心组件
4. 编写生产者组件，向Disruptor容器中去投递数据

## Disruptor核心原理
- 初看Disruptor，给人的印象就是RingBuffer是其核心，生产者向RingBuffer中写入元素，消费者从RingBuffer中消费元素

### RingBuffer到底是啥
- 正如名字所说的一样，它是一个环(首尾相接的环)
- 它用做在不同上下文(线程)间传递数据的buffer!
- RingBuffer拥有一个序号,这个序号指向数组中下一个可用元素

### RingBuffer数据结构深入探究
- 随着你不停地填充这个buffer (可能也会有相应的读取)， 这个序号会一直增长，直到绕过这个环
- 要找到数组中当前序号指向的元素，可以通过mod操作: sequence mod array length = array index (取模操作)以上面的RingBuffer的size 10 为例(java的mod语法) : 12 % 10= 2
- 如果槽的个数是2的N次方更有利于基于二进制的计算机进行计算

> RingBuffer：基于数组的缓存实现，也是创建sequencer与定义WaitStrategy的入口

> Disruptor：持有RingBuffer、消费者线程池Executor、消费者集合ConsumerRepository等引用