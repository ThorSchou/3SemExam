package app.daos;

import app.dtos.CandidatePopularityReportDTO;
import app.entities.Candidate;
import app.entities.CandidateSkill;
import app.entities.Skill;
import app.entities.SkillCategory;
import app.exceptions.DatabaseException;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class CandidateDAO {

    private static final Logger logger = LoggerFactory.getLogger(CandidateDAO.class);

    private final EntityManagerFactory emf;

    public CandidateDAO(EntityManagerFactory emf) {
        this.emf = emf;
    }

    private EntityManager em() {
        return emf.createEntityManager();
    }

    public Candidate create(Candidate c) {
        try (EntityManager em = em()) {
            try {
                em.getTransaction().begin();
                em.persist(c);
                em.getTransaction().commit();
                return c;
            } catch (EntityExistsException e) {
                em.getTransaction().rollback();
                throw e;
            } catch (Exception e) {
                if (em.getTransaction().isActive()) em.getTransaction().rollback();
                logger.error("Error creating candidate", e);
                throw new DatabaseException("Could not create candidate due to database error", e);
            }
        }
    }

    public Candidate find(Long id) {
        try (EntityManager em = em()) {
            try {
                Candidate c = em.createQuery(
                                "select distinct c from Candidate c " +
                                        "left join fetch c.candidateSkills cs " +
                                        "left join fetch cs.skill " +
                                        "where c.id = :id", Candidate.class)
                        .setParameter("id", id)
                        .getSingleResult();
                return c;
            } catch (jakarta.persistence.NoResultException e) {
                throw new EntityNotFoundException("Candidate not found");
            } catch (EntityNotFoundException e) {
                throw e;
            } catch (Exception e) {
                logger.error("Error finding candidate with id {}", id, e);
                throw new DatabaseException("Could not fetch candidate due to database error", e);
            }
        }
    }

    public List<Candidate> findAll() {
        try (EntityManager em = em()) {
            try {
                return em.createQuery(
                                "select distinct c from Candidate c " +
                                        "left join fetch c.candidateSkills cs " +
                                        "left join fetch cs.skill",
                                Candidate.class)
                        .getResultList();
            } catch (Exception e) {
                logger.error("Error fetching all candidates", e);
                throw new DatabaseException("Could not fetch candidates due to database error", e);
            }
        }
    }

    public List<Candidate> findBySkillCategory(SkillCategory category) {
        try (EntityManager em = em()) {
            try {
                return em.createQuery(
                                "select distinct c from Candidate c " +
                                        "join c.candidateSkills cs " +
                                        "join cs.skill s " +
                                        "where s.category = :cat",
                                Candidate.class)
                        .setParameter("cat", category)
                        .getResultList();
            } catch (Exception e) {
                logger.error("Error fetching candidates by category {}", category, e);
                throw new DatabaseException("Could not fetch candidates by category due to database error", e);
            }
        }
    }

    public Candidate update(Long id, Candidate data) {
        try (EntityManager em = em()) {
            try {
                Candidate c = em.find(Candidate.class, id);
                if (c == null) throw new EntityNotFoundException("Candidate not found");
                em.getTransaction().begin();
                c.setName(data.getName());
                c.setPhone(data.getPhone());
                c.setEducation(data.getEducation());
                em.getTransaction().commit();
                return c;
            } catch (EntityNotFoundException e) {
                if (em.getTransaction().isActive()) em.getTransaction().rollback();
                throw e;
            } catch (Exception e) {
                if (em.getTransaction().isActive()) em.getTransaction().rollback();
                logger.error("Error updating candidate with id {}", id, e);
                throw new DatabaseException("Could not update candidate due to database error", e);
            }
        }
    }

    public void delete(Long id) {
        try (EntityManager em = em()) {
            try {
                Candidate c = em.find(Candidate.class, id);
                if (c == null) throw new EntityNotFoundException("Candidate not found");
                em.getTransaction().begin();
                em.remove(c);
                em.getTransaction().commit();
            } catch (EntityNotFoundException e) {
                if (em.getTransaction().isActive()) em.getTransaction().rollback();
                throw e;
            } catch (Exception e) {
                if (em.getTransaction().isActive()) em.getTransaction().rollback();
                logger.error("Error deleting candidate with id {}", id, e);
                throw new DatabaseException("Could not delete candidate due to database error", e);
            }
        }
    }

    public Candidate assignSkill(Long candidateId, Long skillId) {
        try (EntityManager em = em()) {
            try {
                Candidate c = em.find(Candidate.class, candidateId);
                if (c == null) throw new EntityNotFoundException("Candidate not found");
                Skill s = em.find(Skill.class, skillId);
                if (s == null) throw new EntityNotFoundException("Skill not found");

                em.getTransaction().begin();
                CandidateSkill cs = CandidateSkill.builder()
                        .candidate(c)
                        .skill(s)
                        .build();
                em.persist(cs);
                c.getCandidateSkills().add(cs);
                s.getCandidateSkills().add(cs);
                em.getTransaction().commit();

                c.getCandidateSkills().size();
                return c;
            } catch (EntityNotFoundException e) {
                if (em.getTransaction().isActive()) em.getTransaction().rollback();
                throw e;
            } catch (Exception e) {
                if (em.getTransaction().isActive()) em.getTransaction().rollback();
                logger.error("Error assigning skill {} to candidate {}", skillId, candidateId, e);
                throw new DatabaseException("Could not assign skill due to database error", e);
            }
        }
    }
}
