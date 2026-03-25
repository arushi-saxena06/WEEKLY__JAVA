import java.util.*;

public class Problem8_ParkingLot {

    private static final int EMPTY = 0;
    private static final int OCCUPIED = 1;
    private static final int DELETED = 2;

    private static class ParkingSlot {
        int status;
        String licensePlate;
        long entryTime;

        ParkingSlot() {
            this.status = EMPTY;
        }
    }

    private ParkingSlot[] slots;
    private final int size;
    private int occupiedCount = 0;
    private long totalProbes = 0;
    private long totalParkings = 0;

    public Problem8_ParkingLot(int size) {
        this.size = size;
        this.slots = new ParkingSlot[size];
        for (int i = 0; i < size; i++) {
            slots[i] = new ParkingSlot();
        }
    }

    private int hash(String licensePlate) {
        int h = 0;
        for (char c : licensePlate.toCharArray()) {
            h = (h * 31 + c) % size;
        }
        return Math.abs(h);
    }

    public String parkVehicle(String licensePlate) {
        int preferred = hash(licensePlate);
        int probes = 0;
        int idx = preferred;

        while (slots[idx].status == OCCUPIED) {
            probes++;
            idx = (idx + 1) % size;
            if (idx == preferred) {
                return "Parking lot is full!";
            }
        }

        slots[idx].status = OCCUPIED;
        slots[idx].licensePlate = licensePlate;
        slots[idx].entryTime = System.currentTimeMillis();
        occupiedCount++;
        totalProbes += probes;
        totalParkings++;

        return "Assigned spot #" + idx + " (" + probes + " probes)";
    }

    public String exitVehicle(String licensePlate) {
        int preferred = hash(licensePlate);
        int idx = preferred;

        for (int i = 0; i < size; i++) {
            if (slots[idx].status == OCCUPIED && licensePlate.equals(slots[idx].licensePlate)) {
                long durationMs = System.currentTimeMillis() - slots[idx].entryTime;
                long durationMin = durationMs / 60000;
                double fee = (durationMin / 60.0) * 5.0 + (durationMin % 60) * (5.0 / 60.0);

                int spotNumber = idx;
                slots[idx].status = DELETED;
                slots[idx].licensePlate = null;
                occupiedCount--;

                return String.format("Spot #%d freed, Duration: %dh %dm, Fee: $%.2f",
                        spotNumber, durationMin / 60, durationMin % 60, fee);
            }
            idx = (idx + 1) % size;
            if (slots[idx].status == EMPTY) break;
        }
        return "Vehicle not found.";
    }

    public void getStatistics() {
        double occupancy = (occupiedCount * 100.0) / size;
        double avgProbes = totalParkings > 0 ? (totalProbes * 1.0 / totalParkings) : 0;
        System.out.printf("getStatistics() → Occupancy: %.0f%%, Avg Probes: %.1f%n", occupancy, avgProbes);
    }

    public static void main(String[] args) throws InterruptedException {
        Problem8_ParkingLot lot = new Problem8_ParkingLot(500);

        System.out.println("parkVehicle(\"ABC-1234\") → " + lot.parkVehicle("ABC-1234"));
        System.out.println("parkVehicle(\"ABC-1235\") → " + lot.parkVehicle("ABC-1235"));
        System.out.println("parkVehicle(\"XYZ-9999\") → " + lot.parkVehicle("XYZ-9999"));

        Thread.sleep(100);

        System.out.println("exitVehicle(\"ABC-1234\") → " + lot.exitVehicle("ABC-1234"));

        for (int i = 0; i < 385; i++) {
            lot.parkVehicle("VEH-" + String.format("%04d", i));
        }

        lot.getStatistics();
    }
}
