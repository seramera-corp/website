package com.serameracorp

import com.serameracorp.plugins.connectToPostgres
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.freemarker.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.sql.ResultSet

data class User(val id: Int, val name: String, val machine: String)

fun Route.user() {

    val dbConnection = application.connectToPostgres(embedded = false)

    // create user object from statement
    fun userFromResultSet(resultSet: ResultSet): User =
        User(
            resultSet.getInt("id"),
            resultSet.getString("name"),
            resultSet.getString("machine")
        )

    // statement for detail page of user
    val userByIdStatement = dbConnection.prepareStatement(
        """
    | select
    |   app_user.id as id,
    |   app_user.username as name,
    |   app_user.sewingmachine as machine
    | from app_user 
    | where app_user.id = ?
    """.trimMargin()
    )

    // Finds all projects with the given user_id
    val projectByUserStatement = dbConnection.prepareStatement(
        """
    | select
    |   project.id as id,
    |   project.name as name,
    |   project.pattern_id as pattern_id,
    |   pattern.name as pattern,
    |   app_user.username as user,
    |   app_user.id as user_id
    | from project
    | join pattern on project.pattern_id = pattern.id 
    | join app_user on project.app_user_id = app_user.id 
    | where app_user.id = ?
    """.trimMargin()
    )

    // GET for detail
    get("/user/{userId}") {
        val userId = call.parameters["userId"]?.toIntOrNull()
        if (userId == null) {
            return@get call.respond(HttpStatusCode.NotFound)
        }

        userByIdStatement.setInt(1, userId)
        val resultSet = userByIdStatement.executeQuery()
        if (resultSet.next()) {
            val user = userFromResultSet(resultSet)

            projectByUserStatement.setInt(1, userId)
            val projectResults = projectByUserStatement.executeQuery()
            val projects = sequence {
                while (projectResults.next()) {
                    yield(projectFromResultSet(projectResults))
                }
            }.toList()

            val res = FreeMarkerContent("user.ftl", mapOf(
                "user" to user,
                "projects" to projects
            ))
            call.respond(res)
        } else {
            call.respond(HttpStatusCode.NotFound)
        }
    }
}