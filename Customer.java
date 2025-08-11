public class Customer {
    public String name;
    public String roomNumber;
    public String roomType;
    public int day;
    public int month;
    public int stayDays;  // new field

    public Customer(String name, String roomNumber, String roomType, int day, int month, int stayDays) {
        this.name = name;
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.day = day;
        this.month = month;
        this.stayDays = stayDays;
    }
}
