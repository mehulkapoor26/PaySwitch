package com.payswitch.model;

import java.math.BigDecimal;
import java.util.Objects;

/** Wallet account; BigDecimal avoids binary floating-point rounding in monetary values. */
public final class Account {
    private final long accountId;
    private String holderName;
    private BigDecimal balance;

    public Account(long accountId, String holderName, BigDecimal balance) {
        if (accountId <= 0) throw new IllegalArgumentException("Account ID must be positive");
        if (holderName == null || holderName.isBlank()) throw new IllegalArgumentException("Holder name is required");
        this.accountId = accountId;
        this.holderName = holderName.trim();
        this.balance = checkedAmount(balance, true);
    }
    private static BigDecimal checkedAmount(BigDecimal amount, boolean allowZero) {
        if (amount == null || amount.scale() > 2 || amount.signum() < 0 || (!allowZero && amount.signum() == 0))
            throw new IllegalArgumentException("Amount must be non-negative with at most two decimal places");
        return amount.setScale(2);
    }
    public long getAccountId() { return accountId; }
    public String getHolderName() { return holderName; }
    public synchronized BigDecimal getBalance() { return balance; }
    public synchronized void credit(BigDecimal amount) { balance = balance.add(checkedAmount(amount, false)); }
    public synchronized void debit(BigDecimal amount) {
        BigDecimal value = checkedAmount(amount, false);
        if (balance.compareTo(value) < 0) throw new IllegalStateException("Insufficient balance");
        balance = balance.subtract(value);
    }
    @Override public boolean equals(Object other) { return other instanceof Account a && accountId == a.accountId; }
    @Override public int hashCode() { return Objects.hash(accountId); }
    @Override public synchronized String toString() { return "Account{id=" + accountId + ", holder='" + holderName + "', balance=" + balance + "}"; }
}
