package com.example.phinui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.phinui.R
import com.example.phinui.notifications.FCMTokenManager.clearToken
import com.example.phinui.notifications.FCMTokenManager.deleteDeviceToken
import com.example.phinui.ui.navigation.Routes
import com.example.phinui.ui.theme.Background
import com.example.phinui.ui.theme.NavText
import com.example.phinui.ui.theme.PrimaryRed
import com.example.phinui.viewmodel.SettingsViewModel
import com.google.firebase.auth.FirebaseAuth

enum class BottomBarType {
    ON, OFF, WAVE
}

data class BottomBarOption(
    val type: BottomBarType,
    val title: String,
    val image: Int
)

@Composable
fun SettingsScreen(
    navController: NavHostController
) {

    val context = LocalContext.current
    val viewModel = remember { SettingsViewModel(context) }
    val auth = FirebaseAuth.getInstance()


    val selectedType = viewModel.bottomBarType
        .collectAsState()
        .value ?: BottomBarType.ON

    val options = listOf(
        BottomBarOption(BottomBarType.ON, "On", R.drawable.bottom_bar_on),
        BottomBarOption(BottomBarType.OFF, "Off", R.drawable.bottom_bar_off),
        BottomBarOption(BottomBarType.WAVE, "Wave", R.drawable.bottom_bar_wave)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Settings",
            fontSize = 28.sp,
            fontWeight = FontWeight.SemiBold,
            color = NavText
        )

        Spacer(modifier = Modifier.height(24.dp))

        // toggle bottom bar

        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Bottom Bar",
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Left
                )

                OptionRow(
                    options = options,
                    selected = selectedType,
                    onSelect = { viewModel.setBottomBar(it) }
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // logout
        Button(
            onClick = {
                clearToken()
                deleteDeviceToken()
                auth.signOut()
                navController.navigate(Routes.LOGIN) {
                    popUpTo(Routes.HOME) { inclusive = true }
                    launchSingleTop = true
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryRed,
                contentColor = Color.White
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Logout,
                contentDescription = "Log out"
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Log Out")
        }
    }
}

@Composable
fun OptionRow(
    options: List<BottomBarOption>,
    selected: BottomBarType,
    onSelect: (BottomBarType) -> Unit)
{

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(options) { option ->

            val isSelected = option.type == selected

            BottomBarPreviewOption(
                option = option,
                isSelected = isSelected,
                onClick = { onSelect(option.type) }
            )

        }
    }
}

@Composable
fun BottomBarPreviewOption(
    option: BottomBarOption,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(140.dp)
            .clickable { onClick() }
    ) {

        Card(
            shape = RoundedCornerShape(18.dp),
            border = if (isSelected) BorderStroke(2.dp, PrimaryRed) else null,
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 6.dp else 2.dp)
        ) {
            Image(
                painter = painterResource(id = option.image),
                contentDescription = option.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp),
                contentScale = ContentScale.Fit
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = option.title,
            color = if (isSelected) PrimaryRed else Color.Black,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}
