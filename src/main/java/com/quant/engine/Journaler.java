package com.quant.engine;

public class Journaler implements EventHandler<OrderEvent> {
    
    @Override
    public void onEvent(OrderEvent event, long sequence, boolean endOfBatch) {
        
        // System.out.println("Journaler: Appending Order " + event.getOrderId() + " to disk log.");
    }
}