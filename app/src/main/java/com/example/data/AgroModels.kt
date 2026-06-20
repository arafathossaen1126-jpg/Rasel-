package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "partners")
data class Partner(
    @PrimaryKey val id: Int, // 1 to 6
    val name: String,
    val colorHex: String,
    val pin: String,
    val currentSharePercentage: Double,
    val monthlySalary: Double = 0.0
)

@Entity(tableName = "percentage_history")
data class PercentageHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val year: Int,
    val month: Int,
    val partnerId: Int,
    val percentage: Double
)

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val amount: Double,
    val type: String, // "INCOME", "EXPENSE", "DRAWING", "SALARY"
    val category: String, // "খাবার খরচ", "ওষুধ", "পরিবহন", "বেতন", "অন্যান্য", "ব্যক্তিগত উত্তোলন" etc.
    val batchId: Int?, // associated Batch table id
    val paidByPartnerId: Int, // partner who paid
    val isFromPocket: Boolean, // if paid out of partner's pocket (farm owes them)
    val timestamp: Long = System.currentTimeMillis(),
    val approvalStatus: String = "APPROVED", // "PENDING", "APPROVED", "REJECTED" (needs vote if > 5000)
    val receiptUri: String? = null, // base64 code or simulated image URI/preset-name
    val addedByPartnerId: Int
)

@Entity(tableName = "batches")
data class Batch(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String, // e.g. "হাঁস ব্যাচ-০১", "খাবার লট-০২"
    val description: String? = null,
    val startDate: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
)

@Entity(tableName = "partner_approvals")
data class PartnerApproval(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val transactionId: Int,
    val partnerId: Int,
    val isApproved: Boolean,
    val voteTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val partnerId: Int,
    val messageText: String,
    val timestamp: Long = System.currentTimeMillis(),
    val associatedTransactionId: Int? = null
)

@Entity(tableName = "inventory_items")
data class InventoryItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String, // e.g., "পোল্ট্রি ফিড", "ভ্যাকসিন"
    val category: String, // "ফিড", "ওষুধ", "অন্যান্য"
    val currentQuantity: Double,
    val unit: String, // "কেজি", "বস্তা", "পিস"
    val minThresholdAlert: Double = 5.0
)

@Entity(tableName = "credit_ledger_items")
data class CreditLedgerItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val partyName: String, // কাস্টমার বা সাপ্লায়ারের নাম
    val partyType: String, // "SUPPLIER" (আমরা টাকা পাবো/দেবো) or "CUSTOMER" (তারা বাকিতে নিয়েছে)
    val phone: String? = null,
    val totalDue: Double, // positive means outstanding dues
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "audit_logs")
data class AuditLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val partnerId: Int,
    val actionDescription: String,
    val timestamp: Long = System.currentTimeMillis()
)
