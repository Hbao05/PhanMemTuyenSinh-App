package entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "xt_tohop_monthi")
public class ToHopMonThi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idtohop")
    private int idToHop;

    @Column(name = "matohop", unique = true, nullable = false)
    private String maToHop;

    @Column(name = "mon1", nullable = false)
    private String mon1;

    @Column(name = "mon2", nullable = false)
    private String mon2;

    @Column(name = "mon3", nullable = false)
    private String mon3;

    @Column(name = "tentohop")
    private String tenToHop;

    // ── Quan hệ: ToHopMonThi 1-N NganhToHop (theo matohop) ──
    @OneToMany(mappedBy = "toHopMonThi", fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<NganhToHop> danhSachNganhToHop;
}