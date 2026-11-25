package com.payswitch.concurrency;

import com.payswitch.service.PaymentService;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/** Exercises PaymentService from worker threads; the service protects shared wallet state. */
public final class ConcurrentTransferSimulator {
    private final PaymentService paymentService;
    public ConcurrentTransferSimulator(PaymentService paymentService){this.paymentService=Objects.requireNonNull(paymentService);}
    public SimulationResult run(long from, long to, BigDecimal amount, int transferCount, int threadCount) throws InterruptedException {
        if(transferCount<0||threadCount<1) throw new IllegalArgumentException("Transfer count must be non-negative and thread count positive");
        BigDecimal initial=paymentService.getTotalBalance(); AtomicInteger success=new AtomicInteger(), failed=new AtomicInteger();
        ExecutorService pool=Executors.newFixedThreadPool(threadCount);
        try {
            List<Callable<Void>> jobs=new ArrayList<>();
            for(int i=0;i<transferCount;i++) jobs.add(()->{try{paymentService.transfer(from,to,amount);success.incrementAndGet();}catch(RuntimeException e){failed.incrementAndGet();}return null;});
            for(Future<Void> future:pool.invokeAll(jobs)) try{future.get();}catch(ExecutionException e){failed.incrementAndGet();}
        } finally {pool.shutdown(); if(!pool.awaitTermination(30,TimeUnit.SECONDS)) pool.shutdownNow();}
        return new SimulationResult(success.get(),failed.get(),initial,paymentService.getTotalBalance());
    }
}
