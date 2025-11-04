package app.routes;

import app.controllers.ReportController;
import app.security.enums.Role;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;

public class ReportRoutes {

    public static EndpointGroup getRoutes() {
        ReportController c = new ReportController();
        return () -> {
            path("/reports", () -> {
                path("/candidates", () -> {
                    get("/top-by-popularity", c::topByPopularity, Role.USER);
                });
            });
        };
    }
}
