package app.controllers;

import app.config.HibernateConfig;
import app.daos.SkillDAO;
import app.dtos.SkillDTO;
import app.dtos.SkillRequestDTO;
import app.entities.Skill;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class SkillController {

    private static final Logger logger = LoggerFactory.getLogger(SkillController.class);

    private final SkillDAO skillDAO = new SkillDAO(HibernateConfig.getEntityManagerFactory());

    public void getAll(Context ctx) {
        logger.debug("GET /skills - fetching all skills");
        List<SkillDTO> out = skillDAO.findAll().stream().map(SkillDTO::new).toList();
        ctx.status(HttpStatus.OK).json(out);
    }

    public void getById(Context ctx) {
        Long id = Long.parseLong(ctx.pathParam("id"));
        logger.debug("GET /skills/{} - fetching skill", id);
        Skill s = skillDAO.find(id);
        ctx.status(HttpStatus.OK).json(new SkillDTO(s));
    }

    public void create(Context ctx) {
        logger.debug("POST /skills - creating skill");
        SkillRequestDTO in = ctx.bodyAsClass(SkillRequestDTO.class);
        Skill created = skillDAO.create(in.toEntity());
        ctx.status(HttpStatus.CREATED).json(new SkillDTO(created));
    }

    public void update(Context ctx) {
        Long id = Long.parseLong(ctx.pathParam("id"));
        logger.debug("PUT /skills/{} - updating skill", id);
        SkillRequestDTO in = ctx.bodyAsClass(SkillRequestDTO.class);
        Skill updated = skillDAO.update(id, in.toEntity());
        ctx.status(HttpStatus.OK).json(new SkillDTO(updated));
    }

    public void delete(Context ctx) {
        Long id = Long.parseLong(ctx.pathParam("id"));
        logger.debug("DELETE /skills/{} - deleting skill", id);
        skillDAO.delete(id);
        ctx.status(HttpStatus.NO_CONTENT);
    }
}
