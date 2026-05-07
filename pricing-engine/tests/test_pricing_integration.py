"""
Integration tests for PricingEngine via subprocess.
Compiles and runs a small Java driver, then checks the output.

Run with:  python tests/test_pricing_integration.py
Requires:  javac and java on PATH, and the project already built (or uses src directly)
"""

import subprocess
import sys
import os
import unittest

# Helper: tiny Java driver that prints a CSV line: subtotal,discount,tax,final

DRIVER_SRC = """\
import com.pricing.*;


import java.util.*;

public class Driver {
    public static void main(String[] args) {
    
        // args: price1,qty1 price2,qty2 ... CUSTOMER_TYPE DISCOUNT_CODE|NONE
        List<Double>  prices     = new ArrayList<>();
        List<Integer> quantities = new ArrayList<>();

        int i = 0;
        while (i < args.length - 2) {
            String[] pq = args[i].split(",");
            prices.add(Double.parseDouble(pq[0]));
            quantities.add(Integer.parseInt(pq[1]));
            i++;
        }

        PricingEngine.CustomerType type =
            PricingEngine.CustomerType.valueOf(args[i]);
        String code = args[i + 1].equals("NONE") ? null : args[i + 1];

        PricingEngine engine = new PricingEngine();
        PricingResult r = engine.calculate(prices, quantities, type, code);

        System.out.printf("%.2f,%.2f,%.2f,%.2f%n",
            r.getSubtotal(), r.getDiscountAmount(), r.getTax(), r.getFinalPrice());
    }
}
"""

# Build helper

PROJECT_ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SRC_DIR      = os.path.join(PROJECT_ROOT, "src", "main", "java")
BUILD_DIR    = os.path.join(PROJECT_ROOT, "build", "integration")
DRIVER_PATH  = os.path.join(BUILD_DIR, "Driver.java")


def build():
    os.makedirs(BUILD_DIR, exist_ok=True)
    with open(DRIVER_PATH, "w") as f:
        f.write(DRIVER_SRC)

    # Collect all engine sources
    sources = []
    for root, _, files in os.walk(SRC_DIR):
        for file in files:
            if file.endswith(".java"):
                sources.append(os.path.join(root, file))
    sources.append(DRIVER_PATH)

    result = subprocess.run(
        ["javac", "-d", BUILD_DIR] + sources,
        capture_output=True, text=True
    )
    if result.returncode != 0:
        print("Compilation failed:\n", result.stderr)
        sys.exit(1)


def run_engine(*args):
    """Run the Java Driver with given CLI args, return (subtotal, discount, tax, final)."""
    result = subprocess.run(
        ["java", "-cp", BUILD_DIR, "Driver"] + list(args),
        capture_output=True, text=True
    )
    if result.returncode != 0:
        raise RuntimeError(result.stderr)
    values = result.stdout.strip().split(",")
    return tuple(float(v) for v in values)


# Test cases

class TestPricingIntegration(unittest.TestCase):

    @classmethod
    def setUpClass(cls):
        build()

    def test_regular_no_discount(self):
        subtotal, discount, tax, final = run_engine("100.0,1", "REGULAR", "NONE")
        self.assertAlmostEqual(subtotal,  100.0,  places=2)
        self.assertAlmostEqual(discount,  0.0,    places=2)
        self.assertAlmostEqual(tax,       19.0,   places=2)
        self.assertAlmostEqual(final,     119.0,  places=2)

    def test_regular_save10(self):
        subtotal, discount, tax, final = run_engine("200.0,1", "REGULAR", "SAVE10")
        # discount = 20, after = 180, tax = 34.2, final = 214.2
        self.assertAlmostEqual(subtotal,  200.0,  places=2)
        self.assertAlmostEqual(discount,  20.0,   places=2)
        self.assertAlmostEqual(tax,       34.2,   places=2)
        self.assertAlmostEqual(final,     214.2,  places=2)

    def test_vip_no_discount(self):
        subtotal, discount, tax, final = run_engine("100.0,1", "VIP", "NONE")
        # VIP 5% → discount=5, after=95, tax=18.05, final=113.05
        self.assertAlmostEqual(discount,  5.0,    places=2)
        self.assertAlmostEqual(tax,       18.05,  places=2)
        self.assertAlmostEqual(final,     113.05, places=2)

    def test_vip_save20(self):
        # subtotal=200, SAVE20=40, VIP on 160=8 → discount=48
        # after=152, tax=28.88, final=180.88
        subtotal, discount, tax, final = run_engine(
            "100.0,1", "50.0,2", "VIP", "SAVE20"
        )
        self.assertAlmostEqual(subtotal,  200.0,  places=2)
        self.assertAlmostEqual(discount,  48.0,   places=2)
        self.assertAlmostEqual(tax,       28.88,  places=2)
        self.assertAlmostEqual(final,     180.88, places=2)

    def test_multiple_items(self):
        # 3 items: 10*2=20, 5*3=15, 2*10=20 → subtotal=55
        # SAVE30 → discount=16.5, after=38.5, tax=7.315, final=45.815
        subtotal, discount, tax, final = run_engine(
            "10.0,2", "5.0,3", "2.0,10", "REGULAR", "SAVE30"
        )
        self.assertAlmostEqual(subtotal,  55.0,   places=2)
        self.assertAlmostEqual(discount,  16.5,   places=2)
        self.assertAlmostEqual(tax,       7.315,  places=2)
        self.assertAlmostEqual(final,     45.82, places=2)


if __name__ == "__main__":
    unittest.main(verbosity=2)
