# StudentHub Phase 1 — Project Scaffolding, Database, Navigation & Schedule CRUD

> **For agentic workers:** REQUIRED SUB-SKILL: Use subagent-driven-development (recommended) or executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Bootstrap the full StudentHub Android project with Gradle build files, Room database with all 3 entities, Hilt DI, Material3 theme, bottom navigation with 4 tabs, and complete schedule CRUD (create, read, update, delete courses with the weekly schedule view and course detail page).

**Architecture:** Clean Architecture layers — `data/` (Room DAOs + entities), `domain/` (repository interfaces), `ui/` (Compose screens + ViewModels). Single-Activity with Jetpack Navigation Compose.

**Tech Stack:** Kotlin, Jetpack Compose + Material3, Room, Hilt, Navigation Compose, Kotlin Coroutines + Flow

---

## File Structure

```
app.apk/
├── build.gradle.kts                          (root build script)
├── settings.gradle.kts
├── gradle.properties
├── gradle/
│   └── libs.versions.toml                    (version catalog)
├── app/
│   ├── build.gradle.kts                      (app module build script)
│   ├── proguard-rules.pro
│   └── src/
│       └── main/
│           ├── AndroidManifest.xml
│           └── java/com/studenthub/app/
│               ├── StudentHubApp.kt
│               ├── MainActivity.kt
│               ├── data/
│               │   ├── local/
│               │   │   ├── AppDatabase.kt
│               │   │   ├── Converters.kt
│               │   │   └── dao/
│               │   │       ├── CourseDao.kt
│               │   │       ├── GradeItemDao.kt
│               │   │       └── TodoDao.kt
│               │   └── entity/
│               │       ├── CourseEntity.kt
│               │       ├── GradeItemEntity.kt
│               │       └── TodoEntity.kt
│               ├── di/
│               │   └── AppModule.kt
│               ├── domain/
│               │   └── repository/
│               │       └── CourseRepository.kt
│               ├── ui/
│               │   ├── navigation/
│               │   │   ├── AppNavGraph.kt
│               │   │   └── BottomNavBar.kt
│               │   ├── theme/
│               │   │   ├── Theme.kt
│               │   │   ├── Color.kt
│               │   │   └── Type.kt
│               │   └── screens/
│               │       ├── home/
│               │       │   └── HomeScreen.kt
│               │       ├── schedule/
│               │       │   ├── ScheduleScreen.kt
│               │       │   ├── ScheduleViewModel.kt
│               │       │   ├── CourseDetailScreen.kt
│               │       │   ├── AddCourseScreen.kt
│               │       │   └── AddCourseViewModel.kt
│               │       ├── todo/
│               │       │   └── TodoListScreen.kt
│               │       └── profile/
│               │           └── ProfileScreen.kt
│               └── res/
│                   ├── values/
│                   │   ├── strings.xml
│                   │   └── themes.xml
│                   └── drawable/
│                       └── ic_launcher_foreground.xml
```

---

### Task 1: Gradle version catalog & root build files

**Files:**
- Create: `gradle/libs.versions.toml`
- Create: `settings.gradle.kts`
- Create: `build.gradle.kts`
- Create: `gradle.properties`
- Create: `app/proguard-rules.pro`

- [ ] **Step 1: Create `gradle/libs.versions.toml`**

```toml
[versions]
agp = "8.7.0"
kotlin = "2.1.0"
hilt = "2.53.1"
room = "2.6.1"
navigation = "2.8.5"
lifecycle = "2.8.7"
compose-bom = "2024.12.01"
activity-compose = "1.9.3"
core-ktx = "1.15.0"
coroutines = "1.9.0"
gson = "2.11.0"

[libraries]
# AndroidX Core
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "core-ktx" }
androidx-activity-compose = { group = "androidx.activity", name = "activity-compose", version.ref = "activity-compose" }

# Compose BOM
compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "compose-bom" }
compose-ui = { group = "androidx.compose.ui", name = "ui" }
compose-ui-tooling = { group = "androidx.compose.ui", name = "ui-tooling" }
compose-ui-tooling-preview = { group = "androidx.compose.ui", name = "ui-tooling-preview" }
compose-material3 = { group = "androidx.compose.material3", name = "material3" }
compose-material-icons = { group = "androidx.compose.material", name = "material-icons-extended" }

# Navigation
navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigation" }

# Lifecycle
lifecycle-runtime-compose = { group = "androidx.lifecycle", name = "lifecycle-runtime-compose", version.ref = "lifecycle" }
lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "lifecycle" }

# Hilt
hilt-android = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }
hilt-compiler = { group = "com.google.dagger", name = "hilt-android-compiler", version.ref = "hilt" }
hilt-navigation-compose = { group = "androidx.hilt", name = "hilt-navigation-compose", version = "1.2.0" }

# Room
room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }
room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }

# Coroutines
coroutines-core = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-core", version.ref = "coroutines" }
coroutines-android = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-android", version.ref = "coroutines" }

# Gson
gson = { group = "com.google.code.gson", name = "gson", version.ref = "gson" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
hilt = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
ksp = { id = "com.google.devtools.ksp", version = "2.1.0-1.0.29" }
```

- [ ] **Step 2: Create `settings.gradle.kts`**

```kotlin
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolution {
    @Suppress("UnstableApiUsage")
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "StudentHub"
include(":app")
```

- [ ] **Step 3: Create root `build.gradle.kts`**

```kotlin
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
}
```

- [ ] **Step 4: Create `gradle.properties`**

```properties
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
android.useAndroidX=true
kotlin.code.style=official
android.nonTransitiveRClass=true
```

- [ ] **Step 5: Create `app/proguard-rules.pro`** (empty placeholder)

```
# Add project specific ProGuard rules here.
```

---

### Task 2: App module build script & AndroidManifest

**Files:**
- Create: `app/build.gradle.kts`
- Create: `app/src/main/AndroidManifest.xml`

- [ ] **Step 1: Create `app/build.gradle.kts`**

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.studenthub.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.studenthub.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    // AndroidX Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)

    // Compose
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons)
    debugImplementation(libs.compose.ui.tooling)

    // Navigation
    implementation(libs.navigation.compose)

    // Lifecycle
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.lifecycle.viewmodel.compose)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // Room
    implementation(libs.room.runtime)
    ksp(libs.room.compiler)
    implementation(libs.room.ktx)

    // Coroutines
    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)

    // Gson
    implementation(libs.gson)
}
```

- [ ] **Step 2: Create `app/src/main/AndroidManifest.xml`**

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
    <uses-permission android:name="android.permission.CAMERA" />
    <uses-permission android:name="android.permission.INTERNET" />

    <application
        android:name=".StudentHubApp"
        android:allowBackup="true"
        android:label="StudentHub"
        android:supportsRtl="true"
        android:theme="@style/Theme.StudentHub">
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:theme="@style/Theme.StudentHub">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>

</manifest>
```

---

### Task 3: Room entities (data models)

**Files:**
- Create: `app/src/main/java/com/studenthub/app/data/entity/CourseEntity.kt`
- Create: `app/src/main/java/com/studenthub/app/data/entity/GradeItemEntity.kt`
- Create: `app/src/main/java/com/studenthub/app/data/entity/TodoEntity.kt`

- [ ] **Step 1: Create `CourseEntity.kt`**

```kotlin
package com.studenthub.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "courses")
data class CourseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val teacher: String? = null,
    val classroom: String? = null,
    val dayOfWeek: Int,          // 1=Monday ... 7=Sunday
    val startTime: String,       // "08:00"
    val endTime: String,         // "09:35"
    val weekStart: Int = 1,
    val weekEnd: Int = 20,
    val colorHex: String = "#6366f1",
    val notes: String? = null
)
```

- [ ] **Step 2: Create `GradeItemEntity.kt`**

```kotlin
package com.studenthub.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "grade_items")
data class GradeItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val courseId: Long,
    val name: String,            // "作业", "期中", "期末"
    val weight: Int,             // 20, 30, 50
    val score: Float? = null,    // actual score, filled after exam
    val totalScore: Float = 100f // full marks, default 100
)
```

- [ ] **Step 3: Create `TodoEntity.kt`**

```kotlin
package com.studenthub.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "todos")
data class TodoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val courseId: Long? = null,  // null = standalone todo
    val dueDate: Long,           // epoch millis
    val remindAt: Long? = null,  // epoch millis, null = no reminder
    val priority: Int = 0,       // 0=low, 1=medium, 2=high
    val isCompleted: Boolean = false,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val isAiGenerated: Boolean = false
)
```

---

### Task 4: Room DAOs (data access objects)

**Files:**
- Create: `app/src/main/java/com/studenthub/app/data/local/dao/CourseDao.kt`
- Create: `app/src/main/java/com/studenthub/app/data/local/dao/GradeItemDao.kt`
- Create: `app/src/main/java/com/studenthub/app/data/local/dao/TodoDao.kt`

- [ ] **Step 1: Create `CourseDao.kt`**

```kotlin
package com.studenthub.app.data.local.dao

import androidx.room.*
import com.studenthub.app.data.entity.CourseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CourseDao {
    @Query("SELECT * FROM courses WHERE dayOfWeek = :day ORDER BY startTime ASC")
    fun getCoursesByDay(day: Int): Flow<List<CourseEntity>>

    @Query("SELECT * FROM courses ORDER BY dayOfWeek ASC, startTime ASC")
    fun getAllCourses(): Flow<List<CourseEntity>>

    @Query("SELECT * FROM courses WHERE id = :id")
    suspend fun getCourseById(id: Long): CourseEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourse(course: CourseEntity): Long

    @Update
    suspend fun updateCourse(course: CourseEntity)

    @Delete
    suspend fun deleteCourse(course: CourseEntity)

    @Query("SELECT * FROM courses WHERE id = :id")
    fun getCourseByIdFlow(id: Long): Flow<CourseEntity?>
}
```

- [ ] **Step 2: Create `GradeItemDao.kt`**

```kotlin
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
```

- [ ] **Step 3: Create `TodoDao.kt`**

```kotlin
package com.studenthub.app.data.local.dao

import androidx.room.*
import com.studenthub.app.data.entity.TodoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TodoDao {
    @Query("SELECT * FROM todos ORDER BY isCompleted ASC, dueDate ASC")
    fun getAllTodos(): Flow<List<TodoEntity>>

    @Query("SELECT * FROM todos WHERE courseId = :courseId ORDER BY dueDate ASC")
    fun getTodosForCourse(courseId: Long): Flow<List<TodoEntity>>

    @Query("SELECT * FROM todos WHERE courseId IS NULL ORDER BY dueDate ASC")
    fun getStandaloneTodos(): Flow<List<TodoEntity>>

    @Query("SELECT COUNT(*) FROM todos WHERE courseId = :courseId AND isCompleted = 0")
    fun getPendingCountForCourse(courseId: Long): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTodo(todo: TodoEntity): Long

    @Update
    suspend fun updateTodo(todo: TodoEntity)

    @Delete
    suspend fun deleteTodo(todo: TodoEntity)
}
```

---

### Task 5: Room database, type converters & AppModule

**Files:**
- Create: `app/src/main/java/com/studenthub/app/data/local/Converters.kt`
- Create: `app/src/main/java/com/studenthub/app/data/local/AppDatabase.kt`
- Create: `app/src/main/java/com/studenthub/app/di/AppModule.kt`

- [ ] **Step 1: Create `Converters.kt`**

```kotlin
package com.studenthub.app.data.local

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Long? = value

    @TypeConverter
    fun toTimestamp(value: Long?): Long? = value
}
```

- [ ] **Step 2: Create `AppDatabase.kt`**

```kotlin
package com.studenthub.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.studenthub.app.data.entity.CourseEntity
import com.studenthub.app.data.entity.GradeItemEntity
import com.studenthub.app.data.entity.TodoEntity
import com.studenthub.app.data.local.dao.CourseDao
import com.studenthub.app.data.local.dao.GradeItemDao
import com.studenthub.app.data.local.dao.TodoDao

@Database(
    entities = [CourseEntity::class, GradeItemEntity::class, TodoEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun courseDao(): CourseDao
    abstract fun gradeItemDao(): GradeItemDao
    abstract fun todoDao(): TodoDao
}
```

- [ ] **Step 3: Create `AppModule.kt`**

```kotlin
package com.studenthub.app.di

import android.content.Context
import androidx.room.Room
import com.studenthub.app.data.local.AppDatabase
import com.studenthub.app.data.local.dao.CourseDao
import com.studenthub.app.data.local.dao.GradeItemDao
import com.studenthub.app.data.local.dao.TodoDao
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
}
```

---

### Task 6: CourseRepository (domain layer)

**Files:**
- Create: `app/src/main/java/com/studenthub/app/domain/repository/CourseRepository.kt`

- [ ] **Step 1: Create `CourseRepository.kt`**

```kotlin
package com.studenthub.app.domain.repository

import com.studenthub.app.data.entity.CourseEntity
import com.studenthub.app.data.local.dao.CourseDao
import com.studenthub.app.data.local.dao.TodoDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Singleton

data class CourseWithPendingCount(
    val course: CourseEntity,
    val pendingTodoCount: Int
)

@Singleton
class CourseRepository @Inject constructor(
    private val courseDao: CourseDao,
    private val todoDao: TodoDao
) {
    fun getAllCourses(): Flow<List<CourseEntity>> = courseDao.getAllCourses()

    fun getCoursesByDay(day: Int): Flow<List<CourseEntity>> = courseDao.getCoursesByDay(day)

    fun getCoursesByDayWithPendingCount(day: Int): Flow<List<CourseWithPendingCount>> {
        return courseDao.getCoursesByDay(day).combine(todoDao.getAllTodos()) { courses, todos ->
            courses.map { course ->
                val pendingCount = todos.count {
                    it.courseId == course.id && !it.isCompleted
                }
                CourseWithPendingCount(course, pendingCount)
            }
        }
    }

    suspend fun getCourseById(id: Long): CourseEntity? = courseDao.getCourseById(id)

    fun getCourseByIdFlow(id: Long): Flow<CourseEntity?> = courseDao.getCourseByIdFlow(id)

    suspend fun insertCourse(course: CourseEntity): Long = courseDao.insertCourse(course)

    suspend fun updateCourse(course: CourseEntity) = courseDao.updateCourse(course)

    suspend fun deleteCourse(course: CourseEntity) = courseDao.deleteCourse(course)
}
```

---

### Task 7: Application class & theme

**Files:**
- Create: `app/src/main/java/com/studenthub/app/StudentHubApp.kt`
- Create: `app/src/main/java/com/studenthub/app/ui/theme/Color.kt`
- Create: `app/src/main/java/com/studenthub/app/ui/theme/Type.kt`
- Create: `app/src/main/java/com/studenthub/app/ui/theme/Theme.kt`
- Create: `app/src/main/res/values/strings.xml`
- Create: `app/src/main/res/values/themes.xml`

- [ ] **Step 1: Create `StudentHubApp.kt`**

```kotlin
package com.studenthub.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class StudentHubApp : Application()
```

- [ ] **Step 2: Create `Color.kt`**

```kotlin
package com.studenthub.app.ui.theme

import androidx.compose.ui.graphics.Color

// Primary - Indigo
val Primary = Color(0xFF6366F1)
val PrimaryVariant = Color(0xFF4F46E5)
val PrimaryContainer = Color(0xFFEEF2FF)
val OnPrimaryContainer = Color(0xFF312E81)

// Secondary - Purple
val Secondary = Color(0xFF8B5CF6)
val SecondaryContainer = Color(0xFFF5F3FF)

// Surface
val Surface = Color(0xFFFAFAFA)
val SurfaceVariant = Color(0xFFF3F4F6)
val OnSurface = Color(0xFF111827)
val OnSurfaceVariant = Color(0xFF6B7280)

// Success / Error / Warning
val Success = Color(0xFF22C55E)
val Error = Color(0xFFEF4444)
val Warning = Color(0xFFF59E0B)

// Course colors
val CourseColors = listOf(
    Color(0xFF6366F1), // Indigo
    Color(0xFF22C55E), // Green
    Color(0xFFF59E0B), // Amber
    Color(0xFFEF4444), // Red
    Color(0xFFEC4899), // Pink
    Color(0xFF06B6D4), // Cyan
    Color(0xFF8B5CF6), // Purple
    Color(0xFFF97316), // Orange
)
```

- [ ] **Step 3: Create `Type.kt`**

```kotlin
package com.studenthub.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Typography = Typography(
    headlineLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp
    ),
    headlineMedium = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 20.sp
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    bodySmall = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    labelLarge = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    labelSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp
    )
)
```

- [ ] **Step 4: Create `Theme.kt`**

```kotlin
package com.studenthub.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,
    secondary = Secondary,
    secondaryContainer = SecondaryContainer,
    surface = Surface,
    surfaceVariant = SurfaceVariant,
    onSurface = OnSurface,
    onSurfaceVariant = OnSurfaceVariant,
    error = Error,
    background = androidx.compose.ui.graphics.Color.White,
    onBackground = OnSurface
)

@Composable
fun StudentHubTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
```

- [ ] **Step 5: Create `res/values/strings.xml`**

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="app_name">StudentHub</string>
    <string name="nav_home">首页</string>
    <string name="nav_schedule">课表</string>
    <string name="nav_todo">待办</string>
    <string name="nav_profile">我的</string>
</resources>
```

- [ ] **Step 6: Create `res/values/themes.xml`**

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <style name="Theme.StudentHub" parent="android:Theme.Material.Light.NoActionBar">
        <item name="android:statusBarColor">#FF6366F1</item>
    </style>
</resources>
```

---

### Task 8: Bottom navigation & NavGraph

**Files:**
- Create: `app/src/main/java/com/studenthub/app/ui/navigation/BottomNavBar.kt`
- Create: `app/src/main/java/com/studenthub/app/ui/navigation/AppNavGraph.kt`

- [ ] **Step 1: Create `BottomNavBar.kt`**

```kotlin
package com.studenthub.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

sealed class BottomNavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    data object Home : BottomNavItem("home", "首页", Icons.Filled.Home, Icons.Outlined.Home)
    data object Schedule : BottomNavItem("schedule", "课表", Icons.Filled.Schedule, Icons.Outlined.Schedule)
    data object Todo : BottomNavItem("todo", "待办", Icons.Filled.CheckCircle, Icons.Outlined.CheckCircle)
    data object Profile : BottomNavItem("profile", "我的", Icons.Filled.Person, Icons.Outlined.Person)
}

val bottomNavItems = listOf(
    BottomNavItem.Home,
    BottomNavItem.Schedule,
    BottomNavItem.Todo,
    BottomNavItem.Profile
)

@Composable
fun BottomNavBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
        bottomNavItems.forEach { item ->
            val isSelected = currentRoute == item.route
            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(
                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label
                    )
                },
                label = { Text(item.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}
```

- [ ] **Step 2: Create `AppNavGraph.kt`**

```kotlin
package com.studenthub.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.studenthub.app.ui.screens.home.HomeScreen
import com.studenthub.app.ui.screens.profile.ProfileScreen
import com.studenthub.app.ui.screens.schedule.AddCourseScreen
import com.studenthub.app.ui.screens.schedule.CourseDetailScreen
import com.studenthub.app.ui.screens.schedule.ScheduleScreen
import com.studenthub.app.ui.screens.todo.TodoListScreen

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") { HomeScreen() }
        composable("schedule") { ScheduleScreen(navController) }
        composable("todo") { TodoListScreen() }
        composable("profile") { ProfileScreen() }

        composable(
            route = "course_detail/{courseId}",
            arguments = listOf(navArgument("courseId") { type = NavType.LongType })
        ) { backStackEntry ->
            val courseId = backStackEntry.arguments?.getLong("courseId") ?: return@composable
            CourseDetailScreen(courseId = courseId, navController = navController)
        }

        composable("add_course") { AddCourseScreen(navController = navController) }

        composable(
            route = "edit_course/{courseId}",
            arguments = listOf(navArgument("courseId") { type = NavType.LongType })
        ) { backStackEntry ->
            val courseId = backStackEntry.arguments?.getLong("courseId") ?: return@composable
            AddCourseScreen(navController = navController, editCourseId = courseId)
        }
    }
}
```

---

### Task 9: MainActivity with scaffold

**Files:**
- Create: `app/src/main/java/com/studenthub/app/MainActivity.kt`

- [ ] **Step 1: Create `MainActivity.kt`**

```kotlin
package com.studenthub.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.studenthub.app.ui.navigation.AppNavGraph
import com.studenthub.app.ui.navigation.BottomNavBar
import com.studenthub.app.ui.theme.StudentHubTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StudentHubTheme {
                StudentHubMainScreen()
            }
        }
    }
}

@Composable
fun StudentHubMainScreen() {
    val navController = rememberNavController()
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = { BottomNavBar(navController) }
    ) { innerPadding ->
        AppNavGraph(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}
```

We need to update AppNavGraph to accept the modifier parameter:

- [ ] **Step 2: Update `AppNavGraph.kt` to accept modifier**

Edit `AppNavGraph.kt` — add `modifier: Modifier = Modifier` parameter and pass it to NavHost:

```kotlin
@Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = "home",
        modifier = modifier
    ) {
        // ... same composable routes as above
    }
}
```

---

### Task 10: Placeholder screens for Home, Todo & Profile

**Files:**
- Create: `app/src/main/java/com/studenthub/app/ui/screens/home/HomeScreen.kt`
- Create: `app/src/main/java/com/studenthub/app/ui/screens/todo/TodoListScreen.kt`
- Create: `app/src/main/java/com/studenthub/app/ui/screens/profile/ProfileScreen.kt`

- [ ] **Step 1: Create `HomeScreen.kt`**

```kotlin
package com.studenthub.app.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "早上好 👋",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "首页功能将在后续阶段完善",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
```

- [ ] **Step 2: Create `TodoListScreen.kt`**

```kotlin
package com.studenthub.app.ui.screens.todo

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun TodoListScreen() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "待办",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "待办功能将在 Phase 2 实现",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
```

- [ ] **Step 3: Create `ProfileScreen.kt`**

```kotlin
package com.studenthub.app.ui.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun ProfileScreen() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "我的",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "设置页面将在后续阶段完善",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
```

---

### Task 11: Schedule screen (weekly view with day tabs)

**Files:**
- Create: `app/src/main/java/com/studenthub/app/ui/screens/schedule/ScheduleScreen.kt`
- Create: `app/src/main/java/com/studenthub/app/ui/screens/schedule/ScheduleViewModel.kt`

- [ ] **Step 1: Create `ScheduleViewModel.kt`**

```kotlin
package com.studenthub.app.ui.screens.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studenthub.app.domain.repository.CourseRepository
import com.studenthub.app.data.entity.CourseEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val courseRepository: CourseRepository
) : ViewModel() {

    private val _selectedDay = MutableStateFlow(getCurrentDayOfWeek())
    val selectedDay: StateFlow<Int> = _selectedDay.asStateFlow()

    val courses: StateFlow<List<CourseEntity>> = _selectedDay
        .flatMapLatest { day -> courseRepository.getCoursesByDay(day) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCourses: StateFlow<List<CourseEntity>> = courseRepository.getAllCourses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectDay(day: Int) {
        _selectedDay.value = day
    }

    suspend fun deleteCourse(course: CourseEntity) {
        courseRepository.deleteCourse(course)
    }

    private fun getCurrentDayOfWeek(): Int {
        val calendar = java.util.Calendar.getInstance()
        val dayOfWeek = calendar.get(java.util.Calendar.DAY_OF_WEEK)
        return when (dayOfWeek) {
            java.util.Calendar.MONDAY -> 1
            java.util.Calendar.TUESDAY -> 2
            java.util.Calendar.WEDNESDAY -> 3
            java.util.Calendar.THURSDAY -> 4
            java.util.Calendar.FRIDAY -> 5
            java.util.Calendar.SATURDAY -> 6
            java.util.Calendar.SUNDAY -> 7
            else -> 1
        }
    }
}
```

- [ ] **Step 2: Create `ScheduleScreen.kt`**

```kotlin
package com.studenthub.app.ui.screens.schedule

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.studenthub.app.data.entity.CourseEntity
import com.studenthub.app.ui.theme.CourseColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    navController: NavController,
    viewModel: ScheduleViewModel = hiltViewModel()
) {
    val selectedDay by viewModel.selectedDay.collectAsState()
    val courses by viewModel.courses.collectAsState()
    val allCourses by viewModel.allCourses.collectAsState()

    val dayLabels = listOf("一", "二", "三", "四", "五", "六", "日")

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp)) {
                Text(
                    text = "课表",
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "2026年 春季学期",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Day tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                dayLabels.forEachIndexed { index, label ->
                    val dayNumber = index + 1
                    val isSelected = selectedDay == dayNumber
                    val hasCourse = allCourses.any { it.dayOfWeek == dayNumber }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .clickable { viewModel.selectDay(dayNumber) }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (hasCourse && !isSelected) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Box(
                                    modifier = Modifier
                                        .size(4.dp)
                                        .background(
                                            MaterialTheme.colorScheme.primary,
                                            CircleShape
                                        )
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Course list or empty state
            if (courses.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "这天还没有课程",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "点击右下角 + 添加课程",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(courses, key = { it.id }) { course ->
                        CourseBlock(
                            course = course,
                            onClick = { navController.navigate("course_detail/${course.id}") }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }

            // Bottom action buttons
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(
                        onClick = { /* Camera recognition in Phase 4 */ },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("拍课表")
                    }

                    FilledTonalButton(
                        onClick = { navController.navigate("add_course") },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("添加课程")
                    }
                }
            }
        }
    }
}

@Composable
fun CourseBlock(
    course: CourseEntity,
    onClick: () -> Unit
) {
    val courseColor = Color(android.graphics.Color.parseColor(course.colorHex))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = courseColor.copy(alpha = 0.08f))
    ) {
        Row(modifier = Modifier.padding(14.dp)) {
            // Time column
            Column(
                modifier = Modifier.width(48.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = course.startTime.substringBefore(":"),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = courseColor
                )
                Text(
                    text = course.startTime.substringAfter(":"),
                    style = MaterialTheme.typography.labelSmall,
                    color = courseColor.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(20.dp)
                        .background(courseColor.copy(alpha = 0.2f), RoundedCornerShape(1.dp))
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = course.endTime.substringBefore(":"),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = courseColor.copy(alpha = 0.6f)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Vertical color bar
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(courseColor, RoundedCornerShape(2.dp))
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Course info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = course.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))

                if (!course.classroom.isNullOrBlank()) {
                    Text(
                        text = "📍 ${course.classroom}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (!course.teacher.isNullOrBlank()) {
                    Text(
                        text = "👨‍🏫 ${course.teacher}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
```

---

### Task 12: Add/Edit course screen

**Files:**
- Create: `app/src/main/java/com/studenthub/app/ui/screens/schedule/AddCourseScreen.kt`
- Create: `app/src/main/java/com/studenthub/app/ui/screens/schedule/AddCourseViewModel.kt`

- [ ] **Step 1: Create `AddCourseViewModel.kt`**

```kotlin
package com.studenthub.app.ui.screens.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studenthub.app.data.entity.CourseEntity
import com.studenthub.app.domain.repository.CourseRepository
import com.studenthub.app.ui.theme.CourseColors
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddCourseUiState(
    val name: String = "",
    val teacher: String = "",
    val classroom: String = "",
    val dayOfWeek: Int = 1,
    val startHour: String = "08",
    val startMinute: String = "00",
    val endHour: String = "09",
    val endMinute: String = "35",
    val weekStart: String = "1",
    val weekEnd: String = "20",
    val colorHex: String = "#6366f1",
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    val nameError: String? = null
)

@HiltViewModel
class AddCourseViewModel @Inject constructor(
    private val courseRepository: CourseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddCourseUiState())
    val uiState: StateFlow<AddCourseUiState> = _uiState.asStateFlow()

    private var editingCourseId: Long? = null

    fun loadCourse(courseId: Long) {
        viewModelScope.launch {
            val course = courseRepository.getCourseById(courseId) ?: return@launch
            editingCourseId = course.id
            _uiState.value = AddCourseUiState(
                name = course.name,
                teacher = course.teacher ?: "",
                classroom = course.classroom ?: "",
                dayOfWeek = course.dayOfWeek,
                startHour = course.startTime.substringBefore(":"),
                startMinute = course.startTime.substringAfter(":"),
                endHour = course.endTime.substringBefore(":"),
                endMinute = course.endTime.substringAfter(":"),
                weekStart = course.weekStart.toString(),
                weekEnd = course.weekEnd.toString(),
                colorHex = course.colorHex,
                isEditing = true
            )
        }
    }

    fun updateName(name: String) {
        _uiState.value = _uiState.value.copy(name = name, nameError = null)
    }

    fun updateTeacher(teacher: String) {
        _uiState.value = _uiState.value.copy(teacher = teacher)
    }

    fun updateClassroom(classroom: String) {
        _uiState.value = _uiState.value.copy(classroom = classroom)
    }

    fun updateDayOfWeek(day: Int) {
        _uiState.value = _uiState.value.copy(dayOfWeek = day)
    }

    fun updateStartTime(hour: String, minute: String) {
        _uiState.value = _uiState.value.copy(startHour = hour, startMinute = minute)
    }

    fun updateEndTime(hour: String, minute: String) {
        _uiState.value = _uiState.value.copy(endHour = hour, endMinute = minute)
    }

    fun updateWeekRange(start: String, end: String) {
        _uiState.value = _uiState.value.copy(weekStart = start, weekEnd = end)
    }

    fun updateColorHex(color: String) {
        _uiState.value = _uiState.value.copy(colorHex = color)
    }

    fun save(onSuccess: () -> Unit) {
        val state = _uiState.value

        if (state.name.isBlank()) {
            _uiState.value = state.copy(nameError = "课程名称不能为空")
            return
        }

        _uiState.value = state.copy(isSaving = true)

        viewModelScope.launch {
            val course = CourseEntity(
                id = editingCourseId ?: 0,
                name = state.name.trim(),
                teacher = state.teacher.trim().ifBlank { null },
                classroom = state.classroom.trim().ifBlank { null },
                dayOfWeek = state.dayOfWeek,
                startTime = "${state.startHour}:${state.startMinute}",
                endTime = "${state.endHour}:${state.endMinute}",
                weekStart = state.weekStart.toIntOrNull() ?: 1,
                weekEnd = state.weekEnd.toIntOrNull() ?: 20,
                colorHex = state.colorHex
            )

            if (editingCourseId != null) {
                courseRepository.updateCourse(course)
            } else {
                courseRepository.insertCourse(course)
            }

            onSuccess()
        }
    }
}
```

- [ ] **Step 2: Create `AddCourseScreen.kt`**

```kotlin
package com.studenthub.app.ui.screens.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.studenthub.app.ui.theme.CourseColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCourseScreen(
    navController: NavController,
    editCourseId: Long? = null,
    viewModel: AddCourseViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(editCourseId) {
        if (editCourseId != null && editCourseId > 0) {
            viewModel.loadCourse(editCourseId)
        }
    }

    val dayLabels = listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (uiState.isEditing) "编辑课程" else "添加课程",
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            // Course Name
            Text("课程名称", style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = uiState.name,
                onValueChange = viewModel::updateName,
                placeholder = { Text("例如：高等数学") },
                isError = uiState.nameError != null,
                supportingText = uiState.nameError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Teacher & Classroom
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("教师", style = MaterialTheme.typography.labelLarge)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = uiState.teacher,
                        onValueChange = viewModel::updateTeacher,
                        placeholder = { Text("选填") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("教室", style = MaterialTheme.typography.labelLarge)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = uiState.classroom,
                        onValueChange = viewModel::updateClassroom,
                        placeholder = { Text("选填") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Day of Week
            Text("上课日期", style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                dayLabels.forEachIndexed { index, label ->
                    val dayNum = index + 1
                    val isSelected = uiState.dayOfWeek == dayNum
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .clickable { viewModel.updateDayOfWeek(dayNum) }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Time Range
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("开始时间", style = MaterialTheme.typography.labelLarge)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = uiState.startHour,
                            onValueChange = { viewModel.updateStartTime(it, uiState.startMinute) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            placeholder = { Text("08") }
                        )
                        Text(
                            text = ":",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(top = 12.dp)
                        )
                        OutlinedTextField(
                            value = uiState.startMinute,
                            onValueChange = { viewModel.updateStartTime(uiState.startHour, it) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            placeholder = { Text("00") }
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("结束时间", style = MaterialTheme.typography.labelLarge)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = uiState.endHour,
                            onValueChange = { viewModel.updateEndTime(it, uiState.endMinute) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            placeholder = { Text("09") }
                        )
                        Text(
                            text = ":",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(top = 12.dp)
                        )
                        OutlinedTextField(
                            value = uiState.endMinute,
                            onValueChange = { viewModel.updateEndTime(uiState.endHour, it) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            placeholder = { Text("35") }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Week Range
            Text("周数范围", style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = uiState.weekStart,
                    onValueChange = { viewModel.updateWeekRange(it, uiState.weekEnd) },
                    modifier = Modifier.width(80.dp),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    placeholder = { Text("1") }
                )
                Text("—", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = uiState.weekEnd,
                    onValueChange = { viewModel.updateWeekRange(uiState.weekStart, it) },
                    modifier = Modifier.width(80.dp),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    placeholder = { Text("20") }
                )
                Text("周", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Color Picker
            Text("课程颜色", style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CourseColors.forEach { color ->
                    val hex = color.toArgbColorHex()
                    val isSelected = uiState.colorHex == hex
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(color)
                            .then(
                                if (isSelected) Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                else Modifier.border(2.dp, color.copy(alpha = 0.3f), CircleShape)
                            )
                            .clickable { viewModel.updateColorHex(hex) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Save Button
            Button(
                onClick = { viewModel.save(onSuccess = { navController.popBackStack() }) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !uiState.isSaving,
                shape = RoundedCornerShape(14.dp)
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        if (uiState.isEditing) "保存修改" else "添加课程",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// Helper to convert Compose Color to hex string
fun Color.toArgbColorHex(): String {
    val red = (this.red * 255).toInt()
    val green = (this.green * 255).toInt()
    val blue = (this.blue * 255).toInt()
    return "#%02x%02x%02x".format(red, green, blue)
}
```

---

### Task 13: Course detail screen

**Files:**
- Create: `app/src/main/java/com/studenthub/app/ui/screens/schedule/CourseDetailScreen.kt`

- [ ] **Step 1: Create `CourseDetailScreen.kt`**

```kotlin
package com.studenthub.app.ui.screens.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.studenthub.app.domain.repository.CourseRepository
import com.studenthub.app.data.entity.CourseEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CourseDetailViewModel @Inject constructor(
    private val courseRepository: CourseRepository
) : ViewModel() {

    private val _course = MutableStateFlow<CourseEntity?>(null)
    val course: StateFlow<CourseEntity?> = _course.asStateFlow()

    private val _showDeleteConfirm = MutableStateFlow(false)
    val showDeleteConfirm: StateFlow<Boolean> = _showDeleteConfirm.asStateFlow()

    fun loadCourse(courseId: Long) {
        viewModelScope.launch {
            courseRepository.getCourseByIdFlow(courseId).collect { entity ->
                _course.value = entity
            }
        }
    }

    fun showDeleteDialog() {
        _showDeleteConfirm.value = true
    }

    fun dismissDeleteDialog() {
        _showDeleteConfirm.value = false
    }

    fun deleteCourse(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _course.value?.let { courseRepository.deleteCourse(it) }
            onSuccess()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseDetailScreen(
    courseId: Long,
    navController: NavController,
    viewModel: CourseDetailViewModel = hiltViewModel()
) {
    val course by viewModel.course.collectAsStateWithLifecycle()
    val showDeleteConfirm by viewModel.showDeleteConfirm.collectAsStateWithLifecycle()

    LaunchedEffect(courseId) {
        viewModel.loadCourse(courseId)
    }

    // Delete confirmation dialog
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = viewModel::dismissDeleteDialog,
            title = { Text("删除课程") },
            text = { Text("确定要删除「${course?.name}」吗？此操作不可撤销。") },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.deleteCourse(onSuccess = { navController.popBackStack() }) },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissDeleteDialog) {
                    Text("取消")
                }
            }
        )
    }

    val dayNames = listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(course?.name ?: "课程详情", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    if (course != null) {
                        IconButton(onClick = { navController.navigate("edit_course/${courseId}") }) {
                            Icon(Icons.Default.Edit, contentDescription = "编辑")
                        }
                        IconButton(onClick = viewModel::showDeleteDialog) {
                            Icon(Icons.Default.Delete, contentDescription = "删除",
                                tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        course?.let { c ->
            val courseColor = Color(android.graphics.Color.parseColor(c.colorHex))

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Course info banner
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = courseColor.copy(alpha = 0.08f))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = c.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = courseColor
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        if (!c.teacher.isNullOrBlank() || !c.classroom.isNullOrBlank()) {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = courseColor.copy(alpha = 0.15f)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Info grid
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (!c.teacher.isNullOrBlank()) {
                                InfoRow(label = "👨‍🏫 教师", value = c.teacher)
                            }
                            if (!c.classroom.isNullOrBlank()) {
                                InfoRow(label = "🏫 教室", value = c.classroom)
                            }
                            InfoRow(label = "⏰ 时间", value = "${dayNames[c.dayOfWeek - 1]} ${c.startTime}—${c.endTime}")
                            InfoRow(label = "📅 周数", value = "第${c.weekStart}—${c.weekEnd}周")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Grade Section (placeholder for Phase 3)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "📊 成绩构成",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Placeholder grade items
                        listOf("作业 20%", "期中考试 30%", "期末考试 50%").forEach { item ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(item, style = MaterialTheme.typography.bodyMedium)
                                    Text(
                                        "待录入",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Notes Section (placeholder for Phase 3)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "📝 备注",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        if (!c.notes.isNullOrBlank()) {
                            Text(
                                text = c.notes,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        } else {
                            Text(
                                text = "暂无备注",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        } ?: run {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}
```

---

### Task 14: Drawable launcher icon

**Files:**
- Create: `app/src/main/res/drawable/ic_launcher_foreground.xml`

- [ ] **Step 1: Create simple vector drawable launcher icon**

```xml
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    <group android:translateX="22"
        android:translateY="22">
        <!-- Book icon -->
        <path
            android:fillColor="#6366F1"
            android:pathData="M32,4C16.536,4 4,16.536 4,32s12.536,28 28,28s28,-12.536 28,-28S47.464,4 32,4zM32,52C20.954,52 12,43.046 12,32S20.954,12 32,12s20,8.954 20,20S43.046,52 32,52z"/>
        <path
            android:fillColor="#FFFFFF"
            android:pathData="M24,22h16v2H24zM24,28h12v2H24zM24,34h14v2H24z"/>
    </group>
</vector>
```

---

## Spec Coverage Check

- ✅ **Project scaffolding** — Tasks 1-2 (Gradle files, manifest)
- ✅ **Room database** — Tasks 3-5 (entities, DAOs, database, type converters)
- ✅ **Hilt DI** — Task 5 (AppModule providing DB & DAOs)
- ✅ **Material3 theme** — Task 7 (Color, Type, Theme composable)
- ✅ **Bottom navigation (4 tabs)** — Task 8 (BottomNavBar + NavItems)
- ✅ **Navigation graph** — Task 8 (AppNavGraph with routes)
- ✅ **MainActivity with scaffold** — Task 9 (MainActivity + StudentHubMainScreen)
- ✅ **Schedule CRUD** — Tasks 11-13 (ScheduleScreen, AddCourseScreen, CourseDetailScreen)
- ✅ **Course weekly view with day tabs** — Task 11 (ScheduleScreen)
- ✅ **Add/Edit course form** — Task 12 (AddCourseScreen + ViewModel)
- ✅ **Course detail page** — Task 13 (CourseDetailScreen + ViewModel)
- ✅ **Placeholder screens** — Task 10 (Home, Todo, Profile)

**Note:** Course color picker shows actual CourseColors in AddCourseScreen. The `toArgbColorHex()` helper function is included in AddCourseScreen.kt.

---

## Plan complete and saved to `docs/superpowers/plans/2026-06-21-StudentHub-phase1.md`

Two execution options:

**1. Subagent-Driven (recommended)** — I dispatch a fresh subagent per task, review between tasks, fast iteration. This is best for generating all 20+ files.

**2. Inline Execution** — Execute tasks in this session one by one with checkpoints.

**Which approach?** I'd recommend **subagent-driven** given the number of files to create, so we can work through tasks efficiently while I review each step.
