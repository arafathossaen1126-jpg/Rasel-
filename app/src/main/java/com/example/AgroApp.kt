package com.example

import android.app.Application
import com.example.data.AgroDatabase
import com.example.data.AgroRepository

class AgroApp : Application() {
    val database by lazy { AgroDatabase.getDatabase(this) }
    val repository by lazy { AgroRepository(database.agroDao()) }
}
