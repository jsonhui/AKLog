package com.yuwei.face.util;

import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/*
    采用Timer的函数调用方式调用ScheduledExecutorService
 */
public class MyOwnTimer{

    ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    
    public void schedule(TimerTask task, long delay, long period) {
       
        executor.scheduleAtFixedRate(
                task,
                delay,
                period,
                TimeUnit.MILLISECONDS);
    }
    
    public void scheduleOneShort(TimerTask task, long delay){
        executor.schedule(  
                task,
                delay,
                TimeUnit.MILLISECONDS);
    }
    
    public void cancel() {
        executor.shutdownNow();
        executor.shutdown();
        
    }
}
