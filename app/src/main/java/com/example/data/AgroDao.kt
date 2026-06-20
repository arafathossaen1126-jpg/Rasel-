package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AgroDao {

    // --- Partners ---
    @Query("SELECT * FROM partners ORDER BY id ASC")
    fun getAllPartners(): Flow<List<Partner>>

    @Query("SELECT * FROM partners WHERE id = :id LIMIT 1")
    suspend fun getPartnerById(id: Int): Partner?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPartner(partner: Partner)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPartners(partners: List<Partner>)

    @Update
    suspend fun updatePartner(partner: Partner)


    // --- Percentage History ---
    @Query("SELECT * FROM percentage_history WHERE year = :year AND month = :month")
    fun getPercentageHistory(year: Int, month: Int): Flow<List<PercentageHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPercentageHistory(history: PercentageHistory)


    // --- Transactions ---
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(tx: Transaction): Long

    @Update
    suspend fun updateTransaction(tx: Transaction)

    @Delete
    suspend fun deleteTransaction(tx: Transaction)


    // --- Batches ---
    @Query("SELECT * FROM batches ORDER BY id DESC")
    fun getAllBatches(): Flow<List<Batch>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatch(batch: Batch): Long

    @Update
    suspend fun updateBatch(batch: Batch)


    // --- Partner Approvals ---
    @Query("SELECT * FROM partner_approvals WHERE transactionId = :txId")
    fun getApprovalsForTransaction(txId: Int): Flow<List<PartnerApproval>>

    @Query("SELECT * FROM partner_approvals")
    fun getAllApprovals(): Flow<List<PartnerApproval>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApproval(approval: PartnerApproval)


    // --- Chat Messages ---
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllChatMessages(): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(msg: ChatMessage)


    // --- Inventory ---
    @Query("SELECT * FROM inventory_items ORDER BY category ASC, name ASC")
    fun getAllInventoryItems(): Flow<List<InventoryItem>>

    @Query("SELECT * FROM inventory_items WHERE id = :id LIMIT 1")
    suspend fun getInventoryItemById(id: Int): InventoryItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInventoryItem(item: InventoryItem)

    @Update
    suspend fun updateInventoryItem(item: InventoryItem)

    @Delete
    suspend fun deleteInventoryItem(item: InventoryItem)


    // --- Credit Ledger ---
    @Query("SELECT * FROM credit_ledger_items ORDER BY lastUpdated DESC")
    fun getAllCreditLedgerItems(): Flow<List<CreditLedgerItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCreditLedgerItem(item: CreditLedgerItem)

    @Update
    suspend fun updateCreditLedgerItem(item: CreditLedgerItem)

    @Delete
    suspend fun deleteCreditLedgerItem(item: CreditLedgerItem)


    // --- Audit Logs ---
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT 200")
    fun getAllAuditLogs(): Flow<List<AuditLog>>

    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC")
    fun getAllAuditLogsForExport(): List<AuditLog>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AuditLog)
}
