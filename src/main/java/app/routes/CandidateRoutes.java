package app.routes;

import app.controllers.CandidateController;
import app.security.enums.Role;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;

public class CandidateRoutes {

    public static EndpointGroup getRoutes() {
        CandidateController c = new CandidateController();
        return () -> {
            path("/candidates", () -> {
                get(c::getAll, Role.USER);
                get("/{id}", c::getById, Role.USER);

                post(c::create, Role.ADMIN);
                put("/{id}", c::update, Role.ADMIN);
                delete("/{id}", c::delete, Role.ADMIN);
                put("/{candidateId}/skills/{skillId}", c::assignSkill, Role.ADMIN);
            });
        };
    }
}
