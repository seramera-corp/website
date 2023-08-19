package com.serameracorp.plugins

import io.ktor.server.application.*
import java.sql.Connection
import java.sql.DriverManager

/**
 * Makes a connection to a Postgres database.
 *
 * @param embedded -- if [true] defaults to an embedded database for tests that runs locally in the same process.
 * In this case you don't have to provide any parameters in configuration file, and you don't have to run a process.
 *
 * @return [Connection] that represent connection to the database. Please, don't forget to close this connection when
 * your application shuts down by calling [Connection.close]
 * */
fun connectToPostgres(embedded: Boolean): Connection {
    Class.forName("org.postgresql.Driver")
    return if (embedded) {
        DriverManager.getConnection("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", "root", "")
    } else {
        val url = "jdbc:postgresql://0.0.0.0:5432/postgres"
        val user = "postgres"
        val password = "password"

        DriverManager.getConnection(url, user, password)
    }
}
