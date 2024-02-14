package com.serameracorp.pattern

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.thymeleaf.*

fun Route.patterns() {

    val patternService = PatternService()

    // GET for search
    get("/pattern") {
        val searchParam = call.request.queryParameters["pattern-search"]

        val patterns = patternService.findAllPatternsByName(searchParam)

        val res = ThymeleafContent("patterns.html", mapOf("patterns" to patterns))
        call.respond(res)
    }

    // GET for create
    get("/pattern/create") {
        val res = ThymeleafContent("pattern_create.html", mapOf())
        call.respond(res)
    }

    // POST for create
    post("/pattern/create") {
        val formParams = call.receiveParameters()

        val patternParams = PatternParams(
            name = formParams["name"],
            publisher = formParams["publisher"],
            publishedIn = formParams["published_in"],
            difficulty = formParams["difficulty"],
            patternFabric = getFabricParams(formParams)
        )

        val patternId = patternService.createPattern(patternParams)

        kotlin.runCatching {
            patternService.createPatternFabrics(patternId, patternParams)
        }.onFailure {
            if (it is IllegalArgumentException) {
                return@post call.respond(HttpStatusCode.BadRequest, "${it.message}")
            }
        }
        call.respondRedirect("/pattern/$patternId")
    }

    // GET for detail
    get("/pattern/{patternId}") {
        val patternId = call.parameters["patternId"]?.toIntOrNull()
        if (patternId == null) {
            return@get call.respond(HttpStatusCode.NotFound)
        }

        val pattern = patternService.findPatternWithDetails(patternId)
        if (pattern == null) {
            return@get call.respond(HttpStatusCode.NotFound, "The searched pattern was not found")
        }
        val res = ThymeleafContent(
            "pattern.html", mapOf(
                "pattern" to pattern,
                "projects" to patternService.findProjectsByPatternId(patternId)
            )
        )
        call.respond(res)
    }
}

fun getFabricParams(formParams: Parameters): MutableList<Pair<String?, String?>> {
    val fabrics: MutableList<Pair<String?, String?>> = mutableListOf()
    for (x in 0..2) {
        fabrics.add(Pair(formParams["fabric$x"], formParams["fabric${x}_length"]))
    }
    return fabrics
}
