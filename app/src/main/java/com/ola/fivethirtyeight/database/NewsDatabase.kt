package com.ola.fivethirtyeight.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ola.fivethirtyeight.dao.BusinessItemDao
import com.ola.fivethirtyeight.dao.FeedItemDao
import com.ola.fivethirtyeight.dao.FeedRefreshDao
import com.ola.fivethirtyeight.dao.FiveThirtyEightItemDao
import com.ola.fivethirtyeight.dao.HealthItemDao
import com.ola.fivethirtyeight.dao.PoliticsItemDao
import com.ola.fivethirtyeight.dao.SportsItemDao
import com.ola.fivethirtyeight.dao.TechItemDao
import com.ola.fivethirtyeight.dao.WorldItemDao
import com.ola.fivethirtyeight.model.BusinessItemEntity
import com.ola.fivethirtyeight.model.FeedItemEntity
import com.ola.fivethirtyeight.model.FeedRefreshEntity
import com.ola.fivethirtyeight.model.FiveThirtyEightItemEntity
import com.ola.fivethirtyeight.model.HealthItemEntity
import com.ola.fivethirtyeight.model.PoliticsItemEntity
import com.ola.fivethirtyeight.model.SportsItemEntity
import com.ola.fivethirtyeight.model.TechItemEntity
import com.ola.fivethirtyeight.model.WorldItemEntity


@Database(entities = [FeedItemEntity::class , PoliticsItemEntity::class, WorldItemEntity::class, BusinessItemEntity::class, SportsItemEntity::class, TechItemEntity::class, HealthItemEntity::class, FiveThirtyEightItemEntity::class, FeedRefreshEntity::class], version = 4,
    exportSchema = false)
abstract class NewsDatabase: RoomDatabase() {
    abstract fun feedItemDao(): FeedItemDao
    abstract fun politicsItemDao(): PoliticsItemDao
    abstract fun worldItemDao(): WorldItemDao
    abstract fun sportsItemDao(): SportsItemDao
    abstract fun businessItemDao(): BusinessItemDao
    abstract fun techItemDao(): TechItemDao
    abstract fun healthItemDao(): HealthItemDao
    abstract fun fiveThirtyEightItemDao(): FiveThirtyEightItemDao
    abstract fun refreshDao(): FeedRefreshDao
}







