package cn.mireal.disruptor.test;

import com.lmax.disruptor.EventHandler;

/**
 * @author mireal
 */
public class Consumer implements EventHandler<User> {

    private long startTime;
    private int i;

    public Consumer() {
        this.startTime = System.currentTimeMillis();
    }

    @Override
    public void onEvent(User event, long sequence, boolean endOfBatch) throws Exception {
        i++;
        if (i == 10000000) {
            long endTime = System.currentTimeMillis();
            System.out.println("Disruptor costTime = " + (endTime - startTime) + "ms");
        }
    }
}
