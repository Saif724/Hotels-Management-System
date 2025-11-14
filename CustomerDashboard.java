package com.hotel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;

public class CustomerDashboard extends JPanel {
    private JFrame parent;
    private DefaultTableModel roomModel, myBookingModel;

    public CustomerDashboard(JFrame parent) {
        this.parent = parent;
        initUI();
        loadRooms();
        loadMyBookings();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        JPanel top = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        top.add(new JLabel("Customer: " + CurrentUser.getUsername()));
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
        JPanel rActions = new JPanel();
        JButton book = new JButton("Book Selected"); book.addActionListener(e -> bookSelected(rTable));
        JButton refresh = new JButton("Refresh"); refresh.addActionListener(e -> loadRooms());
        rActions.add(book); rActions.add(refresh);
        rooms.add(rActions, BorderLayout.SOUTH);
        tabs.add("Rooms", rooms);

        // My Bookings
        JPanel mb = new JPanel(new BorderLayout());
        myBookingModel = new DefaultTableModel(new Object[]{"BookingID","Room","CheckIn","CheckOut","Amount","Status"},0);
        JTable mbTable = new JTable(myBookingModel);
        mb.add(new JScrollPane(mbTable), BorderLayout.CENTER);
        JPanel mbActions = new JPanel();
        JButton checkout = new JButton("Checkout Selected"); checkout.addActionListener(e -> doCheckout(mbTable));
        JButton refreshB = new JButton("Refresh"); refreshB.addActionListener(e -> loadMyBookings());
        mbActions.add(checkout); mbActions.add(refreshB);
        mb.add(mbActions, BorderLayout.SOUTH);
        tabs.add("My Bookings", mb);

        // Feedback
        JPanel fb = new JPanel(new BorderLayout());
        JButton leaveFB = new JButton("Leave Feedback"); leaveFB.addActionListener(e -> leaveFeedbackDialog());
        fb.add(leaveFB, BorderLayout.NORTH);
        tabs.add("Feedback", fb);

        add(tabs, BorderLayout.CENTER);
    }

    private void loadRooms() {
        roomModel.setRowCount(0);
        String sql = "SELECT room_id, room_type, price, status FROM room ORDER BY room_id";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) roomModel.addRow(new Object[]{rs.getInt("room_id"), rs.getString("room_type"), rs.getBigDecimal("price"), rs.getString("status")});
        } catch (SQLException ex) { ex.printStackTrace(); JOptionPane.showMessageDialog(this, ex.getMessage()); }
    }

    private void bookSelected(JTable table) {
        int r = table.getSelectedRow();
        if (r < 0) { JOptionPane.showMessageDialog(this, "Select a room."); return; }
        int roomId = (int) table.getValueAt(r, 0);
        if (!"Available".equals(table.getValueAt(r,3))) { JOptionPane.showMessageDialog(this, "Room not available."); return; }

        JTextField checkin = new JTextField("yyyy-mm-dd");
        JTextField checkout = new JTextField("yyyy-mm-dd");
        if (JOptionPane.showConfirmDialog(this, new Object[]{"Check-in:", checkin, "Check-out:", checkout}, "Book Room " + roomId, JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                LocalDate in = LocalDate.parse(checkin.getText().trim());
                LocalDate out = LocalDate.parse(checkout.getText().trim());
                if (!in.isBefore(out)) { JOptionPane.showMessageDialog(this, "Invalid dates."); return; }
                // get customer id from CurrentUser
                Integer custId = CurrentUser.getCustomerId();
                if (custId == null) {
                    JOptionPane.showMessageDialog(this, "Customer record not found for this user.");
                    return;
                }
                try (Connection conn = DBConnection.getConnection()) {
                    conn.setAutoCommit(false);
                    // overlap check
                    String overlap = "SELECT COUNT(*) AS cnt FROM booking WHERE room_id = ? AND status IN ('Booked','CheckedIn') AND NOT (checkout_date <= ? OR checkin_date >= ?)";
                    try (PreparedStatement pov = conn.prepareStatement(overlap)) {
                        pov.setInt(1, roomId); pov.setDate(2, java.sql.Date.valueOf(in)); pov.setDate(3, java.sql.Date.valueOf(out));
                        try (ResultSet rset = pov.executeQuery()) { rset.next(); if (rset.getInt("cnt") > 0) { conn.rollback(); JOptionPane.showMessageDialog(this, "Room already booked."); return; } }
                    }
                    // price
                    double price;
                    try (PreparedStatement pr = conn.prepareStatement("SELECT price FROM room WHERE room_id = ?")) { pr.setInt(1, roomId); try (ResultSet rr = pr.executeQuery()) { rr.next(); price = rr.getDouble(1); } }
                    long nights = java.time.temporal.ChronoUnit.DAYS.between(in, out);
                    double total = price * nights;
                    try (PreparedStatement pb = conn.prepareStatement("INSERT INTO booking (customer_id, room_id, checkin_date, checkout_date, total_amount, status) VALUES (?,?,?,?,?,'Booked')")) {
                        pb.setInt(1, custId); pb.setInt(2, roomId); pb.setDate(3, java.sql.Date.valueOf(in)); pb.setDate(4, java.sql.Date.valueOf(out)); pb.setDouble(5, total); pb.executeUpdate();
                    }
                    try (PreparedStatement pr2 = conn.prepareStatement("UPDATE room SET status='Booked' WHERE room_id = ?")) { pr2.setInt(1, roomId); pr2.executeUpdate(); }
                    conn.commit();
                    JOptionPane.showMessageDialog(this, "Booked! Total: " + total);
                    loadRooms(); loadMyBookings();
                }
            } catch (Exception ex) { ex.printStackTrace(); JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
        }
    }

    private void loadMyBookings() {
        myBookingModel.setRowCount(0);
        Integer cid = CurrentUser.getCustomerId();
        if (cid == null) return;
        String sql = "SELECT booking_id, room_id, checkin_date, checkout_date, total_amount, status FROM booking WHERE customer_id = ? ORDER BY created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, cid);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) myBookingModel.addRow(new Object[]{rs.getInt("booking_id"), rs.getInt("room_id"), rs.getDate("checkin_date"), rs.getDate("checkout_date"), rs.getBigDecimal("total_amount"), rs.getString("status")});
            }
        } catch (SQLException ex) { ex.printStackTrace(); JOptionPane.showMessageDialog(this, ex.getMessage()); }
    }

    private void doCheckout(JTable table) {
        int r = table.getSelectedRow();
        if (r < 0) { JOptionPane.showMessageDialog(this, "Select booking."); return; }
        int bookingId = (int) table.getValueAt(r, 0);
        int roomId = (int) table.getValueAt(r, 1);
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement("UPDATE booking SET status='CheckedOut' WHERE booking_id = ?")) { ps.setInt(1, bookingId); ps.executeUpdate(); }
            try (PreparedStatement pr = conn.prepareStatement("UPDATE room SET status='Available' WHERE room_id = ?")) { pr.setInt(1, roomId); pr.executeUpdate(); }
            conn.commit();
            JOptionPane.showMessageDialog(this, "Checked out booking " + bookingId);
            loadRooms(); loadMyBookings();
        } catch (SQLException ex) { ex.printStackTrace(); JOptionPane.showMessageDialog(this, ex.getMessage()); }
    }

    private void leaveFeedbackDialog() {
        JTextArea comments = new JTextArea(5,30);
        JComboBox<Integer> rating = new JComboBox<>(new Integer[]{1,2,3,4,5});
        Object[] fields = {"Rating:", rating, "Comments:", new JScrollPane(comments)};
        if (JOptionPane.showConfirmDialog(this, fields, "Leave Feedback", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            int r = (Integer) rating.getSelectedItem();
            String comm = comments.getText().trim();
            Integer cid = CurrentUser.getCustomerId();
            if (cid == null) { JOptionPane.showMessageDialog(this, "Customer not linked."); return; }
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement pf = conn.prepareStatement("INSERT INTO feedback (customer_id, comments, rating) VALUES (?,?,?)")) {
                pf.setInt(1, cid); pf.setString(2, comm); pf.setInt(3, r); pf.executeUpdate();
                JOptionPane.showMessageDialog(this, "Feedback submitted. Thank you!");
            } catch (SQLException ex) { ex.printStackTrace(); JOptionPane.showMessageDialog(this, ex.getMessage()); }
        }
    }
}
