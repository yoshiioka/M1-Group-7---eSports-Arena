package models;

public class EventTicket {
    private int    ticketTypeId;
    private int    matchId;
    private String eventName;
    private String section;
    private double price;
    private int    totalSeats;
    private int    soldSeats;

    public EventTicket() {}

    public EventTicket(int ticketTypeId, int matchId, String eventName,
                       String section, double price, int totalSeats, int soldSeats) {
        this.ticketTypeId = ticketTypeId;
        this.matchId      = matchId;
        this.eventName    = eventName;
        this.section      = section;
        this.price        = price;
        this.totalSeats   = totalSeats;
        this.soldSeats    = soldSeats;
    }

    public int    getTicketTypeId() { return ticketTypeId; }
    public int    getMatchId()      { return matchId;      }
    public String getEventName()    { return eventName;    }
    public String getSection()      { return section;      }
    public double getPrice()        { return price;        }
    public int    getTotalSeats()   { return totalSeats;   }
    public int    getSoldSeats()    { return soldSeats;    }

    public void setTicketTypeId(int ticketTypeId)  { this.ticketTypeId = ticketTypeId; }
    public void setMatchId(int matchId)            { this.matchId      = matchId;      }
    public void setEventName(String eventName)     { this.eventName    = eventName;    }
    public void setSection(String section)         { this.section      = section;      }
    public void setPrice(double price)             { this.price        = price;        }
    public void setTotalSeats(int totalSeats)      { this.totalSeats   = totalSeats;   }
    public void setSoldSeats(int soldSeats)        { this.soldSeats    = soldSeats;    }

    public boolean isAvailable()      { return soldSeats < totalSeats; }
    public int     getAvailableSeats(){ return totalSeats - soldSeats; }
    public String  getStatus()        { return isAvailable() ? "Available" : "SOLD OUT"; }

    @Override
    public String toString() {
        return eventName + " | Section: " + section +
               " | P" + String.format("%.0f", price) + " | " + getStatus();
    }
}
