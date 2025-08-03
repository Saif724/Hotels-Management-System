import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class BookingPanel extends JPanel {

    public BookingPanel() {
        setLayout(new BorderLayout());

        // Available Rooms Panel
        JTextArea roomArea = new JTextArea(10, 30);
        roomArea.setEditable(false);
        updateRoomList(roomArea);

        JScrollPane scroll = new JScrollPane(roomArea);
        add(scroll, BorderLayout.CENTER);

        // Booking Form Panel
        JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        JTextField nameField = new JTextField();
        JTextField dateField = new JTextField();
        JTextField roomField = new JTextField();

        formPanel.add(new JLabel("Customer Name:"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Check-In Date (DD-MM-YYYY):"));
        formPanel.add(dateField);
        formPanel.add(new JLabel("Room Number:"));
        formPanel.add(roomField);

        JButton bookBtn = new JButton("Book Room");
        formPanel.add(bookBtn);
        formPanel.add(new JLabel()); // Empty cell

        add(formPanel, BorderLayout.SOUTH);

        // Booking Action
        bookBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            String date = dateField.getText().trim();
            int roomNo;

            try {
                roomNo = Integer.parseInt(roomField.getText().trim());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid Room Number");
                return;
            }

            Room selected = null;
            for (Room room : HomePage.rooms) {
                if (room.getRoomNumber() == roomNo && room.isAvailable()) {
                    selected = room;
                    break;
                }
            }

            if (selected == null) {
                JOptionPane.showMessageDialog(this, "Room not available!");
            } else {
                HomePage.customers.add(new Customer(name, roomNo, date));
                selected.setAvailable(false);
                JOptionPane.showMessageDialog(this, "Room Booked Successfully!");
                updateRoomList(roomArea);
                nameField.setText("");
                dateField.setText("");
                roomField.setText("");
            }
        });
    }

    private void updateRoomList(JTextArea roomArea) {
        StringBuilder sb = new StringBuilder("Available Rooms:\n");
        for (Room room : HomePage.rooms) {
            if (room.isAvailable()) {
                sb.append("Room ").append(room.getRoomNumber())
                  .append(" (").append(room.getType()).append(")\n");
            }
        }
        roomArea.setText(sb.toString());
    }
}
