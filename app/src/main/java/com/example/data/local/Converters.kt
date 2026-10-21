package com.example.data.local

import androidx.room.TypeConverter
import com.example.model.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class Converters {
    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val possibleCausesType = Types.newParameterizedType(List::class.java, PossibleCause::class.java)
    private val messagesType = Types.newParameterizedType(List::class.java, DiagnosticMessage::class.java)
    private val stringListType = Types.newParameterizedType(List::class.java, String::class.java)

    @TypeConverter
    fun fromProblemInput(input: ProblemInput?): String {
        if (input == null) return ""
        return moshi.adapter(ProblemInput::class.java).toJson(input)
    }

    @TypeConverter
    fun toProblemInput(json: String?): ProblemInput {
        if (json.isNullOrBlank()) return ProblemInput()
        return try {
            moshi.adapter(ProblemInput::class.java).fromJson(json) ?: ProblemInput()
        } catch (_: Exception) {
            ProblemInput()
        }
    }

    @TypeConverter
    fun fromPossibleCauses(list: List<PossibleCause>?): String {
        if (list == null) return "[]"
        return moshi.adapter<List<PossibleCause>>(possibleCausesType).toJson(list)
    }

    @TypeConverter
    fun toPossibleCauses(json: String?): List<PossibleCause> {
        if (json.isNullOrBlank()) return emptyList()
        return try {
            moshi.adapter<List<PossibleCause>>(possibleCausesType).fromJson(json) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromSafeTest(test: SafeTest?): String? {
        if (test == null) return null
        return moshi.adapter(SafeTest::class.java).toJson(test)
    }

    @TypeConverter
    fun toSafeTest(json: String?): SafeTest? {
        if (json.isNullOrBlank()) return null
        return try {
            moshi.adapter(SafeTest::class.java).fromJson(json)
        } catch (_: Exception) {
            null
        }
    }

    @TypeConverter
    fun fromRepairPlan(plan: RepairPlan?): String? {
        if (plan == null) return null
        return moshi.adapter(RepairPlan::class.java).toJson(plan)
    }

    @TypeConverter
    fun toRepairPlan(json: String?): RepairPlan? {
        if (json.isNullOrBlank()) return null
        return try {
            moshi.adapter(RepairPlan::class.java).fromJson(json)
        } catch (_: Exception) {
            null
        }
    }

    @TypeConverter
    fun fromStringList(list: List<String>?): String {
        if (list == null) return "[]"
        return moshi.adapter<List<String>>(stringListType).toJson(list)
    }

    @TypeConverter
    fun toStringList(json: String?): List<String> {
        if (json.isNullOrBlank()) return emptyList()
        return try {
            moshi.adapter<List<String>>(stringListType).fromJson(json) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromMessages(list: List<DiagnosticMessage>?): String {
        if (list == null) return "[]"
        return moshi.adapter<List<DiagnosticMessage>>(messagesType).toJson(list)
    }

    @TypeConverter
    fun toMessages(json: String?): List<DiagnosticMessage> {
        if (json.isNullOrBlank()) return emptyList()
        return try {
            moshi.adapter<List<DiagnosticMessage>>(messagesType).fromJson(json) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromCostComparison(cost: CostComparison?): String {
        if (cost == null) return ""
        return moshi.adapter(CostComparison::class.java).toJson(cost)
    }

    @TypeConverter
    fun toCostComparison(json: String?): CostComparison {
        if (json.isNullOrBlank()) return CostComparison()
        return try {
            moshi.adapter(CostComparison::class.java).fromJson(json) ?: CostComparison()
        } catch (_: Exception) {
            CostComparison()
        }
    }
}
