package com.hotel;

public class CurrentUser {
    private static String username;
    private static String role;
    private static Integer customerId; // optional linked customer id

    public static void set(String u, String r, Integer cid) {
        username = u; role = r; customerId = cid;
    }
    public static String getUsername() { return username; }
    public static String getRole() { return role; }
    public static Integer getCustomerId() { return customerId; }
    public static void clear() { username = null; role = null; customerId = null; }
}
