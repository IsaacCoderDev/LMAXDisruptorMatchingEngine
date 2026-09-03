package com.quant.engine;

public class RiskValidator implements EventHandler<OrderEvent> {
    @Override
    public void onEvent(OrderEvent event, long sequence, boolean endOfBatch) {
        
        // System.out.println("Risk Check: Validating Order " + event.getOrderId());
        
        if (event.getQuantity() <= 0) {
            // event.setValid(false); 
        }
    }
}
