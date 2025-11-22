package rideshare;

public class RiderBooking {
    private int bookingId;
    private int riderId;
    private String pickup;
    private String destination;

    // constructor
    public RiderBooking(int bookingId, int riderId, String pickup, String destination) {
        this.bookingId = bookingId;
        this.riderId = riderId;
        this.pickup = pickup;
        this.destination = destination;
    }

    // getters
    public int getBookingId() { return bookingId; }
    public int getRiderId() { return riderId; }
    public String getPickup() { return pickup; }
    public String getDestination() { return destination; }
}