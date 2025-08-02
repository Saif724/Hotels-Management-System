public class Customer {
    private String name;
    private int roomNumber;
    private String checkInDate;

    public Customer(String name, int roomNumber, String checkInDate) {
        this.name = name;
        this.roomNumber = roomNumber;
        this.checkInDate = checkInDate;
    }

    public String getName() { return name; }
    public int getRoomNumber() { return roomNumber; }
    public String getCheckInDate() { return checkInDate; }
}
