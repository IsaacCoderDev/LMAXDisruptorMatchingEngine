package com.quant.engine;

import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

// Scope.Benchmark means the state is shared across all testing threads.
@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@BenchmarkMode(Mode.Throughput)
@Fork(1)
@Warmup(iterations = 3, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 3, timeUnit = TimeUnit.SECONDS)
public class DisruptorBenchmark {

    private Disruptor<OrderEvent> disruptor;
    private RingBuffer<OrderEvent> ringBuffer;
    private ArrayBlockingQueue<OrderEvent> blockingQueue;

    @Setup(Level.Trial)
    public void setup() {
        int bufferSize = 1048576;
        
        // 1. Setup Disruptor
        disruptor = new Disruptor<>(
                new OrderEventFactory(),
                bufferSize,
                DaemonThreadFactory.INSTANCE,
                ProducerType.SINGLE,
                new YieldingWaitStrategy()
        );
        
        disruptor.handleEventsWith((event, sequence, endOfBatch) -> {
            // Consumer logic for Disruptor
        });
        
        disruptor.start();
        ringBuffer = disruptor.getRingBuffer();

        // 2. Setup Standard Java Queue
        blockingQueue = new ArrayBlockingQueue<>(bufferSize);
        
        new Thread(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    blockingQueue.take();
                }
            } catch (InterruptedException e) {
                
            }
        }).start();
    }

    @TearDown(Level.Trial)
    public void teardown() {
        disruptor.shutdown();
    }

    // ========================================================================
    // BENCHMARK 1: The standard Java Object/Locking approach
    // ========================================================================
    @Benchmark
    public void testArrayBlockingQueue(Blackhole blackhole) throws InterruptedException {
        OrderEvent event = new OrderEvent();
        event.set(1001L, System.nanoTime(), 50000.0, 1.0, (short) 1, (byte) 0);
        
        blockingQueue.put(event);
        blackhole.consume(event); // Prevent JIT from deleting the allocation
    }

    // ========================================================================
    // BENCHMARK 2: The Zero-Allocation Disruptor approach
    // ========================================================================
    @Benchmark
    public void testLmaxDisruptor(Blackhole blackhole) {
        long sequence = ringBuffer.next();
        try {
            OrderEvent event = ringBuffer.get(sequence);
            // In-place mutation. Zero allocation.
            event.set(1001L, System.nanoTime(), 50000.0, 1.0, (short) 1, (byte) 0);
            blackhole.consume(event);
        } finally {
            ringBuffer.publish(sequence);
        }
    }

    public static void main(String[] args) throws Exception {
        org.openjdk.jmh.Main.main(args);
    }
}