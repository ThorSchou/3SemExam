package app.dtos;

import app.entities.Skill;
import app.entities.SkillCategory;
import lombok.Data;

@Data
public class SkillRequestDTO {
    private String name;
    private String slug;
    private SkillCategory category;
    private String description;

    public Skill toEntity() {
        return Skill.builder()
                .name(name)
                .slug(slug)
                .category(category)
                .description(description)
                .build();
    }
}
