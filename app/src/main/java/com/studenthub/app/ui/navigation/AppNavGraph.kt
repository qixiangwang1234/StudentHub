package com.studenthub.app.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.cubicBezier
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.studenthub.app.ui.screens.home.HomeScreen
import com.studenthub.app.ui.screens.profile.ProfileScreen
import com.studenthub.app.ui.screens.profile.SettingsScreen
import com.studenthub.app.ui.screens.schedule.AddCourseScreen
import com.studenthub.app.ui.screens.schedule.CameraScheduleScreen
import com.studenthub.app.ui.screens.schedule.CourseDetailScreen
import com.studenthub.app.ui.screens.schedule.ScheduleScreen
import com.studenthub.app.ui.screens.ai.AiChatScreen
import com.studenthub.app.ui.screens.todo.AddTodoScreen
import com.studenthub.app.ui.screens.todo.TodoListScreen

// Shared transition: sliding from right, fading
private val slideIn = slideInHorizontally(
    animationSpec = tween(280, easing = EaseOutCubic)
) { it / 8 } + fadeIn(tween(280))

private val slideOut = slideOutHorizontally(
    animationSpec = tween(200, easing = EaseInCubic)
) { it / 4 } + fadeOut(tween(200))

// For bottom tabs — instant, no slide
private val fadeInFast = fadeIn(tween(200))
private val fadeOutFast = fadeOut(tween(150))

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
        // ── Bottom Nav Tabs (fade only) ──
        composable(
            "home",
            enterTransition = { fadeInFast },
            exitTransition = { fadeOutFast },
            popEnterTransition = { fadeInFast },
            popExitTransition = { fadeOutFast }
        ) { HomeScreen(navController = navController) }

        composable(
            "schedule",
            enterTransition = { fadeInFast },
            exitTransition = { fadeOutFast },
            popEnterTransition = { fadeInFast },
            popExitTransition = { fadeOutFast }
        ) { ScheduleScreen(navController) }

        composable(
            "todo",
            enterTransition = { fadeInFast },
            exitTransition = { fadeOutFast },
            popEnterTransition = { fadeInFast },
            popExitTransition = { fadeOutFast }
        ) { TodoListScreen(navController = navController) }

        composable(
            "profile",
            enterTransition = { fadeInFast },
            exitTransition = { fadeOutFast },
            popEnterTransition = { fadeInFast },
            popExitTransition = { fadeOutFast }
        ) { ProfileScreen(navController = navController) }

        // ── Detail screens (slide right) ──
        composable(
            "settings",
            enterTransition = { slideIn },
            exitTransition = { slideOut },
            popEnterTransition = { fadeInFast },
            popExitTransition = { slideOut }
        ) { SettingsScreen(navController = navController) }
        composable(
            "ai_chat",
            enterTransition = { slideIn },
            exitTransition = { slideOut },
            popEnterTransition = { fadeInFast },
            popExitTransition = { slideOut }
        ) { AiChatScreen(navController = navController) }

        composable("add_todo",
            enterTransition = { slideIn },
            exitTransition = { slideOut },
            popEnterTransition = { fadeInFast },
            popExitTransition = { slideOut }
        ) { AddTodoScreen(navController = navController) }

        composable(
            route = "edit_todo/{todoId}",
            arguments = listOf(navArgument("todoId") { type = NavType.LongType }),
            enterTransition = { slideIn },
            exitTransition = { slideOut },
            popEnterTransition = { fadeInFast },
            popExitTransition = { slideOut }
        ) { backStackEntry ->
            val todoId = backStackEntry.arguments?.getLong("todoId") ?: return@composable
            AddTodoScreen(navController = navController, editTodoId = todoId)
        }

        composable(
            route = "course_detail/{courseId}",
            arguments = listOf(navArgument("courseId") { type = NavType.LongType }),
            enterTransition = { slideIn },
            exitTransition = { slideOut },
            popEnterTransition = { fadeInFast },
            popExitTransition = { slideOut }
        ) { backStackEntry ->
            val courseId = backStackEntry.arguments?.getLong("courseId") ?: return@composable
            CourseDetailScreen(courseId = courseId, navController = navController)
        }

        composable(
            "camera_schedule",
            enterTransition = { slideIn },
            exitTransition = { slideOut },
            popEnterTransition = { fadeInFast },
            popExitTransition = { slideOut }
        ) { CameraScheduleScreen(navController = navController) }

        composable(
            "add_course",
            enterTransition = { slideIn },
            exitTransition = { slideOut },
            popEnterTransition = { fadeInFast },
            popExitTransition = { slideOut }
        ) { AddCourseScreen(navController = navController) }

        composable(
            route = "edit_course/{courseId}",
            arguments = listOf(navArgument("courseId") { type = NavType.LongType }),
            enterTransition = { slideIn },
            exitTransition = { slideOut },
            popEnterTransition = { fadeInFast },
            popExitTransition = { slideOut }
        ) { backStackEntry ->
            val courseId = backStackEntry.arguments?.getLong("courseId") ?: return@composable
            AddCourseScreen(navController = navController, editCourseId = courseId)
        }
    }
}

private val EaseOutCubic = androidx.compose.animation.core.cubicBezier(0.33, 1.0, 0.68, 1.0)
private val EaseInCubic = androidx.compose.animation.core.cubicBezier(0.32, 0.0, 0.67, 0.0)
