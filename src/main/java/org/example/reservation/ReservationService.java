
package org.example.reservation;

public class ReservationService {

    private final ReservationRepository repository;
    private final SeatValidator validator;

    public ReservationService(
            ReservationRepository repository,
            SeatValidator validator) {
        this.repository = repository;
        this.validator = validator;
    }

    public SeatReservation reserveSeat(String seatNumber) {
        SeatReservation reservation =
                repository.findBySeatNumber(seatNumber);

        if (reservation == null) {
            reservation = new SeatReservation(seatNumber);
        }

        validator.validate(reservation);
        reservation.reserve();
        repository.save(reservation);

        return reservation;
    }

    public void cancelSeat(String seatNumber) {
        SeatReservation reservation =
                repository.findBySeatNumber(seatNumber);

        if (reservation == null) {
            throw new IllegalArgumentException(
                    "Reservation not found"
            );
        }

        reservation.cancel();
        repository.save(reservation);
    }

    public SeatReservation getReservation(String seatNumber) {
        return repository.findBySeatNumber(seatNumber);
    }
}
