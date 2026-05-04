package services;
 
import models.EventTicket;
import models.PurchasedTicket;
import models.Spectator;
import models.Transaction;
import payment.CardPayment;
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
        boolean selecting = true;
        while (selecting) {
            System.out.println("================================================================");
            System.out.println(" SELECT EVENT TICKET");
            System.out.println("================================================================");
 
            List<EventTicket> tickets = eventTicketRepo.getAllEventTickets();
 
            if (tickets.isEmpty()) {
                System.out.println("[!] No event tickets available.");
                pausePrompt();
                return;
            }
 
            System.out.println();
            System.out.printf(" %-4s %-28s %-10s %-9s %-12s %-10s%n",
                "No.", "Event", "Section", "Price", "Available", "Status");
            System.out.println(" " + "-".repeat(3)  + " " + "-".repeat(28) + " " +
                                    "-".repeat(10) + " " + "-".repeat(9)  + " " +
                                    "-".repeat(12) + " " + "-".repeat(10));
 
            for (int i = 0; i < tickets.size(); i++) {
                EventTicket et = tickets.get(i);
                System.out.printf(" [%d] %-28s %-10s P%-8.0f %-12s %-10s%n",
                    i + 1,
                    et.getEventName(),
                    et.getSection(),
                    et.getPrice(),
                    et.isAvailable() ? et.getAvailableSeats() + " seats" : "—",
                    et.getStatus()
                );
            }
 
            System.out.println();
            System.out.println(" [0] Back");
            System.out.println("================================================================");
            System.out.print(" Enter choice: ");
 
            String input = scanner.nextLine().trim();
            if (input.equals("0")) return;
 
            int choice;
            try {
                choice = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("[!] Invalid input. Please enter a number.");
                pausePrompt();
                continue;
            }
 
            if (choice < 1 || choice > tickets.size()) {
                System.out.println("[!] Invalid choice.");
                pausePrompt();
                continue;
            }
 
            EventTicket chosen = eventTicketRepo.getEventTicketById(
                                    tickets.get(choice - 1).getTicketTypeId());
 
            if (chosen == null || !chosen.isAvailable()) {
                System.out.println("[!] The spectator could not select the event because it was sold out.");
                pausePrompt();
                continue;
            }
 
            System.out.printf("%n How many tickets would you like? (1 - %d available): ",
                chosen.getAvailableSeats());
            String qtyInput = scanner.nextLine().trim();
 
            int qty;
            try {
                qty = Integer.parseInt(qtyInput);
            } catch (NumberFormatException e) {
                System.out.println("[!] Invalid quantity. Please enter a number.");
                pausePrompt();
                continue;
            }
 
            if (qty < 1) {
                System.out.println("[!] Quantity must be at least 1.");
                pausePrompt();
                continue;
            }
            if (qty > chosen.getAvailableSeats()) {
                System.out.printf("[!] Only %d seat(s) available for this event.%n",
                    chosen.getAvailableSeats());
                pausePrompt();
                continue;
            }
 
            selectedEventTicket = chosen;
            selectedQuantity    = qty;
 
            System.out.printf("%n[✓] Selected: %s  x%d  — Subtotal: P%.0f%n",
                chosen.getEventName(), qty, chosen.getPrice() * qty);
            pausePrompt();
            selecting = false;
        }
    }
 
   
    public void purchaseTicket(Spectator spectator) {
        System.out.println("================================================================");
        System.out.println(" PURCHASE TICKET");
        System.out.println("================================================================");
 
        if (selectedEventTicket == null) {
            System.out.println("[!] Please select an event ticket first (Option 2).");
            pausePrompt();
            return;
        }
 
        EventTicket et = eventTicketRepo.getEventTicketById(selectedEventTicket.getTicketTypeId());
        if (et == null || !et.isAvailable()) {
            System.out.println("[!] Sorry, this event is now sold out.");
            selectedEventTicket = null;
            pausePrompt();
            return;
        }
 
        if (selectedQuantity > et.getAvailableSeats()) {
            System.out.printf("[!] Only %d seat(s) still available. Please re-select your quantity.%n",
                et.getAvailableSeats());
            selectedEventTicket = null;
            pausePrompt();
            return;
        }
 
        double baseAmount = et.getPrice() * selectedQuantity;
        double discount   = 0.0;   // extend here if you add promo codes
        int    startSeat  = eventTicketRepo.getNextSeatNumber(et.getTicketTypeId());
 
       
        System.out.println();
        System.out.println(" Event    : " + et.getEventName());
        System.out.println(" Section  : " + et.getSection());
        System.out.printf( " Seats    : %d  (Seat %d", selectedQuantity, startSeat);
        if (selectedQuantity > 1) System.out.printf(" – %d", startSeat + selectedQuantity - 1);
        System.out.println(")");
        System.out.printf( " Price    : P%.2f x %d = P%.2f%n",
            et.getPrice(), selectedQuantity, baseAmount);
        System.out.printf( " VAT(12%%) : P%.2f%n", baseAmount * 0.12);
        System.out.printf( " Discount : P%.2f%n", discount);
        System.out.printf( " TOTAL    : P%.2f%n", baseAmount + (baseAmount * 0.12) - discount);
        System.out.println();
 
        
        System.out.print(" Card No. (16 digits) : ");
        String cardNo = scanner.nextLine().trim();
 
        System.out.print(" Expiry   (MM/YY)     : ");
        String expiry = scanner.nextLine().trim();
 
        System.out.print(" CVV      (3 digits)  : ");
        String cvv = scanner.nextLine().trim();
 
        System.out.println();
 
       
        CardPayment payment = new CardPayment(
            baseAmount, cardNo, expiry, cvv, spectator.getFullName());
 
        payment.processInvoice(discount);
 
        if (!payment.isTransactionComplete()) {
            System.out.println("[!] The spectator could not purchase the ticket because the payment failed.");
            // Save FAILED transaction record
            saveTransaction(spectator, et, selectedQuantity,
                baseAmount, payment.getVatAmount(), discount,
                baseAmount + payment.getVatAmount() - discount,
                "Card", "FAILED");
            pausePrompt();
            return;
        }
 
        System.out.println();
        System.out.println(" [1] Confirm & Pay   [0] Cancel");
        System.out.print(" Enter choice: ");
        String confirm = scanner.nextLine().trim();
 
        if (!confirm.equals("1")) {
            System.out.println("[!] Purchase cancelled.");
            pausePrompt();
            return;
        }
 
        
        String datePart        = new SimpleDateFormat("yyyyMMdd").format(new Date());
        List<String> ticketNos = new ArrayList<>();
        boolean      allOk     = true;
 
        for (int i = 0; i < selectedQuantity; i++) {
            boolean reserved = eventTicketRepo.incrementSoldSeats(et.getTicketTypeId());
            if (!reserved) {
                System.out.printf("[!] Could only secure %d of %d seat(s). Stopping here.%n",
                    i, selectedQuantity);
                allOk = false;
                break;
            }
 
            int    seatNo = startSeat + i;
            String tktNo  = "ESA" + datePart +
                            String.format("%04d", (int)(Math.random() * 9000) + 1000) +
                            "-" + (i + 1);
 
            PurchasedTicket pt = new PurchasedTicket();
            pt.setTicketNo(tktNo);
            pt.setSpectatorId(spectator.getSpectatorId());
            pt.setTicketTypeId(et.getTicketTypeId());
            pt.setSeatNo(seatNo);
            pt.setCardNo(cardNo);
            pt.setStatus("CONFIRMED");
 
            if (purchasedTicketRepo.purchaseTicket(pt)) {
                ticketNos.add(tktNo);
            } else {
                System.out.println("[!] Failed to save ticket #" + (i + 1) + " due to a system error.");
                allOk = false;
                break;
            }
        }
 
        
        double vatAmt   = baseAmount * 0.12;
        double finalTotal = baseAmount + vatAmt - discount;
        String txStatus = (ticketNos.size() == selectedQuantity) ? "SUCCESS" : "FAILED";
 
        saveTransaction(spectator, et, ticketNos.size(),
            baseAmount, vatAmt, discount, finalTotal, "Card", txStatus);
 
      
        if (!ticketNos.isEmpty()) {
            System.out.println();
            System.out.println("================================================================");
            System.out.println(" PURCHASE RECEIPT");
            System.out.println("================================================================");
            System.out.println(" Event    : " + et.getEventName());
            System.out.println(" Section  : " + et.getSection());
            System.out.println();
            for (int i = 0; i < ticketNos.size(); i++) {
                System.out.printf("   Ticket %d  |  No: %-30s  |  Seat: %d%n",
                    i + 1, ticketNos.get(i), startSeat + i);
            }
            System.out.println();
            System.out.printf(" Tickets   : %d%n",           ticketNos.size());
            System.out.printf(" Base Amt  : P%.2f%n",        baseAmount);
            System.out.printf(" VAT(12%%) : P%.2f%n",        vatAmt);
            System.out.printf(" Discount  : P%.2f%n",        discount);
            System.out.printf(" TOTAL     : P%.2f%n",        finalTotal);
            System.out.println("================================================================");
            System.out.println();
            if (allOk) {
                System.out.println("[✓] The spectator successfully purchased " +
                    ticketNos.size() + " ticket(s)!");
            } else {
                System.out.println("[!] Partial purchase. " + ticketNos.size() + " ticket(s) confirmed.");
            }
        } else {
            System.out.println("[!] The spectator could not purchase the ticket due to a system error.");
        }
 
        selectedEventTicket = null;
        selectedQuantity    = 1;
        pausePrompt();
    }
 
    
    public void viewTicketDetails(Spectator spectator) {
        System.out.println("================================================================");
        System.out.println(" TICKET DETAILS");
        System.out.println("================================================================");
 
        List<PurchasedTicket> tickets =
            purchasedTicketRepo.getTicketsBySpectator(spectator.getSpectatorId());
 
        if (tickets.isEmpty()) {
            System.out.println("[!] The spectator could not access the ticket information. No tickets purchased yet.");
            pausePrompt();
            return;
        }
 
        System.out.println();
        for (int i = 0; i < tickets.size(); i++) {
            PurchasedTicket pt = tickets.get(i);
            System.out.printf(" [%d] %s — %s (Seat %d)%n",
                i + 1, pt.getTicketNo(), pt.getEventName(), pt.getSeatNo());
        }
        System.out.println("\n [0] Back");
        System.out.print(" Select a ticket to view details: ");
 
        String input = scanner.nextLine().trim();
        if (input.equals("0")) return;
 
        int choice;
        try {
            choice = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("[!] Invalid input.");
            pausePrompt();
            return;
        }
 
        if (choice < 1 || choice > tickets.size()) {
            System.out.println("[!] Invalid choice.");
            pausePrompt();
            return;
        }
 
        PurchasedTicket pt = tickets.get(choice - 1);
        printTicketDetails(pt);
 
        System.out.println(" [1] Download Ticket   [0] Back");
        System.out.print(" Enter choice: ");
        if (scanner.nextLine().trim().equals("1")) {
            downloadDigitalTicket(pt);
        }
    }
 
  
    public void downloadDigitalTicket(Spectator spectator) {
        System.out.println("================================================================");
        System.out.println(" DOWNLOAD DIGITAL TICKET");
        System.out.println("================================================================");
 
        List<PurchasedTicket> tickets =
            purchasedTicketRepo.getTicketsBySpectator(spectator.getSpectatorId());
 
        if (tickets.isEmpty()) {
            System.out.println("[!] The spectator could not download the ticket. No tickets found.");
            pausePrompt();
            return;
        }
 
        System.out.println();
        for (int i = 0; i < tickets.size(); i++) {
            PurchasedTicket pt = tickets.get(i);
            System.out.printf(" [%d] %s — %s (Seat %d)%n",
                i + 1, pt.getTicketNo(), pt.getEventName(), pt.getSeatNo());
        }
        System.out.println("\n [0] Back");
        System.out.print(" Select ticket to download: ");
 
        String input = scanner.nextLine().trim();
        if (input.equals("0")) return;
 
        int choice;
        try {
            choice = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("[!] Invalid input.");
            pausePrompt();
            return;
        }
 
        if (choice < 1 || choice > tickets.size()) {
            System.out.println("[!] Invalid choice.");
            pausePrompt();
            return;
        }
 
        downloadDigitalTicket(tickets.get(choice - 1));
    }
 
   
    private void downloadDigitalTicket(PurchasedTicket pt) {
        System.out.println();
        System.out.println("================================================================");
        System.out.println(" DIGITAL TICKET");
        System.out.println("================================================================");
        System.out.println();
        System.out.println(" +--------------------------------------------------+");
        System.out.println(" |       ESPORTS ARENA OFFICIAL ENTRY TICKET        |");
        System.out.println(" |                                                  |");
        System.out.printf( " |  Event   : %-38s|%n", pt.getEventName());
        System.out.printf( " |  Date    : %-38s|%n", pt.getMatchDatetime());
        System.out.printf( " |  Section : %-15s Seat : %-17s|%n", pt.getSection(), pt.getSeatNo());
        System.out.printf( " |  Holder  : %-38s|%n", pt.getHolderName());
        System.out.println(" |                                                  |");
        System.out.printf( " |  Ticket ID : %-36s|%n", pt.getTicketNo());
        System.out.printf( " |  QR Code   : %-36s|%n", generateQRCode(pt.getTicketNo()));
        System.out.println(" +--------------------------------------------------+");
        System.out.println();
        System.out.println(" [✓] The spectator successfully downloaded the digital ticket.");
        System.out.println(" Ticket downloaded successfully!");
        System.out.println();
        pausePrompt();
    }
 
 
    private void saveTransaction(Spectator spectator, EventTicket et,
                                 int qty, double base, double vat,
                                 double discount, double total,
                                 String method, String status) {
        try {
            Transaction t = new Transaction();
            t.setSpectatorId(spectator.getSpectatorId());
            t.setTicketTypeId(et.getTicketTypeId());
            t.setQuantity(qty);
            t.setBaseAmount(base);
            t.setVatAmount(vat);
            t.setDiscountAmount(discount);
            t.setTotalAmount(total);
            t.setPaymentMethod(method);
            t.setStatus(status);
            transactionRepo.saveTransaction(t);
        } catch (Exception e) {
            System.err.println("[TicketService ERROR] saveTransaction: " + e.getMessage());
        }
    }
 
   
    private void printTicketDetails(PurchasedTicket pt) {
        System.out.println();
        System.out.println(" Ticket No. : #" + pt.getTicketNo());
        System.out.println(" Event      : " + pt.getEventName());
        System.out.println(" Date/Time  : " + pt.getMatchDatetime());
        System.out.println(" Section    : " + pt.getSection());
        System.out.println(" Seat       : " + pt.getSeatNo());
        System.out.println(" Holder     : " + pt.getHolderName());
        System.out.println(" Status     : " + pt.getStatus());
        System.out.println(" Purchased  : " + pt.getPurchasedAt());
        System.out.println();
        System.out.println(" [✓] The spectator successfully viewed the ticket details.");
        System.out.println();
    }
 
    private String generateQRCode(String ticketNo) {
        int hash = Math.abs(ticketNo.hashCode());
        StringBuilder qr = new StringBuilder("[");
        for (int i = 0; i < 20; i++) {
            qr.append((hash >> (i % 32) & 1) == 1 ? "##" : "  ");
            if (i % 5 == 4 && i < 19) qr.append("|");
        }
        qr.append("]");
        return qr.toString();
    }
 
    private void pausePrompt() {
        System.out.print("\nPress ENTER to continue...");
        scanner.nextLine();
    }
}
 
