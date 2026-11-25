package com.payswitch;

import com.payswitch.service.PaymentService;
import com.payswitch.ui.ConsoleMenu;
import java.util.Scanner;

/** Application entry point. */
public final class Main {
    private Main(){}
    public static void main(String[] args){new ConsoleMenu(new PaymentService(),new Scanner(System.in)).run();}
}
