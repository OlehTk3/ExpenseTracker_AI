package com.example.myapp

import android.app.Application
import com.example.myapp.data.AppDatabase
import com.example.myapp.data.ExpenseRepository

class MyAppApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { ExpenseRepository(database.expenseDao()) }
}