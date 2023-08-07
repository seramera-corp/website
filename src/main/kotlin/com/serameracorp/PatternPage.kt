package com.serameracorp

import com.serameracorp.plugins.connectToPostgres
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.freemarker.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.sql.ResultSet

data class Pattern(val id: Int, val name: String, val publisher: String)

fun Route.patterns() {

    val dbConnection = application.connectToPostgres(embedded = false)

    // create pattern object from statement
    fun patternFromResultSet(resultSet: ResultSet): Pattern =
        Pattern(
            resultSet.getInt("id"),
            resultSet.getString("name"),
            resultSet.getString("publisher")
        )

    // statement for search page
    val searchPatternStatement = dbConnection.prepareStatement(
        """
    | select
    |   pattern.id as id,
    |   pattern.name as name,
    |   pattern.publisher as publisher
    | from pattern 
    """.trimMargin()
    )

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

    // GET for search
    get("/pattern") {
        val resultSet = searchPatternStatement.executeQuery()

        val patterns: List<Pattern> = sequence {
            while (resultSet.next()) {
                yield(patternFromResultSet(resultSet))
            }
        }.toList()

        val res = FreeMarkerContent("patterns.ftl", mapOf("patterns" to patterns))
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
            val res = FreeMarkerContent("pattern.ftl", mapOf("pattern" to pattern))
            call.respond(res)
        } else {
            call.respond(HttpStatusCode.NotFound)
        }
    }
}