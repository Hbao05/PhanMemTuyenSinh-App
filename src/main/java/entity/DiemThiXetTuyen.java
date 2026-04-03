package entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "xt_diemthixettuyen")
public class DiemThiXetTuyen {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "iddiemthi")
    private int idDiemThi;

    @Column(name = "cccd", unique = true, nullable = false)
    private String cccd;

    @Column(name = "sobaodanh")
    private String soBaoDanh;

    @Column(name = "d_phuongthuc")
    private String phuongThuc;

    @Column(name = "TO")
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
}
