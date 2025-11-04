package daos;

import app.config.HibernateConfig;
import app.daos.CandidateDAO;
import app.entities.Candidate;
import app.entities.SkillCategory;
import app.security.daos.SecurityDAO;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.*;
import populators.Populator;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CandidateDAOTest {

    private static final EntityManagerFactory emf = HibernateConfig.getEntityManagerFactoryForTest();
    private static final SecurityDAO securityDAO = new SecurityDAO(emf);
    private static final Populator populator = new Populator(securityDAO, emf);

    private final CandidateDAO candidateDAO = new CandidateDAO(emf);

    @BeforeAll
    static void init() {
        HibernateConfig.setTest(true);
    }

    @BeforeEach
    void setUp() {
        populator.populateCandidatesAndSkills();
    }

    @AfterEach
    void tearDown() {
        populator.cleanUpDb();
    }

    @Test
    void findAll_returnsData() {
        List<Candidate> list = candidateDAO.findAll();
        assertNotNull(list);
        assertFalse(list.isEmpty());
    }

    @Test
    void createAndFindCandidate() {
        Candidate c = Candidate.builder()
                .name("JUnit Candidate")
                .phone("12345678")
                .education("Computer Science")
                .build();

        Candidate created = candidateDAO.create(c);
        assertNotNull(created.getId());

        Candidate found = candidateDAO.find(created.getId());
        assertEquals("JUnit Candidate", found.getName());
        assertEquals("12345678", found.getPhone());
    }

    @Test
    void findBySkillCategory_returnsList() {
        List<Candidate> list = candidateDAO.findBySkillCategory(SkillCategory.PROG_LANG);
        assertNotNull(list);
    }

    @Test
    void deleteCandidate_removesIt() {
        List<Candidate> existing = candidateDAO.findAll();
        assertFalse(existing.isEmpty());
        Long id = existing.get(0).getId();

        candidateDAO.delete(id);

        assertThrows(EntityNotFoundException.class, () -> candidateDAO.find(id));
    }
}
