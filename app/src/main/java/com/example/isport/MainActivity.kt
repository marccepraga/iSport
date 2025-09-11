package com.example.isport

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SportsTennis
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.isport.ui.screens.*
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ISportApp()
        }
    }
}

@Composable
fun ISportApp() {
    val nav = rememberNavController()
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val isLoggedIn = currentUser != null
    val userId = currentUser?.uid ?: ""

    NavHost(
        navController = nav,
        startDestination = if (isLoggedIn) "main" else "login"
    ) {
        composable("login") {
            LoginScreen(nav)
        }

        composable("main") {
            MainScaffold(nav = nav, userId = userId)
        }
    }
}


@Composable
fun MainScaffold(nav: NavHostController, userId: String) {
    val items = listOf(
        BottomNavItem("facilities", "Campi", Icons.Filled.SportsTennis),
        BottomNavItem("bookings", "Prenotazioni", Icons.Filled.Event),
        BottomNavItem("profile", "Profilo", Icons.Filled.Person)
    )

    val childNav = rememberNavController()
    val currentRoute = childNav.currentBackStackEntryAsState().value?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentRoute !in listOf("bookingForm")) {
                NavigationBar {
                    items.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                            selected = currentRoute == item.route,
                            onClick = {
                                childNav.navigate(item.route) {
                                    popUpTo(childNav.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = childNav,
            startDestination = "facilities",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("facilities") { FacilitiesScreen(nav = childNav, userId = userId) }

            composable("facility_detail") {
                FacilityDetailScreen(navController = childNav)
            }

            composable("bookings") {
                BookingsScreen(userId = userId, nav = childNav)
            }

            composable("profile") { ProfileScreen(userId = userId, nav = childNav) }


            composable("login") {
                LoginScreen(nav)
            }

            composable("bookingForm/{facilityId}/{facilityName}") { backStack ->
                val fid = backStack.arguments?.getString("facilityId") ?: ""
                val name = backStack.arguments?.getString("facilityName") ?: ""
                BookingForm(userId = userId, facilityId = fid, facilityName = name) {
                    childNav.navigate("bookings") {
                        popUpTo("facilities") { inclusive = false }
                        launchSingleTop = true
                    }
                }
            }

            // ✅ Solo UNA definizione per edit_facility
            composable("edit_facility/{facilityId}") { backStackEntry ->
                val fid = backStackEntry.arguments?.getString("facilityId") ?: ""
                EditFacilityScreen(facilityId = fid, nav = childNav)
            }

            // ✅ Nuovo campo (senza ID)
            composable("new_facility") {
                NewFacilityScreen(nav = childNav)
            }
        }
    }
}



data class BottomNavItem(val route: String, val label: String, val icon: ImageVector)
