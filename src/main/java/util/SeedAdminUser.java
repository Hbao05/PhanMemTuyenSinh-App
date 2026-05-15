package util;

import bus.NguoiDungBUS;
import entity.NguoiDung;

/**
 * Chạy 1 lần để tạo tài khoản admin mặc định.
 * Username: admin / Password: admin123
 */
public class SeedAdminUser {
    public static void main(String[] args) {
        NguoiDungBUS bus = new NguoiDungBUS();

        NguoiDung nd = new NguoiDung();
        nd.setUsername("admin");
        nd.setHoTen("Quản trị viên");
        nd.setEmail("admin@tuyensinh.edu.vn");
        nd.setQuyen(NguoiDung.Quyen.ADMIN);
        nd.setKichHoat(true);

        String result = bus.addUser(nd, "admin123");
        System.out.println(result.startsWith("Success")
                ? "Tạo admin thành công! Username: admin | Password: admin123"
                : result);

        System.exit(0);
    }
}
