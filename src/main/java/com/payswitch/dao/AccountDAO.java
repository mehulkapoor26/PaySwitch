package com.payswitch.dao;

import com.payswitch.model.Account;
import java.sql.SQLException;
import java.util.List;
public interface AccountDAO { void insert(Account account)throws SQLException; Account findById(long id)throws SQLException; List<Account> findAll()throws SQLException; }
