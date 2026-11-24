package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.Delivery
import com.example.data.model.Order

// Aquí declaramos las tablas que usará la base de datos[cite: 3]
@Database(entities = [Order::class, Delivery::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun operationsDao(): OperationsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            // Patrón Singleton para no abrir múltiples conexiones a la BD a la vez
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ventas_distribucion_db" // Nombre del archivo SQLite
                )
                    // En un proyecto real se usan Migrations, pero para un MVP académico basta con limpiar si cambia la versión
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}