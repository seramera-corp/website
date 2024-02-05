package com.serameracorp

import com.serameracorp.plugins.connectToPostgres
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.thymeleaf.*
import java.sql.PreparedStatement
import java.sql.ResultSet

data class Pattern(
    val id: Int,
    val name: String,
    val publisher: String,
    val img_url: String,
    val patternFabric: MutableList<PatternFabric> = mutableListOf(),
)

data class PatternFabric(
    val fabric_type: String,
    val length: Double
)

data class PatternFabricDetails(
    val id: Int,
    val name: String,
    val length: Double,
    val fabric_type: String
)

fun Route.patterns() {

    val dbConnection = connectToPostgres(embedded = false)

    // create pattern object from statement
    fun patternFromResultSet(resultSet: ResultSet): Pattern =
        Pattern(
            resultSet.getInt("id"),
            resultSet.getString("name"),
            resultSet.getString("publisher"),
            resultSet.getString("img_url")
        )

    // create pattern object from statement
    fun patternDetailsFromResultSet(resultSet: ResultSet): Pattern =
        Pattern(
            resultSet.getInt("id"),
            resultSet.getString("name"),
            resultSet.getString("publisher"),
            resultSet.getString("img_url"),
        )

    // statement for search page
    fun searchPatternStatement(searchParam: String?): PreparedStatement {
        val filter = searchParam?.let { "where pattern.name ilike '%${it}%'" } ?: ""
        return dbConnection.prepareStatement(
            """
            | select
            |   pattern.id as id,
            |   pattern.name as name,
            |   pattern.publisher as publisher,
            |   pattern.img_url as img_url
            | from pattern
            | $filter
            | LIMIT 25
        """.trimMargin()
        )
    }

    // statement for detail page of pattern
    val patternByIdStatement = dbConnection.prepareStatement(
        """
    | select
    |   pattern.id as id,
    |   pattern.name as name,
    |   pattern.publisher as publisher,
    |   pattern.img_url as img_url
    | from pattern 
    | where pattern.id = ?
    """.trimMargin()
    )

    // statement for fabrics matching the pattern
    val patternFabricByPatternIdStatement = dbConnection.prepareStatement(
        """
    | select
    |   pattern_fabric.fabric_length as length,
    |   fabric_type.name as fabric_type
    | from pattern_fabric
    | join fabric_type on pattern_fabric.fabric_type_id = fabric_type.id
    | where pattern_fabric.pattern_id = ?
    """.trimMargin()
    )

    // Finds all projects with the given pattern_id
    val projectByPatternStatement = dbConnection.prepareStatement(
        """
    | select
    |   project.id as id,
    |   project.name as name,
    |   project.pattern_id as pattern_id,
    |   project.app_user_id as app_user_id,
    |   pattern.name as pattern,
    |   app_user.username as user
    | from project
    | join pattern on project.pattern_id = pattern.id 
    | join app_user on project.app_user_id = app_user.id 
    | where pattern.id = ?
    | LIMIT 25
    """.trimMargin()
    )

    // CREATE statement for new pattern
    val createPatternStatement = dbConnection.prepareStatement(
        """
    | insert into pattern (name, publisher, difficulty, published_in) 
    | values (?, ?, ?, ?) 
    | returning id;
    """.trimMargin()
    )

    // CREATE statement for new fabric pattern relation
    val createPatternFabricStatement = dbConnection.prepareStatement(
        """
    | insert into pattern_fabric (pattern_id, fabric_type_id, fabric_length) 
    | values (?, ?, ?) 
    | returning id;
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

    // GET for create
    get("/pattern/create") {

        val res = ThymeleafContent("pattern_create.html", mapOf())
        call.respond(res)
    }

    // POST for create
    post("/pattern/create") {
        val formParams = call.receiveParameters()
        val nameParam = formParams["name"]
        val publisherParam = formParams["publisher"]
        val publishedInParam = formParams["published_in"]
        val difficultyParam = formParams["difficulty"]

        createPatternStatement.setString(1, nameParam ?: "")
        createPatternStatement.setString(2, publisherParam ?: "")
        createPatternStatement.setString(3, difficultyParam ?: "")
        createPatternStatement.setString(4, publishedInParam ?: "")
        val resultSet = createPatternStatement.executeQuery()
        resultSet.next()
        val patternId = resultSet.getInt("id")

        for (x in 0..2) {
            val fabricParam = formParams["fabric$x"]
            val fabricLengthParam = formParams["fabric${x}_length"]
            if (fabricParam != null && fabricParam != "") {
                val fabricId = fabricParam.toIntOrNull()
                if (fabricId == null) {
                    return@post call.respond(HttpStatusCode.BadRequest, "Invalid fabricId: '$fabricParam'")
                }
                val fabricLength = fabricLengthParam?.toDoubleOrNull()
                if (fabricLength == null) {
                    return@post call.respond(HttpStatusCode.BadRequest, "Invalid fabricLength: '$fabricLengthParam'")
                }
                createPatternFabricStatement.setInt(1, patternId)
                createPatternFabricStatement.setInt(2, fabricId)
                createPatternFabricStatement.setDouble(3, fabricLength)
                createPatternFabricStatement.executeQuery()
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

        patternByIdStatement.setInt(1, patternId)
        val resultSet = patternByIdStatement.executeQuery()
        patternFabricByPatternIdStatement.setInt(1, patternId)
        val fabricPatternResultSet = patternFabricByPatternIdStatement.executeQuery()
        if (resultSet.next()) {
            // no fabric details yet
            val pattern = patternDetailsFromResultSet(resultSet)
            // now add fabrics
            while (fabricPatternResultSet.next()) {
                pattern.patternFabric.add(
                    PatternFabric(
                        fabricPatternResultSet.getString("fabric_type"),
                        fabricPatternResultSet.getDouble("length"),
                    )
                )
            }

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