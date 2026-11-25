package com.payswitch.service;

import com.payswitch.exception.*;
import com.payswitch.model.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/** In-memory payment core. Synchronized public operations make balance updates and audit writes atomic. */
public class PaymentService {
    private final Map<Long, Account> accounts = new HashMap<>();
    private final List<Transaction> transactions = new ArrayList<>();
    private final AtomicLong idGenerator = new AtomicLong(1000);

    public synchronized Account createAccount(String holderName, BigDecimal openingBalance) {
        if (openingBalance == null || openingBalance.signum() < 0 || openingBalance.scale() > 2)
            throw new IllegalArgumentException("Opening balance must be non-negative with at most two decimal places");
        Account account = new Account(idGenerator.incrementAndGet(), holderName, openingBalance);
        accounts.put(account.getAccountId(), account); return account;
    }
    public synchronized Account getAccount(long id) { Account a = accounts.get(id); if (a == null) throw new InvalidAccountException("Account not found: " + id); return a; }
    public synchronized BigDecimal getBalance(long id) { return getAccount(id).getBalance(); }
    public synchronized List<Account> getAllAccounts() { return List.copyOf(accounts.values()); }
    public synchronized Transaction deposit(long receiverId, BigDecimal amount) {
        try { validateAmount(amount); Account receiver = getAccount(receiverId); receiver.credit(amount); return record(null, receiverId, amount, Transaction.Status.SUCCESS, null); }
        catch (RuntimeException ex) { record(null, receiverId, amount, Transaction.Status.FAILED, ex.getMessage()); throw ex; }
    }
    public synchronized Transaction transfer(long senderId, long receiverId, BigDecimal amount) {
        try {
            validateAmount(amount);
            if (senderId == receiverId) throw new IllegalArgumentException("Sender and receiver must differ");
            Account sender = getAccount(senderId), receiver = getAccount(receiverId);
            if (sender.getBalance().compareTo(amount) < 0) throw new InsufficientBalanceException("Insufficient balance in account " + senderId);
            // Holding the service monitor prevents interleaved debit/credit and audit updates; one global lock also removes lock-order deadlocks.
            sender.debit(amount); receiver.credit(amount);
            return record(senderId, receiverId, amount, Transaction.Status.SUCCESS, null);
        } catch (RuntimeException ex) { record(senderId, receiverId, amount, Transaction.Status.FAILED, ex.getMessage()); throw ex; }
    }
    private Transaction record(Long from, Long to, BigDecimal amount, Transaction.Status status, String reason) {
        Transaction tx = new Transaction(UUID.randomUUID(), from, to, amount, status, reason, LocalDateTime.now()); transactions.add(tx); return tx;
    }
    private static void validateAmount(BigDecimal amount) { if (amount == null || amount.scale() > 2 || amount.signum() <= 0) throw new IllegalArgumentException("Amount must be positive and have at most two decimal places"); }
    public synchronized List<Transaction> getAllTransactions() { return List.copyOf(transactions); }
    public synchronized List<Transaction> getTransactionsForAccount(long id) { return transactions.stream().filter(t -> Objects.equals(t.getSenderId(), id) || Objects.equals(t.getReceiverId(), id)).toList(); }
    public synchronized BigDecimal getTotalBalance() { return accounts.values().stream().map(Account::getBalance).reduce(BigDecimal.ZERO, BigDecimal::add); }
}
