package com.serameracorp.pattern

import com.serameracorp.plugins.connectToPostgres
import java.sql.PreparedStatement

class PatternRepository {

    val dbConnection = connectToPostgres(embedded = false)
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

    // statement for clothing types matching the pattern
    val patternClothingTypeByPatternIdStatement = dbConnection.prepareStatement(
        """
    | select
    |   clothing_type.name as clothing_type
    | from pattern_clothing_type
    | join clothing_type on pattern_clothing_type.clothing_type_id = clothing_type.id
    | where pattern_clothing_type.pattern_id = ?
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

}