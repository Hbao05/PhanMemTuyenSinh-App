package entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "xt_bangquydoi")

public class BangQuyDoi {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idqd")
    private int idQd;

    @Column(name = "d_phuongthuc")
    private String phuongThuc;

    @Column(name = "d_tohop")
    private String toHop;

    @Column(name = "d_mon")
    private String mon;

    @Column(name = "d_diema")
    private Double diemA;

    @Column(name = "d_diemb")
    private Double diemB;

    @Column(name = "d_diemc")
    private Double diemC;

    @Column(name = "d_diemd")
    private Double diemD;

    @Column(name = "d_maquydoi", unique = true)
    private String maQuyDoi;

    @Column(name = "d_phanvi")
    private String phanVi;
}
