package com.example.isport

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.layout.padding
import androidx.navigation.compose.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SportsTennis
import androidx.compose.material.icons.filled.Event
import com.example.isport.ui.screens.FacilitiesScreen
import com.example.isport.ui.screens.BookingsScreen
import com.example.isport.ui.screens.ProfileScreen
import com.example.isport.ui.screens.BookingForm

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
    val fakeUserId = "user1" // 👈 utente fittizio per ora

    val items = listOf(
        BottomNavItem("facilities", "Campi", Icons.Filled.SportsTennis),
        BottomNavItem("bookings", "Prenotazioni", Icons.Filled.Event),
        BottomNavItem("profile", "Profilo", Icons.Filled.Person)
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val currentRoute = nav.currentBackStackEntryAsState().value?.destination?.route
                items.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = currentRoute == item.route,
                        onClick = {
                            nav.navigate(item.route) {
                                popUpTo(nav.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = nav,
            startDestination = "facilities",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("facilities") { FacilitiesScreen(nav) } // 👈 passo il nav
            composable("bookings") { BookingsScreen(userId = fakeUserId) }
            composable("profile") { ProfileScreen(userId = fakeUserId) }
            composable("bookingForm/{facilityId}") { backStack ->
                val fid = backStack.arguments?.getString("facilityId") ?: ""
                BookingForm(userId = fakeUserId, facilityId = fid) {
                    nav.popBackStack() // 👈 torna indietro dopo prenotazione
                }
            }
        }
    }
}

data class BottomNavItem(val route: String, val label: String, val icon: ImageVector)
