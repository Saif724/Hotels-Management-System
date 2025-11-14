package com.hotel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;

public class EmployeeDashboard extends JPanel {
    private JFrame parent;
    private DefaultTableModel roomModel, bookingModel;

    public EmployeeDashboard(JFrame parent) {
        this.parent = parent;
        initUI();
        loadRooms();
        loadBookings();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        JPanel top = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        top.add(new JLabel("Employee: " + CurrentUser.getUsername()));
        JButton logout = new JButton("Logout"); logout.addActionListener(e -> {
            CurrentUser.clear();
            parent.setContentPane(new StartScreen(parent));
            parent.revalidate();
        });
        top.add(logout);
        add(top, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();

        // Rooms
        JPanel rooms = new JPanel(new BorderLayout());
        roomModel = new DefaultTableModel(new Object[]{"Room","Type","Price","Status"},0);
        JTable rTable = new JTable(roomModel);
        rooms.add(new JScrollPane(rTable), BorderLayout.CENTER);
        JButton refreshR = new JButton("Refresh"); refreshR.addActionListener(e -> loadRooms());
        rooms.add(refreshR, BorderLayout.SOUTH);
        tabs.add("Rooms", rooms);

        // Bookings
        JPanel bpanel = new JPanel(new BorderLayout());
        bookingModel = new DefaultTableModel(new Object[]{"BookingID","Customer","Room","CheckIn","CheckOut","Amount","Status"},0);
        JTable bTable = new JTable(bookingModel);
        bpanel.add(new JScrollPane(bTable), BorderLayout.CENTER);

        JPanel actions = new JPanel();
        JButton newBooking = new JButton("New Booking"); newBooking.addActionListener(e -> createBookingDialog());
        JButton checkout = new JButton("Checkout"); checkout.addActionListener(e -> doCheckout(bTable));
        JButton refreshB = new JButton("Refresh"); refreshB.addActionListener(e -> loadBookings());
        actions.add(newBooking); actions.add(checkout); actions.add(refreshB);
        bpanel.add(actions, BorderLayout.SOUTH);
        tabs.add("Bookings", bpanel);

        add(tabs, BorderLayout.CENTER);
    }

    private void loadRooms() {
        roomModel.setRowCount(0);
        String sql = "SELECT room_id, room_type, price, status FROM room ORDER BY room_id";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                roomModel.addRow(new Object[]{
                        rs.getInt("room_id"), rs.getString("room_type"), rs.getBigDecimal("price"), rs.getString("status")
                });
            }
        } catch (SQLException ex) { ex.printStackTrace(); JOptionPane.showMessageDialog(this, ex.getMessage()); }
    }

    private void loadBookings() {
        bookingModel.setRowCount(0);
        String sql = "SELECT b.booking_id, c.name, b.room_id, b.checkin_date, b.checkout_date, b.total_amount, b.status " +
                "FROM booking b JOIN customer c ON b.customer_id = c.customer_id ORDER BY b.created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                bookingModel.addRow(new Object[]{
                        rs.getInt("booking_id"), rs.getString("name"), rs.getInt("room_id"),
                        rs.getDate("checkin_date"), rs.getDate("checkout_date"), rs.getBigDecimal("total_amount"),
                        rs.getString("status")
                });
            }
        } catch (SQLException ex) { ex.printStackTrace(); JOptionPane.showMessageDialog(this, ex.getMessage()); }
    }

    private void createBookingDialog() {
        JTextField cname = new JTextField();
        JTextField contact = new JTextField();
        JComboBox<String> rtype = new JComboBox<>(new String[]{"Single","Double","Deluxe"});
        JComboBox<Integer> rlist = new JComboBox<>();
        JTextField checkin = new JTextField("yyyy-mm-dd");
        JTextField checkout = new JTextField("yyyy-mm-dd");

        rtype.addActionListener(e -> loadAvailableRoomsByType((String) rtype.getSelectedItem(), rlist));
        loadAvailableRoomsByType("Single", rlist);

        Object[] fields = {"Customer Name:", cname, "Contact:", contact, "Room Type:", rtype, "Available Rooms:", rlist, "Check-in (yyyy-mm-dd):", checkin, "Check-out (yyyy-mm-dd):", checkout};
        if (JOptionPane.showConfirmDialog(this, fields, "New Booking", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                String name = cname.getText().trim(); String cont = contact.getText().trim();
                int roomId = (Integer) rlist.getSelectedItem();
                LocalDate in = LocalDate.parse(checkin.getText().trim());
                LocalDate out = LocalDate.parse(checkout.getText().trim());
                if (!in.isBefore(out)) { JOptionPane.showMessageDialog(this, "Check-in before check-out."); return; }
                try (Connection conn = DBConnection.getConnection()) {
                    conn.setAutoCommit(false);
                    // create customer
                    int custId;
                    try (PreparedStatement pc = conn.prepareStatement("INSERT INTO customer (name, contact) VALUES (?,?)", Statement.RETURN_GENERATED_KEYS)) {
                        pc.setString(1, name); pc.setString(2, cont); pc.executeUpdate();
                        try (ResultSet gk = pc.getGeneratedKeys()) { gk.next(); custId = gk.getInt(1); }
                    }
                    // overlap check
                    String overlap = "SELECT COUNT(*) AS cnt FROM booking WHERE room_id = ? AND status IN ('Booked','CheckedIn') AND NOT (checkout_date <= ? OR checkin_date >= ?)";
                    try (PreparedStatement pov = conn.prepareStatement(overlap)) {
                        pov.setInt(1, roomId); pov.setDate(2, java.sql.Date.valueOf(in)); pov.setDate(3, java.sql.Date.valueOf(out));
                        try (ResultSet r = pov.executeQuery()) { r.next(); if (r.getInt("cnt") > 0) { conn.rollback(); JOptionPane.showMessageDialog(this, "Room already booked for overlapping dates."); return; } }
                    }
                    // price and insert booking
                    double price;
                    try (PreparedStatement pr = conn.prepareStatement("SELECT price FROM room WHERE room_id = ?")) { pr.setInt(1, roomId); try (ResultSet rr = pr.executeQuery()) { rr.next(); price = rr.getDouble(1); } }
                    long nights = java.time.temporal.ChronoUnit.DAYS.between(in, out);
                    double total = price * nights;
                    try (PreparedStatement pb = conn.prepareStatement("INSERT INTO booking (customer_id, room_id, checkin_date, checkout_date, total_amount, status) VALUES (?,?,?,?,?,'Booked')")) {
                        pb.setInt(1, custId); pb.setInt(2, roomId); pb.setDate(3, java.sql.Date.valueOf(in)); pb.setDate(4, java.sql.Date.valueOf(out)); pb.setDouble(5, total); pb.executeUpdate();
                    }
                    try (PreparedStatement pr2 = conn.prepareStatement("UPDATE room SET status='Booked' WHERE room_id = ?")) { pr2.setInt(1, roomId); pr2.executeUpdate(); }
                    conn.commit();
                    JOptionPane.showMessageDialog(this, "Booking done. Total: " + total);
                    loadRooms(); loadBookings();
                }
            } catch (Exception ex) { ex.printStackTrace(); JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
        }
    }

    private void loadAvailableRoomsByType(String type, JComboBox<Integer> target) {
        target.removeAllItems();
        String sql = "SELECT room_id FROM room WHERE room_type = ? AND status = 'Available' ORDER BY room_id";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, type);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) target.addItem(rs.getInt("room_id"));
            }
        } catch (SQLException ex) { ex.printStackTrace(); }
    }

    private void doCheckout(JTable table) {
        int r = table.getSelectedRow();
        if (r < 0) { JOptionPane.showMessageDialog(this, "Select booking row."); return;}
        int bookingId = (int) table.getValueAt(r, 0);
        int roomId = (int) table.getValueAt(r, 2);
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement("UPDATE booking SET status='CheckedOut' WHERE booking_id = ?")) { ps.setInt(1, bookingId); ps.executeUpdate(); }
            try (PreparedStatement pr = conn.prepareStatement("UPDATE room SET status='Available' WHERE room_id = ?")) { pr.setInt(1, roomId); pr.executeUpdate(); }
            conn.commit();
            JOptionPane.showMessageDialog(this, "Checked out booking " + bookingId);
            loadRooms(); loadBookings();
        } catch (SQLException ex) { ex.printStackTrace(); JOptionPane.showMessageDialog(this, ex.getMessage()); }
    }
}
