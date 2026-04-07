package models;

public class Match {
    private int    matchId;
    private String game;
    private String teamA;
    private String teamB;
    private String matchDatetime;

    public Match() {}

    public Match(int matchId, String game, String teamA, String teamB, String matchDatetime) {
        this.matchId       = matchId;
        this.game          = game;
        this.teamA         = teamA;
        this.teamB         = teamB;
        this.matchDatetime = matchDatetime;
    }

    public int    getMatchId()       { return matchId;       }
    public String getGame()          { return game;          }
    public String getTeamA()         { return teamA;         }
    public String getTeamB()         { return teamB;         }
    public String getMatchDatetime() { return matchDatetime; }

    public void setMatchId(int matchId)             { this.matchId       = matchId;       }
    public void setGame(String game)                { this.game          = game;          }
    public void setTeamA(String teamA)              { this.teamA         = teamA;         }
    public void setTeamB(String teamB)              { this.teamB         = teamB;         }
    public void setMatchDatetime(String matchDatetime) { this.matchDatetime = matchDatetime; }

    public String getTeams() {
        return teamA + " vs " + teamB;
    }

    @Override
    public String toString() {
        return game + " | " + getTeams() + " | " + matchDatetime;
    }
}
