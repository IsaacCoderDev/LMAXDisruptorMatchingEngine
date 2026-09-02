package com.quant.engine;

import com.lmax.disruptor.EventFactory;

class OrderEventFactory implements EventFactory<OrderEvent> {
    
    @Override
    public OrderEvent newInstance() {
        
        return new OrderEvent();
    }
}
