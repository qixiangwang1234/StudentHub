package com.studenthub.app.domain.repository

import com.studenthub.app.data.entity.GradeItemEntity
import com.studenthub.app.data.local.dao.GradeItemDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GradeItemRepository @Inject constructor(
    private val gradeItemDao: GradeItemDao
) {
    fun getGradeItemsForCourse(courseId: Long): Flow<List<GradeItemEntity>> =
        gradeItemDao.getGradeItemsForCourse(courseId)

    suspend fun insertGradeItem(item: GradeItemEntity): Long =
        gradeItemDao.insertGradeItem(item)

    suspend fun updateGradeItem(item: GradeItemEntity) =
        gradeItemDao.updateGradeItem(item)

    suspend fun deleteGradeItem(item: GradeItemEntity) =
        gradeItemDao.deleteGradeItem(item)

    suspend fun getGradeItemsForCourseOnce(courseId: Long): List<GradeItemEntity> =
        gradeItemDao.getGradeItemsForCourse(courseId).first()

    fun calculateWeightedTotal(items: List<GradeItemEntity>): Float? {
        val scored = items.filter { it.score != null }
        if (scored.isEmpty()) return null
        val totalWeight = scored.sumOf { it.weight.toDouble() }.toFloat()
        if (totalWeight == 0f) return null
        val weightedSum = scored.sumOf {
            (it.score!! / it.totalScore * it.weight).toDouble()
        }.toFloat()
        return (weightedSum / totalWeight) * 100
    }
}
