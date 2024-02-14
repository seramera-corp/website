package com.serameracorp.user;

import com.serameracorp.plugins.connectToPostgres

public class UserRepository {
    val dbConnection = connectToPostgres(embedded = false)

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
}
