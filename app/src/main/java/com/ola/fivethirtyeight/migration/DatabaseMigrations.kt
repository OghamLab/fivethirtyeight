package com.ola.fivethirtyeight.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase


object DatabaseMigrations {

    val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {

            db.execSQL(
                """
            CREATE TABLE feed_items_new (
                title TEXT NOT NULL,
                description TEXT NOT NULL,
                content TEXT NOT NULL,
                author TEXT NOT NULL,
                publishedAt TEXT NOT NULL,
                imageUrl TEXT NOT NULL,
                link TEXT NOT NULL PRIMARY KEY,
                savedDate TEXT NOT NULL,
                timeInMil INTEGER NOT NULL,
                isSavedForLater INTEGER NOT NULL
            )
            """.trimIndent()
            )

            db.execSQL(
                """
            INSERT INTO feed_items_new (
                title, description, content, author, publishedAt,
                imageUrl, link, savedDate, timeInMil, isSavedForLater
            )
            SELECT
                title, description, content, author, publishedAt,
                imageUrl, link, savedDate, timeInMil, isSavedForLater
            FROM feed_items
            """.trimIndent()
            )

            db.execSQL("DROP TABLE feed_items")
            db.execSQL("ALTER TABLE feed_items_new RENAME TO feed_items")

            // Correct indices
            db.execSQL("CREATE INDEX index_feed_items_timeInMil ON feed_items(timeInMil)")
            db.execSQL("CREATE INDEX index_feed_items_isSavedForLater ON feed_items(isSavedForLater)")
            db.execSQL("CREATE INDEX index_feed_items_publishedAt ON feed_items(publishedAt)")
        }
    }


    /*val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {

            // 1. Create final table (NO category)
            db.execSQL(
                """
                CREATE TABLE feed_items_new (
                    title TEXT NOT NULL,
                    description TEXT NOT NULL,
                    content TEXT NOT NULL,
                    author TEXT NOT NULL,
                    publishedAt TEXT NOT NULL,
                    imageUrl TEXT NOT NULL,
                    link TEXT NOT NULL PRIMARY KEY,
                    savedDate TEXT NOT NULL,
                    timeInMil INTEGER NOT NULL,
                    isSavedForLater INTEGER NOT NULL
                )
                """.trimIndent()
            )

            // 2. Copy only columns that exist in final schema
            db.execSQL(
                """
                INSERT INTO feed_items_new (
                    title, description, content, author, publishedAt,
                    imageUrl, link, savedDate, timeInMil, isSavedForLater
                )
                SELECT
                    title, description, content, author, publishedAt,
                    imageUrl, link, savedDate, timeInMil, isSavedForLater
                FROM feed_items
                """.trimIndent()
            )

            // 3. Replace table
            db.execSQL("DROP TABLE feed_items")
            db.execSQL("ALTER TABLE feed_items_new RENAME TO feed_items")

            // 4. Recreate indices (must match Entity)
            db.execSQL("CREATE INDEX index_feed_items_timeInMil ON feed_items(timeInMil)")
            db.execSQL("CREATE INDEX index_feed_items_isSavedForLater ON feed_items(isSavedForLater)")
            db.execSQL("CREATE INDEX index_feed_items_link ON feed_items(link)")
        }
    }*/
}


/*
=======
>>>>>>> origin/master
// DatabaseMigrations.kt
object DatabaseMigrations {

    val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
<<<<<<< HEAD
                ALTER TABLE feed_items
                ADD COLUMN category TEXT NOT NULL DEFAULT ''
            """.trimIndent()
            )
        }
    }
    */
/*val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                ALTER TABLE feed_items
=======
                ALTER TABLE world_items
>>>>>>> origin/master
                ADD COLUMN category TEXT NOT NULL DEFAULT ''
            """.trimIndent()
            )
        }
<<<<<<< HEAD
    }*//*



    val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {

            // 1. Create new table with the new schema
            db.execSQL(
                """
            CREATE TABLE IF NOT EXISTS feed_items_new (
                title TEXT NOT NULL,
                description TEXT NOT NULL,
                content TEXT NOT NULL,
                author TEXT NOT NULL,
                publishedAt TEXT NOT NULL,
                imageUrl TEXT NOT NULL,
                link TEXT NOT NULL PRIMARY KEY,
                savedDate TEXT NOT NULL,
                timeInMil INTEGER NOT NULL,
                isSavedForLater INTEGER NOT NULL
            )
        """
            )

            // 2. Copy data from old table (only columns that still exist)
            db.execSQL(
                """
            INSERT INTO feed_items_new (
                title, description, content, author, publishedAt,
                imageUrl, link, savedDate, timeInMil, isSavedForLater
            )
            SELECT
                title, description, content, author, publishedAt,
                imageUrl, link, savedDate, timeInMil, isSavedForLater
            FROM feed_items
        """
            )

            // 3. Drop old table
            db.execSQL("DROP TABLE feed_items")

            // 4. Rename new table
            db.execSQL("ALTER TABLE feed_items_new RENAME TO feed_items")

            // 5. Recreate indices
            db.execSQL("CREATE INDEX IF NOT EXISTS index_feed_items_timeInMil ON feed_items(timeInMil)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_feed_items_link ON feed_items(link)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_feed_items_isSavedForLater ON feed_items(isSavedForLater)")
        }
    }

}*/

