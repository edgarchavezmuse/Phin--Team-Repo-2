package com.example.phinui.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.phinui.ui.theme.HeaderRed
import com.example.phinui.ui.theme.HeaderText
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.ui.res.painterResource
import com.example.phinui.R


@Composable
fun TopHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(HeaderRed)
            .padding(top = 0.dp, bottom = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy((-5).dp)
            ) {
                Text(
                    text = "Ph",
                    color = HeaderText,
                    fontSize = 46.sp,
                    fontWeight = FontWeight.Bold
                )

                // ı with dolphin as dot
                Box(
                    contentAlignment = Alignment.TopCenter
                ) {
                    Text(
                        text = "ı",
                        color = HeaderText,
                        fontSize = 46.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Image(
                        painter = painterResource(id = R.drawable.whitephin),
                        contentDescription = "Phin logo",
                        modifier = Modifier
                            .size(20.dp)
                            .align(Alignment.TopCenter)
                            .offset(x = 7.dp, y = (-1).dp) // x: horizontal adjust y: vertical adjust
                    )
                }

                Text(
                    text = "n",
                    color = HeaderText,
                    fontSize = 46.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = "CSUCI",
                color = HeaderText,
                fontSize = 18.sp,
                fontWeight = FontWeight.Light
            )
        }
    }
}