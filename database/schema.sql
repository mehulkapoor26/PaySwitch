CREATE DATABASE IF NOT EXISTS payswitch_db;
USE payswitch_db;

CREATE TABLE IF NOT EXISTS accounts (
  account_id BIGINT PRIMARY KEY,
  holder_name VARCHAR(120) NOT NULL,
  balance DECIMAL(15,2) NOT NULL CHECK (balance >= 0),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS transactions (
  transaction_id CHAR(36) PRIMARY KEY,
  sender_id BIGINT NULL,
  receiver_id BIGINT NOT NULL,
  amount DECIMAL(15,2) NOT NULL CHECK (amount > 0),
  status VARCHAR(10) NOT NULL CHECK (status IN ('SUCCESS','FAILED')),
  failure_reason VARCHAR(500),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_tx_sender FOREIGN KEY (sender_id) REFERENCES accounts(account_id),
  CONSTRAINT fk_tx_receiver FOREIGN KEY (receiver_id) REFERENCES accounts(account_id)
);
