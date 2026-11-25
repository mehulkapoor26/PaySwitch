package com.payswitch.exception;
/** Runtime failure for an unknown or invalid wallet account. */
public class InvalidAccountException extends RuntimeException { public InvalidAccountException(String message) { super(message); } }
