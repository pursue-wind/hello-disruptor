package cn.mireal.disruptor.quickstart_java8;

import com.lmax.disruptor.EventTranslatorOneArg;
import com.lmax.disruptor.RingBuffer;

import java.nio.ByteBuffer;

/**
 * 在Disruptor的3.0版中，添加了更丰富的Lambda样式的API，以通过将这种复杂性封装在环形缓冲区中来帮助开发人员，因此
 * 在3.0版之后，发布消息的首选方法是通过API的Event Publisher / Event Translator部分。
 *
 * @author Mireal
 */
public class LongEventProducerWithTranslator {
    private final RingBuffer<LongEvent> ringBuffer;

    public LongEventProducerWithTranslator(RingBuffer<LongEvent> ringBuffer) {
        this.ringBuffer = ringBuffer;
    }

    private static final EventTranslatorOneArg<LongEvent, ByteBuffer> TRANSLATOR = (event, sequence, bb) -> event.set(bb.getLong(0));

    public void onData(ByteBuffer bb) {
        ringBuffer.publishEvent(TRANSLATOR, bb);
    }
}