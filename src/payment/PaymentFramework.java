package payment;

public abstract class PaymentFramework {

    protected double baseAmount;              // charge before tax / discount
    protected double totalAmount;             // running total after tax & discount
    protected final double VAT_RATE = 0.12;   // 12% VAT inclusive tax rate

    public PaymentFramework(double baseAmount) {
        if (baseAmount < 0) {
            throw new IllegalArgumentException("Base amount cannot be negative.");
        }
        this.baseAmount  = baseAmount;
        this.totalAmount = baseAmount;
    }

    public abstract boolean validatePayment();

    public void applyTax() {
        try {
            double vatAmount = this.baseAmount * VAT_RATE;
            this.totalAmount += vatAmount;
        } catch (Exception e) {
            System.err.println("[PaymentFramework ERROR] applyTax failed: " + e.getMessage());
            throw new RuntimeException("Tax computation failed.", e);
        }
    }

    public void applyDiscount(double discountAmount) {
        try {
            if (discountAmount < 0) {
                throw new IllegalArgumentException("Discount cannot be negative.");
            }
            if (discountAmount > this.totalAmount) {
                throw new IllegalArgumentException(
                    "Discount (P" + discountAmount + ") exceeds total (P" + this.totalAmount + ").");
            }
            this.totalAmount -= discountAmount;
        } catch (IllegalArgumentException e) {
            System.err.println("[PaymentFramework ERROR] applyDiscount: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.err.println("[PaymentFramework ERROR] applyDiscount failed: " + e.getMessage());
            throw new RuntimeException("Discount application failed.", e);
        }
    }

    public abstract void finalizeTransaction();

    public void processInvoice(double discountAmount) {
        try {
            System.out.println(" --- Starting Payment Process ---");

            if (!validatePayment()) {
                System.out.println("[!] Error: Payment validation failed.");
                return;
            }

            applyTax();
            applyDiscount(discountAmount);
            finalizeTransaction();

            System.out.println(" Invoice Processed Successfully.");
            System.out.printf(" Base Amount  : P%.2f%n", baseAmount);
            System.out.printf(" VAT (12%%)    : P%.2f%n", baseAmount * VAT_RATE);
            System.out.printf(" Discount     : P%.2f%n", discountAmount);
            System.out.printf(" Total Amount : P%.2f%n", totalAmount);

        } catch (IllegalArgumentException e) {
            System.err.println("[!] Payment Error: " + e.getMessage());
        } catch (RuntimeException e) {
            System.err.println("[!] Payment processing error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("[!] Unexpected payment error: " + e.getMessage());
        }
    }

    public double getBaseAmount()  { return baseAmount;              }
    public double getTotalAmount() { return totalAmount;             }
    public double getVatAmount()   { return baseAmount * VAT_RATE;  }
}
