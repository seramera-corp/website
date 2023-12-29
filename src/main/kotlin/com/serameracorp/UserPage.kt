package com.serameracorp

import com.serameracorp.plugins.connectToPostgres
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.thymeleaf.*
import java.sql.ResultSet

data class User(val id: Int, val name: String, val machine: String)

fun Route.user() {

    val dbConnection = connectToPostgres(embedded = false)

    // create user object from statement
    fun userFromResultSet(resultSet: ResultSet): User =
        User(
            resultSet.getInt("id"),
            resultSet.getString("name"),
            resultSet.getString("machine")
        )

    // SQL Statement for fetching all users
    val allUsersStatement = dbConnection.prepareStatement(
        """
    | select
    |   id,
    |   username as name,
    |   sewingmachine as machine
    | from app_user LIMIT 25
    """.trimMargin()
    )

    // GET for users with pagination
    get("/users") {
        val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 0
        val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 25

        val pagedUsersStatement = dbConnection.prepareStatement(
            """
        | select
        |   id,
        |   username as name,
        |   sewingmachine as machine
        | from app_user
        | limit ? offset ?
        """.trimMargin()
        )

        pagedUsersStatement.setInt(1, size)
        pagedUsersStatement.setInt(2, page * size)

        val resultSet = pagedUsersStatement.executeQuery()
        val users = sequence {
            while (resultSet.next()) {
                yield(userFromResultSet(resultSet))
            }
        }.toList()

        val res = ThymeleafContent("users.html", mapOf(
            "users" to users,
            "currentPage" to page,
            "size" to size
        ))
        call.respond(res)
    }

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
    |   project.app_user_id as app_user_id,
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

            val res = ThymeleafContent("user.html", mapOf(
                "user" to user,
                "projects" to projects
            ))
            call.respond(res)
        } else {
            call.respond(HttpStatusCode.NotFound)
        }
    }
}