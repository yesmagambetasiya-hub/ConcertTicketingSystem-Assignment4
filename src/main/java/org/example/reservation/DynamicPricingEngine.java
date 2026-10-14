
package org.example.reservation;

public class DynamicPricingEngine {

    public double calculatePrice(String seatCategory) {

        if (seatCategory == null) {
            throw new IllegalArgumentException(
                    "Seat category cannot be null"
            );
        }

        switch (seatCategory.toUpperCase()) {

            case "VIP":
                return 30000.0;

            case "STANDARD":
                return 15000.0;

            case "ECONOMY":
                return 8000.0;

            default:
                throw new IllegalArgumentException(
                        "Invalid seat category"
                );
        }
    }
}
