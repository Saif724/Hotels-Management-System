package com.hotel;

import javax.swing.*;
import java.awt.*;

public class StartScreen extends JPanel {
    private JFrame parent;
    public StartScreen(JFrame parent) {
        this.parent = parent;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        JLabel title = new JLabel("Hotel Management System", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 26));
        add(title, BorderLayout.NORTH);

        JPanel center = new JPanel();
        center.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        JButton signIn = new JButton("Sign In");
        signIn.setPreferredSize(new Dimension(200,50));
        signIn.addActionListener(e -> {
            parent.setContentPane(new LoginForm(parent));
            parent.revalidate();
        });

        JButton signUp = new JButton("Sign Up (Customer)");
        signUp.setPreferredSize(new Dimension(200,50));
        signUp.addActionListener(e -> {
            parent.setContentPane(new SignUpForm(parent));
            parent.revalidate();
        });

        gbc.insets = new Insets(20,20,20,20);
        gbc.gridy = 0;
        center.add(signIn, gbc);
        gbc.gridy = 1;
        center.add(signUp, gbc);

        add(center, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JLabel note = new JLabel("Admin: use sign in; Admin creates employee accounts.");
        bottom.add(note);
        add(bottom, BorderLayout.SOUTH);
    }
}
