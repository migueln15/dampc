package com.example.dampc.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.dampc.presentation.auth.LoginScreen
import com.example.dampc.presentation.auth.RegisterScreen
import com.example.dampc.presentation.jugada.JugadaScreen
import com.example.dampc.presentation.seguimiento.SeguimientoScreen

@Composable
fun AppNavGraph(){
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login")
    {
        composable("register") { RegisterScreen(navController) }
        composable("login") { LoginScreen(navController) }
        composable("jugada") { JugadaScreen(navController) }
        composable("seguimiento") { SeguimientoScreen(navController) }
        /*composable("home") {
            DrawerScaffold(navController) {
                HomeScreen()
            }
        }*/
    }
}
