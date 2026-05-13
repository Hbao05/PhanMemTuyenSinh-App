package entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "xt_nguoidung")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "password")
public class NguoiDung {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idnguoidung")
    private Integer id;

    @Column(name = "username", nullable = false, unique = true, length = 100)
    private String username;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "hoten", length = 200)
    private String hoTen;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "dien_thoai", length = 20)
    private String dienThoai;

    @Enumerated(EnumType.STRING)
    @Column(name = "quyen", nullable = false, length = 10)
    private Quyen quyen = Quyen.USER;

    @Column(name = "kich_hoat", nullable = false)
    private boolean kichHoat = true;

    public enum Quyen {
        ADMIN, USER
    }
}
