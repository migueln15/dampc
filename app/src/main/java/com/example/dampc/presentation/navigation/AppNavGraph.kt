package com.example.dampc.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.dampc.presentation.auth.RegisterScreen

@Composable
fun AppNavGraph(){
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "register")
    {
        composable("register") { RegisterScreen(navController) }
        //composable("login") { LoginScreen(navController) }
        /*composable("home") {
            DrawerScaffold(navController) {
                HomeScreen()
            }
        }*/
    }
}