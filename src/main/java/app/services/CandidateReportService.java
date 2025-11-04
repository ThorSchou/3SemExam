package app.services;

import app.daos.CandidateDAO;
import app.dtos.CandidatePopularityReportDTO;
import app.dtos.SkillStatDTO;
import app.entities.Candidate;
import app.entities.CandidateSkill;
import app.entities.Skill;
import app.exceptions.ApiException;
import app.exceptions.DatabaseException;
import app.config.HibernateConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CandidateReportService {

    private final CandidateDAO candidateDAO = new CandidateDAO(HibernateConfig.getEntityManagerFactory());
    private final SkillStatsService skillStatsService = new SkillStatsService();

    public CandidatePopularityReportDTO topByPopularity() {
        List<Candidate> candidates = candidateDAO.findAll();
        if (candidates.isEmpty()) {
            throw new ApiException(404, "No candidates found");
        }

        CandidatePopularityReportDTO best = null;

        for (Candidate c : candidates) {
            List<String> slugs = new ArrayList<>();
            for (CandidateSkill cs : c.getCandidateSkills()) {
                Skill s = cs.getSkill();
                if (s != null && s.getSlug() != null) {
                    slugs.add(s.getSlug());
                }
            }
            if (slugs.isEmpty()) continue;

            Map<String, SkillStatDTO> stats = skillStatsService.fetchStatsForSlugs(slugs);
            if (stats.isEmpty()) continue;

            int sum = 0;
            int count = 0;
            for (String slug : slugs) {
                SkillStatDTO stat = stats.get(slug);
                if (stat != null && stat.getPopularityScore() != null) {
                    sum += stat.getPopularityScore();
                    count++;
                }
            }
            if (count == 0) continue;

            double avg = (double) sum / count;
            if (best == null || avg > best.getAveragePopularityScore()) {
                best = new CandidatePopularityReportDTO(c.getId(), c.getName(), avg);
            }
        }

        if (best == null) {
            throw new ApiException(404, "No candidates with popularity data");
        }
        return best;
    }
}
