package com.ola.fivethirtyeight.appModule

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import com.ola.fivethirtyeight.api.ApiService
import com.ola.fivethirtyeight.dao.BusinessItemDao
import com.ola.fivethirtyeight.dao.FeedItemDao
import com.ola.fivethirtyeight.dao.FeedRefreshDao
import com.ola.fivethirtyeight.dao.FiveThirtyEightItemDao
import com.ola.fivethirtyeight.dao.HealthItemDao
import com.ola.fivethirtyeight.dao.PoliticsItemDao
import com.ola.fivethirtyeight.dao.SportsItemDao
import com.ola.fivethirtyeight.dao.TechItemDao
import com.ola.fivethirtyeight.dao.WorldItemDao
import com.ola.fivethirtyeight.dataSource.BusinessDataSource
import com.ola.fivethirtyeight.dataSource.BusinessDataSourceImpl
import com.ola.fivethirtyeight.dataSource.HealthDataSource
import com.ola.fivethirtyeight.dataSource.HealthDataSourceImpl
import com.ola.fivethirtyeight.dataSource.NewsDataSource
import com.ola.fivethirtyeight.dataSource.NewsDataSourceImpl
import com.ola.fivethirtyeight.dataSource.PoliticsDataSource
import com.ola.fivethirtyeight.dataSource.PoliticsDataSourceImpl
import com.ola.fivethirtyeight.dataSource.SportsDataSource
import com.ola.fivethirtyeight.dataSource.SportsDataSourceImpl
import com.ola.fivethirtyeight.dataSource.TechDataSource
import com.ola.fivethirtyeight.dataSource.TechDataSourceImpl
import com.ola.fivethirtyeight.dataSource.TopStoriesDataSource
import com.ola.fivethirtyeight.dataSource.TopStoriesDataSourceImpl
import com.ola.fivethirtyeight.dataSource.WorldDataSource
import com.ola.fivethirtyeight.dataSource.WorldDataSourceImpl
import com.ola.fivethirtyeight.database.NewsDatabase
import com.ola.fivethirtyeight.datastore.SyncPreferences
import com.ola.fivethirtyeight.migration.DatabaseMigrations
import com.ola.fivethirtyeight.repository.BusinessFeedRepository
import com.ola.fivethirtyeight.repository.FiveThirtyEightFeedRepository
import com.ola.fivethirtyeight.repository.HealthFeedRepository
import com.ola.fivethirtyeight.repository.PoliticsFeedRepository
import com.ola.fivethirtyeight.repository.SettingsRepository
import com.ola.fivethirtyeight.repository.SportsFeedRepository
import com.ola.fivethirtyeight.repository.TechFeedRepository
import com.ola.fivethirtyeight.repository.TopStoriesFeedRepository
import com.ola.fivethirtyeight.repository.WorldFeedRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideRetrofit(): ApiService =
        Retrofit.Builder()
            .baseUrl(ApiService.BASE_URL)
            .addConverterFactory(ScalarsConverterFactory.create())
            .build().create(ApiService::class.java)


    @Singleton
    @Provides
    fun providesNewsDataSource(apiService: ApiService): NewsDataSource {
        return NewsDataSourceImpl(apiService)

    }


    @Singleton
    @Provides
    fun providesNewsRepository(newsDataSource: NewsDataSource, fiveThirtyEightItemDao: FiveThirtyEightItemDao): FiveThirtyEightFeedRepository {
        return FiveThirtyEightFeedRepository(newsDataSource, fiveThirtyEightItemDao)

    }


    @Singleton
    @Provides
    fun providesTopStoriesRepository(topStoriesDataSource: TopStoriesDataSource,
        feedItemDao: FeedItemDao,  refreshDao: FeedRefreshDao, database: NewsDatabase
    ): TopStoriesFeedRepository {
        return TopStoriesFeedRepository (topStoriesDataSource, feedItemDao, refreshDao, database)

    }


    @Provides
    @Singleton
    fun provideRefreshDao(newsDatabase: NewsDatabase) = newsDatabase.refreshDao()




    @Singleton
    @Provides
    fun providesTopStoriesDataSource(apiService: ApiService): TopStoriesDataSource {
        return TopStoriesDataSourceImpl(apiService)

    }


    @Singleton
    @Provides
    fun providesPoliticsRepository(
        politicsDataSource: PoliticsDataSource,
        politicsItemDao: PoliticsItemDao
    ): PoliticsFeedRepository {
        return PoliticsFeedRepository(politicsDataSource, politicsItemDao)

    }


    @Singleton
    @Provides
    fun providesPoliticsDataSource(apiService: ApiService): PoliticsDataSource {
        return PoliticsDataSourceImpl(apiService)

    }


    @Singleton
    @Provides
    fun providesWorldRepository(
        worldDataSource: WorldDataSource,
        worldItemDao: WorldItemDao
    ): WorldFeedRepository {
        return WorldFeedRepository(worldDataSource, worldItemDao)
    }


    @Singleton
    @Provides
    fun providesWorldDataSource(apiService: ApiService): WorldDataSource {
        return WorldDataSourceImpl(apiService)
    }


    @Singleton
    @Provides
    fun providesBusinessRepository(
        businessDataSource: BusinessDataSource,
        businessItemDao: BusinessItemDao, syncPreferences: SyncPreferences
    ): BusinessFeedRepository {
        return BusinessFeedRepository(businessDataSource, businessItemDao, syncPreferences)
    }

    @Singleton
    @Provides
    fun providesBusinessDataSource(apiService: ApiService): BusinessDataSource {
        return BusinessDataSourceImpl(apiService)

    }

    @Singleton
    @Provides
    fun providesSportsRepository(
        sportsDataSource: SportsDataSource,
        sportsItemDao: SportsItemDao
    ): SportsFeedRepository {
        return SportsFeedRepository(sportsDataSource, sportsItemDao)
    }

    @Singleton
    @Provides
    fun providesSportsDataSource(apiService: ApiService): SportsDataSource {
        return SportsDataSourceImpl(apiService)

    }


    @Singleton
    @Provides
    fun providesTechRepository(
        techDataSource: TechDataSource,
        techItemDao: TechItemDao
    ): TechFeedRepository {
        return TechFeedRepository(techDataSource, techItemDao)
    }

    @Singleton
    @Provides
    fun providesTechDataSource(apiService: ApiService): TechDataSource {
        return TechDataSourceImpl(apiService)
    }


    @Singleton
    @Provides
    fun providesHealthRepository(
        healthDataSource: HealthDataSource,
        healthItemDao: HealthItemDao
    ): HealthFeedRepository {
        return HealthFeedRepository(healthDataSource, healthItemDao)
    }

    @Singleton
    @Provides
    fun providesHealthDataSource(apiService: ApiService): HealthDataSource {
        return HealthDataSourceImpl(apiService)
    }


    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context) =
        Room.databaseBuilder(context, NewsDatabase::class.java, "news_db")
            .addMigrations(DatabaseMigrations.MIGRATION_3_4)
            .build()



    @Provides
    @Singleton
    fun provideFiveThirtyEightDao(newsDatabase: NewsDatabase) = newsDatabase.fiveThirtyEightItemDao()


    @Provides
    @Singleton
    fun provideDao(newsDatabase: NewsDatabase) = newsDatabase.feedItemDao()


    @Provides
    @Singleton
    fun providePoliticsDao(newsDatabase: NewsDatabase) = newsDatabase.politicsItemDao()


    @Provides
    @Singleton
    fun provideWorldDao(newsDatabase: NewsDatabase) = newsDatabase.worldItemDao()


    @Provides
    @Singleton
    fun provideBusinessDao(newsDatabase: NewsDatabase) = newsDatabase.businessItemDao()

    @Provides
    @Singleton
    fun provideSportsDao(newsDatabase: NewsDatabase) = newsDatabase.sportsItemDao()


    @Provides
    @Singleton
    fun provideTechDao(newsDatabase: NewsDatabase) = newsDatabase.techItemDao()


    @Provides
    @Singleton
    fun provideHealthDao(newsDatabase: NewsDatabase) = newsDatabase.healthItemDao()


    private const val DATASTORE_NAME = "sync_prefs"


    /*@Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(
            produceFile = { context.preferencesDataStoreFile(DATASTORE_NAME) }
        )


    }*/


    @Singleton
    @Provides
    fun providesSettingsRepository(syncPreferences: SyncPreferences): SettingsRepository {
        return SettingsRepository(syncPreferences)
    }


    @Provides
    @Singleton
    fun provideSyncPreferences(dataStore: DataStore<Preferences>): SyncPreferences {
        return SyncPreferences(dataStore)
    }

        @Provides
        @Singleton
        fun providePreferencesDataStore(
            @ApplicationContext context: Context
        ): DataStore<Preferences> =
            PreferenceDataStoreFactory.create(
                produceFile = {
                    context.preferencesDataStoreFile("sync_prefs")
                }
            )
    }




















