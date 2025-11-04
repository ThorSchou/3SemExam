package app.dtos;

import app.entities.Skill;
import app.entities.SkillCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SkillDTO {
    private Long id;
    private String name;
    private String slug;
    private SkillCategory category;
    private String description;

    public SkillDTO(Skill s) {
        this.id = s.getId();
        this.name = s.getName();
        this.slug = s.getSlug();
        this.category = s.getCategory();
        this.description = s.getDescription();
    }
}
