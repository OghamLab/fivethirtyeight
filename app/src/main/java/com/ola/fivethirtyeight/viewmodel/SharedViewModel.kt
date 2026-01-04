package com.ola.fivethirtyeight.viewmodel

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.ola.fivethirtyeight.datastore.SyncPreferences
import com.ola.fivethirtyeight.model.FeedItem
import com.ola.fivethirtyeight.repository.BusinessFeedRepository
import com.ola.fivethirtyeight.repository.FiveThirtyEightFeedRepository
import com.ola.fivethirtyeight.repository.HealthFeedRepository
import com.ola.fivethirtyeight.repository.PoliticsFeedRepository
import com.ola.fivethirtyeight.repository.SportsFeedRepository
import com.ola.fivethirtyeight.repository.TechFeedRepository
import com.ola.fivethirtyeight.repository.TopStoriesFeedRepository
import com.ola.fivethirtyeight.repository.WorldFeedRepository
import com.ola.fivethirtyeight.resource.ResourceState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class SharedViewModel @Inject constructor(
    private val syncPreferences: SyncPreferences,
    private val topStoriesFeedRepository: TopStoriesFeedRepository,
    private val politicsFeedRepository: PoliticsFeedRepository,
    private val worldFeedRepository: WorldFeedRepository,
    private val businessFeedRepository: BusinessFeedRepository,
    private val techFeedRepository: TechFeedRepository,
    private val sportsFeedRepository: SportsFeedRepository,
    private val healthFeedRepository: HealthFeedRepository,
    private val fiveThirtyEightRepository: FiveThirtyEightFeedRepository,
    savedStateHandle: SavedStateHandle,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _hasLoadedOnce = MutableStateFlow(false)
    val hasLoadedOnce: StateFlow<Boolean> = _hasLoadedOnce

    private val _hasLoadedOnceFive = MutableStateFlow(false)
    val hasLoadedOnceFive: StateFlow<Boolean> = _hasLoadedOnceFive

    private val _hasLoadedOncePol = MutableStateFlow(false)
    val hasLoadedOncePol: StateFlow<Boolean> = _hasLoadedOncePol

    private val _hasLoadedOnceWorld = MutableStateFlow(false)
    val hasLoadedOnceWorld: StateFlow<Boolean> = _hasLoadedOnceWorld

    private val _hasLoadedOnceBus = MutableStateFlow(false)
    val hasLoadedOnceBus: StateFlow<Boolean> = _hasLoadedOnceBus

    private val _hasLoadedOnceSports = MutableStateFlow(false)
    val hasLoadedOnceSports: StateFlow<Boolean> = _hasLoadedOnceSports

    private val _hasLoadedOnceTech = MutableStateFlow(false)
    val hasLoadedOnceTech: StateFlow<Boolean> = _hasLoadedOnceTech

    private val _hasLoadedOnceHealth = MutableStateFlow(false)
    val hasLoadedOnceHealth: StateFlow<Boolean> = _hasLoadedOnceHealth


    // --- Selected feed item ---
    private val _selectedFeedItem = MutableStateFlow<FeedItem?>(null)
    val selectedFeedItem: StateFlow<FeedItem?> = _selectedFeedItem

    fun selectFeedItem(item: FeedItem) {
        _selectedFeedItem.value = item
    }

    // --- Scroll position ---
    private val _firstVisibleItemIndex = MutableStateFlow(0)
    val firstVisibleItemIndex: StateFlow<Int> = _firstVisibleItemIndex

    private val _firstVisibleItemScrollOffset = MutableStateFlow(0)
    val firstVisibleItemScrollOffset: StateFlow<Int> = _firstVisibleItemScrollOffset

   fun saveScrollPosition(index: Int, offset: Int) {
       _firstVisibleItemIndex.value = index
        _firstVisibleItemScrollOffset.value = offset
    }


    // --- Top FiveThirtyEight state ---
    private val _fiveThirtyEightFeedState =
        MutableStateFlow<ResourceState<List<FeedItem>>>(ResourceState.Loading())
    val fiveThirtyEightFeedState : StateFlow<ResourceState<List<FeedItem>>> = _fiveThirtyEightFeedState

    private val _isRefreshingFiveThirtyEight = MutableStateFlow(false)
    val isRefreshingFiveThirtyEight = _isRefreshingFiveThirtyEight

    private val _isRefreshingWorld = MutableStateFlow(false)
    val isRefreshingWorld = _isRefreshingWorld

    private val _isRefreshingBusiness = MutableStateFlow(false)
    val isRefreshingBusiness = _isRefreshingBusiness

    private val _isRefreshingTech = MutableStateFlow(false)
    val isRefreshingTech = _isRefreshingTech

    private val _isRefreshingSports = MutableStateFlow(false)
    val isRefreshingSports = _isRefreshingSports

    private val _isRefreshingHealth = MutableStateFlow(false)
    val isRefreshingHealth = _isRefreshingHealth

    private val _isRefreshingPolitics = MutableStateFlow(false)
    val isRefreshingPolitics: StateFlow<Boolean> = _isRefreshingPolitics


    // --- Politics feed state ---
    private val _politicsFeedState =
        MutableStateFlow<ResourceState<List<FeedItem>>>(ResourceState.Loading())
    val politicsFeedState: StateFlow<ResourceState<List<FeedItem>>> = _politicsFeedState


    // --- World feed state ---
    private val _worldFeedState =
        MutableStateFlow<ResourceState<List<FeedItem>>>(ResourceState.Loading())
    val worldFeedState: StateFlow<ResourceState<List<FeedItem>>> = _worldFeedState


    // --- Business feed state ---
    private val _businessFeedState =
        MutableStateFlow<ResourceState<List<FeedItem>>>(ResourceState.Loading())
    val businessFeedState: StateFlow<ResourceState<List<FeedItem>>> = _businessFeedState


    // --- Tech feed state ---
    private val _techFeedState =
        MutableStateFlow<ResourceState<List<FeedItem>>>(ResourceState.Loading())
    val techFeedState: StateFlow<ResourceState<List<FeedItem>>> = _techFeedState


    // --- Sport feed state ---
    private val _sportsFeedState =
        MutableStateFlow<ResourceState<List<FeedItem>>>(ResourceState.Loading())
    val sportsFeedState: StateFlow<ResourceState<List<FeedItem>>> = _sportsFeedState


    // --- Heath feed state ---
    private val _healthFeedState =
        MutableStateFlow<ResourceState<List<FeedItem>>>(ResourceState.Loading())
    val healthFeedState: StateFlow<ResourceState<List<FeedItem>>> = _healthFeedState


    /* ---------- TOP STORIES ---------- */

   // private val _topStoriesState =
       // MutableStateFlow<ResourceState<List<FeedItem>>>(ResourceState.Loading())
    //val topStoriesState: StateFlow<ResourceState<List<FeedItem>>> = _topStoriesState


    val topStoriesPaging =
        topStoriesFeedRepository
            .pagingTopStories()
            .cachedIn(viewModelScope)

    /*val topStoriesPaging = Pager(
        config = PagingConfig(pageSize = 20),
        pagingSourceFactory = { TopStoriesSource(repo) }).flow.cachedIn(viewModelScope)*/

   // private val _isRefreshingTop = MutableStateFlow(false)
   // val isRefreshingTop: StateFlow<Boolean> = _isRefreshingTop


    private val _hasLoadedOnceTop = MutableStateFlow(false)
    val hasLoadedOnceTop: StateFlow<Boolean> = _hasLoadedOnceTop


    // --- Init ---
    /*init {
        // Start observing TopStories DB once
        observeTopStoriesDb()
        observePoliticsDb()
        observeFiveThirtyEightDb()
        observeWorldDb()
        observeBusinessDb()
        observeTechDb()
        observeSportsDb()
        observeHealthDb()


        // One-time initial sync (optional; keep if you want initial fetch)
        // Your existing other-feed init can stay as-is
        if (savedStateHandle.get<Boolean>("hasFetchedOtherFeeds") != true) {
           refreshTopStories()
            fetchPoliticsStories()
            fetchWorldStories()
            fetchBusinessStories()
            fetchTechStories()
            fetchSportsStories()
            fetchHealthStories()
            fetchFiveThirtyEightStories()
            savedStateHandle["hasFetchedOtherFeeds"] = true
        }
    }*/

    init {
       // observeTopStoriesDb()
        observePoliticsDb()
        observeWorldDb()
        observeBusinessDb()
        observeTechDb()
        observeSportsDb()
        observeHealthDb()
        observeFiveThirtyEightDb()

        val hasFetchedTopStories =
            savedStateHandle.get<Boolean>("hasFetchedTopStories") == true

        if (!hasFetchedTopStories) {
            viewModelScope.launch {
                topStoriesFeedRepository.syncTopStories()
            }
            savedStateHandle["hasFetchedTopStories"] = true
        }


        val hasFetchedOtherFeeds =
            savedStateHandle.get<Boolean>("hasFetchedOtherFeeds") == true

        if (!hasFetchedOtherFeeds) {
            fetchPoliticsStories()
            fetchWorldStories()
            fetchBusinessStories()
            fetchTechStories()
            fetchSportsStories()
            fetchHealthStories()
            fetchFiveThirtyEightStories()
            savedStateHandle["hasFetchedOtherFeeds"] = true
        }


    }


    /*private fun observeTopStoriesDb() {
        viewModelScope.launch {
            topStoriesFeedRepository.getTopStoriesFeedList().collect { state ->
                _topStoriesState.value = state

                if (state is ResourceState.Success && state.data.isNotEmpty()) {
                    _hasLoadedOnceTop.value = true
                }
            }
        }
    }*/


    /*fun refreshTopStories() {
        if (_isRefreshingTop.value) return

        viewModelScope.launch {
            _isRefreshingTop.value = true
            try {

                topStoriesFeedRepository.syncTopStories()

                val newItems = topStoriesFeedRepository.getNewItemsSinceLastCheck()
                if (newItems.isNotEmpty()) {
                    showFeedNotification(newItems, context)
                }
            } finally {
                _isRefreshingTop.value = false
            }
        }
    }
*/

    /*fun refreshTopStories() {
        if (_isRefreshingTop.value) return

        viewModelScope.launch {
            _isRefreshingTop.value = true
            try {
                topStoriesFeedRepository.syncTopStories()
            } finally {
                _isRefreshingTop.value = false
            }
        }
    }*/


    // --- Fetch fiveThirtyEight stories ---
    private fun observeFiveThirtyEightDb() {
        viewModelScope.launch {
          fiveThirtyEightRepository.getFiveThirtyEightFeedList()
                .collect { result ->
                    _fiveThirtyEightFeedState.value = result

                    if (result is ResourceState.Success && result.data.isNotEmpty()) {
                        _hasLoadedOnce.value = true
                    }
                }
        }
    }


    fun fetchFiveThirtyEightStories(isUserRefresh: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {

            if (isUserRefresh) {
                _isRefreshingFiveThirtyEight.value = true
            }
            try {
                // One-shot background sync
                fiveThirtyEightRepository.syncFiveThirtyEight()

            } catch (e: Exception) {
                // log if needed
            } finally {
                _isRefreshingFiveThirtyEight.value = false
            }
        }
    }





    private fun observePoliticsDb() {
        viewModelScope.launch {
            politicsFeedRepository.getPoliticsFeedList()
                .collect { result ->
                    _politicsFeedState.value = result

                    if (result is ResourceState.Success && result.data.isNotEmpty()) {
                        _hasLoadedOnce.value = true
                    }
                }
        }
    }


    fun fetchPoliticsStories(isUserRefresh: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {

            if (isUserRefresh) {
                _isRefreshingPolitics.value = true
            }
            try {
                // One-shot background sync
               politicsFeedRepository.syncPolitics()

            } catch (e: Exception) {
                // log if needed
            } finally {
                _isRefreshingPolitics.value = false
            }
        }
    }


    // --- Fetch world stories ---
    private fun observeWorldDb() {
        viewModelScope.launch {
           worldFeedRepository.getWorldFeedList()
                .collect { result ->
                    _worldFeedState.value = result

                    if (result is ResourceState.Success && result.data.isNotEmpty()) {
                        _hasLoadedOnce.value = true
                    }
                }
        }
    }



    fun fetchWorldStories(isUserRefresh: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {

            if (isUserRefresh) {
                _isRefreshingWorld.value = true
            }
            try {
                // One-shot background sync
                worldFeedRepository.syncWorld()

            } catch (e: Exception) {
                // log if needed
            } finally {
                _isRefreshingWorld.value = false
            }
        }
    }


    // --- Fetch world stories ---
    private fun observeBusinessDb() {
        viewModelScope.launch {
           businessFeedRepository.getBusinessFeedList()
                .collect { result ->
                    _businessFeedState.value = result

                    if (result is ResourceState.Success && result.data.isNotEmpty()) {
                        _hasLoadedOnce.value = true
                    }
                }
        }
    }



    fun fetchBusinessStories(isUserRefresh: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {

            if (isUserRefresh) {
                _isRefreshingBusiness.value = true
            }
            try {
                // One-shot background sync
               businessFeedRepository.syncBusiness()

            } catch (e: Exception) {
                // log if needed
            } finally {
                _isRefreshingBusiness.value = false
            }
        }
    }


    // --- Fetch tech stories ---
    private fun observeTechDb() {
        viewModelScope.launch {
           techFeedRepository.getTechFeedList()
                .collect { result ->
                    _techFeedState.value = result

                    if (result is ResourceState.Success && result.data.isNotEmpty()) {
                        _hasLoadedOnce.value = true
                    }
                }
        }
    }



    fun fetchTechStories(isUserRefresh: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {

            if (isUserRefresh) {
                _isRefreshingTech.value = true
            }
            try {
                // One-shot background sync
                techFeedRepository.syncTech()

            } catch (e: Exception) {
                // log if needed
            } finally {
                _isRefreshingTech.value = false
            }
        }
    }



    // --- Fetch Sports stories ---
    private fun observeSportsDb() {
        viewModelScope.launch {
            sportsFeedRepository.getSportsFeedList()
                .collect { result ->
                    _sportsFeedState.value = result

                    if (result is ResourceState.Success && result.data.isNotEmpty()) {
                        _hasLoadedOnce.value = true
                    }
                }
        }
    }



    fun fetchSportsStories(isUserRefresh: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {

            if (isUserRefresh) {
                _isRefreshingSports.value = true
            }
            try {
                // One-shot background sync
                sportsFeedRepository.syncSports()

            } catch (e: Exception) {
                // log if needed
            } finally {
                _isRefreshingSports.value = false
            }
        }
    }




    // --- Fetch Health stories ---
    private fun observeHealthDb() {
        viewModelScope.launch {
            healthFeedRepository.getHealthFeedList()
                .collect { result ->
                    _healthFeedState.value = result

                    if (result is ResourceState.Success && result.data.isNotEmpty()) {
                        _hasLoadedOnceHealth.value = true
                    }
                }
        }
    }



    fun fetchHealthStories(isUserRefresh: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {

            if (isUserRefresh) {
                _isRefreshingHealth.value = true
            }
            try {
                // One-shot background sync
                healthFeedRepository.syncHealth()

            } catch (e: Exception) {
                // log if needed
            } finally {
                _isRefreshingHealth.value = false
            }
        }
    }



    // --- Bookmark toggle ---
    fun toggleSave(link: String, save: Boolean) {
        viewModelScope.launch {
            topStoriesFeedRepository.toggleSave(link, save)
            politicsFeedRepository.toggleSave(link, save)
        }
    }



    // --- Saved article checks ---
    fun isArticleSaved(link: String): Flow<Boolean> {
        val topStoriesFlow = topStoriesFeedRepository.isArticleSaved(link)
        val politicsFlow = politicsFeedRepository.isArticleSaved(link)

        return combine(topStoriesFlow, politicsFlow) { topSaved, polSaved ->
            topSaved || polSaved
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)
    }



    fun getSavedArticles(): Flow<List<FeedItem>> {
        val topStoriesSaved = topStoriesFeedRepository.getSavedArticles()
        val politicsSaved = politicsFeedRepository.getSavedArticles()


        return combine(topStoriesSaved, politicsSaved) { topList, polList ->
            (topList + polList).distinctBy { it.link }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())
    }






}









