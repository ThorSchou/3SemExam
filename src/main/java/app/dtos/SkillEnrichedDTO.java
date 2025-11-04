package app.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SkillEnrichedDTO {
    private Long id;
    private String name;
    private String slug;
    private String description;
    private Integer popularityScore;
    private Integer averageSalary;
}
