package services;

import models.EventTicket;
import models.PurchasedTicket;
import models.Spectator;
import repository.EventTicketRepository;
import repository.PurchasedTicketRepository;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

public class TicketService {

    private final EventTicketRepository     eventTicketRepo;
    private final PurchasedTicketRepository purchasedTicketRepo;
    private final Scanner                   scanner;

    
    private EventTicket selectedEventTicket = null;

    public TicketService(Scanner scanner) {
        this.eventTicketRepo     = new EventTicketRepository();
        this.purchasedTicketRepo = new PurchasedTicketRepository();
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
            System.out.printf(" %-4s %-28s %-12s %-8s %-10s%n",
                "No.", "Event", "Section", "Price", "Status");
            System.out.println(" " + "-".repeat(3)  + " " +
                                    "-".repeat(28) + " " +
                                    "-".repeat(12) + " " +
                                    "-".repeat(8)  + " " +
                                    "-".repeat(10));

            for (int i = 0; i < tickets.size(); i++) {
                EventTicket et = tickets.get(i);
                System.out.printf(" [%d] %-28s %-12s P%-7.0f %-10s%n",
                    i + 1,
                    et.getEventName(),
                    et.getSection(),
                    et.getPrice(),
                    et.getStatus()
                );
            }

            System.out.println();
            System.out.println(" [0] Back");
            System.out.println("================================================================");
            System.out.print(" Enter choice: ");

            String input = scanner.nextLine().trim();

            if (input.equals("0")) {
                selecting = false;
                return;
            }

            int choice;
            try {
                choice = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("[!] Invalid input.");
                pausePrompt();
                continue;
            }

            if (choice < 1 || choice > tickets.size()) {
                System.out.println("[!] Invalid choice.");
                pausePrompt();
                continue;
            }

            EventTicket chosen = tickets.get(choice - 1);

            if (!chosen.isAvailable()) {
                System.out.println("[!] The spectator could not select the event because it was sold out.");
                pausePrompt();
                continue;
            }

            selectedEventTicket = chosen;
            System.out.println("\n[✓] The spectator successfully selected an event ticket: " + chosen.getEventName());
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

        int nextSeat = eventTicketRepo.getNextSeatNumber(et.getTicketTypeId());

        System.out.println();
        System.out.println(" Event   : " + et.getEventName());
        System.out.println(" Section : " + et.getSection() + "   Seat : " + nextSeat);
        System.out.println(" Price   : P" + String.format("%.0f", et.getPrice()));
        System.out.println();

        
        System.out.print(" Card No. (16 digits) : ");
        String cardNo = scanner.nextLine().trim();

        System.out.print(" Expiry (MM/YY)        : ");
        String expiry = scanner.nextLine().trim();

        System.out.print(" CVV (3 digits)        : ");
        String cvv = scanner.nextLine().trim();

        System.out.println();

        
        if (!cardNo.matches("\\d{16}")) {
            System.out.println("[!] The spectator could not purchase the ticket because the payment failed.");
            System.out.println("    Card number must be exactly 16 digits.");
            pausePrompt();
            return;
        }
        if (!expiry.matches("(0[1-9]|1[0-2])/\\d{2}")) {
            System.out.println("[!] Invalid expiry format. Use MM/YY.");
            pausePrompt();
            return;
        }
        if (!cvv.matches("\\d{3}")) {
            System.out.println("[!] CVV must be exactly 3 digits.");
            pausePrompt();
            return;
        }

        System.out.println(" [1] Confirm & Pay   [0] Back");
        System.out.print(" Enter choice: ");
        String confirm = scanner.nextLine().trim();

        if (!confirm.equals("1")) {
            System.out.println("[!] Purchase cancelled.");
            pausePrompt();
            return;
        }

       
        String datePart  = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String ticketNo  = "ESA" + datePart + String.format("%04d", (int)(Math.random() * 9000) + 1000);

       
        PurchasedTicket pt = new PurchasedTicket();
        pt.setTicketNo(ticketNo);
        pt.setSpectatorId(spectator.getSpectatorId());
        pt.setTicketTypeId(et.getTicketTypeId());
        pt.setSeatNo(nextSeat);
        pt.setCardNo(cardNo);
        pt.setStatus("CONFIRMED");

      
        boolean seatReserved = eventTicketRepo.incrementSoldSeats(et.getTicketTypeId());
        if (!seatReserved) {
            System.out.println("[!] Sorry, no more seats available.");
            pausePrompt();
            return;
        }

        boolean saved = purchasedTicketRepo.purchaseTicket(pt);
        if (saved) {
            System.out.println("[✓] The spectator successfully purchased a ticket!");
            System.out.println("    Ticket No. : " + ticketNo);
            selectedEventTicket = null; // clear selection after purchase
        } else {
            System.out.println("[!] The spectator could not purchase the ticket due to a system error.");
        }
        pausePrompt();
    }




  
  
    public void viewTicketDetails(Spectator spectator) {
        System.out.println("================================================================");
        System.out.println(" TICKET DETAILS");
        System.out.println("================================================================");

        List<PurchasedTicket> tickets = purchasedTicketRepo.getTicketsBySpectator(spectator.getSpectatorId());

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
        String next = scanner.nextLine().trim();

        if (next.equals("1")) {
            downloadDigitalTicket(pt);
        }
    }







  
    public void downloadDigitalTicket(Spectator spectator) {
        System.out.println("================================================================");
        System.out.println(" DOWNLOAD DIGITAL TICKET");
        System.out.println("================================================================");

        List<PurchasedTicket> tickets = purchasedTicketRepo.getTicketsBySpectator(spectator.getSpectatorId());

        if (tickets.isEmpty()) {
            System.out.println("[!] The spectator could not download the ticket. No tickets found.");
            pausePrompt();
            return;
        }

        System.out.println();
        for (int i = 0; i < tickets.size(); i++) {
            System.out.printf(" [%d] %s%n", i + 1, tickets.get(i).getTicketNo());
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

        String qr = generateQRCode(pt.getTicketNo());

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
        System.out.printf( " |  QR Code   : %-36s|%n", qr);
        System.out.println(" +--------------------------------------------------+");
        System.out.println();
        System.out.println(" [✓] The spectator successfully downloaded the digital ticket.");
        System.out.println(" Ticket downloaded successfully!");
        System.out.println();
        pausePrompt();
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
