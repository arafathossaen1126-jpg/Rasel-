package com.example.data

import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AgroRepository(private val agroDao: AgroDao) {

    // --- State Observables ---
    val allPartners: Flow<List<Partner>> = agroDao.getAllPartners()
    val allTransactions: Flow<List<Transaction>> = agroDao.getAllTransactions()
    val allBatches: Flow<List<Batch>> = agroDao.getAllBatches()
    val allApprovals: Flow<List<PartnerApproval>> = agroDao.getAllApprovals()
    val allChatMessages: Flow<List<ChatMessage>> = agroDao.getAllChatMessages()
    val allInventoryItems: Flow<List<InventoryItem>> = agroDao.getAllInventoryItems()
    val allCreditLedgerItems: Flow<List<CreditLedgerItem>> = agroDao.getAllCreditLedgerItems()
    val allAuditLogs: Flow<List<AuditLog>> = agroDao.getAllAuditLogs()

    // --- Partners ---
    suspend fun updatePartnerInfo(partner: Partner, actionByPartnerId: Int) {
        agroDao.updatePartner(partner)
        logAudit(
            partnerId = actionByPartnerId,
            action = "সদস্য ${partner.name}-এর প্রোফাইল হালনাগাদ করা হয়েছে।"
        )
    }

    suspend fun updateSharesAndLog(partners: List<Partner>, actionByPartnerId: Int) {
        agroDao.insertPartners(partners)
        logAudit(
            partnerId = actionByPartnerId,
            action = "পার্টনারশিপের শেয়ার পার্সেন্টেজ পরিবর্তন করা হয়েছে।"
        )
    }

    suspend fun savePercentageHistory(history: PercentageHistory, actionByPartnerId: Int) {
        agroDao.insertPercentageHistory(history)
        logAudit(
            partnerId = actionByPartnerId,
            action = "${history.year} সালের ${getMonthBangla(history.month)} মাসের শেয়ার অনুপাত লক করা হয়েছে।"
        )
    }

    fun getLockedPercentageHistory(year: Int, month: Int): Flow<List<PercentageHistory>> {
        return agroDao.getPercentageHistory(year, month)
    }

    suspend fun validatePin(partnerId: Int, pin: String): Partner? {
        val partner = agroDao.getPartnerById(partnerId)
        return if (partner != null && partner.pin == pin) {
            logAudit(partnerId, "সফলভাবে লগইন করেছেন।")
            partner
        } else {
            null
        }
    }

    // --- Transactions ---
    suspend fun addTransaction(tx: Transaction, actionByPartnerId: Int) {
        val id = agroDao.insertTransaction(tx)
        val typeBangla = when (tx.type) {
            "INCOME" -> "আয়"
            "EXPENSE" -> "খরচ"
            "DRAWING" -> "ব্যক্তিগত উত্তোলন"
            "SALARY" -> "পারিশ্রমিক বেতন"
            else -> tx.type
        }
        val approvalText = if (tx.approvalStatus == "PENDING") " (বড় এন্ট্রি হওয়ার কারণে অনুমোদনের জন্য অপেক্ষমাণ)" else ""
        logAudit(
            partnerId = actionByPartnerId,
            action = "নতুন লেনদেন যুক্ত করেছেন: ${tx.title}, পরিমাণ: ৳${tx.amount} " +
                    "[$typeBangla - ক্যাটাগরি: ${tx.category}]$approvalText"
        )
    }

    suspend fun updateTransactionStatus(tx: Transaction, actionByPartnerId: Int, status: String) {
        val updatedTx = tx.copy(approvalStatus = status)
        agroDao.updateTransaction(updatedTx)
        val statusBangla = when (status) {
            "APPROVED" -> "অনুমোদিত"
            "REJECTED" -> "প্রত্যাখ্যাত"
            else -> status
        }
        logAudit(
            partnerId = actionByPartnerId,
            action = "লেনদেন '${tx.title}' (৳${tx.amount})-এর অবস্থা '${statusBangla}' করা হয়েছে।"
        )
    }

    // --- Batches ---
    suspend fun addBatch(batch: Batch, actionByPartnerId: Int) {
        agroDao.insertBatch(batch)
        logAudit(
            partnerId = actionByPartnerId,
            action = "নতুন খামার ব্যাচ তৈরি করা হয়েছে: ${batch.name}"
        )
    }

    suspend fun updateBatch(batch: Batch, actionByPartnerId: Int) {
        agroDao.updateBatch(batch)
        val statusText = if (batch.isActive) "চলমান" else "সম্পন্ন"
        logAudit(
            partnerId = actionByPartnerId,
            action = "খামার ব্যাচ '${batch.name}' এর তথ্য পরিবর্তন করা হয়েছে [অবস্থা: $statusText]"
        )
    }

    // --- Approvals ---
    suspend fun castVote(approval: PartnerApproval, actionByPartnerId: Int, isApproved: Boolean, txTitle: String) {
        agroDao.insertApproval(approval)
        val voteText = if (isApproved) "অনুমোদন" else "প্রত্যাখ্যান"
        logAudit(
            partnerId = actionByPartnerId,
            action = "লেনদেন '${txTitle}'-এর পক্ষে ডিজিটাল সম্মতি প্রদান করেছেন [মত: $voteText]"
        )
    }

    // --- Chat Messages ---
    suspend fun postChatMessage(msg: ChatMessage) {
        agroDao.insertChatMessage(msg)
    }

    // --- Inventory ---
    suspend fun addInventoryItem(item: InventoryItem, actionByPartnerId: Int) {
        agroDao.insertInventoryItem(item)
        logAudit(
            partnerId = actionByPartnerId,
            action = "নতুন ইনভেন্টরি স্টক বা পণ্য যুক্ত করেছেন: ${item.name} (${item.currentQuantity} ${item.unit})"
        )
    }

    suspend fun updateInventoryStock(item: InventoryItem, actionByPartnerId: Int, previousQty: Double) {
        agroDao.insertInventoryItem(item)
        logAudit(
            partnerId = actionByPartnerId,
            action = "পণ্য '${item.name}'-এর স্টক আপডেট করা হয়েছে: $previousQty -> ${item.currentQuantity} ${item.unit}"
        )
    }

    suspend fun deleteInventory(item: InventoryItem, actionByPartnerId: Int) {
        agroDao.deleteInventoryItem(item)
        logAudit(
            partnerId = actionByPartnerId,
            action = "ইনভেন্টরি স্টক থেকে '${item.name}' মুছে ফেলা হয়েছে।"
        )
    }

    // --- Credit Ledger ---
    suspend fun addCreditItem(item: CreditLedgerItem, actionByPartnerId: Int) {
        agroDao.insertCreditLedgerItem(item)
        val typeBangla = if (item.partyType == "SUPPLIER") "সাপ্লায়ার" else "কাস্টমার"
        logAudit(
            partnerId = actionByPartnerId,
            action = "বাকির লেজারে নতুন $typeBangla এন্ট্রি করা হয়েছে: ${item.partyName}, বকেয়া: ৳${item.totalDue}"
        )
    }

    suspend fun updateCreditItem(item: CreditLedgerItem, actionByPartnerId: Int, prevAmt: Double) {
        agroDao.insertCreditLedgerItem(item)
        logAudit(
            partnerId = actionByPartnerId,
            action = "বকেয়া লেজার পরিবর্তন করা হয়েছে: ${item.partyName}, বকেয়ার পরিমাণ ৳$prevAmt থেকে ৳${item.totalDue} করা হয়েছে।"
        )
    }

    suspend fun deleteCredit(item: CreditLedgerItem, actionByPartnerId: Int) {
        agroDao.deleteCreditLedgerItem(item)
        logAudit(
            partnerId = actionByPartnerId,
            action = "বকেয়া লেজার থেকে '${item.partyName}' মুছে ফেলা হয়েছে।"
        )
    }

    // --- Internal Helpers ---
    private suspend fun logAudit(partnerId: Int, action: String) {
        agroDao.insertAuditLog(AuditLog(partnerId = partnerId, actionDescription = action))
    }

    fun getAllAuditLogsForExport(): List<AuditLog> {
        return agroDao.getAllAuditLogsForExport()
    }

    private fun getMonthBangla(month: Int): String {
        return when (month) {
            1 -> "জানুয়ারি"
            2 -> "ফেব্রুয়ারি"
            3 -> "মার্চ"
            4 -> "এপ্রিল"
            5 -> "মে"
            6 -> "জুন"
            7 -> "জুলাই"
            8 -> "আগস্ট"
            9 -> "সেপ্টেম্বর"
            10 -> "অক্টোবর"
            11 -> "নভেম্বর"
            12 -> "ডিসেম্বর"
            else -> month.toString()
        }
    }
}
