package com.example.phinui.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.phinui.R
import com.example.phinui.ui.theme.HeaderRed
import com.example.phinui.ui.theme.HeaderText

@Composable
fun AuthHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .background(HeaderText)
            .padding(top = 55.dp, bottom = 0.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy((-13).dp)
            ) {
                Text(
                    text = "Ph",
                    color = HeaderRed,
                    fontSize = 100.sp,
                    fontWeight = FontWeight.Bold
                )

                // ı with dolphin as dot
                Box(
                    contentAlignment = Alignment.TopCenter
                ) {
                    Text(
                        text = "ı",
                        color = HeaderRed,
                        fontSize = 100.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Image(
                        painter = painterResource(id = R.drawable.redphin),
                        contentDescription = "Phin logo",
                        modifier = Modifier
                            .size(50.dp)
                            .align(Alignment.TopCenter)
                            .offset(
                                x = 17.dp,
                                y = (-8).dp
                            ) // x: horizontal adjust y: vertical adjust
                    )
                }

                Text(
                    text = "n",
                    color = HeaderRed,
                    fontSize = 100.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}