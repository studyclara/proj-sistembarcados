package com.example.salaautomatizada

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.salaautomatizada.ui.ConfigScreen
import com.example.salaautomatizada.ui.LoginScreen
import com.example.salaautomatizada.ui.MainScreen
import com.example.salaautomatizada.ui.SplashScreen
import com.example.salaautomatizada.ui.theme.SalaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Splash do sistema (Android 12+ e compatibilidade); depois entra a splash animada em Compose.
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SalaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNav()
                }
            }
        }
    }
}

@Composable
private fun AppNav() {
    val nav = rememberNavController()
    val vm: AppViewModel = viewModel()

    NavHost(navController = nav, startDestination = "splash") {
        composable("splash") {
            SplashScreen(
                onFinished = {
                    val destination = if (vm.email.value != null) "main" else "login"
                    nav.navigate(destination) { popUpTo("splash") { inclusive = true } }
                }
            )
        }
        composable("login") {
            LoginScreen(
                vm = vm,
                onLoggedIn = {
                    nav.navigate("main") { popUpTo("login") { inclusive = true } }
                }
            )
        }
        composable("main") {
            MainScreen(
                vm = vm,
                onNew = { nav.navigate("config/-1") },
                onEdit = { id -> nav.navigate("config/$id") },
                onLogout = {
                    vm.logout()
                    nav.navigate("login") { popUpTo("main") { inclusive = true } }
                }
            )
        }
        composable(
            route = "config/{id}",
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) { entry ->
            ConfigScreen(
                vm = vm,
                configId = entry.arguments?.getLong("id") ?: -1L,
                onBack = { nav.popBackStack() }
            )
        }
    }
}
