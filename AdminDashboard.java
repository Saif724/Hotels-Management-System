import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class AdminDashboard extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private JComboBox<String> roomBox, typeBox;
    private JComboBox<Integer> dayBox, monthBox;
    private JTextField customerNameField;

    private JComboBox<String> employeeBox;
    private JTextArea employeeStatusArea;
    private JTextArea earningsArea;

    private static final ArrayList<Employee> employees = new ArrayList<>();
    private static final ArrayList<Customer> bookedCustomers = new ArrayList<>();
    private static final ArrayList<Customer> checkedOutCustomers = new ArrayList<>();
    public static final ArrayList<Feedback> feedbackList = new ArrayList<>();

    private static final Set<String> bookedRooms = new HashSet<>();
    private static double totalEarnings = 0;
    private static double salaryPaid = 0;

    public AdminDashboard() {
        setTitle("Admin Dashboard");
        setSize(700, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        employees.add(new Employee("Alice", 5000));
        employees.add(new Employee("Bob", 6000));
        employees.add(new Employee("Charlie", 4500));

        JPanel topPanel = new JPanel();
        JButton bookingBtn = new JButton("Booking");
        JButton employeeBtn = new JButton("Employee Details");
        JButton earningsBtn = new JButton("Earnings");
        JButton logoutBtn = new JButton("Logout");

        topPanel.add(bookingBtn);
        topPanel.add(employeeBtn);
        topPanel.add(earningsBtn);
        topPanel.add(logoutBtn);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        mainPanel.add(createBookingPanel(), "Booking");
        mainPanel.add(createEmployeePanel(), "Employee");
        mainPanel.add(createEarningsPanel(), "Earnings");

        add(topPanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);

        bookingBtn.addActionListener(e -> cardLayout.show(mainPanel, "Booking"));
        employeeBtn.addActionListener(e -> cardLayout.show(mainPanel, "Employee"));
        earningsBtn.addActionListener(e -> {
            updateEarningsArea();
            cardLayout.show(mainPanel, "Earnings");
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

            if (bookedRooms.contains(room)) {
                JOptionPane.showMessageDialog(this, "Room already booked!");
                return;
            }
            if(name.isEmpty()){
                JOptionPane.showMessageDialog(this, "Please enter customer name.");
                return;
            }

            Customer c = new Customer(name, room, type, day, month, stayDays);
            bookedCustomers.add(c);
            bookedRooms.add(room);
            JOptionPane.showMessageDialog(this, "Room Booked Successfully!");
        });

        return panel;
    }


    private JPanel createEmployeePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        employeeBox = new JComboBox<>();
        for (Employee emp : employees) employeeBox.addItem(emp.name);

        JButton payBtn = new JButton("Pay Salary");
        employeeStatusArea = new JTextArea(10, 30);
        updateEmployeeStatus();

        JPanel top = new JPanel();
        top.add(new JLabel("Select Employee:"));
        top.add(employeeBox);
        top.add(payBtn);

        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(employeeStatusArea), BorderLayout.CENTER);

        payBtn.addActionListener(e -> {
            String selected = (String) employeeBox.getSelectedItem();
            for (Employee emp : employees) {
                if (emp.name.equals(selected) && !emp.paid) {
                    emp.paid = true;
                    salaryPaid += emp.salary;
                    JOptionPane.showMessageDialog(this, "Salary Paid to " + emp.name);
                    updateEmployeeStatus();
                    break;
                }
            }
        });

        return panel;
    }

    private JPanel createEarningsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        earningsArea = new JTextArea(20, 40);
        earningsArea.setEditable(false);
        panel.add(new JScrollPane(earningsArea), BorderLayout.CENTER);
        updateEarningsArea();
        return panel;
    }

    private void updateEmployeeStatus() {
        employeeStatusArea.setText("");
        for (Employee emp : employees) {
            employeeStatusArea.append("Name: " + emp.name + " | Salary: " + emp.salary +
                    " | Paid: " + (emp.paid ? "Yes" : "No") + "\n");
        }
    }

    private void updateEarningsArea() {
        totalEarnings = checkedOutCustomers.size() * 3000; // per customer earning
        double balance = totalEarnings - salaryPaid;

        earningsArea.setText("Total Customers Checked Out: " + checkedOutCustomers.size() + "\n");
        earningsArea.append("Total Earnings: " + totalEarnings + "\n");
        earningsArea.append("Total Salary Paid: " + salaryPaid + "\n");
        earningsArea.append("Current Balance: " + balance + "\n");
    }

    // Shared access
    public static ArrayList<Customer> getBookedCustomers() {
        return bookedCustomers;
    }

    public static ArrayList<Customer> getCheckedOutCustomers() {
        return checkedOutCustomers;
    }

    public static Set<String> getBookedRooms() {
        return bookedRooms;
    }

    public static void releaseRoom(String room) {
        bookedRooms.remove(room);
    }
}
