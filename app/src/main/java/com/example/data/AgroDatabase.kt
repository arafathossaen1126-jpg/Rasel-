package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Partner::class,
        PercentageHistory::class,
        Transaction::class,
        Batch::class,
        PartnerApproval::class,
        ChatMessage::class,
        InventoryItem::class,
        CreditLedgerItem::class,
        AuditLog::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AgroDatabase : RoomDatabase() {
    abstract fun agroDao(): AgroDao

    companion object {
        @Volatile
        private var INSTANCE: AgroDatabase? = null

        fun getDatabase(context: Context): AgroDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AgroDatabase::class.java,
                    "agro_farm_partnership_db"
                )
                .fallbackToDestructiveMigration()
                .addCallback(DatabaseCallback())
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // Prepopulate database with initial state (6 partners + some helper items)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        val dao = database.agroDao()
                        
                        // Initial 6 partners with specific colors and pins
                        // 1. আব্দুর রহমান (#D1E7DD - green)
                        // 2. মো হাসিব (#CFE2FF - blue)
                        // 3. ফারিয়া আক্তার (#F8D7DA - pink/red)
                        // 4. কামাল উদ্দিন (#FFF3CD - yellow)
                        // 5. সাদিয়া ইসলাম (#E2D9F3 - purple)
                        // 6. আরিফুল হাসান (#E0F2FE - light sky blue)
                        val initialPartners = listOf(
                            Partner(1, "আব্দুর রহমান", "#D1E7DD", "1111", 16.66, 12000.0),
                            Partner(2, "মো হাসিব", "#CFE2FF", "2222", 16.66, 0.0),
                            Partner(3, "ফারিয়া আক্তার", "#F8D7DA", "3333", 16.66, 0.0),
                            Partner(4, "কামাল উদ্দিন", "#FFF3CD", "4444", 16.66, 8000.0),
                            Partner(5, "সাদিয়া ইসলাম", "#E2D9F3", "5555", 16.66, 0.0),
                            Partner(6, "আরিফুল হাসান", "#E0F2FE", "6666", 16.70, 0.0) // slightly adjusted for sum of 100%
                        )
                        dao.insertPartners(initialPartners)

                        // Insert initial batch
                        val defaultBatchId = dao.insertBatch(
                            Batch(name = "হাঁস ব্যাচ-০১", description = "উদ্বোধনী হাঁসের লট - ২০২৬")
                        )
                        dao.insertBatch(Batch(name = "ব্রয়লার লট-০২", description = "মাংসের মুরগি - ৫০টি"))

                        // Add some starter credit ledger items (Baki account)
                        dao.insertCreditLedgerItem(
                            CreditLedgerItem(partyName = "রফিক ফিড সাপ্লাইয়ার", partyType = "SUPPLIER", totalDue = 15000.0) // we owe them
                        )
                        dao.insertCreditLedgerItem(
                            CreditLedgerItem(partyName = "আজাদ এগ্রো ডিস্ট্রিবিউটর", partyType = "CUSTOMER", totalDue = -4500.0) // they owe us (or vice-versa, we'll keep positive as credit)
                        )

                        // Populate base inventory items
                        dao.insertInventoryItem(InventoryItem(name = "স্টার্টার পোল্ট্রি ফিড", category = "ফিড", currentQuantity = 20.0, unit = "বস্তা", minThresholdAlert = 4.0))
                        dao.insertInventoryItem(InventoryItem(name = "রেনামাইসিন অ্যান্টিবায়োটিক", category = "ওষুধ", currentQuantity = 12.0, unit = "পিস", minThresholdAlert = 3.0))
                        dao.insertInventoryItem(InventoryItem(name = "ভিটামিন এডি৩ই সাপ্লিমেন্ট", category = "ওষুধ", currentQuantity = 2.0, unit = "পিস", minThresholdAlert = 5.0))

                        // Populate starter chat messages
                        dao.insertChatMessage(ChatMessage(partnerId = 1, messageText = "আসসালামু আলাইকুম, আমাদের এগ্রো ফার্ম পার্টনারশিপ লেজার অ্যাপে স্বাগতম!"))
                        dao.insertChatMessage(ChatMessage(partnerId = 2, messageText = "ওয়ালাইকুম আসসালাম। এখান থেকে আমরা সব কয়জন হিসাব করতে পারব সুন্দরভাবে!"))

                        // Add initial audit log
                        dao.insertAuditLog(AuditLog(partnerId = 1, actionDescription = "সিস্টেম ডাটাবেজ সফলভাবে চালু হয়েছে এবং প্রাথমিক ৬ সদস্য যুক্ত হয়েছেন।"))
                    }
                }
            }
        }
    }
}
