package com.payswitch.dao;

import com.payswitch.model.Transaction;
import java.sql.SQLException;
import java.util.List;
public interface TransactionDAO { void insert(Transaction transaction)throws SQLException; List<Transaction> findAll()throws SQLException; }
