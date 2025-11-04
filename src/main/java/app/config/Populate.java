package app.config;

import app.entities.Candidate;
import app.entities.CandidateSkill;
import app.entities.Skill;
import app.entities.SkillCategory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

public class Populate {

    private final EntityManagerFactory emf;

    public Populate(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public void run() {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();

            Skill java = Skill.builder()
                    .name("Java")
                    .slug("java")
                    .category(SkillCategory.PROG_LANG)
                    .description("General-purpose, strongly-typed language for backend and Android.")
                    .build();

            Skill springBoot = Skill.builder()
                    .name("Spring Boot")
                    .slug("spring-boot")
                    .category(SkillCategory.FRAMEWORK)
                    .description("Java framework for building microservices and REST APIs.")
                    .build();

            Skill postgresql = Skill.builder()
                    .name("PostgreSQL")
                    .slug("postgresql")
                    .category(SkillCategory.DB)
                    .description("Open-source relational database with strong SQL compliance.")
                    .build();

            Skill docker = Skill.builder()
                    .name("Docker")
                    .slug("docker")
                    .category(SkillCategory.DEVOPS)
                    .description("Container platform used for packaging and running applications.")
                    .build();

            Skill react = Skill.builder()
                    .name("React")
                    .slug("react")
                    .category(SkillCategory.FRONTEND)
                    .description("JavaScript library for building user interfaces.")
                    .build();

            Skill junit = Skill.builder()
                    .name("JUnit")
                    .slug("junit")
                    .category(SkillCategory.TESTING)
                    .description("Unit testing framework for Java.")
                    .build();

            em.persist(java);
            em.persist(springBoot);
            em.persist(postgresql);
            em.persist(docker);
            em.persist(react);
            em.persist(junit);

            Candidate c1 = Candidate.builder()
                    .name("Alice Andersen")
                    .phone("+4511111111")
                    .education("BSc Computer Science")
                    .build();

            Candidate c2 = Candidate.builder()
                    .name("Bob Berg")
                    .phone("+4522222222")
                    .education("MSc Software Engineering")
                    .build();

            Candidate c3 = Candidate.builder()
                    .name("Carla Carlsen")
                    .phone("+4533333333")
                    .education("BSc Web Development")
                    .build();

            em.persist(c1);
            em.persist(c2);
            em.persist(c3);

            CandidateSkill cs1 = CandidateSkill.builder().candidate(c1).skill(java).build();
            CandidateSkill cs2 = CandidateSkill.builder().candidate(c1).skill(springBoot).build();
            CandidateSkill cs3 = CandidateSkill.builder().candidate(c1).skill(postgresql).build();

            CandidateSkill cs4 = CandidateSkill.builder().candidate(c2).skill(docker).build();
            CandidateSkill cs5 = CandidateSkill.builder().candidate(c2).skill(postgresql).build();
            CandidateSkill cs6 = CandidateSkill.builder().candidate(c2).skill(junit).build();

            CandidateSkill cs7 = CandidateSkill.builder().candidate(c3).skill(react).build();
            CandidateSkill cs8 = CandidateSkill.builder().candidate(c3).skill(java).build();

            em.persist(cs1);
            em.persist(cs2);
            em.persist(cs3);
            em.persist(cs4);
            em.persist(cs5);
            em.persist(cs6);
            em.persist(cs7);
            em.persist(cs8);

            em.getTransaction().commit();
        }
    }

    public static void main(String[] args) {
        new Populate(HibernateConfig.getEntityManagerFactory()).run();
    }
}
