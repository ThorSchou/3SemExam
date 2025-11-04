package app.dtos;

import app.entities.Candidate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CandidateDTO {
    private Long id;
    private String name;
    private String phone;
    private String education;

    public CandidateDTO(Candidate c) {
        this.id = c.getId();
        this.name = c.getName();
        this.phone = c.getPhone();
        this.education = c.getEducation();
    }
}
