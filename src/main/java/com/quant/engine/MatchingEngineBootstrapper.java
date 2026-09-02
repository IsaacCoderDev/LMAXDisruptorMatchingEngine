package com.quant.engine;

import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.util.DaemonThreadFactory;

import java.nio.ByteBuffer;

public class MatchingEngineBootstrapper {
    public static void main(String[] args) {
        // The size of the Ring Buffer MUST be a power of 2 (e.g., 1024, 4096, 1048576).
        // This allows the Disruptor to use bitwise AND (&) for lightning-fast modulo operations.
        int bufferSize = 1048576; 

        Disruptor<OrderEvent> disruptor = new Disruptor<>(
                new OrderEventFactory(),
                bufferSize,
                DaemonThreadFactory.INSTANCE,
                ProducerType.SINGLE,
                new YieldingWaitStrategy()
        );

        disruptor.start();

        RingBuffer<OrderEvent> ringBuffer = disruptor.getRingBuffer();
        OrderProducer producer = new OrderProducer(ringBuffer);

        System.out.println("LMAX Disruptor Booted. Pre-allocated 1,048,576 OrderEvents.");

        // Simulate incoming UDP/TCP traffic
        ByteBuffer mockNetworkPacket = ByteBuffer.allocate(35);
        mockNetworkPacket.putLong(1001L);
        mockNetworkPacket.putDouble(50000.50);
        mockNetworkPacket.putDouble(2.5);
        mockNetworkPacket.putShort((short) 1);
        mockNetworkPacket.put((byte) 0);
        mockNetworkPacket.flip();

        // Push the mock packet into the pipeline
        producer.onData(mockNetworkPacket);
    }
}