package com.studenthub.app.di

import android.content.Context
import androidx.room.Room
import com.studenthub.app.data.local.AppDatabase
import com.studenthub.app.data.local.dao.CourseDao
import com.studenthub.app.data.local.dao.GradeItemDao
import com.studenthub.app.data.local.dao.TodoDao
import com.studenthub.app.domain.repository.GradeItemRepository
import com.studenthub.app.domain.repository.TodoRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "studenthub.db"
        ).build()
    }

    @Provides
    fun provideCourseDao(db: AppDatabase): CourseDao = db.courseDao()

    @Provides
    fun provideGradeItemDao(db: AppDatabase): GradeItemDao = db.gradeItemDao()

    @Provides
    fun provideTodoDao(db: AppDatabase): TodoDao = db.todoDao()

    @Provides
    @Singleton
    fun provideTodoRepository(
        todoDao: TodoDao,
        courseDao: CourseDao
    ): TodoRepository = TodoRepository(todoDao, courseDao)

    @Provides
    @Singleton
    fun provideGradeItemRepository(
        gradeItemDao: GradeItemDao
    ): GradeItemRepository = GradeItemRepository(gradeItemDao)
}
