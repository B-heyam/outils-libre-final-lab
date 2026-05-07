package com.pricing;

import java.util.List;

// BAD DESIGN - Everything crammed into one method, magic numbers,
// no separation of concerns, poor naming, hard to test or maintain.
public class PricingEngineBad {

    public double calc(List<Double> p, List<Integer> q, String t, String code) {
        double tot = 0;
        for (int i = 0; i < p.size(); i++) {
            tot += p.get(i) * q.get(i);
        }

        double disc = 0;
        if (code != null) {
            if (code.equals("SAVE10")) {
                disc = tot * 0.10;
            } else if (code.equals("SAVE20")) {
                disc = tot * 0.20;
            } else if (code.equals("SAVE30")) {
                disc = tot * 0.30;
            }
        }

        if (t.equals("VIP")) {
            disc += (tot - disc) * 0.05;
        }

        double after = tot - disc;

        double tax = after * 0.19; // magic number

        double fin = after + tax;

        System.out.println(tot);
        System.out.println(disc);
        System.out.println(tax);
        System.out.println(fin);

        return fin;
    }
}
