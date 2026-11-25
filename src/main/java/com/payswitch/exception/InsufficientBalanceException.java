package com.payswitch.exception;
/** Runtime failure raised before a debit would make a wallet negative. */
public class InsufficientBalanceException extends RuntimeException { public InsufficientBalanceException(String message) { super(message); } }
