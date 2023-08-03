package com.serameracorp

import com.serameracorp.plugins.connectToPostgres
import io.ktor.server.application.*
import io.ktor.server.freemarker.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

data class Project(val name: String, val pattern: String, val user: String, val imgUrl: String)

fun Route.projects() {

  val dbConnection = application.connectToPostgres(embedded = false)
  val statement = dbConnection.prepareStatement(
    """
    | select
    |   project.name as name,
    |   pattern.name as pattern,
    |   app_user.username as user 
    | from project 
    | join pattern on project.pattern_id = pattern.id 
    | join app_user on project.app_user_id = app_user.id 
    """.trimMargin()
  )

  get("/projects") {
    val resultSet = statement.executeQuery()
    val projects = sequence {
      while (resultSet.next()) {
        val project = Project(
          resultSet.getString("name"),
          resultSet.getString("pattern"),
          resultSet.getString("user"),
          "https://clipart-library.com/newhp/kissclipart-white-clothing-black-dress-line-art-96933c1b56d3f716.png"
        )
        yield(project)
      }
    }.toList()

    val res = FreeMarkerContent("projects.ftl", mapOf("projects" to projects))
    call.respond(res)
  }
}