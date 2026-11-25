package com.payswitch.concurrency;

import java.math.BigDecimal;
/** Immutable summary of a concurrent transfer run. */
public final class SimulationResult {
    private final int successCount, failedCount;
    private final BigDecimal initialTotalBalance, finalTotalBalance;
    public SimulationResult(int successCount, int failedCount, BigDecimal initialTotalBalance, BigDecimal finalTotalBalance) { this.successCount=successCount; this.failedCount=failedCount; this.initialTotalBalance=initialTotalBalance; this.finalTotalBalance=finalTotalBalance; }
    public int getSuccessCount(){return successCount;} public int getFailedCount(){return failedCount;}
    public BigDecimal getInitialTotalBalance(){return initialTotalBalance;} public BigDecimal getFinalTotalBalance(){return finalTotalBalance;}
    @Override public String toString(){return "SimulationResult{success="+successCount+", failed="+failedCount+", initialTotal="+initialTotalBalance+", finalTotal="+finalTotalBalance+"}";}
}
