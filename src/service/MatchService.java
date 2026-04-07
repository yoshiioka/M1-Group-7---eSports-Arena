package services;

import models.Match;
import repository.MatchRepository;

import java.util.List;
import java.util.Scanner;

public class MatchService {

    private final MatchRepository matchRepo;
    private final Scanner         scanner;

    public MatchService(Scanner scanner) {
        this.matchRepo = new MatchRepository();
        this.scanner   = scanner;
    }



  
    public void viewMatchSchedule() {
        System.out.println("================================================================");
        System.out.println(" MATCH SCHEDULE");
        System.out.println("================================================================");

        List<Match> matches = matchRepo.getAllMatches();

        if (matches.isEmpty()) {
            System.out.println("[!] The spectator could not view the match schedule due to system error.");
            pausePrompt();
            return;
        }

        System.out.println();
        System.out.printf(" %-4s %-10s %-34s %-25s%n", "No.", "Game", "Teams", "Date & Time");
        System.out.println(" " + "-".repeat(3) + " " + "-".repeat(10) + " " + "-".repeat(34) + " " + "-".repeat(25));

        for (int i = 0; i < matches.size(); i++) {
            Match m = matches.get(i);
            System.out.printf(" [%d] %-10s %-34s %-25s%n",
                i + 1,
                m.getGame(),
                m.getTeams(),
                m.getMatchDatetime()
            );
        }

        System.out.println();
        System.out.println(" [0] Back");
        System.out.println("================================================================");
        System.out.print("\n The spectator successfully viewed the match schedule.");
        pausePrompt();
    }


  
    private void pausePrompt() {
        System.out.print("\nPress ENTER to continue...");
        scanner.nextLine();
    }
}
