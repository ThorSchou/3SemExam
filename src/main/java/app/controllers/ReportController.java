package app.controllers;

import app.dtos.CandidatePopularityReportDTO;
import app.services.CandidateReportService;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReportController {

    private static final Logger logger = LoggerFactory.getLogger(ReportController.class);

    private final CandidateReportService reportService = new CandidateReportService();

    public void topByPopularity(Context ctx) {
        logger.debug("GET /reports/candidates/top-by-popularity");
        CandidatePopularityReportDTO dto = reportService.topByPopularity();
        ctx.status(HttpStatus.OK).json(dto);
    }
}
