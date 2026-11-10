package com.cdpg.kmppoc.ui

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.cdpg.dhruva.core.session.SessionState
import com.cdpg.dhruva.di.AppGraph
import com.cdpg.kmppoc.ui.auth.FullNameScreen
import com.cdpg.kmppoc.ui.auth.IntroScreen
import com.cdpg.kmppoc.ui.auth.LoginScreen
import com.cdpg.kmppoc.ui.auth.OtpScreen
import com.cdpg.kmppoc.ui.auth.SplashScreen
import com.cdpg.kmppoc.ui.auth.WelcomeScreen
import com.cdpg.kmppoc.ui.common.LocalLocalizer
import com.cdpg.kmppoc.ui.main.MainScreen
import com.cdpg.kmppoc.ui.theme.DhruvaTheme

private object Routes {
    const val SPLASH = "splash"
    const val INTRO = "intro"
    const val LOGIN = "login"
    const val OTP = "otp/{phone}"
    const val FULL_NAME = "fullName"
    const val WELCOME = "welcome/{name}"
    const val MAIN = "main"

    fun otp(phone: String) = "otp/$phone"
    fun welcome(name: String) = "welcome/${Uri.encode(name)}"
}

/** Root: theme, shared strings and navigation. Mirrors Dhruva iOS's AppCoordinator flows. */
@Composable
fun DhruvaApp(graph: AppGraph) {
    val language by graph.preferences.language.collectAsStateWithLifecycle()
    val sessionState by graph.session.state.collectAsStateWithLifecycle()
    val navController = rememberNavController()

    // Session ended (sign out, or the server refused the refresh token): back to Login.
    LaunchedEffect(sessionState) {
        val route = navController.currentDestination?.route
        if (sessionState == SessionState.SignedOut && route == Routes.MAIN) {
            navController.navigateClearingStack(Routes.LOGIN)
        }
    }

    DhruvaTheme {
        CompositionLocalProvider(LocalLocalizer provides graph.localizer) {
            // A language change rebuilds the UI so every shared string is read again.
            key(language) {
                NavHost(navController = navController, startDestination = Routes.SPLASH) {
                    composable(Routes.SPLASH) {
                        SplashScreen(
                            graph = graph,
                            onSignedOut = { navController.navigateClearingStack(Routes.INTRO) },
                            onSignedIn = { navController.navigateClearingStack(Routes.MAIN) },
                        )
                    }
                    composable(Routes.INTRO) {
                        IntroScreen(onContinue = { navController.navigate(Routes.LOGIN) })
                    }
                    composable(Routes.LOGIN) {
                        LoginScreen(graph = graph, onOtpSent = { phone -> navController.navigate(Routes.otp(phone)) })
                    }
                    composable(
                        Routes.OTP,
                        arguments = listOf(navArgument("phone") { type = NavType.StringType }),
                    ) { entry ->
                        OtpScreen(
                            graph = graph,
                            phoneNumber = entry.arguments?.getString("phone").orEmpty(),
                            onBack = { navController.popBackStack() },
                            onNewUser = { navController.navigateClearingStack(Routes.FULL_NAME) },
                            onReturningUser = { navController.navigateClearingStack(Routes.MAIN) },
                        )
                    }
                    composable(Routes.FULL_NAME) {
                        FullNameScreen(graph = graph, onSubmitted = { firstName -> navController.navigate(Routes.welcome(firstName)) })
                    }
                    composable(
                        Routes.WELCOME,
                        arguments = listOf(navArgument("name") { type = NavType.StringType }),
                    ) { entry ->
                        WelcomeScreen(
                            firstName = entry.arguments?.getString("name").orEmpty(),
                            onFinished = { navController.navigateClearingStack(Routes.MAIN) },
                        )
                    }
                    composable(Routes.MAIN) {
                        MainScreen(graph = graph)
                    }
                }
            }
        }
    }
}

private fun NavHostController.navigateClearingStack(route: String) {
    navigate(route) {
        popUpTo(graph.id) { inclusive = true }
        launchSingleTop = true
    }
}
