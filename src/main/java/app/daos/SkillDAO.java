package app.daos;

import app.entities.Skill;
import app.exceptions.DatabaseException;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class SkillDAO {

    private static final Logger logger = LoggerFactory.getLogger(SkillDAO.class);

    private final EntityManagerFactory emf;

    public SkillDAO(EntityManagerFactory emf) {
        this.emf = emf;
    }

    private EntityManager em() {
        return emf.createEntityManager();
    }

    public Skill create(Skill s) {
        try (EntityManager em = em()) {
            try {
                em.getTransaction().begin();
                em.persist(s);
                em.getTransaction().commit();
                return s;
            } catch (EntityExistsException e) {
                em.getTransaction().rollback();
                throw e;
            } catch (Exception e) {
                if (em.getTransaction().isActive()) em.getTransaction().rollback();
                logger.error("Error creating skill", e);
                throw new DatabaseException("Could not create skill due to database error", e);
            }
        }
    }

    public Skill find(Long id) {
        try (EntityManager em = em()) {
            try {
                Skill s = em.find(Skill.class, id);
                if (s == null) throw new EntityNotFoundException("Skill not found");
                return s;
            } catch (EntityNotFoundException e) {
                throw e;
            } catch (Exception e) {
                logger.error("Error finding skill with id {}", id, e);
                throw new DatabaseException("Could not fetch skill due to database error", e);
            }
        }
    }

    public List<Skill> findAll() {
        try (EntityManager em = em()) {
            try {
                return em.createQuery("select s from Skill s", Skill.class).getResultList();
            } catch (Exception e) {
                logger.error("Error fetching all skills", e);
                throw new DatabaseException("Could not fetch skills due to database error", e);
            }
        }
    }

    public Skill update(Long id, Skill data) {
        try (EntityManager em = em()) {
            try {
                Skill s = em.find(Skill.class, id);
                if (s == null) throw new EntityNotFoundException("Skill not found");
                em.getTransaction().begin();
                s.setName(data.getName());
                s.setSlug(data.getSlug());
                s.setCategory(data.getCategory());
                s.setDescription(data.getDescription());
                em.getTransaction().commit();
                return s;
            } catch (EntityNotFoundException e) {
                if (em.getTransaction().isActive()) em.getTransaction().rollback();
                throw e;
            } catch (Exception e) {
                if (em.getTransaction().isActive()) em.getTransaction().rollback();
                logger.error("Error updating skill with id {}", id, e);
                throw new DatabaseException("Could not update skill due to database error", e);
            }
        }
    }

    public void delete(Long id) {
        try (EntityManager em = em()) {
            try {
                Skill s = em.find(Skill.class, id);
                if (s == null) throw new EntityNotFoundException("Skill not found");
                em.getTransaction().begin();
                em.remove(s);
                em.getTransaction().commit();
            } catch (EntityNotFoundException e) {
                if (em.getTransaction().isActive()) em.getTransaction().rollback();
                throw e;
            } catch (Exception e) {
                if (em.getTransaction().isActive()) em.getTransaction().rollback();
                logger.error("Error deleting skill with id {}", id, e);
                throw new DatabaseException("Could not delete skill due to database error", e);
            }
        }
    }
}
