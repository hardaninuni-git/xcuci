package com.example.xcuci.data.local.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.xcuci.data.local.dao.CustomerDao
import com.example.xcuci.data.local.dao.OrderDao
import com.example.xcuci.data.local.entity.OrderEntity

@Database(
    entities = [OrderEntity::class],
    version = 4, // TINGKATKAN LAGI KE 4
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun orderDao(): OrderDao
    abstract fun customerDao(): CustomerDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Migration untuk fix NOT NULL constraint
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Buat table baru dengan schema yang benar
                database.execSQL("""
                    CREATE TABLE orders_new (
                        id INTEGER PRIMARY KEY NOT NULL,
                        customer_name TEXT NOT NULL,
                        phone TEXT NOT NULL,
                        address TEXT NOT NULL,
                        weight REAL NOT NULL,
                        price_per_kg REAL NOT NULL,
                        total_price REAL NOT NULL,
                        status TEXT NOT NULL,
                        created_at TEXT NOT NULL,
                        updated_at TEXT NOT NULL,
                        completion_date TEXT,
                        service_type TEXT,
                        is_synced INTEGER NOT NULL,
                        kaos_qty INTEGER NOT NULL,
                        celana_qty INTEGER NOT NULL,
                        handuk_qty INTEGER NOT NULL,
                        total_pcs INTEGER NOT NULL
                    )
                """.trimIndent())

                // Copy data dari table lama ke baru
                database.execSQL("""
                    INSERT INTO orders_new 
                    SELECT 
                        id, customer_name, phone, address, weight, price_per_kg, total_price,
                        status, created_at, updated_at, completion_date, service_type, is_synced,
                        COALESCE(kaos_qty, 0) as kaos_qty,
                        COALESCE(celana_qty, 0) as celana_qty,
                        COALESCE(handuk_qty, 0) as handuk_qty,
                        COALESCE(total_pcs, 0) as total_pcs
                    FROM orders
                """.trimIndent())

                // Hapus table lama dan rename yang baru
                database.execSQL("DROP TABLE orders")
                database.execSQL("ALTER TABLE orders_new RENAME TO orders")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "laundry_database"
                )
                    .addMigrations(MIGRATION_3_4)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}