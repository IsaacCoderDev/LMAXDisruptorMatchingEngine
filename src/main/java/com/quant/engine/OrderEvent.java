package com.quant.engine;

import jdk.internal.vm.annotation.Contended;

/**
 * A mutable POJO representing a market order.
 * This object will be pre-allocated millions of times on startup.
 */
public class OrderEvent {
    
    @Contended
    private long orderId;

    @Contended
    private long timestampNs;
    
    private double price;
    private double quantity;
    private short instrumentId;
    private byte side;

    public void set(long orderId, long timestampNs, double price, double quantity, short instrumentId, byte side) {
        this.orderId = orderId;
        this.timestampNs = timestampNs;
        this.price = price;
        this.quantity = quantity;
        this.instrumentId = instrumentId;
        this.side = side;
    }

    public long getOrderId() { return orderId; }
    public long getTimestampNs() { return timestampNs; }
}