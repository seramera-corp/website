package com.serameracorp.project

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.thymeleaf.*
import java.sql.ResultSet

fun projectFromResultSet(resultSet: ResultSet): Project =
  Project(
    resultSet.getInt("id"),
    resultSet.getString("name"),
    resultSet.getString("pattern"),
    resultSet.getString("user"),
    resultSet.getInt("app_user_id"),
    "https://clipart-library.com/newhp/kissclipart-white-clothing-black-dress-line-art-96933c1b56d3f716.png",
    resultSet.getInt("pattern_id")
  )

fun Route.projects() {

  val projectRepository = ProjectRepository()

  get("/project") {
    val resultSet = projectRepository.allProjectsStatement.executeQuery()
    val projects = sequence {
      while (resultSet.next()) {
        yield(projectFromResultSet(resultSet))
      }
    }.toList()

    val res = ThymeleafContent("projects.html", mapOf("projects" to projects))
    call.respond(res)
  }

  get("/project/{projectId}") {
    val projectId = call.parameters["projectId"]?.toIntOrNull()
    if (projectId == null) {
      return@get call.respond(HttpStatusCode.NotFound)
    }

    projectRepository.projectByIdStatement.setInt(1, projectId)
    val resultSet = projectRepository.projectByIdStatement.executeQuery()
    if (resultSet.next()) {
      val project = projectFromResultSet(resultSet)
      val res = ThymeleafContent("project.html", mapOf("project" to project))
      call.respond(res)
    } else {
      call.respond(HttpStatusCode.NotFound)
    }
  }
}