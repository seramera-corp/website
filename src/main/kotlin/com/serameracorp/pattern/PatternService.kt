package com.serameracorp.pattern

import com.serameracorp.project.Project
import com.serameracorp.project.projectFromResultSet
import io.ktor.http.*
import io.ktor.server.plugins.*
import java.sql.ResultSet

class PatternService {

    val patternRepository = PatternRepository()
    fun createPattern(patternParams: PatternParams): Int {
        patternRepository.createPatternStatement.setString(1, patternParams.name ?: "")
        patternRepository.createPatternStatement.setString(2, patternParams.publisher ?: "")
        patternRepository.createPatternStatement.setString(3, patternParams.difficulty ?: "")
        patternRepository.createPatternStatement.setString(4, patternParams.publishedIn ?: "")
        val resultSet = patternRepository.createPatternStatement.executeQuery()
        resultSet.next()
        return resultSet.getInt("id")
    }

    fun createPatternFabrics(patternId: Int, patternParams: PatternParams) {
        for (fabricParam in patternParams.patternFabric) {
            createPatternFabric(patternId, fabricParam.first, fabricParam.second)
        }
    }

    fun createPatternFabric(patternId: Int, fabricParam: String?, fabricLengthParam: String?) {
        if (fabricParam != null && fabricParam != "") {
            val fabricId = fabricParam.toIntOrNull()
            if (fabricId == null) {
                throw IllegalArgumentException("Invalid fabricId: '$fabricParam'")
            }
            val fabricLength = fabricLengthParam?.toDoubleOrNull()
            if (fabricLength == null) {
                throw IllegalArgumentException("Invalid fabricLength: '$fabricLengthParam'")
            }
            patternRepository.createPatternFabricStatement.setInt(1, patternId)
            patternRepository.createPatternFabricStatement.setInt(2, fabricId)
            patternRepository.createPatternFabricStatement.setDouble(3, fabricLength)
            patternRepository.createPatternFabricStatement.executeQuery()
        }
    }

    fun findAllPatternsByName(searchParam: String?): List<Pattern> {
        val resultSet = patternRepository.searchPatternStatement(searchParam).executeQuery()

        return sequence {
            while (resultSet.next()) {
                yield(patternFromResultSet(resultSet))
            }
        }.toList()
    }

    fun findPatternWithDetails(patternId: Int): Pattern{
        val pattern = findPatternById(patternId)
        addPatternDetails(pattern)
        return pattern
    }

    fun findPatternById(patternId: Int): Pattern {
        patternRepository.patternByIdStatement.setInt(1, patternId)
        val resultSet = patternRepository.patternByIdStatement.executeQuery()
        if (resultSet.next()) {
            // no fabric details yet
            return patternDetailsFromResultSet(resultSet)
        } else {
            throw NotFoundException("The searched pattern was not found")
        }
    }

    fun findProjectsByPatternId(patternId: Int): List<Project>{ //TODO move to project package?
        patternRepository.projectByPatternStatement.setInt(1, patternId)
        val projectResults = patternRepository.projectByPatternStatement.executeQuery()
        return sequence {
            while (projectResults.next()) {
                yield(projectFromResultSet(projectResults))
            }
        }.toList()
    }

    fun addPatternDetails(pattern: Pattern){
        pattern.patternFabric.addAll(findPatternFabrics(pattern.id))
        pattern.clothingType.addAll(findPatternClothingTypes(pattern.id))
    }

    private fun findPatternClothingTypes(patternId: Int): List<ClothingType> {
        patternRepository.patternClothingTypeByPatternIdStatement.setInt(1, patternId)
        val clothingTypePatternResultSet = patternRepository.patternClothingTypeByPatternIdStatement.executeQuery()
        return sequence {
            while (clothingTypePatternResultSet.next()) {
                yield(ClothingType(clothingTypePatternResultSet.getString("clothing_type")))
            }
        }.toList()
    }

    private fun findPatternFabrics(patternId: Int): List<PatternFabric>{
        patternRepository.patternFabricByPatternIdStatement.setInt(1, patternId)
        val fabricPatternResultSet = patternRepository.patternFabricByPatternIdStatement.executeQuery()
        return sequence {
            while (fabricPatternResultSet.next()) {
                yield(
                    PatternFabric(
                        fabricPatternResultSet.getString("fabric_type"),
                        fabricPatternResultSet.getDouble("length"),
                    )
                )
            }
        }.toList()
    }

    // create pattern object from statement
    private fun patternFromResultSet(resultSet: ResultSet): Pattern =
        Pattern(
            resultSet.getInt("id"),
            resultSet.getString("name"),
            resultSet.getString("publisher"),
            resultSet.getString("img_url")
        )

    // create pattern object from statement
    private fun patternDetailsFromResultSet(resultSet: ResultSet): Pattern =
        Pattern(
            resultSet.getInt("id"),
            resultSet.getString("name"),
            resultSet.getString("publisher"),
            resultSet.getString("img_url"),
        )

}