
package org.example.reservation;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class ReservationRepository {

    private final Path filePath;

    public ReservationRepository() {
        this(Path.of("data", "reservations.csv"));
    }

    public ReservationRepository(Path filePath) {
        this.filePath = filePath;
    }

    // Save the current reservation state
    public void save(SeatReservation reservation) {
        if (reservation == null) {
            throw new IllegalArgumentException(
                    "Reservation cannot be null"
            );
        }

        try {
            Path parent = filePath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }

            String seatNumber = reservation.getSeatNumber();

            if (seatNumber.contains(",")
                    || seatNumber.contains("\n")
                    || seatNumber.contains("\r")) {
                throw new IllegalArgumentException(
                        "Invalid seat number"
                );
            }

            String line = seatNumber + ","
                    + reservation.getStatus() + "\n";

            Files.writeString(
                    filePath,
                    line,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Failed to save reservation", e
            );
        }
    }

    // Load the latest saved state for a seat
    public SeatReservation findBySeatNumber(String seatNumber) {
        if (seatNumber == null || seatNumber.isBlank()) {
            throw new IllegalArgumentException(
                    "Seat number cannot be empty"
            );
        }

        if (!Files.exists(filePath)) {
            return null;
        }

        try {
            List<String> lines = Files.readAllLines(
                    filePath, StandardCharsets.UTF_8
            );

            ReservationStatus latestStatus = null;

            for (String line : lines) {
                String[] parts = line.split(",", -1);

                if (parts.length != 2) {
                    throw new IllegalStateException(
                            "Invalid reservation record"
                    );
                }

                if (parts[0].equals(seatNumber)) {
                    latestStatus = ReservationStatus.valueOf(
                            parts[1]
                    );
                }
            }

            if (latestStatus == null) {
                return null;
            }

            SeatReservation reservation =
                    new SeatReservation(seatNumber);

            if (latestStatus == ReservationStatus.RESERVED) {
                reservation.reserve();
            } else if (latestStatus == ReservationStatus.CANCELLED) {
                reservation.reserve();
                reservation.cancel();
            }

            return reservation;

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Failed to load reservation", e
            );
        }
    }
}
