package com.pricing;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PricingEngineTest {

    private PricingEngine engine;

    @BeforeEach
    void setUp() {
        engine = new PricingEngine();
    }

    // computeSubtotal

    @Test
    void subtotal_singleItem() {
        double result = engine.computeSubtotal(List.of(10.0), List.of(3));
        assertEquals(30.0, result, 0.001);
    }

    @Test
    void subtotal_multipleItems() {
        double result = engine.computeSubtotal(
            List.of(5.0, 20.0, 1.5),
            List.of(2,   1,    4)
        );
        // 10 + 20 + 6 = 36
        assertEquals(36.0, result, 0.001);
    }

    @Test
    void subtotal_emptyList_returnsZero() {
        double result = engine.computeSubtotal(List.of(), List.of());
        assertEquals(0.0, result, 0.001);
    }

    // computeDiscount

    @Test
    void discount_noCodeRegular() {
        double result = engine.computeDiscount(100.0, PricingEngine.CustomerType.REGULAR, null);
        assertEquals(0.0, result, 0.001);
    }

    @Test
    void discount_SAVE10_regular() {
        double result = engine.computeDiscount(100.0, PricingEngine.CustomerType.REGULAR, "SAVE10");
        assertEquals(10.0, result, 0.001);
    }

    @Test
    void discount_SAVE20_regular() {
        double result = engine.computeDiscount(100.0, PricingEngine.CustomerType.REGULAR, "SAVE20");
        assertEquals(20.0, result, 0.001);
    }

    @Test
    void discount_SAVE30_regular() {
        double result = engine.computeDiscount(100.0, PricingEngine.CustomerType.REGULAR, "SAVE30");
        assertEquals(30.0, result, 0.001);
    }

    @Test
    void discount_VIP_noCode() {
        // VIP gets 5% off subtotal
        double result = engine.computeDiscount(100.0, PricingEngine.CustomerType.VIP, null);
        assertEquals(5.0, result, 0.001);
    }

    @Test
    void discount_VIP_withSAVE10() {
        // SAVE10 = 10, then VIP 5% on remaining 90 = 4.5 → total 14.5
        double result = engine.computeDiscount(100.0, PricingEngine.CustomerType.VIP, "SAVE10");
        assertEquals(14.5, result, 0.001);
    }

    @Test
    void discount_unknownCode_givesZero() {
        double result = engine.computeDiscount(100.0, PricingEngine.CustomerType.REGULAR, "FAKECODE");
        assertEquals(0.0, result, 0.001);
    }

    @Test
    void discount_codeIsCaseInsensitive() {
        double lower = engine.computeDiscount(100.0, PricingEngine.CustomerType.REGULAR, "save10");
        assertEquals(10.0, lower, 0.001);
    }

    // computeTax

    @Test
    void tax_standardRate() {
        double result = engine.computeTax(100.0);
        assertEquals(19.0, result, 0.001);
    }

    @Test
    void tax_onZero_isZero() {
        double result = engine.computeTax(0.0);
        assertEquals(0.0, result, 0.001);
    }

    // calculate (full integration)

    @Test
    void calculate_regularNoDiscount() {
        PricingResult r = engine.calculate(
            List.of(100.0), List.of(1),
            PricingEngine.CustomerType.REGULAR, null
        );
        assertEquals(100.0,  r.getSubtotal(),       0.001);
        assertEquals(0.0,    r.getDiscountAmount(),  0.001);
        assertEquals(19.0,   r.getTax(),             0.001);
        assertEquals(119.0,  r.getFinalPrice(),      0.001);
    }

    @Test
    void calculate_vipWithSAVE20() {
        // subtotal = 200, SAVE20 = 40, after = 160, VIP 5% on 160 = 8 → discount=48
        // afterDiscount = 152, tax = 152*0.19 = 28.88, final = 180.88
        PricingResult r = engine.calculate(
            List.of(100.0, 50.0), List.of(1, 2),
            PricingEngine.CustomerType.VIP, "SAVE20"
        );
        assertEquals(200.0,  r.getSubtotal(),       0.001);
        assertEquals(48.0,   r.getDiscountAmount(),  0.001);
        assertEquals(28.88,  r.getTax(),             0.001);
        assertEquals(180.88, r.getFinalPrice(),      0.001);
    }

    @Test
    void calculate_nullLists_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
            engine.calculate(null, List.of(1), PricingEngine.CustomerType.REGULAR, null)
        );
    }

    @Test
    void calculate_mismatchedLists_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
            engine.calculate(
                List.of(10.0, 20.0), List.of(1),
                PricingEngine.CustomerType.REGULAR, null
            )
        );
    }
}
