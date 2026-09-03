package com.quant.engine;

import com.lmax.disruptor.EventHandler;
import org.agrona.collections.Long2LongHashMap;

public class MatchingEngineHandler implements EventHandler<OrderEvent> {

    private final Long2LongHashMap bidBook = new Long2LongHashMap(-1L);
    private final Long2LongHashMap askBook = new Long2LongHashMap(-1L);

    private static final int MAX_RESTING_ORDERS = 1_000_000;
    private final long[] orderIds = new long[MAX_RESTING_ORDERS];
    private final double[] quantities = new double[MAX_RESTING_ORDERS];
    private final long[] nextOrderIndexes = new long[MAX_RESTING_ORDERS];
    
    private int nextAvailableIndex = 0;

    @Override
    public void onEvent(OrderEvent event, long sequence, boolean endOfBatch) {
        
        if (event.getSide() == 0) {
            matchBuyOrder(event);
        } else {
            matchSellOrder(event);
        }
    }

    private void matchBuyOrder(OrderEvent event) {
        
        long priceTick = (long) (event.getPrice() * 100);
        
        long bestAskTick = getBestAskTick(); 
        
        if (bestAskTick != -1L && priceTick >= bestAskTick) {
            System.out.println("Trade Executed! Order: " + event.getOrderId());
            
        } else {
            restOrder(bidBook, priceTick, event);
        }
    }

    private void matchSellOrder(OrderEvent event) {
        
        System.out.println("Processing Sell Order: " + event.getOrderId());
    }

    private void restOrder(Long2LongHashMap book, long priceTick, OrderEvent event) {
        
        int currentIndex = nextAvailableIndex++;
        
        orderIds[currentIndex] = event.getOrderId();
        quantities[currentIndex] = event.getQuantity();
        nextOrderIndexes[currentIndex] = -1L; // End of the queue

        long existingHeadIndex = book.get(priceTick);
        
        if (existingHeadIndex == -1L) {
            book.put(priceTick, currentIndex);
        } else {
            long traverseIndex = existingHeadIndex;
            
            while (nextOrderIndexes[(int) traverseIndex] != -1L) {
                traverseIndex = nextOrderIndexes[(int) traverseIndex];
            }
            
            nextOrderIndexes[(int) traverseIndex] = currentIndex;
        }
    }

    private long getBestAskTick() {
        
        return -1L; 
    }
}