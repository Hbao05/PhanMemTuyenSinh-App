package entity;

import jakarta.persistence.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "xt_diemthixettuyen", uniqueConstraints = @UniqueConstraint(name = "uq_cccd_phuongthuc", columnNames = {"cccd", "d_phuongthuc"}))
public class DiemThiXetTuyen {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "iddiemthi")
    private int idDiemThi;

    @Column(name = "cccd", nullable = false)
    private String cccd;

    @Column(name = "sobaodanh")
    private String soBaoDanh;

    @Column(name = "d_phuongthuc")
    private String phuongThuc;

    @Column(name = "`TO`")
    private Double diemToan;

    @Column(name = "LI")
    private Double diemLy;

    @Column(name = "HO")
    private Double diemHoa;

    @Column(name = "SI")
    private Double diemSinh;

    @Column(name = "SU")
    private Double diemSu;

    @Column(name = "DI")
    private Double diemDia;

    @Column(name = "GDCD")
    private Double diemGdcd;

    @Column(name = "VA")
    private Double diemVan;

    @Column(name = "N1_THI")
    private Double n1Thi;

    @Column(name = "N1_CC")
    private Double n1Cc;

    @Column(name = "CNCN")
    private Double cncn;

    @Column(name = "CNNN")
    private Double cnnn;

    @Column(name = "TI")
    private Double diemTiengAnh;

    @Column(name = "KTPL")
    private Double diemKtpl;

    @Column(name = "NL1")
    private Double nl1;

    @Column(name = "NK1")
    private Double nk1;

    @Column(name = "NK2")
    private Double nk2;

    @Column(name = "NK3")
    private Double nk3;

    @Column(name = "NK4")
    private Double nk4;

    @Column(name = "NK5")
    private Double nk5;

    @Column(name = "NK6")
    private Double nk6;

    @Column(name = "NK7")
    private Double nk7;

    @Column(name = "NK8")
    private Double nk8;

    @Column(name = "NK9")
    private Double nk9;

    @Column(name = "NK10")
    private Double nk10;

    // ── Quan hệ: DiemThiXetTuyen N-1 ThiSinh (theo cccd) ──
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cccd", referencedColumnName = "cccd", insertable = false, updatable = false)
    private ThiSinh thiSinh;
}
