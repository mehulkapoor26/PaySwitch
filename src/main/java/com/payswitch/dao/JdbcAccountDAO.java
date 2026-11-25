package com.payswitch.dao;

import com.payswitch.config.DatabaseConfig;
import com.payswitch.model.Account;
import java.sql.*;
import java.util.*;
public final class JdbcAccountDAO implements AccountDAO {
    public void insert(Account a)throws SQLException{try(Connection c=DatabaseConfig.getConnection();PreparedStatement p=c.prepareStatement("INSERT INTO accounts(account_id,holder_name,balance) VALUES(?,?,?)")){p.setLong(1,a.getAccountId());p.setString(2,a.getHolderName());p.setBigDecimal(3,a.getBalance());p.executeUpdate();}}
    public Account findById(long id)throws SQLException{try(Connection c=DatabaseConfig.getConnection();PreparedStatement p=c.prepareStatement("SELECT account_id,holder_name,balance FROM accounts WHERE account_id=?")){p.setLong(1,id);try(ResultSet r=p.executeQuery()){return r.next()?new Account(r.getLong(1),r.getString(2),r.getBigDecimal(3)):null;}}}
    public List<Account> findAll()throws SQLException{List<Account> out=new ArrayList<>();try(Connection c=DatabaseConfig.getConnection();PreparedStatement p=c.prepareStatement("SELECT account_id,holder_name,balance FROM accounts ORDER BY account_id");ResultSet r=p.executeQuery()){while(r.next())out.add(new Account(r.getLong(1),r.getString(2),r.getBigDecimal(3)));}return List.copyOf(out);}
}
