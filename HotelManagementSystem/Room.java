public class Room {
    private int roomNumber;
    private String type; // "Single", "Double", "Suite"
    private boolean isAvailable;

    public Room(int roomNumber, String type, boolean isAvailable) {
        this.roomNumber = roomNumber;
        this.type = type;
        this.isAvailable = isAvailable;
    }

    public int getRoomNumber() { return roomNumber; }
    public String getType() { return type; }
    public boolean isAvailable() { return isAvailable; }

    public void setAvailable(boolean available) { isAvailable = available; }
}
