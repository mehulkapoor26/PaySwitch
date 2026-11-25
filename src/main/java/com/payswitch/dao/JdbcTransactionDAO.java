package com.payswitch.dao;

import com.payswitch.config.DatabaseConfig;
import com.payswitch.model.Transaction;
import java.sql.*;
import java.util.*;
public final class JdbcTransactionDAO implements TransactionDAO {
    public void insert(Transaction t)throws SQLException{try(Connection c=DatabaseConfig.getConnection();PreparedStatement p=c.prepareStatement("INSERT INTO transactions(transaction_id,sender_id,receiver_id,amount,status,failure_reason,created_at) VALUES(?,?,?,?,?,?,?)")){p.setString(1,t.getTransactionId().toString());if(t.getSenderId()==null)p.setNull(2,Types.BIGINT);else p.setLong(2,t.getSenderId());if(t.getReceiverId()==null)p.setNull(3,Types.BIGINT);else p.setLong(3,t.getReceiverId());p.setBigDecimal(4,t.getAmount());p.setString(5,t.getStatus().name());p.setString(6,t.getFailureReason());p.setTimestamp(7,Timestamp.valueOf(t.getCreatedAt()));p.executeUpdate();}}
    public List<Transaction> findAll()throws SQLException{List<Transaction> out=new ArrayList<>();String q="SELECT transaction_id,sender_id,receiver_id,amount,status,failure_reason,created_at FROM transactions ORDER BY created_at";try(Connection c=DatabaseConfig.getConnection();PreparedStatement p=c.prepareStatement(q);ResultSet r=p.executeQuery()){while(r.next()){long s=r.getLong(2);Long sender=r.wasNull()?null:s;long d=r.getLong(3);Long receiver=r.wasNull()?null:d;out.add(new Transaction(UUID.fromString(r.getString(1)),sender,receiver,r.getBigDecimal(4),Transaction.Status.valueOf(r.getString(5)),r.getString(6),r.getTimestamp(7).toLocalDateTime()));}}return List.copyOf(out);}
}
