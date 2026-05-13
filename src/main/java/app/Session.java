package app;

import entity.NguoiDung;

public class Session {
    private static NguoiDung currentUser;

    public static void login(NguoiDung user) {
        currentUser = user;
    }

    public static void logout() {
        currentUser = null;
    }

    public static NguoiDung getCurrentUser() {
        return currentUser;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static boolean isAdmin() {
        return currentUser != null && currentUser.getQuyen() == NguoiDung.Quyen.ADMIN;
    }

    public static String getDisplayName() {
        if (currentUser == null) return "";
        String name = currentUser.getHoTen();
        return (name != null && !name.isBlank()) ? name : currentUser.getUsername();
    }
}
