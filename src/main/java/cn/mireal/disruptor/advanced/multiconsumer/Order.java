package cn.mireal.disruptor.advanced.multiconsumer;

/**
 * Disruptor中的 Event
 *
 * @author Mireal
 * @version V1.0
 * @date 2020/2/5 14:18
 */
public class Order {

    private String id;
    private String name;
    private double price;

    public Order() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }


}
