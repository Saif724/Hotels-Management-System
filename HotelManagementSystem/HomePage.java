import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class HomePage extends JFrame {

    CardLayout cardLayout;
    JPanel mainPanel;

    public HomePage() {
        setTitle("Hotel Management System");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel buttonPanel = new JPanel(new GridLayout(1, 5, 10, 10));
        JButton btnDashboard = new JButton("Dashboard");
        JButton btnBook = new JButton("Book Room");
        JButton btnCheckout = new JButton("Check Out");
        JButton btnRooms = new JButton("Rooms");
        JButton btnAdmin = new JButton("Admin");

        buttonPanel.add(btnDashboard);
        buttonPanel.add(btnBook);
        buttonPanel.add(btnCheckout);
        buttonPanel.add(btnRooms);
        buttonPanel.add(btnAdmin);

        // Create main panel with card layout
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        mainPanel.add(new JLabel("Dashboard Screen", SwingConstants.CENTER), "Dashboard");
        mainPanel.add(new JLabel("Booking Screen", SwingConstants.CENTER), "Book");
        mainPanel.add(new JLabel("Checkout Screen", SwingConstants.CENTER), "Checkout");
        mainPanel.add(new JLabel("Rooms Screen", SwingConstants.CENTER), "Rooms");
        mainPanel.add(new JLabel("Admin Screen", SwingConstants.CENTER), "Admin");

        btnDashboard.addActionListener(e -> cardLayout.show(mainPanel, "Dashboard"));
        btnBook.addActionListener(e -> cardLayout.show(mainPanel, "Book"));
        btnCheckout.addActionListener(e -> cardLayout.show(mainPanel, "Checkout"));
        btnRooms.addActionListener(e -> cardLayout.show(mainPanel, "Rooms"));
        btnAdmin.addActionListener(e -> cardLayout.show(mainPanel, "Admin"));

        setLayout(new BorderLayout());
        add(buttonPanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);

        setVisible(true);
    }
}
