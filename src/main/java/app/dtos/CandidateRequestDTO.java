package app.dtos;

import app.entities.Candidate;
import lombok.Data;

@Data
public class CandidateRequestDTO {
    private String name;
    private String phone;
    private String education;

    public Candidate toEntity() {
        return Candidate.builder()
                .name(name)
                .phone(phone)
                .education(education)
                .build();
    }
}
