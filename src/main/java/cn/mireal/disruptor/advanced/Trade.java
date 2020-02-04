package cn.mireal.disruptor.advanced;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Mireal
 * @version V1.0
 * @date 2020/2/3 17:16
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Trade {
    private String id;
    private String name;
    private double price;
    private AtomicInteger count = new AtomicInteger(0);
}
