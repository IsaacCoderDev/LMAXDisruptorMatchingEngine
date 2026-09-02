package com.quant.engine;

import com.lmax.disruptor.RingBuffer;
import java.nio.ByteBuffer;

public class OrderProducer {
    
    private final RingBuffer<OrderEvent> ringBuffer;

    public OrderProducer(RingBuffer<OrderEvent> ringBuffer) {
        this.ringBuffer = ringBuffer;
    }

    /**
     * Translates an incoming network byte buffer directly into the Ring Buffer.
     */
    public void onData(ByteBuffer buffer) {
        
        long sequence = ringBuffer.next();
        
        try {
            
            OrderEvent event = ringBuffer.get(sequence);

            
            event.set(
                buffer.getLong(),
                System.nanoTime(),
                buffer.getDouble(),
                buffer.getDouble(),
                buffer.getShort(),
                buffer.get()
            );
        } finally {
            
            ringBuffer.publish(sequence);
        }
    }
}