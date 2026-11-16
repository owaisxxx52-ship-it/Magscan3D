package com.magscan.ultimate3d

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Navy = Color(0xFF071525)
private val Panel = Color(0xFF10283D)
private val Cyan = Color(0xFF31D6D0)
private val Muted = Color(0xFF9BB2C5)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MagScanApp() }
    }
}

@Composable
fun MagScanApp() {
    var page by remember { mutableIntStateOf(0) }

    MaterialTheme(colorScheme = darkColorScheme(
        primary = Cyan,
        background = Navy,
        surface = Panel
    )) {
        Scaffold(
            containerColor = Navy,
            bottomBar = {
                NavigationBar(containerColor = Panel) {
                    NavigationBarItem(
                        selected = page == 0,
                        onClick = { page = 0 },
                        icon = { Icon(Icons.Default.Home, null) },
                        label = { Text("خانه") }
                    )
                    NavigationBarItem(
                        selected = page == 1,
                        onClick = { page = 1 },
                        icon = { Icon(Icons.Default.ViewInAr, null) },
                        label = { Text("اسکن سه‌بعدی") }
                    )
                    NavigationBarItem(
                        selected = page == 2,
                        onClick = { page = 2 },
                        icon = { Icon(Icons.Default.Sensors, null) },
                        label = { Text("سنسورها") }
                    )
                    NavigationBarItem(
                        selected = page == 3,
                        onClick = { page = 3 },
                        icon = { Icon(Icons.Default.History, null) },
                        label = { Text("تاریخچه") }
                    )
                }
            }
        ) { padding ->
            when (page) {
                0 -> HomeScreen(Modifier.padding(padding))
                1 -> ScanScreen(Modifier.padding(padding))
                2 -> SensorScreen(Modifier.padding(padding))
                else -> HistoryScreen(Modifier.padding(padding))
            }
        }
    }
}

@Composable
fun ScreenTitle(title: String, subtitle: String) {
    Column {
        Text(title, fontSize = 25.sp, fontWeight = FontWeight.Bold)
        Text(subtitle, color = Muted, fontSize = 12.sp)
    }
}

@Composable
fun InfoCard(title: String, value: String, unit: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Panel),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, color = Muted)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(value, fontSize = 27.sp, color = Cyan,
                    fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(5.dp))
                Text(unit, color = Muted)
            }
        }
    }
}
