package com.gen.remotesync


import com.gen.remotesync.data.RepositoryImpl
import com.gen.remotesync.data.database.Download
import com.gen.remotesync.data.database.DownloadDao
import com.gen.remotesync.data.downloadmanager.RemoteDownloadManager
import com.gen.remotesync.domain.Repository
import com.gen.remotesync.model.DownloadFile
import com.gen.remotesync.model.DownloadState
import com.gen.remotesync.model.DownloadingState
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.junit.Test

import org.junit.Before
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
internal class RepositoryTest {

    private lateinit var repository: Repository
    private val downloadManager = mock<RemoteDownloadManager>()
    private val downloadDao = mock<DownloadDao>()
    private val testCoroutineDispatcher = TestCoroutineDispatcher()

    @Before
    fun init() {
        repository = RepositoryImpl(downloadManager, downloadDao)
    }

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testCoroutineDispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
        testCoroutineDispatcher.cleanupTestCoroutines()
    }

    @Test
    fun `Should call the download manager to start the download`() {
        testCoroutineDispatcher.runBlockingTest {
            val url = "Test Url"
            val interval = 30
            val file = DownloadFile()
            whenever(downloadManager.download(url)).thenReturn(DownloadState.Started(file))
            repository.download(url, interval)
            verify(downloadManager, times(1)).download(url)
        }
    }

    @Test
    fun `Should get the downloaded files from repo download manager`() {
        testCoroutineDispatcher.runBlockingTest {
            val downloads = listOf(DownloadFile(), DownloadFile(), DownloadFile())
            whenever(downloadManager.getDownloadedFiles()).thenReturn(downloads)
            val files = repository.getDownloadedFiles()
            verify(downloadManager, times(1)).getDownloadedFiles()
            assert(downloads == files)
        }
    }

    @Test
    fun `Should get the downloading state from repo layer`() {
        testCoroutineDispatcher.runBlockingTest {
            val downloadId = 100L
            val downloadDao = mock<DownloadDao>()
            val downloadManager = FakeDownloadManager()
            val repository = RepositoryImpl(downloadManager, downloadDao)
            val downloadingState = mutableListOf<DownloadingState>()

            val jobs = launch {
                repository.getProgress(downloadId).collect{
                    downloadingState.add(it)
                }
            }
            downloadManager.emit(DownloadingState.Unknown)
            downloadManager.emit(DownloadingState.NotStarted)
            downloadManager.emit(DownloadingState.Downloading(10f, DownloadFile()))
            jobs.cancel()
            assert(downloadingState.size == 3)
            assert(downloadingState[0] is DownloadingState.Unknown)
            assert(downloadingState[1] is DownloadingState.NotStarted)
            assert(downloadingState[2] is DownloadingState.Downloading)
        }
    }

    @Test
    fun `Should open the download files using download manager`() {
        testCoroutineDispatcher.runBlockingTest {
            val fileName = "Test"
            repository.openFile(fileName)
            verify(downloadManager, times(1)).openFile(fileName)

        }
    }

    @Test
    fun `Should add the download entry using download database`() {
        testCoroutineDispatcher.runBlockingTest {
            val downloadId = 100L
            val url = "url"
            val syncIntervalInMins = 100
            val lastSyncTimeInMs = 1000L
            val lastUpdateTimeInMs = 3000L
            repository.addDownloadToDB(downloadId, url, syncIntervalInMins, lastSyncTimeInMs, lastUpdateTimeInMs)
            verify(downloadDao, times(1)).insertAll(any())

        }
    }

    @Test
    fun `Should update the last download sync time using download database`() {
        testCoroutineDispatcher.runBlockingTest {
            val downloadId = 100L
            val lastSyncTimeInMs = 1000L
            val lastUpdateTimeInMs = 3000L
            repository.updateLastSyncTime(downloadId,lastSyncTimeInMs, lastUpdateTimeInMs)
            verify(downloadDao, times(1)).updateLastSyncTime(downloadId,lastSyncTimeInMs, lastUpdateTimeInMs)

        }
    }

    @Test
    fun `Should return valid download state if download exist for given URL`() {
        testCoroutineDispatcher.runBlockingTest {
            val downloadUrl = "url"
            val download = Download(
                downloadId = 1000L,
                downloadUrl = "url",
                syncIntervalInMins = 30,
                lastSyncTimeInMs = 100L,
                lastUpdateTimeInMs = 1000L

            )
            whenever(downloadDao.getDownloadsByUrl(downloadUrl)).thenReturn(listOf(download))
            whenever(downloadManager.getDownloadState(download.downloadId)).thenReturn(DownloadingState.Downloading(10f, DownloadFile()))
            val state = repository.getDownloadState(downloadUrl)

            verify(downloadDao, times(1)).getDownloadsByUrl(downloadUrl)
            verify(downloadManager, times(1)).getDownloadState(download.downloadId)
            assert(state is DownloadingState.Downloading)

        }
    }

    @Test
    fun `Should return the null state if download entry not exist in the data base for given URL`() {
        testCoroutineDispatcher.runBlockingTest {
            val downloadUrl = "url"
            val download = Download(
                downloadId = 1000L,
                downloadUrl = "url",
                syncIntervalInMins = 30,
                lastSyncTimeInMs = 100L,
                lastUpdateTimeInMs = 1000L

            )
            whenever(downloadDao.getDownloadsByUrl(downloadUrl)).thenReturn(null)
            whenever(downloadManager.getDownloadState(download.downloadId)).thenReturn(DownloadingState.Downloading(10f, DownloadFile()))
            val state = repository.getDownloadState(downloadUrl)

            verify(downloadDao, times(1)).getDownloadsByUrl(downloadUrl)
            verify(downloadManager, times(0)).getDownloadState(download.downloadId)
            assertNull(state)

        }
    }

}

