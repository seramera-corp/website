package com.serameracorp

import com.serameracorp.plugins.connectToPostgres
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.freemarker.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

data class Pattern(val name: String, val publisher: String)

fun Route.patterns() {

    val dbConnection = application.connectToPostgres(embedded = false)
    val statement = dbConnection.prepareStatement(
        """
    | select
    |   pattern.name as name,
    |   pattern.publisher as publisher
    | from pattern 
    | where pattern.name='Boxy Bag'
    """.trimMargin()
    )

    get("/pattern") {
        val resultSet = statement.executeQuery()
        if (resultSet.next()) {
            val pattern= Pattern(
                resultSet.getString("name"),
                resultSet.getString("publisher")
            )
            val res = FreeMarkerContent("pattern.ftl", mapOf("pattern" to pattern))
            call.respond(res)
        } else {
            throw Exception("Record not found")
        }
    }
}