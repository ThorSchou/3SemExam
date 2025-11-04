package app.controllers;

import app.config.HibernateConfig;
import app.daos.CandidateDAO;
import app.daos.SkillDAO;
import app.dtos.*;
import app.entities.Candidate;
import app.entities.Skill;
import app.entities.SkillCategory;
import app.entities.CandidateSkill;
import app.exceptions.ApiException;
import app.services.SkillStatsService;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class CandidateController {

    private static final Logger logger = LoggerFactory.getLogger(CandidateController.class);

    private final CandidateDAO candidateDAO = new CandidateDAO(HibernateConfig.getEntityManagerFactory());
    private final SkillDAO skillDAO = new SkillDAO(HibernateConfig.getEntityManagerFactory());
    private final SkillStatsService skillStatsService = new SkillStatsService();

    public void getAll(Context ctx) {
        String categoryParam = ctx.queryParam("category");
        logger.debug("GET /candidates?category={}", categoryParam);

        List<Candidate> candidates;
        if (categoryParam != null && !categoryParam.isBlank()) {
            SkillCategory category;
            try {
                category = SkillCategory.valueOf(categoryParam.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new ApiException(400, "Unknown category: " + categoryParam);
            }
            candidates = candidateDAO.findBySkillCategory(category);
        } else {
            candidates = candidateDAO.findAll();
        }

        List<CandidateDTO> out = candidates.stream()
                .map(CandidateDTO::new)
                .toList();

        ctx.status(HttpStatus.OK).json(out);
    }

    public void getById(Context ctx) {
        Long id = Long.parseLong(ctx.pathParam("id"));
        logger.debug("GET /candidates/{} - fetching candidate", id);

        Candidate c = candidateDAO.find(id);

        List<Skill> skills = new ArrayList<>();
        for (CandidateSkill cs : c.getCandidateSkills()) {
            if (cs.getSkill() != null) skills.add(cs.getSkill());
        }

        List<String> slugs = skills.stream()
                .map(Skill::getSlug)
                .filter(Objects::nonNull)
                .toList();

        Map<String, SkillStatDTO> stats = slugs.isEmpty()
                ? Collections.emptyMap()
                : skillStatsService.fetchStatsForSlugs(slugs);

        List<SkillEnrichedDTO> enriched = new ArrayList<>();
        for (Skill s : skills) {
            SkillStatDTO stat = stats.get(s.getSlug());
            SkillEnrichedDTO dto = SkillEnrichedDTO.builder()
                    .id(s.getId())
                    .name(s.getName())
                    .slug(s.getSlug())
                    .description(s.getDescription())
                    .popularityScore(stat != null ? stat.getPopularityScore() : null)
                    .averageSalary(stat != null ? stat.getAverageSalary() : null)
                    .build();
            enriched.add(dto);
        }

        CandidateDetailsDTO out = new CandidateDetailsDTO(c, enriched);
        ctx.status(HttpStatus.OK).json(out);
    }

    public void create(Context ctx) {
        logger.debug("POST /candidates - creating candidate");
        CandidateRequestDTO in = ctx.bodyAsClass(CandidateRequestDTO.class);
        Candidate created = candidateDAO.create(in.toEntity());
        ctx.status(HttpStatus.CREATED).json(new CandidateDTO(created));
    }

    public void update(Context ctx) {
        Long id = Long.parseLong(ctx.pathParam("id"));
        logger.debug("PUT /candidates/{} - updating candidate", id);
        CandidateRequestDTO in = ctx.bodyAsClass(CandidateRequestDTO.class);
        Candidate updated = candidateDAO.update(id, in.toEntity());
        ctx.status(HttpStatus.OK).json(new CandidateDTO(updated));
    }

    public void delete(Context ctx) {
        Long id = Long.parseLong(ctx.pathParam("id"));
        logger.debug("DELETE /candidates/{} - deleting candidate", id);
        candidateDAO.delete(id);
        ctx.status(HttpStatus.NO_CONTENT);
    }

    public void assignSkill(Context ctx) {
        Long candidateId = Long.parseLong(ctx.pathParam("candidateId"));
        Long skillId = Long.parseLong(ctx.pathParam("skillId"));
        logger.debug("PUT /candidates/{}/skills/{} - assigning skill", candidateId, skillId);

        Skill skill = skillDAO.find(skillId);
        if (skill == null) throw new EntityNotFoundException("Skill not found");

        Candidate c = candidateDAO.assignSkill(candidateId, skillId);
        ctx.status(HttpStatus.OK).json(new CandidateDTO(c));
    }
}
