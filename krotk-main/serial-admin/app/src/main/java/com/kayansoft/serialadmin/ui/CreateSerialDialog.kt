package com.kayansoft.serialadmin.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun CreateSerialDialog(
    onDismiss: () -> Unit,
    onCreate: (name: String, network: String, phone: String, months: Int, notes: String, deviceId: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var network by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var months by remember { mutableStateOf("12") }
    var notes by remember { mutableStateOf("") }
    var deviceId by remember { mutableStateOf("") }

    val isValid = name.isNotBlank() && network.isNotBlank() && phone.isNotBlank() &&
        (months.toIntOrNull() ?: 0) > 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("إنشاء سيريال جديد", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name, onValueChange = { name = it },
                    label = { Text("اسم العميل *") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = network, onValueChange = { network = it },
                    label = { Text("اسم الشبكة *") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone, onValueChange = { phone = it },
                    label = { Text("رقم الهاتف *") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = months, onValueChange = { months = it.filter { c -> c.isDigit() } },
                    label = { Text("مدة الاشتراك (أشهر) *") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = deviceId, onValueChange = { deviceId = it },
                    label = { Text("معرّف الجهاز (اختياري - للربط المسبق)") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notes, onValueChange = { notes = it },
                    label = { Text("ملاحظات (اختياري)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = isValid,
                onClick = { onCreate(name.trim(), network.trim(), phone.trim(), months.toInt(), notes.trim(), deviceId.trim()) }
            ) {
                Text("إنشاء", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
}
