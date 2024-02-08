package com.serameracorp.pattern

import com.serameracorp.project.Project
import com.serameracorp.project.projectFromResultSet
import io.ktor.http.*
import io.ktor.server.plugins.*
import java.sql.ResultSet

class PatternService {

    val patternRepository = PatternRepository()
    fun createPattern(formParams: Parameters): Int {
        val nameParam = formParams["name"]
        val publisherParam = formParams["publisher"]
        val publishedInParam = formParams["published_in"]
        val difficultyParam = formParams["difficulty"]

        patternRepository.createPatternStatement.setString(1, nameParam ?: "")
        patternRepository.createPatternStatement.setString(2, publisherParam ?: "")
        patternRepository.createPatternStatement.setString(3, difficultyParam ?: "")
        patternRepository.createPatternStatement.setString(4, publishedInParam ?: "")
        val resultSet = patternRepository.createPatternStatement.executeQuery()
        resultSet.next()
        return resultSet.getInt("id")
    }

    fun createPatternFabrics(patternId: Int, formParams: Parameters) {
        for (x in 0..2) {
            val fabricParam = formParams["fabric$x"]
            val fabricLengthParam = formParams["fabric${x}_length"]
            createPatternFabric(patternId, fabricParam, fabricLengthParam)
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

        val clothingTypes: MutableList<ClothingType> = mutableListOf()
        while (clothingTypePatternResultSet.next()) {
            clothingTypes.add(
                ClothingType(clothingTypePatternResultSet.getString("clothing_type"))
            )
        }
        return clothingTypes
    }

    private fun findPatternFabrics(patternId: Int): List<PatternFabric>{
        patternRepository.patternFabricByPatternIdStatement.setInt(1, patternId)
        val fabricPatternResultSet = patternRepository.patternFabricByPatternIdStatement.executeQuery()
        val fabrics: MutableList<PatternFabric> = mutableListOf()
        while (fabricPatternResultSet.next()) {
            fabrics.add(
                PatternFabric(
                    fabricPatternResultSet.getString("fabric_type"),
                    fabricPatternResultSet.getDouble("length"),
                )
            )
        }
        return fabrics
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