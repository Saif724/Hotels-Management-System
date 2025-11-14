package com.hotel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class AdminDashboard extends JPanel {
    private JFrame parent;
    private DefaultTableModel roomModel, empModel, bookingModel;

    public AdminDashboard(JFrame parent) {
        this.parent = parent;
        initUI();
        loadRooms();
        loadEmployees();
        loadBookings();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        JPanel top = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton logout = new JButton("Logout");
        logout.addActionListener(e -> {
            CurrentUser.clear();
            parent.setContentPane(new StartScreen(parent));
            parent.revalidate();
        });
        top.add(new JLabel("Admin: " + CurrentUser.getUsername()));
        top.add(logout);
        add(top, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();

        // Rooms Tab
        JPanel rooms = new JPanel(new BorderLayout());
        roomModel = new DefaultTableModel(new Object[]{"Room ID","Type","Price","Status"},0);
        JTable roomTable = new JTable(roomModel);
        rooms.add(new JScrollPane(roomTable), BorderLayout.CENTER);
        JPanel rActions = new JPanel();
        JButton refreshRooms = new JButton("Refresh"); refreshRooms.addActionListener(e -> loadRooms());
        JButton addRoom = new JButton("Add Room"); addRoom.addActionListener(e -> addRoomDialog());
        rActions.add(addRoom); rActions.add(refreshRooms);
        rooms.add(rActions, BorderLayout.SOUTH);
        tabs.add("Rooms", rooms);

        // Employees Tab
        JPanel emps = new JPanel(new BorderLayout());
        empModel = new DefaultTableModel(new Object[]{"Emp ID","Name","Salary","Paid","Username"},0);
        JTable empTable = new JTable(empModel);
        emps.add(new JScrollPane(empTable), BorderLayout.CENTER);
        JPanel eActions = new JPanel();
        JButton addEmp = new JButton("Add Employee");
        addEmp.addActionListener(e -> addEmployeeDialog());
        JButton markPaid = new JButton("Mark Paid");
        markPaid.addActionListener(e -> {
            int r = empTable.getSelectedRow();
            if (r < 0) { JOptionPane.showMessageDialog(this, "Select employee row."); return;}
            int empId = (int) empModel.getValueAt(r,0);
            markEmployeePaid(empId);
        });
        JButton refreshEmp = new JButton("Refresh"); refreshEmp.addActionListener(e -> loadEmployees());
        eActions.add(addEmp); eActions.add(markPaid); eActions.add(refreshEmp);
        emps.add(eActions, BorderLayout.SOUTH);
        tabs.add("Employees", emps);

        // Bookings Tab
        JPanel books = new JPanel(new BorderLayout());
        bookingModel = new DefaultTableModel(new Object[]{"Booking ID","Customer","Room","CheckIn","CheckOut","Amount","Status"},0);
        JTable bTable = new JTable(bookingModel);
        books.add(new JScrollPane(bTable), BorderLayout.CENTER);
        JPanel bActions = new JPanel();
        JButton refreshB = new JButton("Refresh"); refreshB.addActionListener(e -> loadBookings());
        JButton earnings = new JButton("Earnings Report"); earnings.addActionListener(e -> showEarnings());
        bActions.add(refreshB); bActions.add(earnings);
        books.add(bActions, BorderLayout.SOUTH);
        tabs.add("Bookings", books);

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
                        rs.getInt("room_id"),
                        rs.getString("room_type"),
                        rs.getBigDecimal("price"),
                        rs.getString("status")
                });
            }
        } catch (SQLException ex) { ex.printStackTrace(); JOptionPane.showMessageDialog(this, ex.getMessage()); }
    }

    private void addRoomDialog() {
        JTextField idF = new JTextField();
        JComboBox<String> type = new JComboBox<>(new String[]{"Single","Double","Deluxe"});
        JTextField priceF = new JTextField();
        Object[] fields = {"Room ID:", idF, "Type:", type, "Price:", priceF};
        if (JOptionPane.showConfirmDialog(this, fields, "Add Room", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try (Connection conn = DBConnection.getConnection()) {
                String sql = "INSERT INTO room (room_id, room_type, price, status) VALUES (?,?,?, 'Available')";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, Integer.parseInt(idF.getText().trim()));
                    ps.setString(2, (String) type.getSelectedItem());
                    ps.setDouble(3, Double.parseDouble(priceF.getText().trim()));
                    ps.executeUpdate();
                    JOptionPane.showMessageDialog(this, "Room added.");
                    loadRooms();
                }
            } catch (SQLException ex) { ex.printStackTrace(); JOptionPane.showMessageDialog(this, ex.getMessage()); }
        }
    }

    private void loadEmployees() {
        empModel.setRowCount(0);
        String sql = "SELECT emp_id, name, salary, paid_status, username FROM employee ORDER BY emp_id";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                empModel.addRow(new Object[]{
                        rs.getInt("emp_id"),
                        rs.getString("name"),
                        rs.getBigDecimal("salary"),
                        rs.getString("paid_status"),
                        rs.getString("username")
                });
            }
        } catch (SQLException ex) { ex.printStackTrace(); JOptionPane.showMessageDialog(this, ex.getMessage()); }
    }

    private void addEmployeeDialog() {
        JTextField nameF = new JTextField();
        JTextField salaryF = new JTextField();
        JTextField usernameF = new JTextField();
        JTextField passwordF = new JTextField();
        Object[] fields = {"Name:", nameF, "Salary:", salaryF, "Username (login):", usernameF, "Password:", passwordF};
        if (JOptionPane.showConfirmDialog(this, fields, "Add Employee", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            String name = nameF.getText().trim(), usr = usernameF.getText().trim(), pwd = passwordF.getText().trim();
            double sal;
            try { sal = Double.parseDouble(salaryF.getText().trim()); } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Invalid salary"); return;}
            try (Connection conn = DBConnection.getConnection()) {
                conn.setAutoCommit(false);
                // create login
                try (PreparedStatement pl = conn.prepareStatement("INSERT INTO login (username, password, role) VALUES (?,?, 'EMPLOYEE')")) {
                    pl.setString(1, usr);
                    pl.setString(2, pwd);
                    pl.executeUpdate();
                }
                // create employee
                try (PreparedStatement pe = conn.prepareStatement("INSERT INTO employee (name, salary, paid_status, username) VALUES (?,?, 'Unpaid', ?)")) {
                    pe.setString(1, name);
                    pe.setDouble(2, sal);
                    pe.setString(3, usr);
                    pe.executeUpdate();
                }
                conn.commit();
                JOptionPane.showMessageDialog(this, "Employee and login created.");
                loadEmployees();
            } catch (SQLException ex) { ex.printStackTrace(); JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
        }
    }

    private void markEmployeePaid(int empId) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE employee SET paid_status='Paid' WHERE emp_id = ?")) {
            ps.setInt(1, empId); ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Marked Paid.");
            loadEmployees();
        } catch (SQLException ex) { ex.printStackTrace(); JOptionPane.showMessageDialog(this, ex.getMessage()); }
    }

    private void loadBookings() {
        bookingModel.setRowCount(0);
        String sql = "SELECT b.booking_id, c.name as customer, b.room_id, b.checkin_date, b.checkout_date, b.total_amount, b.status " +
                "FROM booking b JOIN customer c ON b.customer_id = c.customer_id ORDER BY b.created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                bookingModel.addRow(new Object[]{
                        rs.getInt("booking_id"),
                        rs.getString("customer"),
                        rs.getInt("room_id"),
                        rs.getDate("checkin_date"),
                        rs.getDate("checkout_date"),
                        rs.getBigDecimal("total_amount"),
                        rs.getString("status")
                });
            }
        } catch (SQLException ex) { ex.printStackTrace(); JOptionPane.showMessageDialog(this, ex.getMessage()); }
    }

    private void showEarnings() {
        String incomeSql = "SELECT IFNULL(SUM(total_amount),0) as total_income FROM booking WHERE status IN ('Booked','CheckedIn','CheckedOut')";
        String salarySql = "SELECT IFNULL(SUM(salary),0) as total_salary FROM employee WHERE paid_status = 'Paid'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pi = conn.prepareStatement(incomeSql);
             PreparedStatement ps = conn.prepareStatement(salarySql);
             ResultSet ri = pi.executeQuery(); ResultSet rs2 = ps.executeQuery()) {
            ri.next(); double income = ri.getDouble("total_income");
            rs2.next(); double salary = rs2.getDouble("total_salary");
            double balance = income - salary;
            JOptionPane.showMessageDialog(this, String.format("Total Income: %.2f\nTotal Paid Salaries: %.2f\nBalance: %.2f", income, salary, balance));
        } catch (SQLException ex) { ex.printStackTrace(); JOptionPane.showMessageDialog(this, ex.getMessage()); }
    }
}
