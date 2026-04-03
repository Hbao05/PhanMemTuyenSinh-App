package entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "xt_nganh_tohop")
public class NganhToHop {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "manganh", nullable = false)
    private String maNganh;

    @Column(name = "matohop", nullable = false)
    private String maToHop;

    @Column(name = "th_mon1")
    private String thMon1;

    @Column(name = "hsmon1")
    private Integer hsMon1;

    @Column(name = "th_mon2")
    private String thMon2;

    @Column(name = "hsmon2")
    private Integer hsMon2;

    @Column(name = "th_mon3")
    private String thMon3;

    @Column(name = "hsmon3")
    private Integer hsMon3;

    @Column(name = "tb_keys", unique = true)
    private String tbKeys;

    @Column(name = "N1")
    private Integer n1;

    @Column(name = "TO")
    private Integer to;

    @Column(name = "LI")
    private Integer li;

    @Column(name = "HO")
    private Integer ho;

    @Column(name = "SI")
    private Integer si;

    @Column(name = "VA")
    private Integer va;

    @Column(name = "SU")
    private Integer su;

    @Column(name = "DI")
    private Integer di;

    @Column(name = "TI")
    private Integer ti;

    @Column(name = "KHAC")
    private Integer khac;

    @Column(name = "KTPL")
    private Integer ktpl;

    @Column(name = "dolech")
    private Double doLech;
}
