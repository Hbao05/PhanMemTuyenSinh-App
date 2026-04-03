package entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "xt_nganh")
public class Nganh {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idnganh")
    private int idNganh;

    @Column(name = "manganh", nullable = false)
    private String maNganh;

    @Column(name = "tennganh", nullable = false)
    private String tenNganh;

    @Column(name = "n_tohopgoc")
    private String toHopGoc;

    @Column(name = "n_chitieu", nullable = false)
    private int chiTieu;

    @Column(name = "n_diemsan")
    private Double diemSan;

    @Column(name = "n_diemtrungtuyen")
    private Double diemTrungTuyen;

    @Column(name = "n_tuyenthang")
    private String tuyenThang;

    @Column(name = "n_dgnl")
    private String dgnl;

    @Column(name = "n_thpt")
    private String thpt;

    @Column(name = "n_vsat")
    private String vsat;

    @Column(name = "sl_xtt")
    private Integer slXtt;

    @Column(name = "sl_dgnl")
    private Integer slDgnl;

    @Column(name = "sl_vsat")
    private Integer slVsat;

    @Column(name = "sl_thpt")
    private String slThpt;
}
