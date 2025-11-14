package com.hotel;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class LoginForm extends JPanel {
    private JFrame parent;
    private JTextField userF;
    private JPasswordField passF;
    private JComboBox<String> roleBox;

    public LoginForm(JFrame parent) {
        this.parent = parent;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        JLabel head = new JLabel("Sign In", SwingConstants.CENTER);
        head.setFont(new Font("Arial", Font.BOLD, 18));
        add(head, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(0,2,8,8));
        form.setBorder(BorderFactory.createEmptyBorder(20,150,20,150));
        userF = new JTextField();
        passF = new JPasswordField();
        roleBox = new JComboBox<>(new String[]{"ADMIN","EMPLOYEE","CUSTOMER"});

        form.add(new JLabel("Username:")); form.add(userF);
        form.add(new JLabel("Password:")); form.add(passF);
        form.add(new JLabel("Role:")); form.add(roleBox);

        add(form, BorderLayout.CENTER);

        JPanel actions = new JPanel();
        JButton signIn = new JButton("Sign In");
        signIn.addActionListener(e -> doLogin());
        JButton back = new JButton("Back");
        back.addActionListener(e -> {
            parent.setContentPane(new StartScreen(parent));
            parent.revalidate();
        });
        actions.add(signIn); actions.add(back);
        add(actions, BorderLayout.SOUTH);
    }

    private void doLogin() {
        String username = userF.getText().trim();
        String password = new String(passF.getPassword()).trim();
        String role = (String) roleBox.getSelectedItem();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter username and password.");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT username, password, role FROM login WHERE username = ? AND role = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, username);
                ps.setString(2, role);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String dbPass = rs.getString("password");
                        if (password.equals(dbPass)) {
                            // login success; for customers fetch linked customer_id if exists
                            Integer cid = null;
                            if ("CUSTOMER".equals(role)) {
                                try (PreparedStatement pc = conn.prepareStatement("SELECT customer_id FROM customer WHERE username = ?")) {
                                    pc.setString(1, username);
                                    try (ResultSet rc = pc.executeQuery()) {
                                        if (rc.next()) cid = rc.getInt("customer_id");
                                    }
                                }
                            }
                            CurrentUser.set(username, role, cid);
                            JOptionPane.showMessageDialog(this, "Login successful as " + role);
                            openDashboard(role);
                            return;
                        } else {
                            JOptionPane.showMessageDialog(this, "Invalid password.");
                        }
                    } else {
                        JOptionPane.showMessageDialog(this, "User not found with selected role.");
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "DB Error: " + ex.getMessage());
        }
    }

    private void openDashboard(String role) {
        switch (role) {
            case "ADMIN":
                parent.setContentPane(new AdminDashboard(parent));
                break;
            case "EMPLOYEE":
                parent.setContentPane(new EmployeeDashboard(parent));
                break;
            case "CUSTOMER":
                parent.setContentPane(new CustomerDashboard(parent));
                break;
        }
        parent.revalidate();
    }
}
