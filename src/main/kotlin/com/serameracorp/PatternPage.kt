package com.serameracorp

import com.serameracorp.plugins.connectToPostgres
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.thymeleaf.*
import java.sql.PreparedStatement
import java.sql.ResultSet

data class Pattern(val id: Int, val name: String, val publisher: String)

fun Route.patterns() {

    val dbConnection = connectToPostgres(embedded = false)

    // create pattern object from statement
    fun patternFromResultSet(resultSet: ResultSet): Pattern =
        Pattern(
            resultSet.getInt("id"),
            resultSet.getString("name"),
            resultSet.getString("publisher")
        )

    // statement for search page
    fun searchPatternStatement(searchParam: String?): PreparedStatement {
        val filter = searchParam?.let { "where pattern.name ilike '%${it}%'" } ?: ""
        return dbConnection.prepareStatement(
            """
    | select
    |   pattern.id as id,
    |   pattern.name as name,
    |   pattern.publisher as publisher
    | from pattern
    | $filter
    """.trimMargin()
        )
    }

    // statement for detail page of pattern
    val patternByIdStatement = dbConnection.prepareStatement(
        """
    | select
    |   pattern.id as id,
    |   pattern.name as name,
    |   pattern.publisher as publisher
    | from pattern 
    | where pattern.id = ?
    """.trimMargin()
    )

    // Finds all projects with the given pattern_id
    val projectByPatternStatement = dbConnection.prepareStatement(
        """
    | select
    |   project.id as id,
    |   project.name as name,
    |   project.pattern_id as pattern_id,
    |   pattern.name as pattern,
    |   app_user.username as user
    | from project
    | join pattern on project.pattern_id = pattern.id 
    | join app_user on project.app_user_id = app_user.id 
    | where pattern.id = ?
    """.trimMargin()
    )

    // GET for search
    get("/pattern") {
        val searchParam = call.request.queryParameters["pattern-search"]

        val resultSet = searchPatternStatement(searchParam).executeQuery()

        val patterns: List<Pattern> = sequence {
            while (resultSet.next()) {
                yield(patternFromResultSet(resultSet))
            }
        }.toList()

        val res = ThymeleafContent("patterns.html", mapOf("patterns" to patterns))
        call.respond(res)
    }

    // GET for detail
    get("/pattern/{patternId}") {
        val patternId = call.parameters["patternId"]?.toIntOrNull()
        if (patternId == null) {
            return@get call.respond(HttpStatusCode.NotFound)
        }

        patternByIdStatement.setInt(1, patternId)
        val resultSet = patternByIdStatement.executeQuery()
        if (resultSet.next()) {
            val pattern = patternFromResultSet(resultSet)

            projectByPatternStatement.setInt(1, patternId)
            val projectResults = projectByPatternStatement.executeQuery()
            val projects = sequence {
                while (projectResults.next()) {
                    yield(projectFromResultSet(projectResults))
                }
            }.toList()

            val res = ThymeleafContent(
                "pattern.html", mapOf(
                    "pattern" to pattern,
                    "projects" to projects
                )
            )
            call.respond(res)
        } else {
            call.respond(HttpStatusCode.NotFound)
        }
    }
}