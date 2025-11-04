package populators;

import app.entities.Candidate;
import app.entities.CandidateSkill;
import app.entities.Skill;
import app.entities.SkillCategory;
import app.security.daos.SecurityDAO;
import app.security.entities.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Populator {

    private static SecurityDAO securityDAO;
    private static EntityManagerFactory emf;

    public Populator(SecurityDAO securityDAO, EntityManagerFactory emf) {
        Populator.securityDAO = securityDAO;
        Populator.emf = emf;
    }

    public List<User> populateUsers() {
        User u1 = securityDAO.createUser("user1", "password1");
        User u2 = securityDAO.createUser("user2", "password2");
        return new ArrayList<>(Arrays.asList(u1, u2));
    }

    public List<Candidate> populateCandidatesAndSkills() {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();

            Skill s1 = Skill.builder()
                    .name("Java")
                    .slug("java")
                    .description("Java programming language")
                    .category(SkillCategory.PROG_LANG)
                    .build();

            Skill s2 = Skill.builder()
                    .name("Spring Boot")
                    .slug("spring-boot")
                    .description("Spring Boot framework")
                    .category(SkillCategory.FRAMEWORK)
                    .build();

            Skill s3 = Skill.builder()
                    .name("PostgreSQL")
                    .slug("postgresql")
                    .description("PostgreSQL database")
                    .category(SkillCategory.DATA)
                    .build();

            em.persist(s1);
            em.persist(s2);
            em.persist(s3);

            Candidate c1 = Candidate.builder()
                    .name("Alice Candidate")
                    .phone("+4511111111")
                    .education("Computer Science")
                    .build();

            Candidate c2 = Candidate.builder()
                    .name("Bob Candidate")
                    .phone("+4522222222")
                    .education("Software Engineering")
                    .build();

            Candidate c3 = Candidate.builder()
                    .name("Charlie Candidate")
                    .phone("+4533333333")
                    .education("Information Technology")
                    .build();

            em.persist(c1);
            em.persist(c2);
            em.persist(c3);

            CandidateSkill cs1 = CandidateSkill.builder()
                    .candidate(c1)
                    .skill(s1)
                    .build();

            CandidateSkill cs2 = CandidateSkill.builder()
                    .candidate(c1)
                    .skill(s2)
                    .build();

            CandidateSkill cs3 = CandidateSkill.builder()
                    .candidate(c2)
                    .skill(s1)
                    .build();

            CandidateSkill cs4 = CandidateSkill.builder()
                    .candidate(c3)
                    .skill(s3)
                    .build();

            em.persist(cs1);
            em.persist(cs2);
            em.persist(cs3);
            em.persist(cs4);

            em.getTransaction().commit();
            return Arrays.asList(c1, c2, c3);
        }
    }

    public void cleanUpDb() {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.createNativeQuery("DELETE FROM user_roles").executeUpdate();
            em.createQuery("DELETE FROM CandidateSkill").executeUpdate();
            em.createQuery("DELETE FROM Candidate").executeUpdate();
            em.createQuery("DELETE FROM Skill").executeUpdate();
            em.createQuery("DELETE FROM User").executeUpdate();
            em.createQuery("DELETE FROM Role").executeUpdate();
            em.createNativeQuery("ALTER SEQUENCE candidates_id_seq RESTART WITH 1").executeUpdate();
            em.createNativeQuery("ALTER SEQUENCE skills_id_seq RESTART WITH 1").executeUpdate();
            em.createNativeQuery("ALTER SEQUENCE candidate_skills_id_seq RESTART WITH 1").executeUpdate();
            em.getTransaction().commit();
        }
    }
}
