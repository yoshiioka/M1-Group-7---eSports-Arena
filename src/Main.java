import database.DatabaseConnection;
import database.DatabaseInitializer;
import models.Spectator;
import services.AuthService;
import services.MatchService;
import services.TicketService;

import java.util.Scanner;


public class Main {

    private static final Scanner     scanner      = new Scanner(System.in);
    private static final AuthService authService  = new AuthService(scanner);
    private static final MatchService matchService = new MatchService(scanner);
    private static final TicketService ticketService = new TicketService(scanner);


    public static void main(String[] args) {

      
        DatabaseInitializer.initialize();

        boolean running = true;
        while (running) {
            printWelcome();
            System.out.print(" Enter choice: ");
            String input = scanner.nextLine().trim();

            switch (input) {
                case "1" -> authService.createAccount();
                case "2" -> {
                    Spectator logged = authService.login();
                    if (logged != null) {
                        mainMenu(logged);
                    }
                }
                case "3" -> {
                    System.out.println("\n Thank you for visiting the eSports Arena! Goodbye!\n");
                    running = false;
                }
                default  -> System.out.println(" [!] Invalid choice. Please try again.\n");
            }
        }

        DatabaseConnection.closeConnection();
        scanner.close();
    }






  
    private static void mainMenu(Spectator spectator) {
        boolean loggedIn = true;
        while (loggedIn) {
            printMainMenu(spectator);
            System.out.print(" Enter choice: ");
            String input = scanner.nextLine().trim();

            switch (input) {
                case "1" -> matchService.viewMatchSchedule();
                case "2" -> ticketService.selectEventTicket();
                case "3" -> ticketService.purchaseTicket(spectator);
                case "4" -> ticketService.viewTicketDetails(spectator);
                case "5" -> ticketService.downloadDigitalTicket(spectator);
                case "6" -> {
                    System.out.println("\n [✓] Logged out successfully. See you at the arena!\n");
                    loggedIn = false;
                }
                default  -> System.out.println(" [!] Invalid choice.\n");
            }
        }
    }



  
    private static void printWelcome() {
        System.out.println();
        System.out.println("==============================================");
        System.out.println("      ESPORTS ARENA TICKETING SYSTEM         ");
        System.out.println("==============================================");
        System.out.println();
        System.out.println("             Welcome!");
        System.out.println();
        System.out.println(" [1] Create Account");
        System.out.println(" [2] Login Account");
        System.out.println(" [3] Exit");
        System.out.println();
        System.out.println("==============================================");
    }

    private static void printMainMenu(Spectator spectator) {
        System.out.println();
        System.out.println("================================================================");
        System.out.println(" MAIN MENU");
        System.out.println("================================================================");
        System.out.println();
        System.out.println(" Welcome, " + spectator.getFullName() + "!");
        System.out.println();
        System.out.println(" [1] View Match Schedule");
        System.out.println(" [2] Select Event Ticket");
        System.out.println(" [3] Purchase Ticket");
        System.out.println(" [4] View Ticket Details");
        System.out.println(" [5] Download Digital Ticket");
        System.out.println(" [6] Logout");
        System.out.println();
        System.out.println("================================================================");
    }
}
