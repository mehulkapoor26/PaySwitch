package com.payswitch.ui;

import com.payswitch.concurrency.ConcurrentTransferSimulator;
import com.payswitch.service.PaymentService;
import com.payswitch.model.Account;
import com.payswitch.config.DatabaseConfig;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.Scanner;

/** Scanner based CLI, kept separate from business rules for straightforward classroom demonstrations. */
public final class ConsoleMenu {
    private final PaymentService service;
    private final Scanner scanner;
    public ConsoleMenu(PaymentService service, Scanner scanner){this.service=service;this.scanner=scanner;}
    public void run(){boolean running=true;while(running){
        System.out.println("\n=== PaySwitch ===\n1. Create Account\n2. Deposit Money\n3. Transfer Money\n4. Check Balance & Statement\n5. Concurrent Transfer Simulation\n6. Full Ledger & Summary\n7. Test MySQL Connection\n8. Exit\nChoice: ");
        try {int choice=Integer.parseInt(scanner.nextLine().trim());switch(choice){case 1->create();case 2->deposit();case 3->transfer();case 4->statement();case 5->simulate();case 6->report();case 7->dbCheck();case 8->running=false;default->System.out.println("Choose 1 through 8.");}}
        catch(RuntimeException e){System.out.println("Request failed: "+e.getMessage());}
    }System.out.println("Goodbye.");}
    private void create(){System.out.print("Holder name: ");String name=scanner.nextLine();System.out.print("Opening balance: ");Account a=service.createAccount(name,new BigDecimal(scanner.nextLine().trim()));System.out.println("Created "+a);}
    private void deposit(){System.out.print("Account ID: ");long id=Long.parseLong(scanner.nextLine().trim());System.out.print("Amount: ");System.out.println(service.deposit(id,new BigDecimal(scanner.nextLine().trim())));}
    private void transfer(){System.out.print("Sender ID: ");long from=Long.parseLong(scanner.nextLine().trim());System.out.print("Receiver ID: ");long to=Long.parseLong(scanner.nextLine().trim());System.out.print("Amount: ");System.out.println(service.transfer(from,to,new BigDecimal(scanner.nextLine().trim())));}
    private void statement(){System.out.print("Account ID: ");long id=Long.parseLong(scanner.nextLine().trim());System.out.println(service.getAccount(id));service.getTransactionsForAccount(id).forEach(System.out::println);}
    private void simulate(){System.out.print("Sender ID: ");long from=Long.parseLong(scanner.nextLine().trim());System.out.print("Receiver ID: ");long to=Long.parseLong(scanner.nextLine().trim());System.out.print("Amount per transfer: ");BigDecimal amount=new BigDecimal(scanner.nextLine().trim());System.out.print("Number of transfers: ");int count=Integer.parseInt(scanner.nextLine().trim());System.out.print("Worker threads: ");int workers=Integer.parseInt(scanner.nextLine().trim());try{System.out.println(new ConcurrentTransferSimulator(service).run(from,to,amount,count,workers));}catch(InterruptedException e){Thread.currentThread().interrupt();System.out.println("Simulation interrupted.");}}
    private void report(){System.out.println("Accounts: "+service.getAllAccounts().size()+", total balance: "+service.getTotalBalance()+", transactions: "+service.getAllTransactions().size());service.getAllTransactions().forEach(System.out::println);}
    private void dbCheck(){try(Connection ignored=DatabaseConfig.getConnection()){System.out.println("MySQL connection successful.");}catch(Exception e){System.out.println("MySQL is unavailable; in-memory CLI remains usable. ("+e.getMessage()+")");}}
}
