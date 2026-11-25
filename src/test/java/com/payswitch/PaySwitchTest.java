package com.payswitch;

import com.payswitch.concurrency.ConcurrentTransferSimulator;
import com.payswitch.exception.*;
import com.payswitch.model.Account;
import com.payswitch.service.PaymentService;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

class PaySwitchTest {
    private static BigDecimal money(String value){return new BigDecimal(value);}
    @Test void accountCreationAndDeposit(){PaymentService s=new PaymentService();Account a=s.createAccount("Mehul",money("10.00"));s.deposit(a.getAccountId(),money("2.50"));assertEquals(money("12.50"),s.getBalance(a.getAccountId()));}
    @Test void successfulTransferMovesMoney(){PaymentService s=new PaymentService();Account a=s.createAccount("A",money("20")),b=s.createAccount("B",money("0"));s.transfer(a.getAccountId(),b.getAccountId(),money("7.25"));assertEquals(money("12.75"),s.getBalance(a.getAccountId()));assertEquals(money("7.25"),s.getBalance(b.getAccountId()));}
    @Test void insufficientTransferRollsBackAndLogsFailure(){PaymentService s=new PaymentService();Account a=s.createAccount("A",money("1")),b=s.createAccount("B",money("0"));assertThrows(InsufficientBalanceException.class,()->s.transfer(a.getAccountId(),b.getAccountId(),money("2")));assertEquals(money("1.00"),s.getBalance(a.getAccountId()));assertEquals(money("0.00"),s.getBalance(b.getAccountId()));assertEquals("FAILED",s.getAllTransactions().getFirst().getStatus().name());assertNotNull(s.getAllTransactions().getFirst().getFailureReason());}
    @Test void invalidAccountThrows(){PaymentService s=new PaymentService();assertThrows(InvalidAccountException.class,()->s.getBalance(42));}
    @Test void invalidAmountsAndSelfTransferReject(){PaymentService s=new PaymentService();Account a=s.createAccount("A",money("10"));assertThrows(IllegalArgumentException.class,()->s.deposit(a.getAccountId(),money("0")));assertThrows(IllegalArgumentException.class,()->s.transfer(a.getAccountId(),a.getAccountId(),money("1")));assertThrows(IllegalArgumentException.class,()->s.transfer(a.getAccountId(),999,money("-1")));}
    @Test void concurrentBidirectionalTransfersPreserveTotalAndDoNotDeadlock() throws Exception {PaymentService s=new PaymentService();Account a=s.createAccount("A",money("500")),b=s.createAccount("B",money("500"));ExecutorService pool=Executors.newFixedThreadPool(2);try{Future<?> one=pool.submit(()->{for(int i=0;i<200;i++)s.transfer(a.getAccountId(),b.getAccountId(),money("1"));});Future<?> two=pool.submit(()->{for(int i=0;i<200;i++)s.transfer(b.getAccountId(),a.getAccountId(),money("1"));});one.get(10,TimeUnit.SECONDS);two.get(10,TimeUnit.SECONDS);}finally{pool.shutdownNow();}assertEquals(money("1000.00"),s.getTotalBalance());}
    @Test void simulatorCountsOutcomes(){PaymentService s=new PaymentService();Account a=s.createAccount("A",money("3")),b=s.createAccount("B",money("0"));try{var result=new ConcurrentTransferSimulator(s).run(a.getAccountId(),b.getAccountId(),money("1"),5,3);assertEquals(3,result.getSuccessCount());assertEquals(2,result.getFailedCount());assertEquals(result.getInitialTotalBalance(),result.getFinalTotalBalance());}catch(InterruptedException e){Thread.currentThread().interrupt();fail(e);}}
}
