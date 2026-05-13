package entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="xt_thisinhxettuyen25")
public class ThiSinh {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idthisinh")
    private int idThiSinh;

    @Column(name = "cccd", unique = true)
    private String cccd;

    @Column(name = "sobaodanh")
    private String soBaoDanh;

    @Column(name = "ho")
    private String ho;

    @Column(name = "ten")
    private String ten;

    @Column(name = "ngay_sinh")
    private String ngaySinh;

    @Column(name = "dien_thoai")
    private String dienThoai;

    @Column(name = "password")
    private String password;

    @Column(name = "gioi_tinh")
    private String gioiTinh;

    @Column(name = "email")
    private String email;

    @Column(name = "noi_sinh")
    private String noiSinh;

    @Column(name = "updated_at")
    private LocalDate updatedAt;

    @Column(name = "doi_tuong")
    private String doiTuong;

    @Column(name = "khu_vuc")
    private String khuVuc;

    @PrePersist
    private void prePersist() {
        updatedAt = LocalDate.now();
    }

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "nn_cccd", referencedColumnName = "cccd", insertable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<NguyenVongXetTuyen> danhSachNguyenVong;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "ts_cccd", referencedColumnName = "cccd", insertable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<DiemCongXetTuyen> danhSachDiemCong;
}
