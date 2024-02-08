package com.serameracorp.plugins

import com.serameracorp.project.projects
import com.serameracorp.pattern.patterns
import com.serameracorp.user.user
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondRedirect("/project")
        }

        projects()
        patterns()
        user()
    }
}
