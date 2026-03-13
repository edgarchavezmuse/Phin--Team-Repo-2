package com.example.phinui.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.phinui.ui.theme.Background
import com.example.phinui.ui.theme.NavText
import com.example.phinui.ui.theme.SelectedPill

@Composable
fun EventsScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(top = 24.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        // LazyColumn allows scroll effect
        LazyColumn(
            modifier = Modifier.padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Elements must be wrapped in "item{}" to make scrollable work
            item {
                Text(
                    text = "Events",
                    fontSize = 28.sp,
                    color = NavText
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                EventCard(
                    title = "Health Science Advising Sessions",
                    dateTime = "Date: March 9\nTime: 9:00am",
                    location = "Location: del Norte Hall 1500"
                )
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }

            item {
                EventCard(
                    title = "Student Success Workshop",
                    dateTime = "Date: March 10\nTime: 9:00am",
                    location = "Location: Bell Tower Courtyard"
                )
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }

            item {
                EventCard(
                    title = "Events 3 Name",
                    dateTime = "Date: TBD\nTime: TBD",
                    location = "Location: TBD"
                )
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }

            item {
                EventCard(
                    title = "Events 4 Name",
                    dateTime = "Date: TBD\nTime: TBD",
                    location = "Location: TBD"
                )
            }

            item {
                EventCard(
                    title = "Events 5 Name",
                    dateTime = "Date: TBD\nTime: TBD",
                    location = "Location: TBD"
                )
            }
        }
    }
}

@Composable
fun EventCard(
    title: String,
    dateTime: String,
    location: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SelectedPill)
            .padding(20.dp)
    ) {
        Text(
            text = title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = NavText
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = dateTime,
            fontSize = 16.sp,
            color = NavText
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = location,
            fontSize = 16.sp,
            color = NavText
        )
    }
}