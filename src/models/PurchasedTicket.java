package models;

public class PurchasedTicket {
    private int    purchaseId;
    private String ticketNo;
    private int    spectatorId;
    private int    ticketTypeId;
    private int    seatNo;
    private String cardNo;
    private String status;
    private String purchasedAt;

    private String eventName;
    private String section;
    private String matchDatetime;
    private String holderName;

    public PurchasedTicket() {}

    public int    getPurchaseId()    { return purchaseId;    }
    public String getTicketNo()      { return ticketNo;      }
    public int    getSpectatorId()   { return spectatorId;   }
    public int    getTicketTypeId()  { return ticketTypeId;  }
    public int    getSeatNo()        { return seatNo;        }
    public String getCardNo()        { return cardNo;        }
    public String getStatus()        { return status;        }
    public String getPurchasedAt()   { return purchasedAt;   }
    public String getEventName()     { return eventName;     }
    public String getSection()       { return section;       }
    public String getMatchDatetime() { return matchDatetime; }
    public String getHolderName()    { return holderName;    }

    public void setPurchaseId(int purchaseId)        { this.purchaseId    = purchaseId;    }
    public void setTicketNo(String ticketNo)         { this.ticketNo      = ticketNo;      }
    public void setSpectatorId(int spectatorId)      { this.spectatorId   = spectatorId;   }
    public void setTicketTypeId(int ticketTypeId)    { this.ticketTypeId  = ticketTypeId;  }
    public void setSeatNo(int seatNo)                { this.seatNo        = seatNo;        }
    public void setCardNo(String cardNo)             { this.cardNo        = cardNo;        }
    public void setStatus(String status)             { this.status        = status;        }
    public void setPurchasedAt(String purchasedAt)   { this.purchasedAt   = purchasedAt;   }
    public void setEventName(String eventName)       { this.eventName     = eventName;     }
    public void setSection(String section)           { this.section       = section;       }
    public void setMatchDatetime(String matchDatetime){ this.matchDatetime = matchDatetime; }
    public void setHolderName(String holderName)     { this.holderName    = holderName;    }

    /** Mask card number for display: show only last 4 digits. */
    public String getMaskedCardNo() {
        if (cardNo == null || cardNo.length() < 4) return "****";
        return "**** **** **** " + cardNo.substring(cardNo.length() - 4);
    }

    @Override
    public String toString() {
        return "Ticket[" + ticketNo + "] " + eventName + " Seat:" + seatNo + " Status:" + status;
    }
}
