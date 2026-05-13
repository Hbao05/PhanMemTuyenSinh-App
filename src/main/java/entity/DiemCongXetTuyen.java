package entity;

import jakarta.persistence.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "xt_diemcongxetuyen")
public class DiemCongXetTuyen {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "iddiemcong")
    private int idDiemCong;

    @Column(name = "ts_cccd", nullable = false)
    private String cccd;

    @Column(name = "manganh")
    private String maNganh;

    @Column(name = "matohop")
    private String maToHop;

    @Column(name = "phuongthuc")
    private String phuongThuc;

    @Column(name = "diemCC")
    private Double diemCc;

    @Column(name = "diemUtxt")
    private Double diemUtXt;

    @Column(name = "diemTong")
    private Double diemTong;

    @Column(name = "ghichu", columnDefinition = "TEXT")
    private String ghiChu;

    @Column(name = "dc_keys", unique = true, nullable = false)
    private String dcKeys;

    // ── Quan hệ: DiemCongXetTuyen N-1 ThiSinh (theo ts_cccd → cccd) ──
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ts_cccd", referencedColumnName = "cccd", insertable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private ThiSinh thiSinh;

    // ── Quan hệ: DiemCongXetTuyen N-1 Nganh (theo manganh) ──
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manganh", referencedColumnName = "manganh", insertable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Nganh nganh;
}
