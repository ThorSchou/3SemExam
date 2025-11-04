package app.routes;

import app.controllers.SkillController;
import app.security.enums.Role;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;

public class SkillRoutes {

    public static EndpointGroup getRoutes() {
        SkillController c = new SkillController();
        return () -> {
            path("/skills", () -> {
                get(c::getAll, Role.USER);
                get("/{id}", c::getById, Role.USER);
                post(c::create, Role.ADMIN);
                put("/{id}", c::update, Role.ADMIN);
                delete("/{id}", c::delete, Role.ADMIN);
            });
        };
    }
}
