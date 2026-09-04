package com.quant.engine;

import net.openhft.affinity.AffinityLock;
import net.openhft.affinity.AffinityStrategies;
import java.util.concurrent.ThreadFactory;

public class PinnedThreadFactory implements ThreadFactory {
    private final String threadName;
    private final int cpuId;

    public PinnedThreadFactory(String threadName, int cpuId) {
        this.threadName = threadName;
        this.cpuId = cpuId;
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread thread = new Thread(() -> {
            
            try (AffinityLock al = AffinityLock.acquireLock(cpuId)) {
                r.run();
            }
        });
        
        thread.setName(threadName);
        thread.setDaemon(true);
        
        return thread;
    }
}