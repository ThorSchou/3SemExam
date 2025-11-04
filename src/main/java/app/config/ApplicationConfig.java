package app.config;

import app.exceptions.ApiException;
import app.routes.CandidateRoutes;
import app.routes.ReportRoutes;
import app.routes.SkillRoutes;
import app.security.controllers.AccessController;
import app.security.enums.Role;
import app.security.routes.SecurityRoutes;
import app.utils.Utils;
import io.javalin.Javalin;
import io.javalin.config.JavalinConfig;
import io.javalin.http.Context;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApplicationConfig {

    private static final AccessController accessController = new AccessController();
    private static final Logger logger = LoggerFactory.getLogger(ApplicationConfig.class);

    public static void configuration(JavalinConfig config) {
        config.showJavalinBanner = true;
        config.bundledPlugins.enableRouteOverview("/routes", Role.ANYONE);

        config.router.contextPath = "/api";

        config.router.apiBuilder(CandidateRoutes.getRoutes());
        config.router.apiBuilder(SkillRoutes.getRoutes());
        config.router.apiBuilder(ReportRoutes.getRoutes());

        config.router.apiBuilder(SecurityRoutes.getSecuredRoutes());
        config.router.apiBuilder(SecurityRoutes.getSecurityRoutes());
    }

    public static Javalin startServer(int port) {
        Javalin app = Javalin.create(ApplicationConfig::configuration);

        app.beforeMatched(accessController::accessHandler);

        app.exception(ApiException.class, ApplicationConfig::apiExceptionHandler);
        app.exception(EntityNotFoundException.class, ApplicationConfig::entityNotFoundExceptionHandler);
        app.exception(Exception.class, ApplicationConfig::generalExceptionHandler);

        app.start(port);
        return app;
    }

    public static void stopServer(Javalin app) {
        app.stop();
    }

    private static void generalExceptionHandler(Exception e, Context ctx) {
        logger.error("Unhandled exception", e);
        ctx.status(500);
        ctx.json(Utils.convertToJsonMessage(ctx, "error", "Internal server error"));
    }

    private static void apiExceptionHandler(ApiException e, Context ctx) {
        ctx.status(e.getStatusCode());
        logger.warn("API exception: status={}, message={}", e.getStatusCode(), e.getMessage());
        ctx.json(Utils.convertToJsonMessage(ctx, "warning", e.getMessage()));
    }

    private static void entityNotFoundExceptionHandler(EntityNotFoundException e, Context ctx) {
        ctx.status(404);
        logger.warn("Entity not found: {}", e.getMessage());
        ctx.json(Utils.convertToJsonMessage(ctx, "error", e.getMessage()));
    }
}
