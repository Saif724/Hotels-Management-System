import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class EmployeeDashboard extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;

    private JComboBox<String> roomBox, typeBox;
    private JComboBox<Integer> dayBox, monthBox;
    private JTextField customerNameField;
    private JTextArea checkoutArea;
    private JComboBox<String> checkoutBox;

    public EmployeeDashboard(String employeeName) {
        setTitle("Employee Dashboard - " + employeeName);
        setSize(700, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel topPanel = new JPanel();
        JButton bookingBtn = new JButton("Booking");
        JButton checkoutBtn = new JButton("Checkout");
        JButton logoutBtn = new JButton("Logout");

        topPanel.add(bookingBtn);
        topPanel.add(checkoutBtn);
        topPanel.add(logoutBtn);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        mainPanel.add(createBookingPanel(), "Booking");
        mainPanel.add(createCheckoutPanel(), "Checkout");

        add(topPanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);

        bookingBtn.addActionListener(e -> cardLayout.show(mainPanel, "Booking"));
        checkoutBtn.addActionListener(e -> {
            updateCheckoutPanel();
            cardLayout.show(mainPanel, "Checkout");
        });
        logoutBtn.addActionListener(e -> {
            dispose();
            new LoginPage();
        });

        setVisible(true);
    }

    private JPanel createBookingPanel() {
        JPanel panel = new JPanel(new GridLayout(7, 2, 10, 10)); // changed 6 to 7 rows

        customerNameField = new JTextField();
        roomBox = new JComboBox<>();
        typeBox = new JComboBox<>(new String[]{"Single", "Double"});
        dayBox = new JComboBox<>();
        monthBox = new JComboBox<>();
        JComboBox<Integer> stayDaysBox = new JComboBox<>();

        for (int i = 101; i <= 110; i++) roomBox.addItem(String.valueOf(i));
        for (int i = 1; i <= 31; i++) dayBox.addItem(i);
        for (int i = 1; i <= 12; i++) monthBox.addItem(i);
        for (int i = 1; i <= 15; i++) stayDaysBox.addItem(i);

        JButton bookBtn = new JButton("Book Room");

        panel.add(new JLabel("Customer Name:"));
        panel.add(customerNameField);
        panel.add(new JLabel("Room Number:"));
        panel.add(roomBox);
        panel.add(new JLabel("Room Type:"));
        panel.add(typeBox);
        panel.add(new JLabel("Day:"));
        panel.add(dayBox);
        panel.add(new JLabel("Month:"));
        panel.add(monthBox);

        panel.add(new JLabel("Days to Stay:"));
        panel.add(stayDaysBox);

        panel.add(new JLabel());
        panel.add(bookBtn);

        bookBtn.addActionListener(e -> {
            String name = customerNameField.getText();
            String room = (String) roomBox.getSelectedItem();
            String type = (String) typeBox.getSelectedItem();
            int day = (int) dayBox.getSelectedItem();
            int month = (int) monthBox.getSelectedItem();
            int stayDays = (int) stayDaysBox.getSelectedItem();

            if (AdminDashboard.getBookedRooms().contains(room)) {
                JOptionPane.showMessageDialog(this, "Room already booked!");
                return;
            }
            if(name.isEmpty()){
                JOptionPane.showMessageDialog(this, "Please enter customer name.");
                return;
            }

            Customer c = new Customer(name, room, type, day, month, stayDays);
            AdminDashboard.getBookedCustomers().add(c);
            AdminDashboard.getBookedRooms().add(room);
            JOptionPane.showMessageDialog(this, "Room Booked Successfully!");
        });

        return panel;
    }


    private JPanel createCheckoutPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        checkoutArea = new JTextArea(10, 40);
        checkoutBox = new JComboBox<>();
        JButton checkoutBtn = new JButton("Checkout");

        JPanel top = new JPanel();
        top.add(new JLabel("Select Customer:"));
        top.add(checkoutBox);
        top.add(checkoutBtn);

        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(checkoutArea), BorderLayout.CENTER);

        checkoutBtn.addActionListener(e -> {
            String selected = (String) checkoutBox.getSelectedItem();
            if (selected != null) {
                ArrayList<Customer> list = AdminDashboard.getBookedCustomers();
                for (Customer c : list) {
                    if (c.name.equals(selected)) {
                        AdminDashboard.getCheckedOutCustomers().add(c);
                        AdminDashboard.releaseRoom(c.roomNumber);
                        AdminDashboard.getBookedCustomers().remove(c);
                        JOptionPane.showMessageDialog(this, "Customer Checked Out!");
                        break;
                    }
                }
                updateCheckoutPanel();
            }
        });

        return panel;
    }

    private void updateCheckoutPanel() {
        checkoutBox.removeAllItems();
        checkoutArea.setText("");
        for (Customer c : AdminDashboard.getBookedCustomers()) {
            checkoutBox.addItem(c.name);
            checkoutArea.append("Name: " + c.name + ", Room: " + c.roomNumber + ", Type: " + c.roomType + "\n");
        }
    }
}
