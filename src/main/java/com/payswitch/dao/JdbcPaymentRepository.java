package com.payswitch.dao;

import com.payswitch.config.DatabaseConfig;
import com.payswitch.exception.InsufficientBalanceException;
import com.payswitch.model.Transaction;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.UUID;

/** Performs a database transfer as one ACID unit using prepared statements and explicit transaction control. */
public final class JdbcPaymentRepository {
    public Transaction transferAtomicInDb(long senderId,long receiverId,BigDecimal amount)throws SQLException {
        if(amount==null||amount.signum()<=0||amount.scale()>2)throw new IllegalArgumentException("Amount must be positive with at most two decimal places");
        if(senderId==receiverId)throw new IllegalArgumentException("Sender and receiver must differ");
        try(Connection c=DatabaseConfig.getConnection()){
            c.setAutoCommit(false);
            try {
                // Lock rows in ascending ID order so two opposite transfers request locks consistently.
                long first=Math.min(senderId,receiverId), second=Math.max(senderId,receiverId);
                try(PreparedStatement lock=c.prepareStatement("SELECT account_id FROM accounts WHERE account_id IN (?,?) ORDER BY account_id FOR UPDATE")){lock.setLong(1,first);lock.setLong(2,second);try(ResultSet rs=lock.executeQuery()){int found=0;while(rs.next())found++;if(found!=2)throw new SQLException("Sender or receiver account does not exist");}}
                try(PreparedStatement debit=c.prepareStatement("UPDATE accounts SET balance=balance-? WHERE account_id=? AND balance>=?")){debit.setBigDecimal(1,amount);debit.setLong(2,senderId);debit.setBigDecimal(3,amount);if(debit.executeUpdate()!=1)throw new InsufficientBalanceException("Insufficient balance in account "+senderId);}
                try(PreparedStatement credit=c.prepareStatement("UPDATE accounts SET balance=balance+? WHERE account_id=?")){credit.setBigDecimal(1,amount);credit.setLong(2,receiverId);if(credit.executeUpdate()!=1)throw new SQLException("Receiver account disappeared");}
                Transaction tx=new Transaction(UUID.randomUUID(),senderId,receiverId,amount.setScale(2),Transaction.Status.SUCCESS,null,LocalDateTime.now());
                try(PreparedStatement log=c.prepareStatement("INSERT INTO transactions(transaction_id,sender_id,receiver_id,amount,status,created_at) VALUES(?,?,?,?,?,?)")){log.setString(1,tx.getTransactionId().toString());log.setLong(2,senderId);log.setLong(3,receiverId);log.setBigDecimal(4,amount);log.setString(5,tx.getStatus().name());log.setTimestamp(6,Timestamp.valueOf(tx.getCreatedAt()));log.executeUpdate();}
                c.commit();return tx;
            } catch(SQLException|RuntimeException e){try{c.rollback();}catch(SQLException rollback){e.addSuppressed(rollback);}throw e;}
        }
    }
}
