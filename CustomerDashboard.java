import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class CustomerDashboard extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;

    private JComboBox<String> roomBox, typeBox;
    private JComboBox<Integer> dayBox, monthBox;
    private JTextArea feedbackArea;
    private JTextArea checkoutInfoArea;
    private JComboBox<String> checkoutBox;

    private String customerName;

    public CustomerDashboard(String customerName) {
        this.customerName = customerName;

        setTitle("Customer Dashboard - " + customerName);
        setSize(700, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel topPanel = new JPanel();
        JButton bookingBtn = new JButton("Booking");
        JButton checkoutBtn = new JButton("Checkout");
        JButton feedbackBtn = new JButton("Feedback");
        JButton logoutBtn = new JButton("Logout");

        topPanel.add(bookingBtn);
        topPanel.add(checkoutBtn);
        topPanel.add(feedbackBtn);
        topPanel.add(logoutBtn);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        mainPanel.add(createBookingPanel(), "Booking");
        mainPanel.add(createCheckoutPanel(), "Checkout");
        mainPanel.add(createFeedbackPanel(), "Feedback");

        add(topPanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);

        bookingBtn.addActionListener(e -> cardLayout.show(mainPanel, "Booking"));
        checkoutBtn.addActionListener(e -> {
            updateCheckoutPanel();
            cardLayout.show(mainPanel, "Checkout");
        });
        feedbackBtn.addActionListener(e -> cardLayout.show(mainPanel, "Feedback"));

        logoutBtn.addActionListener(e -> {
            dispose();
            new LoginPage();
        });

        setVisible(true);
    }

    private JPanel createBookingPanel() {
        JPanel panel = new JPanel(new GridLayout(7, 2, 10, 10));  // changed rows from 6 to 7
        roomBox = new JComboBox<>();
        typeBox = new JComboBox<>(new String[]{"Single", "Double"});
        dayBox = new JComboBox<>();
        monthBox = new JComboBox<>();
        JComboBox<Integer> stayDaysBox = new JComboBox<>(); // new dropdown

        for (int i = 101; i <= 110; i++) roomBox.addItem(String.valueOf(i));
        for (int i = 1; i <= 31; i++) dayBox.addItem(i);
        for (int i = 1; i <= 12; i++) monthBox.addItem(i);
        for (int i = 1; i <= 15; i++) stayDaysBox.addItem(i); // fill days to stay 1-15

        JButton bookBtn = new JButton("Book Room");

        panel.add(new JLabel("Customer Name:"));
        panel.add(new JLabel(customerName)); // Display customer name, not editable
        panel.add(new JLabel("Room Number:"));
        panel.add(roomBox);
        panel.add(new JLabel("Room Type:"));
        panel.add(typeBox);
        panel.add(new JLabel("Day:"));
        panel.add(dayBox);
        panel.add(new JLabel("Month:"));
        panel.add(monthBox);

        panel.add(new JLabel("Days to Stay:"));   // new label
        panel.add(stayDaysBox);                   // new dropdown

        panel.add(new JLabel());
        panel.add(bookBtn);

        bookBtn.addActionListener(e -> {
            String room = (String) roomBox.getSelectedItem();
            String type = (String) typeBox.getSelectedItem();
            int day = (int) dayBox.getSelectedItem();
            int month = (int) monthBox.getSelectedItem();
            int stayDays = (int) stayDaysBox.getSelectedItem(); // read new value

            if (AdminDashboard.getBookedRooms().contains(room)) {
                JOptionPane.showMessageDialog(this, "Room already booked!");
                return;
            }

            // Pass stayDays to Customer constructor
            Customer c = new Customer(customerName, room, type, day, month, stayDays);
            AdminDashboard.getBookedCustomers().add(c);
            AdminDashboard.getBookedRooms().add(room);
            JOptionPane.showMessageDialog(this, "Room Booked Successfully!");
        });

        return panel;
    }

    private JPanel createCheckoutPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        checkoutInfoArea = new JTextArea(10, 40);
        checkoutBox = new JComboBox<>();
        JButton checkoutBtn = new JButton("Checkout");

        JPanel top = new JPanel();
        top.add(new JLabel("Select Your Booking:"));
        top.add(checkoutBox);
        top.add(checkoutBtn);

        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(checkoutInfoArea), BorderLayout.CENTER);

        checkoutBtn.addActionListener(e -> {
            String selected = (String) checkoutBox.getSelectedItem();
            if (selected != null) {
                ArrayList<Customer> list = AdminDashboard.getBookedCustomers();
                for (Customer c : list) {
                    if (c.name.equals(customerName) && c.roomNumber.equals(selected)) {
                        AdminDashboard.getCheckedOutCustomers().add(c);
                        AdminDashboard.releaseRoom(c.roomNumber);
                        AdminDashboard.getBookedCustomers().remove(c);
                        JOptionPane.showMessageDialog(this, "Checked Out Successfully!");
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
        checkoutInfoArea.setText("");
        for (Customer c : AdminDashboard.getBookedCustomers()) {
            if (c.name.equals(customerName)) {
                checkoutBox.addItem(c.roomNumber);
                checkoutInfoArea.append("Room: " + c.roomNumber + ", Type: " + c.roomType +
                        ", Day: " + c.day + ", Month: " + c.month + "\n");
            }
        }
    }

    private JPanel createFeedbackPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        feedbackArea = new JTextArea(10, 40);
        JButton submitBtn = new JButton("Submit Feedback");

        panel.add(new JScrollPane(feedbackArea), BorderLayout.CENTER);
        panel.add(submitBtn, BorderLayout.SOUTH);

        submitBtn.addActionListener(e -> {
            String feedbackText = feedbackArea.getText().trim();
            if (!feedbackText.isEmpty()) {
                AdminDashboard.feedbackList.add(new Feedback(customerName, feedbackText));
                feedbackArea.setText("");
                JOptionPane.showMessageDialog(this, "Feedback submitted. Thank you!");
            } else {
                JOptionPane.showMessageDialog(this, "Please enter feedback before submitting.");
            }
        });

        return panel;
    }
}
