package com.example.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AgroViewModel(private val repository: AgroRepository) : ViewModel() {

    // --- Active User State ---
    private val _activePartner = MutableStateFlow<Partner?>(null)
    val activePartner: StateFlow<Partner?> = _activePartner.asStateFlow()

    // --- State Observables gathered from DB ---
    val partners: StateFlow<List<Partner>> = repository.allPartners
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactions: StateFlow<List<Transaction>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val batches: StateFlow<List<Batch>> = repository.allBatches
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val approvals: StateFlow<List<PartnerApproval>> = repository.allApprovals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chatMessages: StateFlow<List<ChatMessage>> = repository.allChatMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val inventoryItems: StateFlow<List<InventoryItem>> = repository.allInventoryItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val creditItems: StateFlow<List<CreditLedgerItem>> = repository.allCreditLedgerItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val auditLogs: StateFlow<List<AuditLog>> = repository.allAuditLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Initial load handler to set default active partner
    init {
        viewModelScope.launch {
            partners.collectLatest { list ->
                if (_activePartner.value == null && list.isNotEmpty()) {
                    _activePartner.value = list.first()
                }
            }
        }
    }

    // --- Pin Login/Switch Gate ---
    fun switchPartnerWithPin(partnerId: Int, pin: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val validated = repository.validatePin(partnerId, pin)
            if (validated != null) {
                _activePartner.value = validated
                onSuccess()
            } else {
                onError("ভুল পিন কোড! দয়া করে আবার চেষ্টা করুন।")
            }
        }
    }

    fun quickSwitchPartner(partner: Partner) {
        _activePartner.value = partner
    }

    // --- Operations ---

    // Transactions API
    fun addNewTransaction(
        title: String,
        amount: Double,
        type: String, // "INCOME", "EXPENSE", "DRAWING", "SALARY"
        category: String,
        batchId: Int?,
        isFromPocket: Boolean,
        addedByPartnerId: Int,
        receiptUri: String? = null,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            // Determine initial approval status. If Expense and Amount > 5000, set to "PENDING".
            val isExpense = type == "EXPENSE"
            val status = if (isExpense && amount > 5000.0) "PENDING" else "APPROVED"

            val tx = Transaction(
                title = title,
                amount = amount,
                type = type,
                category = category,
                batchId = batchId,
                paidByPartnerId = addedByPartnerId,
                isFromPocket = isFromPocket,
                approvalStatus = status,
                receiptUri = receiptUri,
                addedByPartnerId = addedByPartnerId
            )
            repository.addTransaction(tx, addedByPartnerId)
            onSuccess()
        }
    }

    // Approvals voting system
    fun voteOnTransaction(tx: Transaction, partnerId: Int, isApproved: Boolean) {
        viewModelScope.launch {
            // Check if already voted
            val votesForTx = approvals.value.filter { it.transactionId == tx.id }
            val existingVote = votesForTx.find { it.partnerId == partnerId }
            if (existingVote != null) {
                // Already voted
                return@launch
            }

            // Record vote
            val approval = PartnerApproval(
                transactionId = tx.id,
                partnerId = partnerId,
                isApproved = isApproved
            )
            repository.castVote(approval, partnerId, isApproved, tx.title)

            // Re-evaluate transaction state
            val allVotesNow = votesForTx + approval
            val approveCount = allVotesNow.count { it.isApproved }
            val rejectCount = allVotesNow.count { !it.isApproved }

            // Threshold: Out of 6 partners, 3 approval votes secure approval!
            if (approveCount >= 3) {
                repository.updateTransactionStatus(tx, partnerId, "APPROVED")
            } else if (rejectCount >= 3) {
                repository.updateTransactionStatus(tx, partnerId, "REJECTED")
            }
        }
    }

    // Capital & Percentage adjustment
    fun updatePartnerPercentages(newPercentages: Map<Int, Double>, actionByPartnerId: Int, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val total = newPercentages.values.sum()
            // Margin of error check for Floats/Doubles
            if (Math.abs(total - 100.0) > 0.1) {
                onError("শেয়ারের যোগফল অবশ্যই ১০০% হতে হবে! বর্তমান যোগফল: ${String.format("%.2f", total)}%")
                return@launch
            }

            val currentList = partners.value
            val updatedPartners = currentList.map { partner ->
                val newPercentage = newPercentages[partner.id] ?: partner.currentSharePercentage
                partner.copy(currentSharePercentage = newPercentage)
            }

            // Save in database
            repository.updateSharesAndLog(updatedPartners, actionByPartnerId)

            // Save historical percentage locking
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH) + 1 // 1-indexed

            updatedPartners.forEach { partner ->
                val historyEntry = PercentageHistory(
                    year = year,
                    month = month,
                    partnerId = partner.id,
                    percentage = partner.currentSharePercentage
                )
                repository.savePercentageHistory(historyEntry, actionByPartnerId)
            }

            onSuccess()
        }
    }

    // Active Salary Change
    fun updatePartnerSalarySetting(partnerId: Int, salary: Double, actionByPartnerId: Int) {
        viewModelScope.launch {
            val currentList = partners.value
            val targetPartner = currentList.find { it.id == partnerId }
            if (targetPartner != null) {
                val updated = targetPartner.copy(monthlySalary = salary)
                repository.updatePartnerInfo(updated, actionByPartnerId)
            }
        }
    }

    // Batches
    fun createNewBatch(name: String, description: String?, actionByPartnerId: Int) {
        viewModelScope.launch {
            val batch = Batch(name = name, description = description, isActive = true)
            repository.addBatch(batch, actionByPartnerId)
        }
    }

    fun archiveBatch(batch: Batch, actionByPartnerId: Int) {
        viewModelScope.launch {
            val updated = batch.copy(isActive = false)
            repository.updateBatch(updated, actionByPartnerId)
        }
    }

    // Chattings
    fun sendChatMessage(text: String, partnerId: Int, txId: Int? = null) {
        viewModelScope.launch {
            if (text.trim().isNotEmpty()) {
                val msg = ChatMessage(partnerId = partnerId, messageText = text, associatedTransactionId = txId)
                repository.postChatMessage(msg)
            }
        }
    }

    // Inventory Stock control
    fun addInventoryItem(name: String, category: String, qty: Double, unit: String, minThres: Double, actionBy: Int) {
        viewModelScope.launch {
            val item = InventoryItem(name = name, category = category, currentQuantity = qty, unit = unit, minThresholdAlert = minThres)
            repository.addInventoryItem(item, actionBy)
        }
    }

    fun adjustInventoryStock(item: InventoryItem, newQty: Double, actionBy: Int) {
        viewModelScope.launch {
            val prev = item.currentQuantity
            val updated = item.copy(currentQuantity = newQty)
            repository.updateInventoryStock(updated, actionBy, prev)
        }
    }

    fun deleteInventoryItem(item: InventoryItem, actionBy: Int) {
        viewModelScope.launch {
            repository.deleteInventory(item, actionBy)
        }
    }

    // Credit Baki ledger
    fun addCreditItem(name: String, type: String, phone: String?, amount: Double, actionBy: Int) {
        viewModelScope.launch {
            val item = CreditLedgerItem(partyName = name, partyType = type, phone = phone, totalDue = amount)
            repository.addCreditItem(item, actionBy)
        }
    }

    fun adjustCreditItemAmount(item: CreditLedgerItem, newAmt: Double, actionBy: Int) {
        viewModelScope.launch {
            val prev = item.totalDue
            val updated = item.copy(totalDue = newAmt, lastUpdated = System.currentTimeMillis())
            repository.updateCreditItem(updated, actionBy, prev)
        }
    }

    fun deleteCreditItem(item: CreditLedgerItem, actionBy: Int) {
        viewModelScope.launch {
            repository.deleteCredit(item, actionBy)
        }
    }

    // --- PDF / CSV Report Exporter ---
    fun exportToCsvAndShare(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val transactionsList = transactions.value
            val partnersList = partners.value
            val creditsList = creditItems.value
            val inventoryList = inventoryItems.value
            val auditList = auditLogs.value

            val builder = StringBuilder()
            // Excel UTF-8 BOM
            builder.append("\uFEFF")
            builder.append("এগ্রো পার্টনারশিপ লেজার রিপোর্ট\n")
            builder.append("রিপোর্ট তৈরির সময়: " + SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date()) + "\n\n")

            // Summary
            val approvedTxs = transactionsList.filter { it.approvalStatus == "APPROVED" }
            val totalIncome = approvedTxs.filter { it.type == "INCOME" }.sumOf { it.amount }
            val totalExpense = approvedTxs.filter { it.type == "EXPENSE" }.sumOf { it.amount }
            val netProfit = totalIncome - totalExpense
            builder.append("সংক্ষিপ্ত বিবরণ,,\n")
            builder.append("মোট আয়,৳ $totalIncome,\n")
            builder.append("মোট খরচ,৳ $totalExpense,\n")
            builder.append("নিট মুনাফা,৳ $netProfit,\n\n")

            // Partners
            builder.append("অংশীদারগণের তালিকা,শেয়ার পার্সেন্টেজ,মাসিক পারিশ্রমিক\n")
            partnersList.forEach {
                builder.append("${it.name},${it.currentSharePercentage}%,৳ ${it.monthlySalary}\n")
            }
            builder.append("\n")

            // Transactions details
            builder.append("লেনদেন তালিকা,পরিমাণ,ধরন,ক্যাটাগরি,দাতা সদস্য,অবস্থা,তারিখ\n")
            transactionsList.forEach { tx ->
                val partnerName = partnersList.find { it.id == tx.paidByPartnerId }?.name ?: "অজানা"
                val typeBangla = when(tx.type) {
                    "INCOME" -> "আয়"
                    "EXPENSE" -> "খরচ"
                    "DRAWING" -> "ব্যক্তিগত উত্তোলন"
                    "SALARY" -> "পারিশ্রমিক বেতন"
                    else -> tx.type
                }
                val statusBangla = when(tx.approvalStatus) {
                    "APPROVED" -> "অনুমোদিত"
                    "PENDING" -> "অপেক্ষমাণ"
                    "REJECTED" -> "প্রত্যাখ্যাত"
                    else -> tx.approvalStatus
                }
                val dateStr = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(Date(tx.timestamp))
                builder.append("${tx.title.replace(",", " ")},৳ ${tx.amount},$typeBangla,${tx.category},$partnerName,$statusBangla,$dateStr\n")
            }
            builder.append("\n")

            // Credits
            builder.append("বাকির হিসাব/লেজার,ধরনের বিবরণ,বকেয়া পরিমাণ,সর্বশেষ আপডেট\n")
            creditsList.forEach { c ->
                val partyTypeBangla = if (c.partyType == "SUPPLIER") "সাপ্লায়ার" else "কাস্টমার"
                val dateStr = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(Date(c.lastUpdated))
                builder.append("${c.partyName.replace(",", " ")},$partyTypeBangla,৳ ${c.totalDue},$dateStr\n")
            }
            builder.append("\n")

            // Inventory
            builder.append("ইনভেন্টরি স্টক,ক্যাটাগরি,মজুদ পরিমাণ\n")
            inventoryList.forEach { i ->
                builder.append("${i.name.replace(",", " ")},${i.category},${i.currentQuantity} ${i.unit}\n")
            }
            builder.append("\n")

            // Audit Trail
            builder.append("অ্যাক্টিভিটি লগ (Audit Trail),তারিখ,সদস্য\n")
            auditList.forEach { log ->
                val partnerName = partnersList.find { it.id == log.partnerId }?.name ?: "সিস্টেম"
                val dateStr = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(Date(log.timestamp))
                builder.append("${log.actionDescription.replace(",", " ")},$dateStr,$partnerName\n")
            }

            try {
                val file = java.io.File(context.cacheDir, "Agro_Farm_Report_Bangla.csv")
                file.writeText(builder.toString(), Charsets.UTF_8)

                val uri = androidx.core.content.FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )

                val shareIntent = android.content.Intent().apply {
                    action = android.content.Intent.ACTION_SEND
                    putExtra(android.content.Intent.EXTRA_STREAM, uri)
                    type = "text/csv"
                    addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                
                val chooser = android.content.Intent.createChooser(shareIntent, "বাংলা রিপোর্টটি শেয়ার করুন")
                chooser.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(chooser)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

// Custom ViewModel Factory class to pass Repository dependency
class AgroViewModelFactory(private val repository: AgroRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AgroViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AgroViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
