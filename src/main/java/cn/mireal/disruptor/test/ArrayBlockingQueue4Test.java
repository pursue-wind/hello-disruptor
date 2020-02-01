package cn.mireal.disruptor.test;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * @author mireal
 */
public class ArrayBlockingQueue4Test {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        int num = 10000000;
        final ArrayBlockingQueue<User> queue = new ArrayBlockingQueue<User>(num);
        final long startTime = System.currentTimeMillis();
        //向容器中添加元素
        CompletableFuture.runAsync(() -> {
            long i = 0;
            while (i < num) {
                User data = new User(i, "c" + i);
                try {
                    queue.put(data);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                i++;
            }
        });
        CompletableFuture<String> res = CompletableFuture.supplyAsync(() -> {
            int k = 0;
            while (k < num) {
                try {
                    queue.take();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                k++;
            }
            long endTime = System.currentTimeMillis();

            return "ArrayBlockingQueue costTime = " + (endTime - startTime) + "ms";
        });
        System.out.println(res.get());
    }
}