
package org.example.reservation;

public class SeatValidator {

    public void validate(SeatReservation reservation) {
        if (reservation == null) {
            throw new IllegalArgumentException(
                    "Reservation cannot be null"
            );
        }

        if (reservation.getStatus() != ReservationStatus.AVAILABLE) {
            throw new IllegalStateException(
                    "Seat is not available for reservation"
            );
        }
    }
}

