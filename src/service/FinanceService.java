package services;
 
import models.Transaction;
import repository.TransactionRepository;
 
import java.util.List;
import java.util.Scanner;
 

public class FinanceService {
 
    private final TransactionRepository transactionRepo;
    private final Scanner               scanner;
 
    public FinanceService(Scanner scanner) {
        this.transactionRepo = new TransactionRepository();
        this.scanner         = scanner;
    }
 
   
    public void showIncomeStatement() {
        try {
            System.out.println();
            System.out.println("================================================================");
            System.out.println("        ESPORTS ARENA — INCOME STATEMENT                       ");
            System.out.println("================================================================");
 
            List<Transaction> transactions = transactionRepo.getAllTransactions();
 
            if (transactions.isEmpty()) {
                System.out.println("\n [!] No transactions recorded yet.");
                pausePrompt();
                return;
            }
 
           
            System.out.println();
            System.out.printf(" %-5s %-24s %-5s %-11s %-10s %-10s %-12s %-8s%n",
                "ID", "Event", "Qty", "Base", "VAT", "Discount", "Total", "Status");
            System.out.println(" " + "─".repeat(90));
 
            for (Transaction t : transactions) {
                System.out.printf(" %-5d %-24s %-5d P%-10.2f P%-9.2f P%-9.2f P%-11.2f %-8s%n",
                    t.getTransactionId(),
                    truncate(t.getEventName(), 24),
                    t.getQuantity(),
                    t.getBaseAmount(),
                    t.getVatAmount(),
                    t.getDiscountAmount(),
                    t.getTotalAmount(),
                    t.getStatus()
                );
            }
 
           
            double grossRevenue   = transactionRepo.getTotalBaseRevenue();
            double totalVat       = transactionRepo.getTotalVat();
            double totalDiscounts = transactionRepo.getTotalDiscount();
            double netRevenue     = transactionRepo.getTotalRevenue();
            int    ticketsSold    = transactionRepo.getTotalTicketsSold();
 
            System.out.println();
            System.out.println(" ================================================================");
            System.out.println("  SUMMARY");
            System.out.println(" ================================================================");
            System.out.printf("  Total Tickets Sold   : %d%n",       ticketsSold);
            System.out.printf("  Gross Revenue        : P%,.2f%n",   grossRevenue);
            System.out.printf("  VAT Collected (12%%) : P%,.2f%n",   totalVat);
            System.out.printf("  Total Discounts      : P%,.2f%n",   totalDiscounts);
            System.out.println(" ----------------------------------------------------------------");
            System.out.printf("  NET REVENUE          : P%,.2f%n",   netRevenue);
            System.out.println(" ================================================================");
            System.out.println();
 
        } catch (Exception e) {
            System.err.println("[FinanceService ERROR] showIncomeStatement: " + e.getMessage());
            System.out.println("[!] Could not load income statement due to a system error.");
        }
 
        pausePrompt();
    }
 
   
    public void showMyTransactions(int spectatorId) {
        try {
            System.out.println();
            System.out.println("================================================================");
            System.out.println("          MY TRANSACTION HISTORY                               ");
            System.out.println("================================================================");
 
            List<Transaction> transactions =
                transactionRepo.getTransactionsBySpectator(spectatorId);
 
            if (transactions.isEmpty()) {
                System.out.println("\n [!] You have no transactions yet.");
                pausePrompt();
                return;
            }
 
            System.out.println();
            double myTotal = 0;
 
            for (int i = 0; i < transactions.size(); i++) {
                Transaction t = transactions.get(i);
                System.out.printf(
                    " [%d] %-24s | Qty: %d | Base: P%.2f | VAT: P%.2f | Total: P%.2f | %s%n",
                    i + 1,
                    truncate(t.getEventName(), 24),
                    t.getQuantity(),
                    t.getBaseAmount(),
                    t.getVatAmount(),
                    t.getTotalAmount(),
                    t.getTransactedAt()
                );
                if ("SUCCESS".equals(t.getStatus())) myTotal += t.getTotalAmount();
            }
 
            System.out.println();
            System.out.printf(" Total Spent : P%,.2f%n", myTotal);
            System.out.println("================================================================");
            System.out.println();
 
        } catch (Exception e) {
            System.err.println("[FinanceService ERROR] showMyTransactions: " + e.getMessage());
            System.out.println("[!] Could not load your transactions due to a system error.");
        }
 
        pausePrompt();
    }
 
 
    private String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }
 
    private void pausePrompt() {
        System.out.print("\nPress ENTER to continue...");
        scanner.nextLine();
    }
}
