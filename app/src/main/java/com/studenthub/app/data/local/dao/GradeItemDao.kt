package com.studenthub.app.data.local.dao

import androidx.room.*
import com.studenthub.app.data.entity.GradeItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GradeItemDao {
    @Query("SELECT * FROM grade_items WHERE courseId = :courseId ORDER BY weight DESC")
    fun getGradeItemsForCourse(courseId: Long): Flow<List<GradeItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGradeItem(item: GradeItemEntity): Long

    @Update
    suspend fun updateGradeItem(item: GradeItemEntity)

    @Delete
    suspend fun deleteGradeItem(item: GradeItemEntity)

    @Query("DELETE FROM grade_items WHERE courseId = :courseId")
    suspend fun deleteGradeItemsForCourse(courseId: Long)
}
