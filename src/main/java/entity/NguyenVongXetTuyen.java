package entity;

import jakarta.persistence.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "xt_nguyenvongxettuyen")
public class NguyenVongXetTuyen {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idnv")
    private int idNv;

    @Column(name = "nn_cccd", nullable = false)
    private String cccd;

    @Column(name = "nv_manganh", nullable = false)
    private String maNganh;

    @Column(name = "nv_tt", nullable = false)
    private int thuTuNguyenVong;

    @Column(name = "diem_thxt")
    private Double diemThxt;

    @Column(name = "diem_utqd")
    private Double diemUtqd;

    @Column(name = "diem_cong")
    private Double diemCong;

    @Column(name = "diem_xettuyen")
    private Double diemXetTuyen;

    @Column(name = "nv_ketqua")
    private String ketQua;

    @Column(name = "nv_keys", unique = true)
    private String nvKeys;

    @Column(name = "tt_phuongthuc")
    private String phuongThuc;

    @Column(name = "tt_thm")
    private String toHopMon;

    // ── Quan hệ: NguyenVongXetTuyen N-1 ThiSinh (theo nn_cccd → cccd) ──
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nn_cccd", referencedColumnName = "cccd", insertable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private ThiSinh thiSinh;

    // ── Quan hệ: NguyenVongXetTuyen N-1 Nganh (theo nv_manganh → manganh) ──
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nv_manganh", referencedColumnName = "manganh", insertable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Nganh nganh;
}
