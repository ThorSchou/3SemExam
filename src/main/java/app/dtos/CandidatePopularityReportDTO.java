package app.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CandidatePopularityReportDTO {
    private Long candidateId;
    private String candidateName;
    private double averagePopularityScore;
}
