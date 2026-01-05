package com.ola.fivethirtyeight.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

// DatabaseMigrations.kt
object DatabaseMigrations {

    val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                ALTER TABLE world_items
                ADD COLUMN category TEXT NOT NULL DEFAULT ''
            """.trimIndent()
            )
        }
    }
}
