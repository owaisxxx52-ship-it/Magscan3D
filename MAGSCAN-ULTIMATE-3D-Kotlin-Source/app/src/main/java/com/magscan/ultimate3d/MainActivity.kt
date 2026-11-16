package com.magscan.ultimate3d

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat

private val Navy = Color(0xFF071525)
private val Panel = Color(0xFF10283D)
private val Cyan = Color(0xFF31D6D0)
private val Muted = Color(0xFF9BB2C5)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MagScanApp(this) }
    }
}

@Composable
fun MagScanApp(context: Context) {
    var page by remember { mutableStateOf("home") }
    var btStatus by remember { mutableStateOf("قطع") }
    var devices by remember { mutableStateOf(listOf<String>()) }
    var scanning by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("برای دریافت داده، ابتدا ESP32 را متصل کنید.") }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        if (grants.values.all { it }) {
            startBleScan(context,
                onStart = { scanning = true; message = "جست‌وجوی دستگاه‌های BLE..." },
                onDevice = { name -> devices = (devices + name).distinct() },
                onError = { err -> scanning = false; message = err },
                onFinish = { scanning = false }
            )
        } else message = "مجوز بلوتوث داده نشد."
    }

    MaterialTheme(colorScheme = darkColorScheme(
        primary = Cyan, background = Navy, surface = Panel,
        onPrimary = Navy, onBackground = Color.White, onSurface = Color.White
    )) {
        Scaffold(
            containerColor = Navy,
            bottomBar = {
                NavigationBar(containerColor = Panel, contentColor = Cyan) {
                    NavigationBarItem(selected = page == "home", onClick = { page = "home" },
                        icon = { Icon(Icons.Default.Dashboard, null) }, label = { Text("خانه") })
                    NavigationBarItem(selected = page == "scan", onClick = { page = "scan" },
                        icon = { Icon(Icons.Default.ViewInAr, null) }, label = { Text("اسکن ۳D") })
                    NavigationBarItem(selected = page == "sensors", onClick = { page = "sensors" },
                        icon = { Icon(Icons.Default.Sensors, null) }, label = { Text("سنسورها") })
                    NavigationBarItem(selected = page == "history", onClick = { page = "history" },
                        icon = { Icon(Icons.Default.History, null) }, label = { Text("تاریخچه") })
                }
            }
        ) { pad ->
            when (page) {
                "home" -> HomePage(Modifier.padding(pad), btStatus, message,
                    onConnect = {
                        val perms = if (Build.VERSION.SDK_INT >= 31)
                            arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
                        else arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
                        val missing = perms.filter { ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED }
                        if (missing.isEmpty()) {
                            startBleScan(context,
                                onStart = { scanning = true; message = "جست‌وجوی دستگاه‌های BLE..." },
                                onDevice = { name -> devices = (devices + name).distinct() },
                                onError = { err -> scanning = false; message = err },
                                onFinish = { scanning = false }
                            )
                        } else permissionLauncher.launch(missing.toTypedArray())
                    })
                "scan" -> ScanPage(Modifier.padding(pad))
                "sensors" -> SensorsPage(Modifier.padding(pad), devices, scanning, message,
                    onScan = {
                        val perms = if (Build.VERSION.SDK_INT >= 31)
                            arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
                        else arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
                        val missing = perms.filter { ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED }
                        if (missing.isEmpty()) startBleScan(context,
                            onStart = { scanning = true; message = "در حال جست‌وجو..." },
                            onDevice = { name -> devices = (devices + name).distinct() },
                            onError = { err -> scanning = false; message = err },
                            onFinish = { scanning = false })
                        else permissionLauncher.launch(missing.toTypedArray())
                    })
                else -> HistoryPage(Modifier.padding(pad))
            }
        }
    }
}

private fun startBleScan(
    context: Context,
    onStart: () -> Unit,
    onDevice: (String) -> Unit,
    onError: (String) -> Unit,
    onFinish: () -> Unit
) {
    try {
        val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val adapter: BluetoothAdapter? = manager.adapter
        if (adapter == null || !adapter.isEnabled) {
            onError("بلوتوث گوشی خاموش است یا پشتیبانی نمی‌شود.")
            return
        }
        val scanner = adapter.bluetoothLeScanner
        if (scanner == null) { onError("اسکنر BLE در دسترس نیست."); return }
        onStart()
        scanner.startScan(object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                val label = result.device.name ?: result.scanRecord?.deviceName ?: result.device.address
                onDevice(label)
            }
            override fun onScanFailed(errorCode: Int) {
                onError("خطای جست‌وجوی بلوتوث: $errorCode")
            }
        })
        // اسکن را با دکمه توقف در نسخه بعدی کنترل می‌کنیم؛ فعلاً سیستم اسکن را ادامه می‌دهد.
    } catch (e: SecurityException) {
        onError("مجوز بلوتوث لازم است.")
    } catch (e: Exception) {
        onError("خطا: ${e.localizedMessage ?: "نامشخص"}")
    }
}

@Composable
fun PageTitle(title: String, subtitle: String) {
    Column(Modifier.fillMaxWidth().padding(bottom = 18.dp)) {
        Text(title, fontSize = 25.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text(subtitle, color = Muted, fontSize = 13.sp)
    }
}

@Composable
fun PanelCard(content: @Composable ColumnScope.() -> Unit) {
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Panel),
        shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.padding(16.dp), content = content)
    }
}

@Composable
fun HomePage(modifier: Modifier, bt: String, message: String, onConnect: () -> Unit) {
    Column(modifier.fillMaxSize().padding(18.dp)) {
        PageTitle("MAGSCAN", "ULTIMATE 3D  •  سامانه پایش میدان مغناطیسی")
        PanelCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Bluetooth, null, tint = Cyan, modifier = Modifier.size(28.dp))
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text("وضعیت ارتباط", fontWeight = FontWeight.Bold)
                    Text(bt, color = Muted, fontSize = 13.sp)
                }
                Button(onClick = onConnect, colors = ButtonDefaults.buttonColors(containerColor = Cyan)) {
                    Text("جست‌وجوی ESP32", color = Navy)
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(message, color = Muted, fontSize = 12.sp)
        }
        Spacer(Modifier.height(14.dp))
        Text("نمایش زنده سنسورها", color = Color.White, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricCard("مگنتومتر", "—", "µT", Modifier.weight(1f))
            MetricCard("شتاب‌سنج", "—", "g", Modifier.weight(1f))
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricCard("EMS-100 A", "—", "V", Modifier.weight(1f))
            MetricCard("EMS-100 B", "—", "V", Modifier.weight(1f))
        }
        Spacer(Modifier.height(14.dp))
        PanelCard {
            Text("وضعیت سیستم", fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text("داده واقعی هنوز دریافت نشده است. مقادیر خط تیره عمداً خالی هستند.", color = Muted, fontSize = 12.sp)
        }
    }
}

@Composable
fun MetricCard(name: String, value: String, unit: String, modifier: Modifier) {
    Card(modifier, colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(14.dp)) {
            Text(name, color = Muted, fontSize = 12.sp)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(value, fontSize = 25.sp, fontWeight = FontWeight.Bold, color = Cyan)
                Spacer(Modifier.width(5.dp))
                Text(unit, color = Muted, fontSize = 11.sp)
            }
        }
    }
}

@Composable
fun ScanPage(modifier: Modifier) {
    Column(modifier.fillMaxSize().padding(18.dp)) {
        PageTitle("اسکن سه‌بعدی", "نمایش نمونه‌ای بستر داده؛ هنوز نقشه واقعی ساخته نشده")
        PanelCard {
            Text("نقشه میدان", fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            Canvas(Modifier.fillMaxWidth().height(230.dp).background(Color(0xFF091B2B), RoundedCornerShape(12.dp))) {
                val stepX = size.width / 8
                val stepY = size.height / 8
                for (i in 1..7) {
                    drawLine(Color(0xFF1C5367), Offset(i * stepX, 0f), Offset(i * stepX, size.height), 1f)
                    drawLine(Color(0xFF1C5367), Offset(0f, i * stepY), Offset(size.width, i * stepY), 1f)
                }
                drawCircle(Cyan.copy(alpha = .65f), radius = 25f, center = Offset(size.width * .52f, size.height * .48f), style = androidx.compose.ui.graphics.drawscope.Stroke(3f))
                drawCircle(Cyan.copy(alpha = .3f), radius = 48f, center = Offset(size.width * .52f, size.height * .48f), style = androidx.compose.ui.graphics.drawscope.Stroke(2f))
            }
            Spacer(Modifier.height(10.dp))
            Text("برای تولید نقشه معتبر، باید مختصات حرکت و داده سنسور در طول اسکن ثبت شوند.", color = Muted, fontSize = 12.sp)
        }
    }
}

@Composable
fun SensorsPage(modifier: Modifier, devices: List<String>, scanning: Boolean, message: String, onScan: () -> Unit) {
    Column(modifier.fillMaxSize().padding(18.dp)) {
        PageTitle("مدیریت سنسورها", "وضعیت قطعات و جست‌وجوی بلوتوث")
        listOf("QMC5883L  •  مگنتومتر", "MPU6500  •  شتاب‌سنج و ژیروسکوپ",
            "EMS-100 A  •  ورودی آنالوگ", "EMS-100 B  •  ورودی آنالوگ",
            "VL53L0X  •  فاصله‌سنج", "OLED 0.96  •  نمایشگر").forEach {
            PanelCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Sensors, null, tint = Cyan)
                    Spacer(Modifier.width(10.dp))
                    Text(it, fontSize = 14.sp)
                    Spacer(Modifier.weight(1f))
                    Text("آماده‌سازی", color = Muted, fontSize = 10.sp)
                }
            }
            Spacer(Modifier.height(8.dp))
        }
        Button(onClick = onScan, modifier = Modifier.fillMaxWidth()) {
            Text(if (scanning) "در حال جست‌وجو..." else "جست‌وجوی دستگاه‌های BLE")
        }
        Text(message, color = Muted, fontSize = 12.sp, modifier = Modifier.padding(vertical = 8.dp))
        LazyColumn {
            items(devices) { device ->
                Text("• $device", color = Cyan, modifier = Modifier.padding(6.dp))
            }
        }
    }
}

@Composable
fun HistoryPage(modifier: Modifier) {
    Column(modifier.fillMaxSize().padding(18.dp)) {
        PageTitle("تاریخچه اسکن", "مدیریت برداشت‌های ذخیره‌شده")
        PanelCard {
            Icon(Icons.Default.FolderOpen, null, tint = Cyan, modifier = Modifier.size(34.dp))
            Spacer(Modifier.height(8.dp))
            Text("هنوز اسکن ذخیره‌شده‌ای وجود ندارد.", fontWeight = FontWeight.Bold)
            Text("پس از تکمیل پروتکل دریافت داده، امکان ذخیره و مرور اسکن‌ها فعال می‌شود.", color = Muted, fontSize = 12.sp)
        }
    }
}
