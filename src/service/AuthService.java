package services;

import models.Spectator;
import repository.SpectatorRepository;

import java.util.Scanner;

public class AuthService {

    private final SpectatorRepository spectatorRepo;
    private final Scanner scanner;

    public AuthService(Scanner scanner) {
        this.spectatorRepo = new SpectatorRepository();
        this.scanner       = scanner;
    }


  
    public void createAccount() {
        printBorder("CREATE ACCOUNT");

        System.out.print("Full Name        : ");
        String fullName = scanner.nextLine().trim();

        System.out.print("Email            : ");
        String email = scanner.nextLine().trim();

        System.out.print("Password         : ");
        String password = scanner.nextLine().trim();

        System.out.print("Confirm Password : ");
        String confirmPass = scanner.nextLine().trim();

        System.out.println();


      
        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            System.out.println("[!] The spectator could not create an account because the required information was incomplete.");
            pausePrompt();
            return;
        }

        if (!password.equals(confirmPass)) {
            System.out.println("[!] Passwords do not match. Please try again.");
            pausePrompt();
            return;
        }

        if (!isValidEmail(email)) {
            System.out.println("[!] Invalid email format.");
            pausePrompt();
            return;
        }

        if (spectatorRepo.emailExists(email)) {
            System.out.println("[!] This email is already registered.");
            pausePrompt();
            return;
        }


      
        Spectator spectator = new Spectator();
        spectator.setFullName(fullName);
        spectator.setEmail(email);
        spectator.setPassword(password);

        if (spectatorRepo.createAccount(spectator)) {
            System.out.println("[✓] The spectator successfully created an account.");
        } else {
            System.out.println("[!] The spectator could not create an account due to a system error.");
        }
        pausePrompt();
    }


  
    public Spectator login() {
        printBorder("LOGIN ACCOUNT");

        System.out.print("Email    : ");
        String email = scanner.nextLine().trim();

        System.out.print("Password : ");
        String password = scanner.nextLine().trim();

        System.out.println();

        if (email.isEmpty() || password.isEmpty()) {
            System.out.println("[!] Email and password are required.");
            pausePrompt();
            return null;
        }

        Spectator spectator = spectatorRepo.login(email, password);
        if (spectator != null) {
            System.out.println("[✓] Login successful! Welcome, " + spectator.getFullName() + "!");
            pausePrompt();
            return spectator;
        } else {
            System.out.println("[!] Invalid email or password.");
            pausePrompt();
            return null;
        }
    }


  
    private boolean isValidEmail(String email) {
        return email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$");
    }

    private void printBorder(String title) {
        System.out.println("================================================================");
        System.out.println(" " + title);
        System.out.println("================================================================");
    }

    private void pausePrompt() {
        System.out.print("\nPress ENTER to continue...");
        scanner.nextLine();
    }
}
