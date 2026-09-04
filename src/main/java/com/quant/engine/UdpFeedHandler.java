package com.quant.engine;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.DatagramChannel;

public class UdpFeedHandler {
    private final OrderProducer producer;
    private final ByteBuffer directBuffer;

    public UdpFeedHandler(OrderProducer producer) {
        this.producer = producer;
        
        // Set Little Endian byte order (standard for modern x86_64 Ubuntu systems)
        // to prevent the CPU from having to swap bytes when reading primitives.
        this.directBuffer = ByteBuffer.allocateDirect(1024).order(ByteOrder.LITTLE_ENDIAN);
    }

    public void listen(int port) {
        try (DatagramChannel channel = DatagramChannel.open()) {
            channel.bind(new InetSocketAddress(port));
            
            channel.configureBlocking(true);
            
            System.out.println("UDP Feed Handler active. Listening on port " + port + "...");

            while (true) {
                // 1. Reset the buffer pointers (does not allocate or erase memory)
                directBuffer.clear();
                
                // 2. The OS kernel copies the packet bytes directly into the off-heap buffer
                channel.receive(directBuffer);
                
                // 3. Flip the buffer from "write mode" (OS writing) to "read mode" (Disruptor reading)
                directBuffer.flip();
                
                // 4. Hand the buffer to the Disruptor producer (built on Day 1)
                producer.onData(directBuffer);
            }
            
        } catch (IOException e) {
            System.err.println("Network ingestion error: " + e.getMessage());
        }
    }
}