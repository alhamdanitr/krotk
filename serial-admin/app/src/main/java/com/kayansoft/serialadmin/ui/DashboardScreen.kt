package com.kayansoft.serialadmin.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kayansoft.serialadmin.network.Client
import com.kayansoft.serialadmin.network.Serial

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    clients: List<Client>,
    serials: List<Serial>,
    isLoading: Boolean,
    onRefresh: () -> Unit,
    onLogout: () -> Unit,
    onCreateSerial: (name: String, network: String, phone: String, months: Int, notes: String, deviceId: String) -> Unit,
    onToggle: (Int) -> Unit,
    onReset: (Int) -> Unit,
    onDelete: (Int) -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var pendingDeleteId by remember { mutableStateOf<Int?>(null) }

    val clientsById = remember(clients) { clients.associateBy { it.id } }
    val active = serials.count { it.status == "ACTIVE" }
    val unused = serials.count { it.status == "UNUSED" }
    val revoked = serials.count { it.status == "REVOKED" }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("إدارة السيريالات", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Default.Refresh, contentDescription = "تحديث")
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "خروج")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showCreateDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("سيريال جديد") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            // شريط الإحصائيات
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard("الكل", serials.size.toString(), MaterialTheme.colorScheme.primary, Modifier.weight(1f))
                StatCard("فعّال", active.toString(), Color4CAF50, Modifier.weight(1f))
                StatCard("غير مُفعّل", unused.toString(), Color9E9E9E, Modifier.weight(1f))
                StatCard("موقوف", revoked.toString(), ColorF44336, Modifier.weight(1f))
            }

            if (serials.isEmpty() && !isLoading) {
                Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                    Text(
                        "لا توجد سيريالات بعد. اضغط \"سيريال جديد\" للبدء.",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(serials.sortedByDescending { it.id }) { serial ->
                        SerialCard(
                            serial = serial,
                            client = clientsById[serial.clientId],
                            onToggle = { onToggle(serial.id) },
                            onReset = { onReset(serial.id) },
                            onDeleteRequest = { pendingDeleteId = serial.id }
                        )
                    }
                    item { Spacer(Modifier.height(72.dp)) }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateSerialDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { name, network, phone, months, notes, deviceId ->
                onCreateSerial(name, network, phone, months, notes, deviceId)
                showCreateDialog = false
            }
        )
    }

    pendingDeleteId?.let { id ->
        AlertDialog(
            onDismissRequest = { pendingDeleteId = null },
            title = { Text("تأكيد الحذف") },
            text = { Text("سيتم حذف هذا السيريال والعميل المرتبط به نهائيًا. لا يمكن التراجع عن هذا الإجراء.") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete(id)
                    pendingDeleteId = null
                }) {
                    Text("حذف نهائي", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteId = null }) { Text("إلغاء") }
            }
        )
    }
}

@Composable
private fun StatCard(label: String, value: String, color: androidx.compose.ui.graphics.Color, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, style = MaterialTheme.typography.titleLarge, color = color, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
        }
    }
}

@Composable
private fun SerialCard(
    serial: Serial,
    client: Client?,
    onToggle: () -> Unit,
    onReset: () -> Unit,
    onDeleteRequest: () -> Unit
) {
    val (statusLabel, statusColor) = when (serial.status) {
        "ACTIVE" -> "فعّال" to Color4CAF50
        "REVOKED" -> "موقوف" to ColorF44336
        else -> "غير مُفعّل" to Color9E9E9E
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(serial.serialKey, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text(
                        "${client?.name ?: "—"} · ${client?.networkName ?: ""}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                Box(
                    modifier = Modifier
                        .background(statusColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(statusLabel, color = statusColor, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                }
            }

            Spacer(Modifier.height(8.dp))
            Text(
                "الهاتف: ${client?.phone ?: "—"}  •  المدة: ${serial.durationMonths} شهر  •  ينتهي: ${serial.endDate}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            if (!serial.deviceId.isNullOrBlank()) {
                Text(
                    "مربوط بجهاز: ${serial.deviceId}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }

            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onToggle, modifier = Modifier.weight(1f)) {
                    Text(if (serial.status == "REVOKED") "إعادة تفعيل" else "إيقاف", style = MaterialTheme.typography.labelLarge)
                }
                OutlinedButton(onClick = onReset, modifier = Modifier.weight(1f)) {
                    Text("فك ربط الجهاز", style = MaterialTheme.typography.labelLarge)
                }
                IconButton(onClick = onDeleteRequest) {
                    Icon(Icons.Default.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

private val Color4CAF50 = androidx.compose.ui.graphics.Color(0xFF4CAF50)
private val ColorF44336 = androidx.compose.ui.graphics.Color(0xFFF44336)
private val Color9E9E9E = androidx.compose.ui.graphics.Color(0xFF9E9E9E)
