package com.example.data.repository

import com.example.data.local.Converters
import com.example.data.local.SessionDao
import com.example.data.local.SessionEntity
import com.example.model.DiagnosticSession
import com.example.model.ProductCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DiagnosisRepository(
    private val sessionDao: SessionDao,
    private val converters: Converters = Converters()
) {

    val allSessions: Flow<List<DiagnosticSession>> = sessionDao.getAllSessions().map { list ->
        list.map { entityToSession(it) }
    }

    val recentSessions: Flow<List<DiagnosticSession>> = sessionDao.getRecentSessions(5).map { list ->
        list.map { entityToSession(it) }
    }

    suspend fun getSessionById(id: String): DiagnosticSession? {
        val entity = sessionDao.getSessionById(id) ?: return null
        return entityToSession(entity)
    }

    suspend fun saveSession(session: DiagnosticSession) {
        val entity = sessionToEntity(session)
        sessionDao.insertSession(entity)
    }

    suspend fun deleteSession(id: String) {
        sessionDao.deleteSessionById(id)
    }

    suspend fun clearAll() {
        sessionDao.clearAll()
    }

    private fun sessionToEntity(session: DiagnosticSession): SessionEntity {
        return SessionEntity(
            id = session.id,
            title = session.title,
            categoryId = session.category.id,
            createdAt = session.createdAt,
            updatedAt = session.updatedAt,
            isCompleted = session.isCompleted,
            confidence = session.confidence,
            summary = session.summary,
            technicianRecommended = session.technicianRecommended,
            problemInputJson = converters.fromProblemInput(session.problemInput),
            possibleCausesJson = converters.fromPossibleCauses(session.possibleCauses),
            safeTestJson = converters.fromSafeTest(session.safeTest),
            repairPlanJson = converters.fromRepairPlan(session.repairPlan),
            safetyWarningsJson = converters.fromStringList(session.safetyWarnings),
            messagesJson = converters.fromMessages(session.messages),
            costComparisonJson = converters.fromCostComparison(session.costComparison)
        )
    }

    private fun entityToSession(entity: SessionEntity): DiagnosticSession {
        val problemInput = converters.toProblemInput(entity.problemInputJson)
        val possibleCauses = converters.toPossibleCauses(entity.possibleCausesJson)
        val safeTest = converters.toSafeTest(entity.safeTestJson)
        val repairPlan = converters.toRepairPlan(entity.repairPlanJson)
        val safetyWarnings = converters.toStringList(entity.safetyWarningsJson)
        val messages = converters.toMessages(entity.messagesJson)
        val costComparison = converters.toCostComparison(entity.costComparisonJson)

        return DiagnosticSession(
            id = entity.id,
            title = entity.title,
            category = ProductCategory.fromId(entity.categoryId),
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
            problemInput = problemInput,
            summary = entity.summary,
            possibleCauses = possibleCauses,
            confidence = entity.confidence,
            safeTest = safeTest,
            repairPlan = repairPlan,
            technicianRecommended = entity.technicianRecommended,
            safetyWarnings = safetyWarnings,
            messages = messages,
            costComparison = costComparison,
            isCompleted = entity.isCompleted
        )
    }
}
