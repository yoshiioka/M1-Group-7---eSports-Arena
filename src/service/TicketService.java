package services;

import models.EventTicket;
import models.PurchasedTicket;
import models.Spectator;
import models.Transaction;
import repository.EventTicketRepository;
import repository.PurchasedTicketRepository;
import repository.TransactionRepository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

public class TicketService {

    private final EventTicketRepository     eventTicketRepo;
    private final PurchasedTicketRepository purchasedTicketRepo;
    private final TransactionRepository     transactionRepo;
    private final Scanner                   scanner;

    private EventTicket selectedEventTicket = null;
    private int         selectedQuantity    = 1;

    public TicketService(Scanner scanner) {
        this.eventTicketRepo     = new EventTicketRepository();
        this.purchasedTicketRepo = new PurchasedTicketRepository();
        this.transactionRepo     = new TransactionRepository();
        this.scanner             = scanner;
    }

    public void selectEventTicket() {
        System.out.println("================================================================");
        System.out.println(" SELECT EVENT TICKET");
        System.out.println("================================================================");

        List<EventTicket> tickets = eventTicketRepo.getAllEventTickets();
        if (tickets.isEmpty()) {
            System.out.println("[!] No event tickets available.");
            pausePrompt();
            return;
        }

        for (int i = 0; i < tickets.size(); i++) {
            EventTicket et = tickets.get(i);
            System.out.printf(" [%d] %-28s | P%-8.2f | %d seats left%n",
                    i + 1, et.getEventName(), et.getPrice(), et.getAvailableSeats());
        }

        System.out.print("\n Enter choice (0 to cancel): ");
        try {
            int choice = Integer.parseInt(scanner.nextLine().trim());
            if (choice == 0) return;

            EventTicket chosen = eventTicketRepo.getEventTicketById(tickets.get(choice - 1).getTicketTypeId());
            System.out.print(" How many tickets? ");
            int qty = Integer.parseInt(scanner.nextLine().trim());

            if (qty > 0 && qty <= chosen.getAvailableSeats()) {
                this.selectedEventTicket = chosen;
                this.selectedQuantity = qty;
                System.out.println("[✓] Selection saved.");
            } else {
                System.out.println("[!] Invalid quantity.");
            }
        } catch (Exception e) {
            System.out.println("[!] Selection error.");
        }
        pausePrompt();
    }

    public void purchaseTicket(Spectator spectator) {
        if (selectedEventTicket == null) {
            System.out.println("[!] Please select a ticket first.");
            pausePrompt();
            return;
        }

        double baseAmount = selectedEventTicket.getPrice() * selectedQuantity;
        double vatAmt = baseAmount * 0.12;
        double discount = 0.0;

        System.out.print("Do you have a Promocode? (Y/N): ");
        if (scanner.nextLine().trim().equalsIgnoreCase("Y")) {
            System.out.print("Enter Promocode: ");
            scanner.nextLine();
            discount = baseAmount * 0.005; // 0.5% Discount
            System.out.printf("[✓] Promo applied: -P%.2f%n", discount);
        }

        double finalTotal = baseAmount + vatAmt - discount;

        List<Integer> chosenSeats = new ArrayList<>();
        for (int i = 1; i <= selectedQuantity; i++) {
            boolean valid = false;
            while (!valid) {
                System.out.print(" Pick Seat for Ticket #" + i + ": ");
                try {
                    int seatNo = Integer.parseInt(scanner.nextLine().trim());
                    if (!purchasedTicketRepo.isSeatTaken(selectedEventTicket.getTicketTypeId(), seatNo)) {
                        chosenSeats.add(seatNo);
                        valid = true;
                    } else {
                        System.out.println(" [!] Seat already taken!");
                    }
                } catch (Exception e) { System.out.println(" [!] Enter a number."); }
            }
        }

        String methodLabel = "";
        String paymentID = "";
        boolean paymentDone = false;

        while (!paymentDone) {
            System.out.println("\nSelect Payment Method:");
            System.out.println("[1] Cash \n[2] Credit Card \n[3] E-Wallet");
            System.out.print(" Choice: ");
            String payChoice = scanner.nextLine().trim();

            if (payChoice.equals("1")) {
                methodLabel = "Cash";
                paymentID = "CASH_PAYMENT";
                System.out.printf(" Total: P%.2f | Enter Cash: ", finalTotal);
                double cash = Double.parseDouble(scanner.nextLine().trim());
                System.out.printf(" [✓] Change: P%.2f%n", (cash - finalTotal));
                paymentDone = true;
            } else if (payChoice.equals("2") || payChoice.equals("3")) {
                methodLabel = payChoice.equals("2") ? "Credit Card" : "E-Wallet";
                System.out.print(" Enter Account/Card Number: ");
                String raw = scanner.nextLine().trim();
                // Masking sensitive data
                paymentID = "****" + (raw.length() > 4 ? raw.substring(raw.length()-4) : raw);
                System.out.print(" Enter Verification (CVV/PIN): ");
                scanner.nextLine();
                paymentDone = true;
            } else {
                System.out.println(" [!] Invalid Choice. Please pick 1, 2, or 3.");
            }
        }

        // Finalize and Save
        System.out.println("\n [1] Confirm Purchase [0] Cancel");
        System.out.print(" Choice: "); // Added as requested
        String confirmChoice = scanner.nextLine().trim();

        if (confirmChoice.equals("1")) {
            String datePart = new SimpleDateFormat("yyyyMMdd").format(new Date());
            List<String> tktList = new ArrayList<>();

            for (int seat : chosenSeats) {
                String tktNo = "ESA" + datePart + ((int)(Math.random() * 9000) + 1000);
                tktList.add(tktNo);

                PurchasedTicket pt = new PurchasedTicket();
                pt.setTicketNo(tktNo);
                pt.setSpectatorId(spectator.getSpectatorId());
                pt.setTicketTypeId(selectedEventTicket.getTicketTypeId());
                pt.setSeatNo(seat);
                pt.setCardNo(paymentID); // Ensures card_no is not null
                pt.setStatus("CONFIRMED");

                purchasedTicketRepo.purchaseTicket(pt);
            }
            saveTransaction(spectator, selectedEventTicket, selectedQuantity, baseAmount, vatAmt, discount, finalTotal, methodLabel, "SUCCESS");

            // SHOW RECEIPT IMMEDIATELY
            displayReceipt(spectator, selectedEventTicket, selectedQuantity, baseAmount, vatAmt, discount, finalTotal, methodLabel, tktList);
        }

        selectedEventTicket = null;
        pausePrompt();
    }

    public void downloadDigitalTicket(Spectator spectator) {
        List<PurchasedTicket> tickets = purchasedTicketRepo.getTicketsBySpectator(spectator.getSpectatorId());
        if (tickets.isEmpty()) {
            System.out.println("[!] No tickets found.");
            pausePrompt();
            return;
        }

        System.out.println("\n --- GENERATING PASSES ---");
        for (PurchasedTicket t : tickets) {
            System.out.println(" ╔══════════════════════════════════════════╗");
            System.out.println(" ║          ESPORTS ARENA PASS              ║");
            System.out.println(" ╠══════════════════════════════════════════╣");
            System.out.println(" ║ TICKET NO : " + String.format("%-28s", t.getTicketNo()) + "║");
            System.out.println(" ║ EVENT     : " + String.format("%-28s", t.getEventName() != null ? t.getEventName() : "Arena Match") + "║");
            System.out.println(" ║ HOLDER    : " + String.format("%-28s", spectator.getFullName()) + "║");
            System.out.println(" ║ SEAT      : " + String.format("%-28s", t.getSeatNo()) + "║");
            System.out.println(" ║ STATUS    : " + String.format("%-28s", "VERIFIED") + "║");
            System.out.println(" ╚══════════════════════════════════════════╝");
        }
        pausePrompt();
    }

    // ── HELPER: Receipt Printer ──
    private void displayReceipt(Spectator s, EventTicket et, int q, double b, double v, double d, double t, String m, List<String> tkts) {
        System.out.println("\n========================================");
        System.out.println("           OFFICIAL RECEIPT             ");
        System.out.println("========================================");
        System.out.println(" Customer: " + s.getFullName());
        System.out.println(" Event   : " + et.getEventName());
        System.out.println(" Tickets : " + tkts);
        System.out.println("----------------------------------------");
        System.out.printf(" Subtotal: P%.2f%n", b);
        System.out.printf(" VAT(12%%): P%.2f%n", v);
        System.out.printf(" Discount: -P%.2f%n", d);
        System.out.printf(" TOTAL   : P%.2f%n", t);
        System.out.println(" Method  : " + m);
        System.out.println("========================================\n");
    }

    public void viewTicketDetails(Spectator spectator) {
        List<PurchasedTicket> tickets = purchasedTicketRepo.getTicketsBySpectator(spectator.getSpectatorId());
        if (tickets.isEmpty()) {
            System.out.println("[!] No tickets found.");
        } else {
            for (PurchasedTicket t : tickets) {
                System.out.printf(" Ticket: %s | Event: %s | Seat: %d%n", t.getTicketNo(), t.getEventName(), t.getSeatNo());
            }
        }
        pausePrompt();
    }

    private void saveTransaction(Spectator s, EventTicket et, int q, double b, double v, double d, double t, String m, String st) {
        Transaction tx = new Transaction();
        tx.setSpectatorId(s.getSpectatorId());
        tx.setTicketTypeId(et.getTicketTypeId());
        tx.setQuantity(q);
        tx.setBaseAmount(b);
        tx.setVatAmount(v);
        tx.setDiscountAmount(d);
        tx.setTotalAmount(t);
        tx.setPaymentMethod(m);
        tx.setStatus(st);
        transactionRepo.saveTransaction(tx);
    }

    private void pausePrompt() {
        System.out.print("\nPress ENTER to continue...");
        scanner.nextLine();
    }
}
