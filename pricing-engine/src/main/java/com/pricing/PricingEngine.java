package com.pricing;

import java.util.List;

/**
  REFACTORED VERSION
  Clean separation of concerns, no magic numbers, testable methods.
 */
public class PricingEngine {

    public static final double TAX_RATE      = 0.19;
    public static final double VIP_DISCOUNT  = 0.05;

    public enum CustomerType { REGULAR, VIP }

    // Public API

    public PricingResult calculate(List<Double> prices,
                                   List<Integer> quantities,
                                   CustomerType customerType,
                                   String discountCode) {
        if (prices == null || quantities == null)
            throw new IllegalArgumentException("Prices and quantities must not be null");
        if (prices.size() != quantities.size())
            throw new IllegalArgumentException("Prices and quantities lists must be the same length");

        double subtotal       = computeSubtotal(prices, quantities);
        double discountAmount = computeDiscount(subtotal, customerType, discountCode);
        double afterDiscount  = subtotal - discountAmount;
        double tax            = computeTax(afterDiscount);
        double finalPrice     = afterDiscount + tax;

        return new PricingResult(subtotal, discountAmount, tax, finalPrice);
    }

    // Package-visible helpers (easy to unit-test individually)

    double computeSubtotal(List<Double> prices, List<Integer> quantities) {
        double total = 0;
        for (int i = 0; i < prices.size(); i++) {
            total += prices.get(i) * quantities.get(i);
        }
        return total;
    }

    double computeDiscount(double subtotal, CustomerType customerType, String discountCode) {
        double rate = codeDiscountRate(discountCode);
        double discount = subtotal * rate;

        if (customerType == CustomerType.VIP) {
            discount += (subtotal - discount) * VIP_DISCOUNT;
        }

        return discount;
    }

    double computeTax(double amount) {
        return amount * TAX_RATE;
    }

    // Private helpers

    private double codeDiscountRate(String code) {
        if (code == null) return 0;
        switch (code.toUpperCase()) {
            case "SAVE10": return 0.10;
            case "SAVE20": return 0.20;
            case "SAVE30": return 0.30;
            default:       return 0;
        }
    }
}
