package com.studenthub.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.studenthub.app.ui.screens.home.HomeScreen
import com.studenthub.app.ui.screens.profile.ProfileScreen
import com.studenthub.app.ui.screens.schedule.AddCourseScreen
import com.studenthub.app.ui.screens.schedule.CameraScheduleScreen
import com.studenthub.app.ui.screens.schedule.CourseDetailScreen
import com.studenthub.app.ui.screens.schedule.ScheduleScreen
import com.studenthub.app.ui.screens.ai.AiChatScreen
import com.studenthub.app.ui.screens.todo.AddTodoScreen
import com.studenthub.app.ui.screens.todo.TodoListScreen

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
        composable("home") { HomeScreen(navController = navController) }
        composable("ai_chat") { AiChatScreen(navController = navController) }
        composable("schedule") { ScheduleScreen(navController) }
        composable("todo") { TodoListScreen(navController = navController) }
        composable("profile") { ProfileScreen() }

        composable("add_todo") { AddTodoScreen(navController = navController) }

        composable(
            route = "edit_todo/{todoId}",
            arguments = listOf(navArgument("todoId") { type = NavType.LongType })
        ) { backStackEntry ->
            val todoId = backStackEntry.arguments?.getLong("todoId") ?: return@composable
            AddTodoScreen(navController = navController, editTodoId = todoId)
        }

        composable(
            route = "course_detail/{courseId}",
            arguments = listOf(navArgument("courseId") { type = NavType.LongType })
        ) { backStackEntry ->
            val courseId = backStackEntry.arguments?.getLong("courseId") ?: return@composable
            CourseDetailScreen(courseId = courseId, navController = navController)
        }

        composable("camera_schedule") { CameraScheduleScreen(navController = navController) }

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
