package app.dtos;

import app.entities.Candidate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CandidateDetailsDTO {
    private Long id;
    private String name;
    private String phone;
    private String education;
    private List<SkillEnrichedDTO> skills;

    public CandidateDetailsDTO(Candidate c, List<SkillEnrichedDTO> skills) {
        this.id = c.getId();
        this.name = c.getName();
        this.phone = c.getPhone();
        this.education = c.getEducation();
        this.skills = skills;
    }
}
