package com.serameracorp.project

import com.serameracorp.plugins.connectToPostgres

class ProjectRepository {
    val dbConnection = connectToPostgres(embedded = false)
    val allProjectsStatement = dbConnection.prepareStatement(
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
    """.trimMargin()
    )

    val projectByIdStatement = dbConnection.prepareStatement(
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
    | where project.id = ?
    """.trimMargin()
    )
}