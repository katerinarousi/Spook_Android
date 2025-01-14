package com.example.room_setup_composables


import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.room_setup_composables.ui.theme.Screen

// Μετονομασία της κλάσης από Booking σε BookingsHistory
data class BookingsHistory(val storeId: Int, val storeName: String, val date: String, val location: String)

@Composable
fun ProfileNavigation(
    userId: Int,
    userViewModel:UserViewModel,
    storeViewModel: StoreViewModel,
    bookingViewModel: BookingViewModel,
    reviewViewModel: ReviewViewModel,
    slotViewModel: SlotViewModel
) {

    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.ProfileScreen.route) {
        composable(route = Screen.ProfileScreen.route) {
            ProfileScreen(navController, userId, userViewModel = userViewModel, bookingViewModel, slotViewModel, reviewViewModel,storeViewModel)
        }

        // For navigation to reviews
        composable(
            route = Screen.Reviews.route + "/{storeId}",
            arguments = listOf(
                navArgument("storeId") {
                    type = NavType.IntType
                    defaultValue = 1
                    nullable = false
                }
            )
        ) { entry ->
            val storeId = entry.arguments?.getInt("storeId") ?: 1
            ReviewScreen(navController, userId, userViewModel, reviewViewModel, storeId = storeId)
        }

        composable(
            route = Screen.LoginPage.route,
            arguments = emptyList()
        ) { entry ->
            LoginNavigation(userViewModel, storeViewModel, bookingViewModel, reviewViewModel, slotViewModel)
        }
    }
}


@Composable
fun ProfileScreen(navController: NavController, userId: Int, userViewModel: UserViewModel, bookingViewModel: BookingViewModel , slotViewModel: SlotViewModel, reviewViewModel: ReviewViewModel,storeViewModel: StoreViewModel) {
    bookingViewModel.getBookingsForUser(userId)
    val bookingsHistory by bookingViewModel.bookings.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(0xFFF1F3F4))
            .padding(20.dp),
        verticalArrangement = Arrangement.Top
    ) {
        UserProfileSection(userViewModel,userId)


        BookingHeader()

        Spacer(modifier = Modifier.height(40.dp))

        BookingHistory(
            bookingsHistory = bookingsHistory,
            onCheckClick = { storeId ->
                println("Selected Store ID: $storeId")
            },
            userId = userId,
            storeViewModel = storeViewModel
        )

        Spacer(modifier = Modifier.height(40.dp))

        LogoutButton(navController, onLogoutClick = {
            navController.navigate(Screen.LoginPage.withArgs())
        },)
    }

}

@Composable
fun BookingHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color(0xFFFFA726),
                shape = RoundedCornerShape(
                    bottomStart = 16.dp,
                    bottomEnd = 16.dp
                )
            )
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Bookings History",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
fun UserProfileSection(userViewModel: UserViewModel, userId: Int) {
    val userName by userViewModel.userDetails.collectAsState()
    val email by userViewModel.email.collectAsState()
    // Trigger fetching user details
    LaunchedEffect(userId) {

        userViewModel.fetchUserName(userId)
        userViewModel.fetchEmail(userId)
    }

    Card(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.elevatedCardElevation(6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(color = Color(0xFFF1F3F4)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = ".", fontSize = 24.sp)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = userName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = email,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun BookingHistory(
    bookingsHistory: List<Booking>,
    userId: Int,
    storeViewModel: StoreViewModel,
    onCheckClick: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(bookingsHistory) { booking ->
                BookingItem(
                    booking = booking,
                    storeViewModel = storeViewModel,
                    onCheckClick = onCheckClick
                )
            }
        }
    }
}

@Composable
fun BookingItem(
    booking: Booking,
    storeViewModel: StoreViewModel,
    onCheckClick: (Int) -> Unit
) {

    val storeNames by storeViewModel.storeNames.collectAsState()
    val storeLocations by storeViewModel.storeLocations.collectAsState()
    // Get the name for the current storeId
    val storeName = storeNames[booking.storeId] ?: "Loading..."
    val location = storeLocations[booking.storeId] ?: "Loading..."

    // Trigger fetching user details when the storeId changes
    LaunchedEffect(booking.storeId) {
        Log.d("BookingItem", "Fetching data for : $storeName")
        storeViewModel.fetchStoreName(booking.storeId)
        storeViewModel.fetchStoreLocation(booking.storeId)
    }

    Card(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    //text = booking.storeName,
                    text="Store: $storeName",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                   //text = booking.location,
                    text= "Location: ${location}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Text(
                    text = "Date: ${booking.date}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Text(
                    text = "People: ${booking.persons}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
            Button(
                onClick = { onCheckClick(booking.storeId) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA726))
            ) {
                Text(text = "Review", color = Color.White)
            }
        }
    }
}

@Composable
fun LogoutButton(navController: NavController, onLogoutClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = { onLogoutClick() },
            modifier = Modifier
                .width(200.dp)
                .padding(vertical = 16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA726))
        ) {
            Text(
                text = "Logout",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
    GoBack(navController)
}


@Composable
fun GoBack(navController: NavController) {
// Go Back button at the bottom of the screen
    IconButton(
        onClick = { navController.popBackStack() },
        modifier = Modifier
            .padding(16.dp) // Padding for the button
    ) {
        Icon(
            imageVector = Icons.Default.ArrowBack,
            contentDescription = "Go Back",
            modifier = Modifier
                .background(Color(0xFF007066), RoundedCornerShape(50))
                .padding(12.dp)
        )
    }
}
