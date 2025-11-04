package app.services;

import app.dtos.SkillStatDTO;
import app.dtos.SkillStatResponseDTO;
import app.exceptions.ApiException;
import app.utils.Utils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SkillStatsService {

    private static final Logger logger = LoggerFactory.getLogger(SkillStatsService.class);

    private static final String BASE_URL = "https://apiprovider.cphbusinessapps.dk/api/v1/skills/stats?slugs=";

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new Utils().getObjectMapper();

    public Map<String, SkillStatDTO> fetchStatsForSlugs(List<String> slugs) {
        if (slugs == null || slugs.isEmpty()) return Collections.emptyMap();

        try {
            String joined = String.join(",", slugs);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + joined))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                logger.warn("Skill stats API returned status {} for slugs {}", response.statusCode(), joined);
                return Collections.emptyMap();
            }

            SkillStatResponseDTO dto = mapper.readValue(response.body(), SkillStatResponseDTO.class);
            Map<String, SkillStatDTO> map = new HashMap<>();
            if (dto.getData() != null) {
                for (SkillStatDTO s : dto.getData()) {
                    map.put(s.getSlug(), s);
                }
            }
            return map;
        } catch (Exception e) {
            logger.error("Error calling Skill Stats API", e);
            throw new ApiException(502, "Error calling external Skill Stats API");
        }
    }
}
