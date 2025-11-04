package app.dtos;

import lombok.Data;

import java.util.List;

@Data
public class SkillStatResponseDTO {
    private List<SkillStatDTO> data;
}
