package app.entities;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "candidates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Candidate implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 25)
    private String phone;

    @Column(nullable = false, length = 255)
    private String education;

    @OneToMany(mappedBy = "candidate", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<CandidateSkill> candidateSkills = new HashSet<>();

    public void addSkill(Skill skill) {
        if (skill == null) return;
        CandidateSkill cs = new CandidateSkill();
        cs.setCandidate(this);
        cs.setSkill(skill);
        candidateSkills.add(cs);
        skill.getCandidateSkills().add(cs);
    }
}
