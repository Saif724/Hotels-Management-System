package com.hotel;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class SignUpForm extends JPanel {
    private JFrame parent;
    private JTextField nameF, contactF, emailF, usernameF;
    private JPasswordField passF;

    public SignUpForm(JFrame parent) {
        this.parent = parent;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        JLabel head = new JLabel("Customer Sign Up", SwingConstants.CENTER);
        head.setFont(new Font("Arial", Font.BOLD, 18));
        add(head, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(0,2,8,8));
        form.setBorder(BorderFactory.createEmptyBorder(20,100,20,100));
        nameF = new JTextField();
        contactF = new JTextField();
        emailF = new JTextField();
        usernameF = new JTextField();
        passF = new JPasswordField();

        form.add(new JLabel("Full Name:")); form.add(nameF);
        form.add(new JLabel("Contact:")); form.add(contactF);
        form.add(new JLabel("Email:")); form.add(emailF);
        form.add(new JLabel("Username:")); form.add(usernameF);
        form.add(new JLabel("Password:")); form.add(passF);

        add(form, BorderLayout.CENTER);

        JPanel actions = new JPanel();
        JButton submit = new JButton("Sign Up");
        submit.addActionListener(e -> doSignUp());
        JButton back = new JButton("Back");
        back.addActionListener(e -> {
            parent.setContentPane(new StartScreen(parent));
            parent.revalidate();
        });
        actions.add(submit); actions.add(back);
        add(actions, BorderLayout.SOUTH);
    }

    private void doSignUp() {
        String name = nameF.getText().trim();
        String contact = contactF.getText().trim();
        String email = emailF.getText().trim();
        String username = usernameF.getText().trim();
        String password = new String(passF.getPassword()).trim();

        if (name.isEmpty() || username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name, username and password are required.");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            // check username uniqueness in login
            try (PreparedStatement pch = conn.prepareStatement("SELECT username FROM login WHERE username = ?")) {
                pch.setString(1, username);
                try (ResultSet rs = pch.executeQuery()) {
                    if (rs.next()) {
                        JOptionPane.showMessageDialog(this, "Username already exists.");
                        return;
                    }
                }
            }

            // insert into login
            try (PreparedStatement pli = conn.prepareStatement("INSERT INTO login (username, password, role) VALUES (?, ?, 'CUSTOMER')")) {
                pli.setString(1, username);
                pli.setString(2, password); // demo only: plain text
                pli.executeUpdate();
            }

            // insert into customer linking username
            try (PreparedStatement pc = conn.prepareStatement("INSERT INTO customer (name, contact, email, username) VALUES (?,?,?,?)")) {
                pc.setString(1, name);
                pc.setString(2, contact);
                pc.setString(3, email.isEmpty()? null : email);
                pc.setString(4, username);
                pc.executeUpdate();
            }

            conn.commit();
            JOptionPane.showMessageDialog(this, "Sign up successful. Please sign in.");
            parent.setContentPane(new LoginForm(parent));
            parent.revalidate();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }
}
