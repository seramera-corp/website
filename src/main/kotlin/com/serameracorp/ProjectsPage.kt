package com.serameracorp

import com.serameracorp.plugins.connectToPostgres
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.freemarker.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.sql.ResultSet

data class Project(val id: Int, val name: String, val pattern: String, val user: String, val imgUrl: String)

fun Route.projects() {

  val dbConnection = application.connectToPostgres(embedded = false)
  val allProjectsStatement = dbConnection.prepareStatement(
    """
    | select
    |   project.id as id,
    |   project.name as name,
    |   pattern.name as pattern,
    |   app_user.username as user 
    | from project 
    | join pattern on project.pattern_id = pattern.id 
    | join app_user on project.app_user_id = app_user.id 
    """.trimMargin()
  )

  val projectByIdStatement = dbConnection.prepareStatement(
    """
    | select
    |   project.id as id,
    |   project.name as name,
    |   pattern.name as pattern,
    |   app_user.username as user 
    | from project 
    | join pattern on project.pattern_id = pattern.id 
    | join app_user on project.app_user_id = app_user.id 
    | where project.id = ?
    """.trimMargin()
  )

  fun projectFromResultSet(resultSet: ResultSet): Project =
    Project(
      resultSet.getInt("id"),
      resultSet.getString("name"),
      resultSet.getString("pattern"),
      resultSet.getString("user"),
      "https://clipart-library.com/newhp/kissclipart-white-clothing-black-dress-line-art-96933c1b56d3f716.png"
    )

  get("/project") {
    val resultSet = allProjectsStatement.executeQuery()
    val projects = sequence {
      while (resultSet.next()) {
        yield(projectFromResultSet(resultSet))
      }
    }.toList()

    val res = FreeMarkerContent("projects.ftl", mapOf("projects" to projects))
    call.respond(res)
  }

  get("/project/{projectId}") {
    val projectId = call.parameters["projectId"]?.toIntOrNull()
    if (projectId == null) {
      return@get call.respond(HttpStatusCode.NotFound)
    }

    projectByIdStatement.setInt(1, projectId)
    val resultSet = projectByIdStatement.executeQuery()
    if (resultSet.next()) {
      val project = projectFromResultSet(resultSet)
      val res = FreeMarkerContent("project.ftl", mapOf("project" to project))
      call.respond(res)
    } else {
      call.respond(HttpStatusCode.NotFound)
    }
  }
}