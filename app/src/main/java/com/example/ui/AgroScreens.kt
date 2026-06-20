package com.example.ui

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import java.text.SimpleDateFormat
import java.util.*

// Helper function to safely parse hex color
fun String.toColor(): Color {
    return try {
        Color(android.graphics.Color.parseColor(this))
    } catch (e: Exception) {
        Color.Gray
    }
}

// Ensure contrasting text color for pastel tags
fun Color.getContrastColor(): Color {
    return Color(0xFF212121) // High contrast dark charcoal for pastel backgrounds
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgroMainScreen(viewModel: AgroViewModel) {
    val context = LocalContext.current
    val partners by viewModel.partners.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val batches by viewModel.batches.collectAsStateWithLifecycle()
    val approvals by viewModel.approvals.collectAsStateWithLifecycle()
    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val inventoryItems by viewModel.inventoryItems.collectAsStateWithLifecycle()
    val creditItems by viewModel.creditItems.collectAsStateWithLifecycle()
    val auditLogs by viewModel.auditLogs.collectAsStateWithLifecycle()
    val activePartner by viewModel.activePartner.collectAsStateWithLifecycle()

    var currentTab by remember { mutableStateOf(0) }
    var showLoginDialog by remember { mutableStateOf(false) }
    var selectedLoginPartnerId by remember { mutableStateOf(1) }
    var pinInputValue by remember { mutableStateOf("") }
    
    // Dialog state for adding transactions
    var showAddTxDialog by remember { mutableStateOf(false) }

    // Navigation and Scaffold
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "খামার লোগো",
                            tint = Color(0xFF386B40),
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "৫ এগ্রো",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color(0xFF386B40)
                        )
                    }
                },
                actions = {
                    activePartner?.let { partner ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(partner.colorHex.toColor())
                                .clickable {
                                    showLoginDialog = true
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF386B40))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = partner.name,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = partner.colorHex.toColor().getContrastColor()
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFE8F3E9)
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFFE8F3E9)
            ) {
                val tabs = listOf(
                    Triple("ড্যাশবোর্ড", Icons.Default.Home, 0),
                    Triple("হিসাবখাতা", Icons.Default.List, 1),
                    Triple("খামার স্পেশাল", Icons.Default.ShoppingCart, 2),
                    Triple("আলোচনা চ্যাট", Icons.Default.Face, 3),
                    Triple("সেটিংস", Icons.Default.Settings, 4)
                )

                tabs.forEach { (title, icon, index) ->
                    NavigationBarItem(
                        selected = currentTab == index,
                        onClick = { currentTab = index },
                        icon = { Icon(imageVector = icon, contentDescription = title) },
                        label = { Text(title, fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF386B40),
                            selectedTextColor = Color(0xFF386B40),
                            indicatorColor = Color(0xFFD1E7DD),
                            unselectedIconColor = Color(0xFF5D6257).copy(alpha = 0.6f),
                            unselectedTextColor = Color(0xFF5D6257).copy(alpha = 0.6f)
                        )
                    )
                }
            }
        },
        floatingActionButton = {
            if (currentTab == 1) {
                FloatingActionButton(
                    onClick = { showAddTxDialog = true },
                    containerColor = Color(0xFF386B40),
                    contentColor = Color.White
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "ঐতিহাসিক হিসাব যোগ")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("হিসাব যুক্ত করুন", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF7F9F5))
        ) {
            when (currentTab) {
                0 -> DashboardScreen(
                    partners = partners,
                    transactions = transactions,
                    batches = batches,
                    activePartner = activePartner,
                    viewModel = viewModel,
                    onSwitchRequest = { showLoginDialog = true }
                )
                1 -> LedgerScreen(
                    viewModel = viewModel,
                    partners = partners,
                    transactions = transactions,
                    batches = batches,
                    approvals = approvals,
                    activePartner = activePartner
                )
                2 -> AgroFeaturesScreen(
                    viewModel = viewModel,
                    activePartner = activePartner,
                    batches = batches,
                    creditItems = creditItems,
                    inventoryItems = inventoryItems
                )
                3 -> ChatScreen(
                    viewModel = viewModel,
                    activePartner = activePartner,
                    chatMessages = chatMessages,
                    partners = partners,
                    transactions = transactions
                )
                4 -> SettingsScreen(
                    viewModel = viewModel,
                    activePartner = activePartner,
                    partners = partners,
                    auditLogs = auditLogs
                )
            }
        }
    }

    // --- Secured Profile Switch / Login Dialog ---
    if (showLoginDialog) {
        Dialog(onDismissRequest = { showLoginDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "সদস্য লগইন / প্রোফাইল পরিবর্তন",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF386B40),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "যেকোনো মেম্বার সিলেক্ট করে তার ৪-ডিজিট পিন দিয়ে লগইন করুন:",
                        fontSize = 13.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Grid of 6 partners
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        partners.chunked(2).forEach { rowList ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowList.forEach { partner ->
                                    val isSelected = selectedLoginPartnerId == partner.id
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (isSelected) partner.colorHex.toColor().copy(alpha = 0.8f)
                                                else Color(0xFFF5F5F5)
                                            )
                                            .border(
                                                width = if (isSelected) 2.dp else 1.dp,
                                                color = if (isSelected) Color(0xFF386B40) else Color.LightGray,
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .clickable {
                                                selectedLoginPartnerId = partner.id
                                                pinInputValue = "" // Clear PIN on switch
                                            }
                                            .padding(10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Box(
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .clip(CircleShape)
                                                    .background(partner.colorHex.toColor())
                                                    .border(1.dp, Color.Gray, CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = partner.id.toString(),
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = partner.colorHex.toColor().getContrastColor()
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = partner.name,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.Black,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                    text = "পিন: ${partner.pin}",
                                                    fontSize = 10.sp,
                                                    color = Color.DarkGray
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // PIN Code Input Field
                    OutlinedTextField(
                        value = pinInputValue,
                        onValueChange = {
                            if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                                pinInputValue = it
                            }
                        },
                        label = { Text("৪-ডিজিটের নিরাপত্তা পিন") },
                        placeholder = { Text("যেমন: ১১২২") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        leadingIcon = { Icon(imageVector = Icons.Default.Lock, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF386B40),
                            focusedLabelColor = Color(0xFF386B40)
                        )
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showLoginDialog = false },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.DarkGray)
                        ) {
                            Text("বাতিল")
                        }

                        Button(
                            onClick = {
                                if (pinInputValue.length != 4) {
                                    Toast.makeText(context, "অনুগ্রহ করে ৪ সংখ্যার সম্পূর্ণ পিন দিন!", Toast.LENGTH_SHORT).show()
                                } else {
                                    viewModel.switchPartnerWithPin(
                                        partnerId = selectedLoginPartnerId,
                                        pin = pinInputValue,
                                        onSuccess = {
                                            showLoginDialog = false
                                            Toast.makeText(context, "সাফল্যের সাথে লগইন সম্পন্ন হয়েছে!", Toast.LENGTH_SHORT).show()
                                        },
                                        onError = { errorText ->
                                            Toast.makeText(context, errorText, Toast.LENGTH_LONG).show()
                                        }
                                    )
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF386B40))
                        ) {
                            Text("প্রবেশ করুন", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // --- Add Transaction Dialog ---
    if (showAddTxDialog) {
        var txTitle by remember { mutableStateOf("") }
        var txAmount by remember { mutableStateOf("") }
        var txType by remember { mutableStateOf("EXPENSE") } // INCOME, EXPENSE, DRAWING, SALARY
        var txCategory by remember { mutableStateOf("খাবার খরচ") }
        var isFromPocket by remember { mutableStateOf(false) }
        var selectedBatchId by remember { mutableStateOf<Int?>(null) }
        var hasGeneratedReceipt by remember { mutableStateOf(false) }
        var simulatedReceiptUri by remember { mutableStateOf<String?>(null) }

        val expenseCategories = listOf("খাবার খরচ", "ওষুধ", "পরিবহন", "বেতন", "অন্যান্য")
        val drawingCategories = listOf("ব্যক্তিগত উত্তোলন")
        val incomeCategories = listOf("ডিম বিক্রয়", "মুরগি বিক্রয়", "হাঁস বিক্রয়", "অন্যান্য আয়")
        val salaryCategories = listOf("চলমান শ্রম পারিশ্রমিক")

        val currentCategories = when (txType) {
            "INCOME" -> incomeCategories
            "EXPENSE" -> expenseCategories
            "DRAWING" -> drawingCategories
            "SALARY" -> salaryCategories
            else -> expenseCategories
        }

        // Auto-adjust category if not in list
        LaunchedEffect(txType) {
            txCategory = currentCategories.first()
        }

        Dialog(onDismissRequest = { showAddTxDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "নতুন লেনদেন হিসাব যুক্ত করুন",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF386B40),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )

                    // Transaction Title
                    OutlinedTextField(
                        value = txTitle,
                        onValueChange = { txTitle = it },
                        label = { Text("লেনদেনের বিবরণ / বিবরণী *") },
                        placeholder = { Text("যেমন: পোল্ট্রি ফিড ফিডিং ক্যাশ") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF386B40))
                    )

                    // Transaction Amount
                    OutlinedTextField(
                        value = txAmount,
                        onValueChange = { if (it.all { char -> char.isDigit() || char == '.' }) txAmount = it },
                        label = { Text("টাকার পরিমাণ (৳) *") },
                        placeholder = { Text("যেমন: ৬০০০") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF386B40))
                    )

                    // Transaction Type SELECTOR
                    Text("লেনদেনের ধরন:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(
                            "EXPENSE" to "খরচ",
                            "INCOME" to "আয়",
                            "DRAWING" to "উত্তোলন",
                            "SALARY" to "বেতন"
                        ).forEach { (code, label) ->
                            val isSelected = txType == code
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) Color(0xFFD1E7DD) else Color(0xFFF5F5F5))
                                    .border(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) Color(0xFF386B40) else Color.LightGray,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { txType = code }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color(0xFF386B40) else Color.DarkGray
                                )
                            }
                        }
                    }

                    // Category dropdown simulator
                    Text("ক্যাটাগরি নির্বাচন করুন:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        currentCategories.chunked(3).forEach { rowList ->
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
                                rowList.forEach { cat ->
                                    val isSelected = txCategory == cat
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isSelected) Color(0xFFC8E6C9) else Color(0xFFECEFF1))
                                            .clickable { txCategory = cat }
                                            .padding(6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = cat,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color.Black
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Batch association Selector
                    Text("সংশ্লিষ্ট ব্যাচ (ঐচ্ছিক):", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selectedBatchId == null) Color(0xFFC8E6C9) else Color(0xFFEEEEEE))
                                .clickable { selectedBatchId = null }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text("ব্যাচ ছাড়া", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        batches.forEach { batch ->
                            val isSelected = selectedBatchId == batch.id
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) Color(0xFFD1E7DD) else Color(0xFFEEEEEE))
                                    .clickable { selectedBatchId = batch.id }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(batch.name, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Member pocket spending checkbox
                    if (txType == "EXPENSE") {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                                .clickable { isFromPocket = !isFromPocket }
                                .padding(8.dp)
                        ) {
                            Checkbox(
                                checked = isFromPocket,
                                onCheckedChange = { isFromPocket = it },
                                colors = CheckboxDefaults.colors(checkedColor = Color(0xFF386B40))
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "আমি নিজের পকেট থেকে ফার্মের এই খরচ মেটালাম (ধার বা অবদান হিসেবে সংরক্ষিত হবে)",
                                fontSize = 11.sp,
                                lineHeight = 14.sp,
                                color = Color.DarkGray
                            )
                        }
                    }

                    // Receipt Upload Simulation
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
                        border = BorderStroke(1.dp, Color.LightGray)
                    ) {
                        Row(
                            modifier = Modifier
                                .clickable {
                                    hasGeneratedReceipt = true
                                    simulatedReceiptUri = "receipt_memo_" + System.currentTimeMillis()
                                }
                                .padding(12.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (hasGeneratedReceipt) Icons.Default.Check else Icons.Default.List,
                                contentDescription = null,
                                tint = if (hasGeneratedReceipt) Color(0xFF386B40) else Color.Gray,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (hasGeneratedReceipt) "রসিদ/মেমো মেমোরি সংযুক্ত হয়েছে" else "ডিজিটাল রসিদ বা ক্যাশ মেমো সংযুক্ত করুন",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (hasGeneratedReceipt) Color(0xFF386B40) else Color.Black
                                )
                                Text(
                                    text = if (hasGeneratedReceipt) "মেমো কোড: $simulatedReceiptUri" else "ক্লিক করলেই রসিদের মেমোরি অটো-ছবি জেনারেট হবে",
                                    fontSize = 10.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }

                    // Big expense alert info
                    val amountNum = txAmount.toDoubleOrNull() ?: 0.0
                    if (txType == "EXPENSE" && amountNum > 5000) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFFFF3CD))
                                .padding(10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.Top) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Color(0xFF856404),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "৫,০০০ টাকার বেশি খরচের হিসাব যুক্ত করায় এটি ডিজিটাল অনুমোদন অপেক্ষায় (PENDING) থাকবে। সচল হওয়ার জন্য অন্যান্য মেম্বারদের ভোট লাগবে।",
                                    color = Color(0xFF856404),
                                    fontSize = 11.sp,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }

                    // Dialog Actions
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showAddTxDialog = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("বাতিল")
                        }

                        Button(
                            onClick = {
                                if (txTitle.trim().isEmpty() || txAmount.trim().isEmpty()) {
                                    Toast.makeText(context, "দয়া করে তারকাচিহ্নিত বিবরণ ও টাকা সঠিকভাবে পূরণ করুন!", Toast.LENGTH_SHORT).show()
                                } else {
                                    val amountDouble = txAmount.toDoubleOrNull() ?: 0.0
                                    activePartner?.let { partner ->
                                        viewModel.addNewTransaction(
                                            title = txTitle,
                                            amount = amountDouble,
                                            type = txType,
                                            category = txCategory,
                                            batchId = selectedBatchId,
                                            isFromPocket = if (txType == "EXPENSE") isFromPocket else false,
                                            addedByPartnerId = partner.id,
                                            receiptUri = simulatedReceiptUri,
                                            onSuccess = {
                                                showAddTxDialog = false
                                                Toast.makeText(context, "হিসাবটি সফলভাবে যুক্ত হয়েছে!", Toast.LENGTH_SHORT).show()
                                            }
                                        )
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF386B40)),
                            modifier = Modifier.weight(1.5f)
                        ) {
                            Text("জমা দিন", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 1. DASHBOARD SCREEN
// ==========================================
@Composable
fun DashboardScreen(
    partners: List<Partner>,
    transactions: List<Transaction>,
    batches: List<Batch>,
    activePartner: Partner?,
    viewModel: AgroViewModel,
    onSwitchRequest: () -> Unit
) {
    val context = LocalContext.current
    var detailBatchToShow by remember { mutableStateOf<Batch?>(null) }
    
    // Compute cumulative statistics
    val approvedTxs = transactions.filter { it.approvalStatus == "APPROVED" }
    val totalIncome = approvedTxs.filter { it.type == "INCOME" }.sumOf { it.amount }
    val totalExpense = approvedTxs.filter { it.type == "EXPENSE" }.sumOf { it.amount }
    val netProfit = totalIncome - totalExpense

    // Member due tracking (pocket spendings owed back from farm)
    val membersPocketSpending = approvedTxs.filter { it.isFromPocket && it.type == "EXPENSE" }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome and Quick Profile Switch Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth().shadow(1.dp, RoundedCornerShape(16.dp)),
                border = BorderStroke(1.dp, Color(0xFFE1E4DC))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "স্বাগতম!",
                            fontSize = 12.sp,
                            color = Color(0xFF5D6257),
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = activePartner?.let { "সদস্য: ${it.name}" } ?: "আমাদের খামার",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1C18)
                        )
                    }
                    Button(
                        onClick = onSwitchRequest,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD1E7DD), contentColor = Color(0xFF386B40)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("মেম্বার পরিবর্তন", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Offline-First Sync Status Banner (Responsive Material Design)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFD1E7DD)),
                modifier = Modifier.fillMaxWidth().shadow(1.dp, RoundedCornerShape(16.dp))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "স্বয়ংক্রিয় অফলাইন সিঙ্ক সচল",
                        tint = Color(0xFF386B40),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "স্বয়ংক্রিয় অফলাইন সিঙ্ক সচল (Room)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFF386B40)
                        )
                        Text(
                            text = "ইন্টারনেট না থাকলেও অ্যাপের সব ডাটা ডিভাইস লোকাল SQLite ডাটাবেজে জমা থাকবে এবং নেটওয়ার্ক সচল হওয়ামাত্র স্বয়ংক্রিয়ভাবে সকলের মোবাইল সিঙ্ক হবে।",
                            fontSize = 11.sp,
                            color = Color(0xFF386B40).copy(alpha = 0.85f),
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        // STATS CARDS MERGED BEAUTIFUL HERO CARD (Natural Tones HTML-faithful)
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF386B40)), // Deep forest green
                modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(24.dp))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Text(
                                text = "নিট মুনাফা",
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.8f),
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "৳ ${String.format("%,.0f", netProfit)}",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        // Session badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.2f))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                "আগস্ট ২০২৪",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    Divider(color = Color.White.copy(alpha = 0.15f), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "মোট আয়",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.7f),
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "৳ ${String.format("%,.0f", totalIncome)}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "মোট খরচ",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.7f),
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "৳ ${String.format("%,.0f", totalExpense)}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // Custom Canvas Chart (Sleek Pie Arc + Bar metrics drawing)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth().shadow(1.dp, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "আয় ও ব্যয়ের তুলনা চিত্র",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Draw circular donut arc using canvas
                    val incomePercent = if (totalIncome + totalExpense == 0.0) 0.5f else (totalIncome / (totalIncome + totalExpense)).toFloat()
                    val expensePercent = 1.0f - incomePercent

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(120.dp)) {
                            // Circular slices
                            val strokeWidth = 14.dp.toPx()
                            
                            // Background circle outline
                            drawCircle(
                                color = Color(0xFFEEEEEE),
                                radius = size.minDimension / 2 - strokeWidth / 2,
                                style = Stroke(width = strokeWidth)
                            )
                            
                            // Income Arc (Green)
                            drawArc(
                                color = Color(0xFF386B40),
                                startAngle = -90f,
                                sweepAngle = incomePercent * 360f,
                                useCenter = false,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )

                            // Expense Arc (Red)
                            drawArc(
                                color = Color(0xFFE57373),
                                startAngle = -90f + (incomePercent * 360f),
                                sweepAngle = expensePercent * 360f,
                                useCenter = false,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                        }

                        // Center statistics
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "লাভ অনুপাত",
                                fontSize = 10.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${String.format("%.1f", (incomePercent * 100))}%",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF386B40)
                            )
                        }
                    }

                    // Key details
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(Color(0xFF386B40)))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("আয় (${String.format("%.0f", incomePercent * 100)}%)", fontSize = 11.sp, color = Color.DarkGray)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(Color(0xFFE57373)))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("খরচ (${String.format("%.0f", expensePercent * 100)}%)", fontSize = 11.sp, color = Color.DarkGray)
                        }
                    }
                }
            }
        }

        // Partnership shares circular layout list
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth().shadow(1.dp, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "অংশীদারদের শেয়ার ও লভ্যাংশ বন্টন",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    partners.forEach { partner ->
                        // Calculate individual net share amount
                        val partnerShareAmount = netProfit * (partner.currentSharePercentage / 100.0)
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(partner.colorHex.toColor()),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = partner.id.toString(),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = partner.colorHex.toColor().getContrastColor()
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(partner.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                Text("শেয়ার পার্সেন্টেজ: ${partner.currentSharePercentage}%", fontSize = 11.sp, color = Color.Gray)
                            }
                            Text(
                                text = "৳ ${String.format("%,.1f", partnerShareAmount)}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = if (partnerShareAmount >= 0) Color(0xFF386B40) else Color(0xFFC62828)
                            )
                        }
                        if (partner.id < partners.size) {
                            Divider(color = Color(0xFFF5F5F5), thickness = 1.dp)
                        }
                    }
                }
            }
        }

        // Batch P&L Horizontal summaries list
        item {
            Text(
                text = "চলমান খামার ব্যাচ লাভ-ক্ষতি",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color.Black,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        item {
            if (batches.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Box(modifier = Modifier.padding(24.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("কোনো সচল খামার ব্যাচ পাওয়া যায়নি!", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    batches.forEach { batch ->
                        val txsForBatch = approvedTxs.filter { it.batchId == batch.id }
                        val incBatch = txsForBatch.filter { it.type == "INCOME" }.sumOf { it.amount }
                        val expBatch = txsForBatch.filter { it.type == "EXPENSE" }.sumOf { it.amount }
                        val profitBatch = incBatch - expBatch
                        val profitColor = if (profitBatch >= 0) Color(0xFF386B40) else Color(0xFFC62828)

                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            modifier = Modifier
                                .width(160.dp)
                                .shadow(1.dp, RoundedCornerShape(12.dp))
                                .clickable { detailBatchToShow = batch }
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = batch.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        color = Color.Black
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(if (batch.isActive) Color(0xFF386B40) else Color.Gray)
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text("আয়: ৳ ${String.format("%.0f", incBatch)}", fontSize = 11.sp, color = Color.Gray)
                                Text("ব্যয়: ৳ ${String.format("%.0f", expBatch)}", fontSize = 11.sp, color = Color.Gray)
                                Divider(modifier = Modifier.padding(vertical = 4.dp), color = Color(0xFFF5F5F5))
                                Text(
                                    text = "লাভ: ৳ ${String.format("%.0f", profitBatch)}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = profitColor
                                )
                            }
                        }
                    }
                }

                // Render Batch P&L partners share breakdown dialog
                detailBatchToShow?.let { batch ->
                    val txsForBatch = approvedTxs.filter { it.batchId == batch.id }
                    val incBatch = txsForBatch.filter { it.type == "INCOME" }.sumOf { it.amount }
                    val expBatch = txsForBatch.filter { it.type == "EXPENSE" }.sumOf { it.amount }
                    val profitBatch = incBatch - expBatch

                    AlertDialog(
                        onDismissRequest = { detailBatchToShow = null },
                        title = {
                            Text(
                                text = "${batch.name} - এর লভ্যাংশ বন্টন",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF386B40)
                            )
                        },
                        text = {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "ব্যাচের সাকুল্য নিট প্রফিট: ৳ ${String.format("%,.1f", profitBatch)}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "সদস্যদের সক্রিয় শেয়ার পার্সেন্টেজ অনুযায়ী মুনাফা বণ্টন বিবরণী নিচে দেওয়া হলো:",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                                Divider(color = Color(0xFFEEEEEE), thickness = 1.dp)

                                partners.forEach { partner ->
                                    val partnerBatchShare = profitBatch * (partner.currentSharePercentage / 100.0)
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(10.dp)
                                                    .clip(CircleShape)
                                                    .background(partner.colorHex.toColor())
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = partner.name,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = Color.Black
                                            )
                                        }
                                        Text(
                                            text = "(${partner.currentSharePercentage}%) ৳ ${String.format("%,.1f", partnerBatchShare)}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (partnerBatchShare >= 0) Color(0xFF386B40) else Color(0xFFC62828)
                                        )
                                    }
                                }
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = { detailBatchToShow = null }) {
                                Text("বন্ধ করুন", color = Color(0xFF386B40), fontWeight = FontWeight.Bold)
                            }
                        }
                    )
                }
            }
        }

        // Pocket spender dues listing (farm payback)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth().shadow(1.dp, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "পকেট থেকে পরিশোধ হিসাব (ফার্ম সদস্যের কাছে দেনা)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    val computedPocketDues = partners.map { partner ->
                        val spendSum = approvedTxs.filter { it.paidByPartnerId == partner.id && it.isFromPocket && it.type == "EXPENSE" }.sumOf { it.amount }
                        partner to spendSum
                    }

                    if (computedPocketDues.all { it.second == 0.0 }) {
                        Text(
                            text = "কোনো সদস্য নিজের পকেট থেকে এখনও ফার্মের খরচ করেননি।",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    } else {
                        computedPocketDues.forEach { (partner, totalOwed) ->
                            if (totalOwed > 0) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(partner.colorHex.toColor()),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(partner.id.toString(), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(partner.name, fontSize = 13.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                                    Text(
                                        text = "পাবে: ৳ ${String.format("%,.0f", totalOwed)}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFC62828)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 2. LEDGER SCREEN (TRANSACTION LIST & APPROVALS)
// ==========================================
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LedgerScreen(
    viewModel: AgroViewModel,
    partners: List<Partner>,
    transactions: List<Transaction>,
    batches: List<Batch>,
    approvals: List<PartnerApproval>,
    activePartner: Partner?
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var filterType by remember { mutableStateOf("ALL") } // ALL, APPROVED, PENDING

    val filteredTxs = transactions.filter { tx ->
        val matchesSearch = tx.title.contains(searchQuery, ignoreCase = true) || tx.category.contains(searchQuery, ignoreCase = true)
        val matchesFilter = when (filterType) {
            "ALL" -> true
            "APPROVED" -> tx.approvalStatus == "APPROVED"
            "PENDING" -> tx.approvalStatus == "PENDING"
            else -> true
        }
        matchesSearch && matchesFilter
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Search and Filter Bar
        Card(
            shape = RoundedCornerShape(0.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F3E9)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("খরচ বা আয় খুঁজুন...") },
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = Color(0xFF386B40)
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Filters chips row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "ALL" to "সব লেনদেন",
                        "APPROVED" to "অনুমোদিত",
                        "PENDING" to "অনুমোদন অপেক্ষমাণ"
                    ).forEach { (code, text) ->
                        val isSelected = filterType == code
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) Color(0xFF386B40) else Color.White)
                                            .clickable { filterType = code }
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = text,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else Color.DarkGray
                            )
                        }
                    }

                    // Share button
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(
                        onClick = {
                            viewModel.exportToCsvAndShare(context)
                        },
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "রিপোর্ট ডাউনলোড করুন", tint = Color(0xFF386B40), modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        // Transactions List
        if (filteredTxs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("কোনো হিসাবের তথ্য মেলেনি!", color = Color.Gray, fontSize = 14.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp)
            ) {
                items(filteredTxs, key = { it.id }) { tx ->
                    val payingPartner = partners.find { it.id == tx.paidByPartnerId }
                    val txBatch = batches.find { it.id == tx.batchId }

                    val isApproved = tx.approvalStatus == "APPROVED"
                    val isPending = tx.approvalStatus == "PENDING"
                    val isRejected = tx.approvalStatus == "REJECTED"

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(1.dp, RoundedCornerShape(12.dp)),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            // First Row: title & amount
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        // Visual Tag matching paying member
                                        payingPartner?.let { pp ->
                                            Box(
                                                modifier = Modifier
                                                    .size(18.dp)
                                                    .clip(CircleShape)
                                                    .background(pp.colorHex.toColor()),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(pp.id.toString(), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                            }
                                            Spacer(modifier = Modifier.width(6.dp))
                                        }

                                        Text(
                                            text = tx.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = Color.Black,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    
                                    // Categories & timestamps
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(top = 4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(Color(0xFFD1E7DD))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(tx.category, fontSize = 9.sp, color = Color(0xFF386B40))
                                        }

                                        txBatch?.let { b ->
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(Color(0xFFE1F5FE))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(b.name, fontSize = 9.sp, color = Color(0xFF0277BD))
                                            }
                                        }

                                        if (tx.isFromPocket) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(Color(0xFFFFEBEE))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text("পকেট ডেবিট", fontSize = 9.sp, color = Color.Red)
                                            }
                                        }
                                    }
                                }

                                // Money text
                                val typePrefix = if (tx.type == "INCOME") "+" else "-"
                                val typeColor = if (tx.type == "INCOME") Color(0xFF386B40) else Color(0xFFC62828)
                                Text(
                                    text = "$typePrefix ৳ ${String.format("%,.0f", tx.amount)}",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 16.sp,
                                    color = typeColor
                                )
                            }

                            // Info line
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                val dString = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(tx.timestamp))
                                Text(
                                    text = "যুক্ত করেছেন: ${payingPartner?.name ?: "অজানা"} | $dString",
                                    fontSize = 10.sp,
                                    color = Color.Gray
                                )

                                // Digital receipt badge
                                if (tx.receiptUri != null) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.List, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(10.dp))
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text("রসিদ সংযুক্ত", fontSize = 9.sp, color = Color.Gray)
                                    }
                                }
                            }

                            // Dynamic Approval Workflow UI
                            if (isPending || isApproved || isRejected) {
                                Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFF5F5F5))
                                
                                val txVotes = approvals.filter { it.transactionId == tx.id }
                                val approveCount = txVotes.count { it.isApproved }
                                val rejectCount = txVotes.count { !it.isApproved }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    // Status Badge
                                    val statusColor = if (isApproved) Color(0xFF386B40) else if (isRejected) Color(0xFFC62828) else Color(0xFFEF6C00)
                                    val statusBg = if (isApproved) Color(0xFFD1E7DD) else if (isRejected) Color(0xFFFFEBEE) else Color(0xFFFFF3CD)
                                    val statusText = if (isApproved) "অনুমোদিত" else if (isRejected) "প্রত্যাখ্যাত" else "অনুমোদন অপেক্ষমাণ"

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(statusBg)
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(statusText, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = statusColor)
                                    }

                                    // Vote state summary
                                    Text(
                                        text = "সম্মতি: ${approveCount} হ্যাঁ | ${rejectCount} না (নির্ণায়ক: ৩ ভোট)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.DarkGray
                                    )
                                }

                                // Interactive Vote buttons for OTHER members
                                if (isPending && activePartner != null) {
                                    val alreadyVoted = txVotes.any { it.partnerId == activePartner.id }
                                    val isCreator = tx.paidByPartnerId == activePartner.id

                                    if (!alreadyVoted && !isCreator) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Button(
                                                onClick = {
                                                    viewModel.voteOnTransaction(tx, activePartner.id, isApproved = true)
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF81C784)),
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(vertical = 4.dp)
                                            ) {
                                                Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("হ্যাঁ (অনুমোদন)", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }

                                            Button(
                                                onClick = {
                                                    viewModel.voteOnTransaction(tx, activePartner.id, isApproved = false)
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)),
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(vertical = 4.dp)
                                            ) {
                                                Icon(imageVector = Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("না (প্রত্যাখ্যান)", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    } else if (isCreator) {
                                        Text(
                                            text = "নিজের খরচের এন্ট্রিতে নিজেই ভোট দেওয়া যায় না!",
                                            fontSize = 9.sp,
                                            color = Color.LightGray,
                                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                            textAlign = TextAlign.Center
                                        )
                                    } else {
                                        Text(
                                            text = "আপনি ইতিমধ্যে আপনার ডিজিটাল সম্মতি প্রদান করেছেন!",
                                            fontSize = 10.sp,
                                            color = Color.Gray,
                                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 3. AGRO FEATURES SCREEN (BATCH, INVENTORY, CREDITS)
// ==========================================
@Composable
fun AgroFeaturesScreen(
    viewModel: AgroViewModel,
    activePartner: Partner?,
    batches: List<Batch>,
    creditItems: List<CreditLedgerItem>,
    inventoryItems: List<InventoryItem>
) {
    var selectedFeatureTab by remember { mutableStateOf(0) } // 0: Batches, 1: Credits, 2: Stock

    Column(modifier = Modifier.fillMaxSize()) {
        // Horizontal Switch Tab Bar
        TabRow(
            selectedTabIndex = selectedFeatureTab,
            containerColor = Color(0xFFE8F3E9),
            contentColor = Color(0xFF386B40)
        ) {
            Tab(selected = selectedFeatureTab == 0, onClick = { selectedFeatureTab = 0 }) {
                Text("🐥 ব্যাচসমূহ", modifier = Modifier.padding(14.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Tab(selected = selectedFeatureTab == 1, onClick = { selectedFeatureTab = 1 }) {
                Text("🧾 বকেয়া বাকির লেজার", modifier = Modifier.padding(14.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Tab(selected = selectedFeatureTab == 2, onClick = { selectedFeatureTab = 2 }) {
                Text("📦 খাদ্য ও স্টক", modifier = Modifier.padding(14.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            when (selectedFeatureTab) {
                0 -> BatchesSubScreen(viewModel, batches, activePartner)
                1 -> CreditsSubScreen(viewModel, creditItems, activePartner)
                2 -> InventorySubScreen(viewModel, inventoryItems, activePartner)
            }
        }
    }
}

// Sub-components for Agro Features
@Composable
fun BatchesSubScreen(viewModel: AgroViewModel, batches: List<Batch>, activePartner: Partner?) {
    var newBatchName by remember { mutableStateOf("") }
    var newBatchDesc by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Quick add Batch Card
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth().shadow(1.dp, RoundedCornerShape(12.dp))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("নতুন খামার ব্যাচ শুরু করুন", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF386B40))
                
                OutlinedTextField(
                    value = newBatchName,
                    onValueChange = { newBatchName = it },
                    label = { Text("ব্যাচের নাম (যেমন: হাঁস লট-০৩)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF386B40))
                )

                OutlinedTextField(
                    value = newBatchDesc,
                    onValueChange = { newBatchDesc = it },
                    label = { Text("বর্ণনা (যেমন: ৫০টি হাঁস নিয়ে নতুন লট)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF386B40))
                )

                Button(
                    onClick = {
                        if (newBatchName.trim().isEmpty()) {
                            Toast.makeText(context, "দয়া করে ব্যাচের নাম দিন!", Toast.LENGTH_SHORT).show()
                        } else {
                            activePartner?.let {
                                viewModel.createNewBatch(newBatchName, newBatchDesc, it.id)
                                newBatchName = ""
                                newBatchDesc = ""
                                Toast.makeText(context, "নতুন ব্যাচ চালু হয়েছে!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier.align(Alignment.End),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF386B40))
                ) {
                    Text("ব্যাচ চালু করুন", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Active / Archived Batches Lists
        Text("চলমান খামার ব্যাচসমূহ", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
            items(batches) { batch ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(batch.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            batch.description?.let {
                                Text(it, fontSize = 11.sp, color = Color.Gray)
                            }
                        }

                        // Archive button
                        if (batch.isActive) {
                            Button(
                                onClick = {
                                    activePartner?.let {
                                        viewModel.archiveBatch(batch, it.id)
                                        Toast.makeText(context, "ব্যাচটি সমাপ্ত আর্কাইভে যুক্ত করা হয়েছে।", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0E0E0), contentColor = Color.Black),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("সমাপ্ত করুন", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Text("আর্কাইভড", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CreditsSubScreen(viewModel: AgroViewModel, creditItems: List<CreditLedgerItem>, activePartner: Partner?) {
    var partyName by remember { mutableStateOf("") }
    var partyType by remember { mutableStateOf("SUPPLIER") } // SUPPLIER / CUSTOMER
    var bakiAmount by remember { mutableStateOf("") }
    var phoneInput by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth().shadow(1.dp, RoundedCornerShape(12.dp))
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("বাকির খাতা এন্ট্রি (সাপ্লায়ার ও কাস্টমার লেজার)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF386B40))

                OutlinedTextField(
                    value = partyName,
                    onValueChange = { partyName = it },
                    label = { Text("ব্যক্তি বা প্রতিষ্ঠানের নাম") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF386B40))
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = bakiAmount,
                        onValueChange = { if (it.all { char -> char.isDigit() || char == '-' }) bakiAmount = it },
                        label = { Text("বকেয়া টাকা") },
                        placeholder = { Text("যেমন: ৫০০০") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF386B40))
                    )

                    OutlinedTextField(
                        value = phoneInput,
                        onValueChange = { phoneInput = it },
                        label = { Text("ফোন নম্বর (ঐচ্ছিক)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.weight(1.2f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF386B40))
                    )
                }

                // Type selector
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { partyType = "SUPPLIER" }) {
                        RadioButton(selected = partyType == "SUPPLIER", onClick = { partyType = "SUPPLIER" }, colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF386B40)))
                        Text("আমরা দেবো (সাপ্লায়ার দেনা)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { partyType = "CUSTOMER" }) {
                        RadioButton(selected = partyType == "CUSTOMER", onClick = { partyType = "CUSTOMER" }, colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF386B40)))
                        Text("আমরা পাবো (কাস্টমার পাওনা)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Button(
                    onClick = {
                        if (partyName.trim().isEmpty() || bakiAmount.trim().isEmpty()) {
                            Toast.makeText(context, "দয়া করে নাম ও বকেয়া পূর্ণ করুন!", Toast.LENGTH_SHORT).show()
                        } else {
                            val amt = bakiAmount.toDoubleOrNull() ?: 0.0
                            activePartner?.let {
                                viewModel.addCreditItem(partyName, partyType, phoneInput, amt, it.id)
                                partyName = ""
                                bakiAmount = ""
                                phoneInput = ""
                                Toast.makeText(context, "বকেয়া লেজার এন্ট্রি সফল হয়েছে!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier.align(Alignment.End),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF386B40))
                ) {
                    Text("সংরক্ষণ করুন", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Dues list
        Text("বাকির তালিকা সমূহ", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
            items(creditItems) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val labelText = if (item.partyType == "SUPPLIER") "সাপ্লায়ার দেনা" else "কাস্টমার পাওনা"
                                val labelColor = if (item.partyType == "SUPPLIER") Color(0xFFC62828) else Color(0xFF386B40)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(labelColor.copy(alpha = 0.1f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(labelText, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = labelColor)
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(item.partyName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            item.phone?.let {
                                if (it.isNotEmpty()) Text("ফোন: $it", fontSize = 11.sp, color = Color.Gray)
                            }
                            val dateString = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(Date(item.lastUpdated))
                            Text("হালনাগাদ: $dateString", fontSize = 9.sp, color = Color.LightGray)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "৳ ${String.format("%,.0f", item.totalDue)}",
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp,
                                color = if (item.partyType == "SUPPLIER") Color(0xFFC62828) else Color(0xFF386B40)
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            // Action clickers: adjust settled amount
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Button(
                                    onClick = {
                                        activePartner?.let {
                                            viewModel.adjustCreditItemAmount(item, item.totalDue + 500, it.id)
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE8F5E9), contentColor = Color.Black),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text("+৳৫০০", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = {
                                        activePartner?.let {
                                            val newVal = if (item.totalDue - 500 >= 0) item.totalDue - 500 else 0.0
                                            viewModel.adjustCreditItemAmount(item, newVal, it.id)
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFEBEE), contentColor = Color.Black),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text("-৳৫০০", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }

                                IconButton(
                                    onClick = {
                                        activePartner?.let {
                                            viewModel.deleteCreditItem(item, it.id)
                                        }
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
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
fun InventorySubScreen(viewModel: AgroViewModel, inventoryItems: List<InventoryItem>, activePartner: Partner?) {
    var invName by remember { mutableStateOf("") }
    var invCat by remember { mutableStateOf("ফিড") } // ফিড, ওষুধ, অন্যান্য
    var invQty by remember { mutableStateOf("") }
    var invUnit by remember { mutableStateOf("কেজি") } // কেজি, বস্তা, পিস
    val context = LocalContext.current

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth().shadow(1.dp, RoundedCornerShape(12.dp))
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("ইনভেন্টরি ফিড ও মেডিসিন স্টক এন্ট্রি", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF386B40))

                OutlinedTextField(
                    value = invName,
                    onValueChange = { invName = it },
                    label = { Text("স্টক আইটেমের নাম (যেমন: পোল্ট্রি ফিড)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF386B40))
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = invQty,
                        onValueChange = { if (it.all { char -> char.isDigit() || char == '.' }) invQty = it },
                        label = { Text("মজুদ পরিমাণ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF386B40))
                    )

                    OutlinedTextField(
                        value = invUnit,
                        onValueChange = { invUnit = it },
                        label = { Text("একক (যেমন: বস্তা, কেজি)") },
                        modifier = Modifier.weight(1.2f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF386B40))
                    )
                }

                // Category selector row
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf("ফিড", "ওষুধ", "অন্যান্য").forEach { cat ->
                        val isSel = invCat == cat
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSel) Color(0xFF386B40) else Color(0xFFEEEEEE))
                                .clickable { invCat = cat }
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(cat, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isSel) Color.White else Color.Black)
                        }
                    }
                }

                Button(
                    onClick = {
                        if (invName.trim().isEmpty() || invQty.trim().isEmpty()) {
                            Toast.makeText(context, "দয়া করে নাম ও পরিমাণ দিন!", Toast.LENGTH_SHORT).show()
                        } else {
                            val qtyDouble = invQty.toDoubleOrNull() ?: 0.0
                            activePartner?.let {
                                viewModel.addInventoryItem(invName, invCat, qtyDouble, invUnit, minThres = 5.0, actionBy = it.id)
                                invName = ""
                                invQty = ""
                                Toast.makeText(context, "আইটেম সফলভাবে স্টকে যুক্ত হয়েছে!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier.align(Alignment.End),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF386B40))
                ) {
                    Text("স্টকে যোগ করুন", fontWeight = FontWeight.Bold)
                }
            }
        }

        // List
        Text("বর্তমান মজুদ স্টক বিবরণী", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
            items(inventoryItems) { item ->
                val isThresholdWarning = item.currentQuantity <= item.minThresholdAlert

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isThresholdWarning) Color(0xFFFFF9C4) else Color.White
                    ),
                    border = if (isThresholdWarning) BorderStroke(1.dp, Color(0xFFFBC02D)) else null
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(item.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                if (isThresholdWarning) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color(0xFFE53935))
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Text("স্বল্প স্টক!", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            Text("ক্যাটাগরি: ${item.category}", fontSize = 11.sp, color = Color.Gray)
                        }

                        // Quantity adjustment buttons
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Decrement
                            IconButton(
                                onClick = {
                                    activePartner?.let {
                                        val newQty = if (item.currentQuantity - 1 >= 0) item.currentQuantity - 1 else 0.0
                                        viewModel.adjustInventoryStock(item, newQty, it.id)
                                    }
                                },
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFEEEEEE))
                            ) {
                                Text("-", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.DarkGray)
                            }

                            // Current Display
                            Text(
                                "${item.currentQuantity} ${item.unit}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                modifier = Modifier.widthIn(min = 60.dp),
                                textAlign = TextAlign.Center
                            )

                            // Increment
                            IconButton(
                                onClick = {
                                    activePartner?.let {
                                        viewModel.adjustInventoryStock(item, item.currentQuantity + 1, it.id)
                                    }
                                },
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFEEEEEE))
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            }

                            // Delete
                            IconButton(
                                onClick = {
                                    activePartner?.let {
                                        viewModel.deleteInventoryItem(item, it.id)
                                    }
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 4. CHAT SCREEN (PARTNERS GROUP COMM)
// ==========================================
@Composable
fun ChatScreen(
    viewModel: AgroViewModel,
    activePartner: Partner?,
    chatMessages: List<ChatMessage>,
    partners: List<Partner>,
    transactions: List<Transaction>
) {
    var chatText by remember { mutableStateOf("") }
    val keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
    val listState = rememberLazyListState()

    // Keep chat scrolling to bottom when new messages arrive
    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Chat Area
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 12.dp)
        ) {
            items(chatMessages) { msg ->
                val sender = partners.find { it.id == msg.partnerId }
                val isActiveUser = activePartner?.id == msg.partnerId

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = if (isActiveUser) Alignment.End else Alignment.Start
                ) {
                    // Sender Name tag
                    if (!isActiveUser) {
                        Text(
                            text = sender?.name ?: "অজানা সদস্য",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray,
                            modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
                        )
                    }

                    // Message Bubble
                    Box(
                        modifier = Modifier
                            .clip(
                                RoundedCornerShape(
                                    topStart = 12.dp,
                                    topEnd = 12.dp,
                                    bottomStart = if (isActiveUser) 12.dp else 0.dp,
                                    bottomEnd = if (isActiveUser) 0.dp else 12.dp
                                )
                            )
                            .background(
                                if (isActiveUser) Color(0xFFC8E6C9)
                                else sender?.colorHex?.toColor() ?: Color(0xFFE0E0E0)
                            )
                            .padding(10.dp)
                            .widthIn(max = 260.dp)
                    ) {
                        Column {
                            Text(
                                text = msg.messageText,
                                fontSize = 13.sp,
                                color = if (isActiveUser) Color.Black else (sender?.colorHex?.toColor()?.getContrastColor() ?: Color.Black)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(msg.timestamp))
                            Text(
                                text = timeStr,
                                fontSize = 8.sp,
                                color = Color.Gray,
                                modifier = Modifier.align(Alignment.End)
                            )
                        }
                    }
                }
            }
        }

        // Typing bar
        Card(
            shape = RoundedCornerShape(0.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = chatText,
                    onValueChange = { chatText = it },
                    placeholder = { Text("গ্রুপে আপনার বার্তা লিখুন...") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF386B40)
                    ),
                    shape = RoundedCornerShape(24.dp)
                )

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(
                    onClick = {
                        if (chatText.trim().isNotEmpty() && activePartner != null) {
                            viewModel.sendChatMessage(chatText, activePartner.id)
                            chatText = ""
                        }
                    },
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF386B40))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "পাঠান",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// ==========================================
// 5. SETTINGS SCREEN (PERCENTAGES, WAGES, AUDIT)
// ==========================================
@Composable
fun SettingsScreen(
    viewModel: AgroViewModel,
    activePartner: Partner?,
    partners: List<Partner>,
    auditLogs: List<AuditLog>
) {
    val context = LocalContext.current
    var inputShares = remember { mutableStateMapOf<Int, String>() }

    // Synchronize inputs with current percentages
    LaunchedEffect(partners) {
        partners.forEach { partner ->
            if (!inputShares.containsKey(partner.id)) {
                inputShares[partner.id] = partner.currentSharePercentage.toString()
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // Header title
        item {
            Text(
                text = "পার্টনারশিপের শেয়ার অনুপাত ও পারিশ্রমিক সেটিংস",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF386B40)
            )
        }

        // Percentage slider adjustment
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth().shadow(1.dp, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "ডাইনামিক শেয়ার বন্টন (অবশ্যই যোগফল ১০০% মিলিয়ে সেভ রেশিও করুন):",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color.Black
                    )

                    partners.forEach { partner ->
                        val currentVal = inputShares[partner.id] ?: partner.currentSharePercentage.toString()
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(partner.colorHex.toColor()),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(partner.id.toString(), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = partner.name,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )

                            // Weight adjustment textfield
                            OutlinedTextField(
                                value = currentVal,
                                onValueChange = {
                                    if (it.all { char -> char.isDigit() || char == '.' }) {
                                        inputShares[partner.id] = it
                                    }
                                },
                                suffix = { Text("%") },
                                singleLine = true,
                                modifier = Modifier.width(100.dp),
                                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.End, fontSize = 12.sp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF386B40))
                            )
                        }
                    }

                    // Log current sum
                    val computedSum = inputShares.values.mapNotNull { it.toDoubleOrNull() }.sum()
                    val isValidSum = Math.abs(computedSum - 100.0) < 0.1

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "বর্তমান যোগফল: ${String.format("%.2f", computedSum)}%",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (isValidSum) Color(0xFF386B40) else Color(0xFFC62828)
                        )

                        Button(
                            onClick = {
                                val doubleMap = partners.associate { it.id to (inputShares[it.id]?.toDoubleOrNull() ?: it.currentSharePercentage) }
                                activePartner?.let {
                                    viewModel.updatePartnerPercentages(
                                        doubleMap,
                                        it.id,
                                        onSuccess = {
                                            Toast.makeText(context, "শেয়ার অনুপাত সাকসেসফুল লক করা হয়েছে!", Toast.LENGTH_SHORT).show()
                                        },
                                        onError = { errMsg ->
                                            Toast.makeText(context, errMsg, Toast.LENGTH_LONG).show()
                                        }
                                    )
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF386B40)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("অনুপাত লক করুন", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Active wage salary setup
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth().shadow(1.dp, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "সরাসরি শ্রম দেওয়া একটিভ পার্টনার পারিশ্রমিক (বেতন):",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color.Black
                    )

                    partners.forEach { partner ->
                        var salaryValue by remember(partner.monthlySalary) { mutableStateOf(partner.monthlySalary.toString()) }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(partner.name, fontSize = 12.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                            
                            OutlinedTextField(
                                value = salaryValue,
                                onValueChange = {
                                    if (it.all { char -> char.isDigit() || char == '.' }) {
                                        salaryValue = it
                                    }
                                },
                                prefix = { Text("৳") },
                                singleLine = true,
                                modifier = Modifier.width(120.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF386B40)),
                                textStyle = LocalTextStyle.current.copy(fontSize = 11.sp, textAlign = TextAlign.End)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Button(
                                onClick = {
                                    val salDouble = salaryValue.toDoubleOrNull() ?: 0.0
                                    activePartner?.let {
                                        viewModel.updatePartnerSalarySetting(partner.id, salDouble, it.id)
                                        Toast.makeText(context, "${partner.name}-এর পারিশ্রমিক হালনাগাদ করা হয়েছে।", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                contentPadding = PaddingValues(horizontal = 6.dp),
                                shape = RoundedCornerShape(6.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD1E7DD), contentColor = Color(0xFF386B40)),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text("সেট", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Live Audit Trail Log scroll list
        item {
            Text(
                text = "অ্যাক্টিভিটি লগ (Audit Trail) - বাংলা রেকর্ডস",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth().height(300.dp).shadow(1.dp, RoundedCornerShape(16.dp))
            ) {
                if (auditLogs.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("এখনও কোনো অডিট লগ রেকর্ড স্পট করা যায়নি!", fontSize = 12.sp, color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(auditLogs) { log ->
                            val sName = partners.find { it.id == log.partnerId }?.name ?: "সিস্টেম"
                            val timeStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(log.timestamp))

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFFAFAFA))
                                    .padding(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "মেম্বার: $sName",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = Color(0xFF386B40)
                                    )
                                    Text(
                                        text = timeStr,
                                        fontSize = 8.sp,
                                        color = Color.Gray
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = log.actionDescription,
                                    fontSize = 11.sp,
                                    color = Color.Black,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
