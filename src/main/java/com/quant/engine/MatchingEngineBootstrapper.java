package com.quant.engine;

import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.util.DaemonThreadFactory;

import java.nio.ByteBuffer;

public class MatchingEngineBootstrapper {
    public static void main(String[] args) {
        int bufferSize = 1048576; 

        Disruptor<OrderEvent> disruptor = new Disruptor<>(
                new OrderEventFactory(),
                bufferSize,
                DaemonThreadFactory.INSTANCE,
                ProducerType.SINGLE,
                new YieldingWaitStrategy()
        );

        // ====================================================================
        // THE DIAMOND PIPELINE
        // ====================================================================
        RiskValidator riskValidator = new RiskValidator();
        Journaler journaler = new Journaler();
        MatchingEngineHandler matchingEngine = new MatchingEngineHandler();

        // 1. handleEventsWith() assigns handlers to process immediately upon publication.
        disruptor.handleEventsWith(riskValidator, journaler)
                 .then(matchingEngine);

        // Boot the engine
        disruptor.start();

        RingBuffer<OrderEvent> ringBuffer = disruptor.getRingBuffer();
        OrderProducer producer = new OrderProducer(ringBuffer);

        System.out.println("LMAX Disruptor Booted. Pipeline: [Risk | Journal] -> [Matching Engine]");

        UdpFeedHandler feedHandler = new UdpFeedHandler(producer);
        feedHandler.listen(9000);
    }
}