package com.firmannurcahyo.submission.frontend

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.AsyncPagingDataDiffer
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.recyclerview.widget.ListUpdateCallback
import com.firmannurcahyo.submission.MainDispatcherRule
import com.firmannurcahyo.submission.database.datamodel.BaseResponse
import com.firmannurcahyo.submission.database.datamodel.StoriesDatabase
import com.firmannurcahyo.submission.database.datamodel.StoriesResponse
import com.firmannurcahyo.submission.database.datamodel.UserPreferences
import com.firmannurcahyo.submission.database.datapaging.StoryAdapter
import com.firmannurcahyo.submission.database.datapaging.StoryRepository
import com.firmannurcahyo.submission.frontend.StoriesViewModel
import com.firmannurcahyo.submission.frontend.authentication.Resource
import com.firmannurcahyo.submission.getOrAwaitValue
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class StoriesViewModelTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule
    val mainDispatcherRules = MainDispatcherRule()

    @Mock
    private lateinit var storyRepository: StoryRepository

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        var storiesViewModel = MainViewModel(storyRepository)
    }

    @Test
    fun `Not Null and Return Data`() = runTest {
        val dummyStory = generateDummyStories()
        val data: PagingData<StoriesDatabase> = StoriesPagingSource.snapshot(dummyStory.listStory)
        val expectedQuote = MutableLiveData<PagingData<StoriesDatabase>>()
        expectedQuote.value = data
        Mockito.`when`(storyRepository.getStories()).thenReturn(expectedQuote)

        val storiesViewModel = MainViewModel(storyRepository)
        val actualQuote: PagingData<StoriesDatabase> = storiesViewModel.getStories().getOrAwaitValue()

        val differ = AsyncPagingDataDiffer(
            diffCallback = StoryAdapter.DIFF_CALLBACK,
            updateCallback = noopListUpdateCallback,
            workerDispatcher = Dispatchers.Main,
        )
        differ.submitData(actualQuote)

        assertNotNull(differ.snapshot())
        assertEquals(dummyStory.listStory.size, differ.snapshot().size)
        assertEquals(dummyStory.listStory[0], differ.snapshot()[0])
    }

    @Test
    fun `Empty Should Return No Data`() = runTest {
        val data: PagingData<StoriesDatabase> = PagingData.from(emptyList())
        val expectedQuote = MutableLiveData<PagingData<StoriesDatabase>>()
        expectedQuote.value = data
        Mockito.`when`(storyRepository.getStories()).thenReturn(expectedQuote)
        val storiesViewModel = MainViewModel(storyRepository)
        val actualQuote: PagingData<StoriesDatabase> = storiesViewModel.getStories().getOrAwaitValue()
        val differ = AsyncPagingDataDiffer(
            diffCallback = StoryAdapter.DIFF_CALLBACK,
            updateCallback = noopListUpdateCallback,
            workerDispatcher = Dispatchers.Main,
        )
        differ.submitData(actualQuote)
        assertEquals(0, differ.snapshot().size)
    }
}

class StoriesPagingSource : PagingSource<Int, LiveData<List<StoriesDatabase>>>() {
    companion object {
        fun snapshot(items: List<StoriesDatabase>): PagingData<StoriesDatabase> {
            return PagingData.from(items)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, LiveData<List<StoriesDatabase>>>): Int {
        return 0
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, LiveData<List<StoriesDatabase>>> {
        return LoadResult.Page(emptyList(), 0, 1)
    }
}

val noopListUpdateCallback = object : ListUpdateCallback {
    override fun onInserted(position: Int, count: Int) {}
    override fun onRemoved(position: Int, count: Int) {}
    override fun onMoved(fromPosition: Int, toPosition: Int) {}
    override fun onChanged(position: Int, count: Int, payload: Any?) {}
}

fun generateDummyStories(): StoriesResponse {
    return StoriesResponse(
        error = false,
        message = "success",
        listStory = arrayListOf(
            StoriesDatabase(
                id = "id",
                name = "name",
                description = "description",
                photoUrl = "photoUrl",
                createdAt = "createdAt",
                lat = 0.01,
                lon = 0.01
            )
        )
    )
}