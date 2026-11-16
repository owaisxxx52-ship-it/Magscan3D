اویس, [25/08/1405 07:26 ق.ظ]
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

اویس, [25/08/1405 07:26 ق.ظ]
@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    Column(
        modifier.fillMaxSize().background(Navy).padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        ScreenTitle("MAGSCAN", "ULTIMATE 3D • PROFESSIONAL SCANNER")

        Card(colors = CardDefaults.cardColors(containerColor = Panel)) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Bluetooth, null, tint = Cyan)
                    Spacer(Modifier.width(8.dp))
                    Text("ارتباط ESP32", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.weight(1f))
                    Text("قطع", color = Color(0xFFFFB74D))
                }
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("اتصال به دستگاه")
                }
            }
        }

        Text("مانیتور سنسورها", fontWeight = FontWeight.Bold)

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.weight(1f)) {
                InfoCard("QMC5883L", "—", "µT")
            }
            Box(Modifier.weight(1f)) {
                InfoCard("MPU6500", "—", "g")
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.weight(1f)) {
                InfoCard("EMS-100 A", "—", "V")
            }
            Box(Modifier.weight(1f)) {
                InfoCard("EMS-100 B", "—", "V")
            }
        }

        Card(colors = CardDefaults.cardColors(containerColor = Panel)) {
            Column(Modifier.padding(16.dp)) {
                Text("وضعیت سیستم", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Text(
                    "در انتظار دریافت داده واقعی از ESP32",
                    color = Muted,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun ScanScreen(modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize().background(Navy).padding(18.dp)) {
        ScreenTitle("اسکن سه‌بعدی", "نمایش میدان مغناطیسی")
        Spacer(Modifier.height(18.dp))
        Canvas(
            Modifier.fillMaxWidth().height(280.dp)
                .background(Panel, RoundedCornerShape(16.dp))
        ) {
            val cx = size.width / 2
            val cy = size.height / 2

            for (i in 1..6) {
                drawCircle(
                    Cyan.copy(alpha = 0.7f / i),
                    radius = i * 25f,
                    center = Offset(cx, cy),
                    style = Stroke(2f)
                )
            }

            drawLine(
                Cyan.copy(alpha = .5f),
                Offset(0f, cy),
                Offset(size.width, cy),
                1f
            )
            drawLine(
                Cyan.copy(alpha = .5f),
                Offset(cx, 0f),
                Offset(cx, size.height),
                1f
            )
        }
        Spacer(Modifier.height(12.dp))
        Text("این صفحه فعلاً نمایشی است؛ نقشه واقعی به داده‌های اسکن نیاز دارد.",
            color = Muted, fontSize = 12.sp)
    }
}

@Composable
fun SensorScreen(modifier: Modifier = Modifier) {
    Column(
        modifier.fillMaxSize().background(Navy).padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ScreenTitle("مدیریت سنسورها", "تنظیمات و کالیبراسیون")
        listOf(
            "QMC5883L — مگنتومتر",
            "MPU6500 — شتاب‌سنج و ژیروسکوپ",
            "EMS-100 A — ورودی آنالوگ",
            "EMS-100 B — ورودی آنالوگ",
            "VL53L0X — فاصله‌سنج",

اویس, [25/08/1405 07:26 ق.ظ]
"OLED 0.96 — نمایشگر"
        ).forEach {
            Card(colors = CardDefaults.cardColors(containerColor = Panel)) {
                Row(
                    Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Sensors, null, tint = Cyan)
                    Spacer(Modifier.width(10.dp))
                    Text(it, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun HistoryScreen(modifier: Modifier = Modifier) {
    Column(
        modifier.fillMaxSize().background(Navy).padding(18.dp)
    ) {
        ScreenTitle("تاریخچه اسکن", "گزارش برداشت‌های قبلی")
        Spacer(Modifier.height(20.dp))
        Card(colors = CardDefaults.cardColors(containerColor = Panel)) {
            Column(Modifier.padding(20.dp)) {
                Icon(Icons.Default.FolderOpen, null, tint = Cyan,
                    modifier = Modifier.size(36.dp))
                Spacer(Modifier.height(10.dp))
                Text("هنوز اسکن ذخیره نشده است.")
                Text("پس از پیاده‌سازی ثبت داده، گزارش‌ها اینجا نمایش داده می‌شوند.",
                    color = Muted, fontSize = 12.sp)
            }
        }
    }
}
