package models;


public class Transaction {

    private int    transactionId;
    private int    spectatorId;
    private int    ticketTypeId;
    private int    quantity;
    private double baseAmount;       
    private double vatAmount;        
    private double discountAmount;   
    private double totalAmount;      
    private String paymentMethod; 
    private String status;          
    private String transactedAt;    
    
    private String eventName;
    private String holderName;

    public Transaction() {}
   
    public int    getTransactionId()  { return transactionId;  }
    public int    getSpectatorId()    { return spectatorId;    }
    public int    getTicketTypeId()   { return ticketTypeId;   }
    public int    getQuantity()       { return quantity;       }
    public double getBaseAmount()     { return baseAmount;     }
    public double getVatAmount()      { return vatAmount;      }
    public double getDiscountAmount() { return discountAmount; }
    public double getTotalAmount()    { return totalAmount;    }
    public String getPaymentMethod()  { return paymentMethod;  }
    public String getStatus()         { return status;         }
    public String getTransactedAt()   { return transactedAt;   }
    public String getEventName()      { return eventName;      }
    public String getHolderName()     { return holderName;     }
   
    public void setTransactionId(int transactionId)      { this.transactionId  = transactionId;  }
    public void setSpectatorId(int spectatorId)          { this.spectatorId    = spectatorId;    }
    public void setTicketTypeId(int ticketTypeId)        { this.ticketTypeId   = ticketTypeId;   }
    public void setQuantity(int quantity)                { this.quantity       = quantity;       }
    public void setBaseAmount(double baseAmount)         { this.baseAmount     = baseAmount;     }
    public void setVatAmount(double vatAmount)           { this.vatAmount      = vatAmount;      }
    public void setDiscountAmount(double discountAmount) { this.discountAmount = discountAmount; }
    public void setTotalAmount(double totalAmount)       { this.totalAmount    = totalAmount;    }
    public void setPaymentMethod(String paymentMethod)   { this.paymentMethod  = paymentMethod;  }
    public void setStatus(String status)                 { this.status         = status;         }
    public void setTransactedAt(String transactedAt)     { this.transactedAt   = transactedAt;   }
    public void setEventName(String eventName)           { this.eventName      = eventName;      }
    public void setHolderName(String holderName)         { this.holderName     = holderName;     }

    @Override
    public String toString() {
        return String.format("Transaction[%d] %s qty=%d base=%.2f vat=%.2f total=%.2f %s",
            transactionId, eventName, quantity, baseAmount, vatAmount, totalAmount, status);
    }
}
