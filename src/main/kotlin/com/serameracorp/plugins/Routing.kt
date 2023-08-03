package com.serameracorp.plugins

import com.serameracorp.projects
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondRedirect("projects")
        }

        projects()
    }
}
