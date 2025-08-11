import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class LoginPage extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JComboBox<String> roleComboBox;

    public LoginPage() {
        setTitle("Hotel Management Login");
        setSize(400, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new GridLayout(5, 2, 10, 10));

        JLabel usernameLabel = new JLabel("Username:");
        usernameField = new JTextField();

        JLabel passwordLabel = new JLabel("Password:");
        passwordField = new JPasswordField();

        JLabel roleLabel = new JLabel("Select Role:");
        roleComboBox = new JComboBox<>(new String[]{"Admin", "Employee", "Customer"});

        JButton loginButton = new JButton("Login");

        add(usernameLabel);
        add(usernameField);
        add(passwordLabel);
        add(passwordField);
        add(roleLabel);
        add(roleComboBox);
        add(new JLabel()); // spacer
        add(loginButton);

        loginButton.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            String role = (String) roleComboBox.getSelectedItem();

            switch (role) {
                case "Admin":
                    if (username.equals("admin") && password.equals("admin")) {
                        dispose();
                        new AdminDashboard();
                    } else {
                        JOptionPane.showMessageDialog(this, "Invalid Admin credentials");
                    }
                    break;
                case "Employee":
                    if (username.equals("emp") && password.equals("emp")) {
                        dispose();
                        new EmployeeDashboard(username);
                    } else {
                        JOptionPane.showMessageDialog(this, "Invalid Employee credentials");
                    }
                    break;
                case "Customer":
                    if (!username.isEmpty() && !password.isEmpty()) {
                        dispose();
                        new CustomerDashboard(username);
                    } else {
                        JOptionPane.showMessageDialog(this, "Enter valid customer credentials");
                    }
                    break;
            }
        });

        setVisible(true);
    }

    public static void main(String[] args) {
        new LoginPage();
    }
}
