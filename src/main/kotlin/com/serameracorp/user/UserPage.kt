package com.serameracorp.user

import com.serameracorp.project.projectFromResultSet
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.thymeleaf.*
import java.sql.ResultSet

fun Route.user() {

    val userRepository = UserRepository()

    // create user object from statement
    fun userFromResultSet(resultSet: ResultSet): User =
        User(
            resultSet.getInt("id"),
            resultSet.getString("name"),
            resultSet.getString("machine")
        )

    // GET for users with pagination
    get("/users") {
        val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 0
        val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 25

        userRepository.pagedUsersStatement.setInt(1, size)
        userRepository.pagedUsersStatement.setInt(2, page * size)

        val resultSet = userRepository.pagedUsersStatement.executeQuery()
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

    // GET for detail
    get("/user/{userId}") {
        val userId = call.parameters["userId"]?.toIntOrNull()
        if (userId == null) {
            return@get call.respond(HttpStatusCode.NotFound)
        }

        userRepository.userByIdStatement.setInt(1, userId)
        val resultSet = userRepository.userByIdStatement.executeQuery()
        if (resultSet.next()) {
            val user = userFromResultSet(resultSet)

            userRepository.projectByUserStatement.setInt(1, userId)
            val projectResults = userRepository.projectByUserStatement.executeQuery()
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