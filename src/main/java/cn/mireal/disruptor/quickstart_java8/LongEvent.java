package cn.mireal.disruptor.quickstart_java8;

import lombok.Data;

/**
 * 首先，我们将定义将携​​带数据的事件。
 *
 * @author Mireal
 */
@Data
public class LongEvent {
    private long value;

    public void set(long value) {
        this.value = value;
    }
}