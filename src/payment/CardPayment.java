package payment;

public class CardPayment extends PaymentFramework {

    private final String cardNo;
    private final String expiry;
    private final String cvv;
    private final String holderName;

    private boolean transactionComplete = false;

    public CardPayment(double baseAmount, String cardNo, String expiry,
                       String cvv, String holderName) {
        super(baseAmount);
        this.cardNo     = cardNo     != null ? cardNo.trim()     : "";
        this.expiry     = expiry     != null ? expiry.trim()     : "";
        this.cvv        = cvv        != null ? cvv.trim()        : "";
        this.holderName = holderName != null ? holderName.trim() : "";
    }

    @Override
    public boolean validatePayment() {
        try {
            if (cardNo.isEmpty() || expiry.isEmpty() || cvv.isEmpty()) {
                System.out.println("[!] Card details are incomplete.");
                return false;
            }
            if (!cardNo.matches("\\d{16}")) {
                System.out.println("[!] Card number must be exactly 16 digits.");
                return false;
            }
            if (!expiry.matches("(0[1-9]|1[0-2])/\\d{2}")) {
                System.out.println("[!] Expiry must be in MM/YY format.");
                return false;
            }
            if (!cvv.matches("\\d{3}")) {
                System.out.println("[!] CVV must be exactly 3 digits.");
                return false;
            }
            if (baseAmount <= 0) {
                System.out.println("[!] Payment amount must be greater than zero.");
                return false;
            }
            return true;

        } catch (Exception e) {
            System.err.println("[CardPayment ERROR] validatePayment: " + e.getMessage());
            return false;
        }
    }

    @Override
    public void finalizeTransaction() {
        try {
            transactionComplete = true;
            System.out.println(" Payment Method : Credit / Debit Card");
            System.out.println(" Card           : " + getMaskedCard());
            System.out.println(" Holder         : " + holderName);
            System.out.println(" Status         : APPROVED");
        } catch (Exception e) {
            transactionComplete = false;
            System.err.println("[CardPayment ERROR] finalizeTransaction: " + e.getMessage());
            throw new RuntimeException("Could not finalize card transaction.", e);
        }
    }

    public boolean isTransactionComplete() { return transactionComplete; }

    public String getMaskedCard() {
        if (cardNo.length() < 4) return "**** **** **** ????";
        return "**** **** **** " + cardNo.substring(cardNo.length() - 4);
    }

    public String getCardNo()     { return cardNo;     }
    public String getExpiry()     { return expiry;     }
    public String getHolderName() { return holderName; }
}
