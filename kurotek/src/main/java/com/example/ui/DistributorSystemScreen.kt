package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.models.*
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DistributorSystemScreen(
    viewModel: MainViewModel,
    initialTab: Int,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    val customers by viewModel.distributorCustomers.collectAsState()
    val transactions by viewModel.distributorTransactions.collectAsState()
    val expenses by viewModel.distributorExpenses.collectAsState()
    val capitals by viewModel.distributorCapitals.collectAsState()

    var activeTab by remember { mutableStateOf(initialTab) }
    
    // Dialog states
    var showAddCustomerDialog by remember { mutableStateOf(false) }
    var showPerformSaleDialog by remember { mutableStateOf(false) }
    var showAddExpenseDialog by remember { mutableStateOf(false) }
    var showAddCapitalDialog by remember { mutableStateOf(false) }
    var showAddTransactionDialog by remember { mutableStateOf(false) }
    var selectedCustomerForTx by remember { mutableStateOf<DistributorCustomer?>(null) }
    var showCustomerDetailsDialog by remember { mutableStateOf<DistributorCustomer?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "نظام الموزعين وإدارة الحسابات",
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "رجوع",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                })
        }
) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Summary Row
            Card(
modifier = Modifier.fillMaxWidth()
) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "الملخص المالي العام (محلي)",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val totalSalesVal = customers.sumOf { it.totalSales }
                        val totalPaymentsVal = customers.sumOf { it.totalPayments }
                        val totalDebtsVal = customers.sumOf { it.currentBalance }
                        
                        SummaryItem(title = "المبيعات", value = "${totalSalesVal} ر.ي", color = MaterialTheme.colorScheme.primary)
                        SummaryItem(title = "التحصيلات", value = "${totalPaymentsVal} ر.ي", color = Color(0xFF4CAF50))
                        SummaryItem(title = "الديون القائمة", value = "${totalDebtsVal} ر.ي", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            // Tab bar
            ScrollableTabRow(
                selectedTabIndex = activeTab,
                edgePadding = 0.dp, modifier = Modifier.fillMaxWidth()
            ) {
                Tab(selected = activeTab == 0, onClick = { activeTab = 0 }) {
                    Text("العملاء", modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Bold)
                }
                Tab(selected = activeTab == 1, onClick = { activeTab = 1 }) {
                    Text("إجراء عملية بيع", modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Bold)
                }
                Tab(selected = activeTab == 2, onClick = { activeTab = 2 }) {
                    Text("المصاريف", modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Bold)
                }
                Tab(selected = activeTab == 3, onClick = { activeTab = 3 }) {
                    Text("حركة رأس المال", modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Bold)
                }
            }

            // Main Content Area
            Box(modifier = Modifier.weight(1f)) {
                when (activeTab) {
                    0 -> CustomersTab(
                        customers = customers,
                        onAddCustomer = { showAddCustomerDialog = true },
                        onCustomerClick = { showCustomerDetailsDialog = it },
                        onDeleteCustomer = { viewModel.deleteDistributorCustomer(it.id) },
                        onAddTransaction = { 
                            selectedCustomerForTx = it
                            showAddTransactionDialog = true
                        }
                    )
                    1 -> SaleInvoicingTab(
                        viewModel = viewModel,
                        customers = customers,
                        onSalePerformed = {
                            Toast.makeText(context, "تمت عملية البيع وحفظ الفاتورة بنجاح!", Toast.LENGTH_SHORT).show()
                        }
                    )
                    2 -> ExpensesTab(
                        expenses = expenses,
                        onAddExpense = { showAddExpenseDialog = true },
                        onDeleteExpense = { viewModel.deleteDistributorExpense(it.id) }
                    )
                    3 -> CapitalTab(
                        capitals = capitals,
                        onAddCapital = { showAddCapitalDialog = true },
                        onDeleteCapital = { viewModel.deleteDistributorCapital(it.id) }
                    )
                }
            }
        }
    }

    // --- Dialogs ---

    // 1. Add Customer Dialog
    if (showAddCustomerDialog) {
        var name by remember { mutableStateOf("") }
        var phoneId by remember { mutableStateOf("") }
        
        AlertDialog(
            onDismissRequest = { showAddCustomerDialog = false },
            title = { Text("إضافة عميل موزع جديد") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("اسم العميل") }, modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = phoneId,
                        onValueChange = { phoneId = it },
                        label = { Text("رقم الهاتف أو المعرف (اختياري)") }, modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.trim().isNotEmpty()) {
                            viewModel.insertDistributorCustomer(
                                name = name.trim(),
                                customId = phoneId.trim().ifEmpty { null }
                            )
                            showAddCustomerDialog = false
                            Toast.makeText(context, "تمت إضافة العميل $name بنجاح!", Toast.LENGTH_SHORT).show()
                        }
                    }) {
                    Text("حفظ")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCustomerDialog = false }) {
                    Text("إلغاء", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
)
    }

    // 2. Add Direct Transaction (Payment or Manual Debt)
    if (showAddTransactionDialog && selectedCustomerForTx != null) {
        val customer = selectedCustomerForTx!!
        var type by remember { mutableStateOf("payment") } // "sale" or "payment"
        var amount by remember { mutableStateOf("") }
        var notes by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddTransactionDialog = false },
            title = { Text("قيد مالي: ${customer.name}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { type = "payment" },
                             MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.weight(1f)
                        ) {
                            Text("سند تحصيل (دفعة)")
                        }
                        Button(
                            onClick = { type = "sale" },
                             MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f)
                        ) {
                            Text("قيد دين مباشر")
                        }
                    }
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        label = { Text("المبلغ (ر.ي)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("ملاحظات") }, modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amtVal = amount.toDoubleOrNull() ?: 0.0
                        if (amtVal > 0) {
                            viewModel.insertDistributorTransaction(
                                customerId = customer.id,
                                type = type,
                                amount = amtVal,
                                notes = notes.trim()
                            )
                            showAddTransactionDialog = false
                            Toast.makeText(context, "تم حفظ الحركة المالية بنجاح!", Toast.LENGTH_SHORT).show()
                        }
                    },
                     MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text("حفظ")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTransactionDialog = false }) {
                    Text("إلغاء", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
)
    }

    // 3. Add Expense Dialog
    if (showAddExpenseDialog) {
        var category by remember { mutableStateOf("fuel") } // fuel, rent, salaries, maintenance, other
        val categories = listOf("fuel" to "بترول / مواصلات", "rent" to "إيجار", "salaries" to "رواتب", "maintenance" to "صيانة", "other" to "أخرى")
        var amount by remember { mutableStateOf("") }
        var desc by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddExpenseDialog = false },
            title = { Text("تسجيل مصروف جديد") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Category dropdown imitation (simple Row selection)
                    Text("فئة المصروف:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        categories.forEach { (catKey, catName) ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        if (category == catKey) MaterialTheme.colorScheme.primary,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { category = catKey }
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = catName.substringBefore(" "),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        label = { Text("قيمة المصروف (ر.ي)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = desc,
                        onValueChange = { desc = it },
                        label = { Text("التفاصيل") }, modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amtVal = amount.toDoubleOrNull() ?: 0.0
                        if (amtVal > 0) {
                            viewModel.insertDistributorExpense(
                                category = category,
                                amount = amtVal,
                                description = desc.trim()
                            )
                            showAddExpenseDialog = false
                            Toast.makeText(context, "تم قيد المصروف بنجاح!", Toast.LENGTH_SHORT).show()
                        }
                    }) {
                    Text("حفظ")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddExpenseDialog = false }) {
                    Text("إلغاء", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
)
    }

    // 4. Add Capital Dialog
    if (showAddCapitalDialog) {
        var type by remember { mutableStateOf("deposit") } // "deposit" or "withdraw"
        var amount by remember { mutableStateOf("") }
        var desc by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddCapitalDialog = false },
            title = { Text("قيد حركة رأس مال") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { type = "deposit" },
                             MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.weight(1f)
                        ) {
                            Text("إيداع رأس مال")
                        }
                        Button(
                            onClick = { type = "withdraw" },
                             MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f)
                        ) {
                            Text("سحب رأس مال")
                        }
                    }
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        label = { Text("المبلغ (ر.ي)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = desc,
                        onValueChange = { desc = it },
                        label = { Text("ملاحظات / المصدر") }, modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amtVal = amount.toDoubleOrNull() ?: 0.0
                        if (amtVal > 0) {
                            viewModel.insertDistributorCapital(
                                type = type,
                                amount = amtVal,
                                description = desc.trim()
                            )
                            showAddCapitalDialog = false
                            Toast.makeText(context, "تم حفظ الحركة الحسابية لرأس المال بنجاح!", Toast.LENGTH_SHORT).show()
                        }
                    },
                     MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text("حفظ")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCapitalDialog = false }) {
                    Text("إلغاء", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
)
    }

    // 5. Customer Details Dialog with complete statement history
    if (showCustomerDetailsDialog != null) {
        val cust = showCustomerDetailsDialog!!
        val customerTxs = transactions.filter { it.customerId == cust.id }
        
        AlertDialog(
            onDismissRequest = { showCustomerDetailsDialog = null },
            title = {
                Text(
                    text = "كشف حساب: ${cust.name}",
                    color = MaterialTheme.colorScheme.primary
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
modifier = Modifier.fillMaxWidth()
) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text("المبيعات الكلية:")
                                Text("${cust.totalSales} ر.ي", fontWeight = FontWeight.Bold)
                            }
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text("الواصل (المدفوع):")
                                Text("${cust.totalPayments} ر.ي", color = Color(0xFF4CAF50))
                            }
                            Divider()
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text("الرصيد المتبقي (الدين):")
                                Text("${cust.currentBalance} ر.ي", color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }

                    Text("الحركات المالية الأخيرة:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    
                    if (customerTxs.isEmpty()) {
                        Text("لا توجد حركات سابقة مقيدة لهذا العميل.", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.heightIn(max = 200.dp)
                        ) {
                            items(customerTxs) { tx ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = if (tx.type == "sale") "دين مبيعات" else "دفعة مستلمة",
                                            color = if (tx.type == "sale") MaterialTheme.colorScheme.primary
                                        )
                                        if (tx.notes.isNotEmpty()) {
                                            Text(tx.notes, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "${tx.amount} ر.ي",
                                            fontWeight = FontWeight.Bold
                                        )
                                        IconButton(
                                            onClick = { viewModel.deleteDistributorTransaction(tx.id, cust.id) }, modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "حذف القيد", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showCustomerDetailsDialog = null }) {
                    Text("إغلاق")
                }
            }
)
    }
}

@Composable
fun SummaryItem(title: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, color = color, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun CustomersTab(
    customers: List<DistributorCustomer>,
    onAddCustomer: () -> Unit,
    onCustomerClick: (DistributorCustomer) -> Unit,
    onDeleteCustomer: (DistributorCustomer) -> Unit,
    onAddTransaction: (DistributorCustomer) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("قائمة العملاء الموزعين", color = MaterialTheme.colorScheme.onSurface)
            Button(
                onClick = onAddCustomer) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Text("عميل جديد")
                }
            }
        }

        if (customers.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("لا يوجد عملاء مقيدين حالياً.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(customers) { customer ->
                    Card(modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onCustomerClick(customer) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(customer.name, color = MaterialTheme.colorScheme.onSurface)
                                Text("رقم المعرف: ${customer.id.take(8)}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 4.dp)) {
                                    Text("مبيعات: ${customer.totalSales}", color = MaterialTheme.colorScheme.primary)
                                    Text("واصل: ${customer.totalPayments}", color = Color(0xFF4CAF50))
                                }
                            }
                            
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("الرصيد", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        "${customer.currentBalance} ر.ي",
                                        color = if (customer.currentBalance > 0) MaterialTheme.colorScheme.primary
                                    )
                                }
                                
                                IconButton(onClick = { onAddTransaction(customer) }) {
                                    Icon(Icons.Default.ReceiptLong, contentDescription = "إضافة قيد مالي", tint = MaterialTheme.colorScheme.primary)
                                }

                                IconButton(onClick = { onDeleteCustomer(customer) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "حذف العميل", tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SaleInvoicingTab(
    viewModel: MainViewModel,
    customers: List<DistributorCustomer>,
    onSalePerformed: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedCustomerId by remember { mutableStateOf("") }
    var expandedCustomerMenu by remember { mutableStateOf(false) }

    // Map of Category -> Quantity to sell
    val cardQuantities = remember { mutableStateMapOf<Int, String>() }
    val categories = listOf(
        100 to "كرتوني (100 ر.ي)",
        200 to "فئة 200 ر.ي",
        250 to "فئة 250 ر.ي",
        300 to "فئة 300 ر.ي",
        500 to "فئة 500 ر.ي"
    )

    var partialPayment by remember { mutableStateOf("") }

    // Derive totals
    val totalAmount = categories.sumOf { (cat, _) ->
        val qty = cardQuantities[cat]?.toIntOrNull() ?: 0
        qty * cat
    }

    val selectedCustomer = customers.find { it.id == selectedCustomerId }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(androidx.compose.foundation.rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("فاتورة مبيعات كروت جديدة للموزعين", color = MaterialTheme.colorScheme.onSurface)

        // Dropdown Selection for Customer
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { expandedCustomerMenu = true }, modifier = Modifier.fillMaxWidth()
) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(selectedCustomer?.name ?: "إختر العميل الموزع *", color = if (selectedCustomer != null) MaterialTheme.colorScheme.onSurface)
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
            }

            DropdownMenu(
                expanded = expandedCustomerMenu,
                onDismissRequest = { expandedCustomerMenu = false }, modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                // Add Cash Customer selection manually
                DropdownMenuItem(
                    text = { Text("مبيعات نقدية (CASH)", color = MaterialTheme.colorScheme.onSurface) },
                    onClick = {
                        selectedCustomerId = "CASH"
                        expandedCustomerMenu = false
                    }
                )
                customers.forEach { cust ->
                    DropdownMenuItem(
                        text = { Text(cust.name, color = MaterialTheme.colorScheme.onSurface) },
                        onClick = {
                            selectedCustomerId = cust.id
                            expandedCustomerMenu = false
                        }
                    )
                }
            }
        }

        Divider()

        Text("حدد كمية الكروت المراد بيعها لكل فئة:", color = MaterialTheme.colorScheme.onSurfaceVariant)

        // Card Entry Grid
        categories.forEach { (categoryVal, categoryName) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(categoryName, color = MaterialTheme.colorScheme.onSurface)
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val qty = cardQuantities[categoryVal] ?: ""
                    
                    IconButton(
                        onClick = {
                            val currentQty = qty.toIntOrNull() ?: 0
                            if (currentQty > 0) {
                                cardQuantities[categoryVal] = (currentQty - 1).toString()
                            }
                        }, modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }

                    OutlinedTextField(
                        value = qty,
                        onValueChange = { cardQuantities[categoryVal] = it }, modifier = Modifier.width(70.dp),
                        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )

                    IconButton(
                        onClick = {
                            val currentQty = qty.toIntOrNull() ?: 0
                            cardQuantities[categoryVal] = (currentQty + 1).toString()
                        }, modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF4CAF50))
                    }
                }
            }
        }

        Divider()

        // Total details and Invoice configuration
        Card(
modifier = Modifier.fillMaxWidth()
) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("إجمالي الفاتورة:")
                    Text("$totalAmount ر.ي", color = MaterialTheme.colorScheme.primary)
                }

                OutlinedTextField(
                    value = partialPayment,
                    onValueChange = { partialPayment = it },
                    label = { Text("المبلغ المدفوع كاش (ر.ي) - (إختياري)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth()
                )

                val payVal = partialPayment.toDoubleOrNull() ?: 0.0
                val remainingDebt = totalAmount - payVal
                
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("المبلغ المتبقي (آجل/دين):")
                    Text("$remainingDebt ر.ي", color = if (remainingDebt > 0) MaterialTheme.colorScheme.primary)
                }
            }
        }

        // Submit Button
        Button(
            onClick = {
                if (selectedCustomerId.isEmpty()) {
                    Toast.makeText(context, "الرجاء اختيار العميل الموزع أولاً!", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                if (totalAmount <= 0) {
                    Toast.makeText(context, "الرجاء تحديد كروت لبيعها بقيمة أكبر من الصفر!", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                val salesMap = cardQuantities.mapValues { it.value.toIntOrNull() ?: 0 }.filterValues { it > 0 }
                val payVal = partialPayment.toDoubleOrNull() ?: 0.0

                coroutineScope.launch {
                    val totalBuyingCost = totalAmount * 0.93
                    val calcProfits = totalAmount * 0.07
                    viewModel.performDistributorSale(
                        customerId = selectedCustomerId,
                        quantities = salesMap,
                        totalAmount = totalAmount.toDouble(),
                        totalBuyingCost = totalBuyingCost,
                        calcProfits = calcProfits,
                        receivedAmount = payVal,
                        onComplete = { success, message ->
                            if (success) {
                                // Reset fields
                                cardQuantities.clear()
                                partialPayment = ""
                                onSalePerformed()
                            } else {
                                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                            }
                        }
                    )
                }
            }, modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("تسجيل الفاتورة وحفظ البيع ⚡", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ExpensesTab(
    expenses: List<DistributorExpense>,
    onAddExpense: () -> Unit,
    onDeleteExpense: (DistributorExpense) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("دفتر المصاريف", color = MaterialTheme.colorScheme.onSurface)
            Button(
                onClick = onAddExpense) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Text("تسجيل مصروف")
                }
            }
        }

        if (expenses.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("لم يتم تسجيل أي مصاريف حتى الآن.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(expenses) { expense ->
                    val catName = when (expense.category) {
                        "fuel" -> "بترول / مواصلات 🚗"
                        "rent" -> "إيجار 🏢"
                        "salaries" -> "رواتب 👥"
                        "maintenance" -> "صيانة ومعدات 🛠️"
                        else -> "أخرى 📝"
                    }
                    
                    Card(
modifier = Modifier.fillMaxWidth()
) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(catName, color = MaterialTheme.colorScheme.onSurface)
                                if (expense.description.isNotEmpty()) {
                                    Text(expense.description, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                val dateStr = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date(expense.date))
                                Text(dateStr, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("${expense.amount} ر.ي", color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(end = 8.dp))
                                IconButton(onClick = { onDeleteExpense(expense) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CapitalTab(
    capitals: List<DistributorCapital>,
    onAddCapital: () -> Unit,
    onDeleteCapital: (DistributorCapital) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("رأس المال وحركة السيولة", color = MaterialTheme.colorScheme.onSurface)
            Button(
                onClick = onAddCapital) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Text("قيد سيولة")
                }
            }
        }

        if (capitals.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("لم يتم تقييد أي حركة لرأس المال.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(capitals) { cap ->
                    val isDeposit = cap.type == "deposit"
                    Card(
modifier = Modifier.fillMaxWidth()
) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (isDeposit) "إيداع سيولة (رأس مال) 📥" else "سحب أرباح / سيولة 📤",
                                    color = if (isDeposit) MaterialTheme.colorScheme.secondary
                                )
                                if (cap.description.isNotEmpty()) {
                                    Text(cap.description, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                val dateStr = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date(cap.date))
                                Text(dateStr, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${if (isDeposit) "+" else "-"}${cap.amount} ر.ي",
                                    color = if (isDeposit) MaterialTheme.colorScheme.secondary, modifier = Modifier.padding(end = 8.dp)
                                )
                                IconButton(onClick = { onDeleteCapital(cap) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
