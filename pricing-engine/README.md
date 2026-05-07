# Pricing & Discount Engine

A Java pricing calculator built with Gradle, refactored from bad design to clean code, and fully tested with JUnit and Python.

---

## What it does

Takes a list of items, a customer type, and a discount code, then returns:
- Subtotal
- Discount amount
- Tax (19%)
- Final price

**Customer types:** `REGULAR`, `VIP` (VIP gets an extra 5% off)  
**Discount codes:** `SAVE10`, `SAVE20`, `SAVE30`

---

## Project Structure

```
pricing-engine/
├── src/
│   ├── main/java/com/pricing/
│   │   ├── PricingEngineBad.java   # The bad design (one method, magic numbers)
│   │   ├── PricingEngine.java      # Refactored clean version
│   │   └── PricingResult.java      # Result object (subtotal, discount, tax, final)
│   └── test/java/com/pricing/
│       └── PricingEngineTest.java  # 17 JUnit unit tests
├── tests/
│   └── test_pricing_integration.py # 5 Python end-to-end tests
├── build.gradle
└── settings.gradle
```

---

## What changed in the refactor

The original `PricingEngineBad.java` had everything in one method with no clear structure, magic numbers, and meaningless variable names like `p`, `q`, `tot`, `fin`.

The refactored `PricingEngine.java` separates each responsibility into its own method:
- `computeSubtotal()` — sums item prices × quantities
- `computeDiscount()` — applies code and VIP discounts
- `computeTax()` — applies 19% tax
- `calculate()` — orchestrates everything and returns a clean result

---

## How to run

### Requirements
- Java JDK 21+
- Python 3 (for integration tests)

### Run JUnit tests
```bash
./gradlew test
```
View the full report at: `build/reports/tests/test/index.html`

### Run Python integration tests
```bash
python tests/test_pricing_integration.py
```

---

## Test Results

| Suite | Tests | Passed |
|---|---|---|
| JUnit (Java) | 17 | 17 ✅ |
| Python integration | 5 | 5 ✅ |

---

## Git Workflow

Commits were made step by step to show progression:

1. `init: Gradle project structure`
2. `feat: add bad design PricingEngine starter code`
3. `test: add JUnit unit tests`
4. `refactor: clean PricingEngine with separation of concerns`
5. `test: add Python integration tests`
6. `chore: add Gradle wrapper and README`