
package org.example.reservation;

public class SeatReservation {

    private String seatNumber;
    private ReservationStatus status;

    public SeatReservation(String seatNumber) {
        if (seatNumber == null || seatNumber.isBlank()) {
            throw new IllegalArgumentException("Seat number cannot be empty");
        }

        this.seatNumber = seatNumber;
        this.status = ReservationStatus.AVAILABLE;
    }

    public void reserve() {
        if (status != ReservationStatus.AVAILABLE) {
            throw new IllegalStateException("Seat is not available");
        }

        status = ReservationStatus.RESERVED;
    }

    public void cancel() {
        if (status != ReservationStatus.RESERVED) {
            throw new IllegalStateException("Seat is not reserved");
        }

        status = ReservationStatus.CANCELLED;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public ReservationStatus getStatus() {
        return status;
    }
}
