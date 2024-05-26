package com.gen.remotesync

import android.content.Context
import com.gen.remotesync.data.RepositoryImpl
import com.gen.remotesync.data.database.DownloadDao
import com.gen.remotesync.data.downloadmanager.RemoteDownloadManager
import com.gen.remotesync.domain.Repository
import com.gen.remotesync.domain.UseCase
import com.gen.remotesync.domain.UseCaseImpl
import com.gen.remotesync.model.DownloadFile
import com.gen.remotesync.model.DownloadState
import com.gen.remotesync.model.DownloadingState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Test

import org.junit.Assert.*
import org.junit.Before
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
internal class UseCaseTest {

    private lateinit var useCase: UseCase
    private val repository = mock<Repository>()
    private val context = mock<Context>()
    private val testCoroutineDispatcher = TestCoroutineDispatcher()

    @Before
    fun init() {
        useCase = UseCaseImpl(repository, context)
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
    fun `Should add the entry to database after download initiated successfully`() {
        testCoroutineDispatcher.runBlockingTest {
            val url = "Test Url"
            val interval = 30
            val file = DownloadFile()
            whenever(repository.download(url, interval)).thenReturn(DownloadState.Started(file))
            useCase.download(url, interval)
            verify(repository, times(1)).download(url, interval)
            verify(repository, times(1)).addDownloadToDB(file.id, url, interval, 0,0)
        }
    }

    @Test
    fun `Should not add the entry to database if download not initiated successfully`() {
        testCoroutineDispatcher.runBlockingTest {
            val url = "Test Url"
            val interval = 30
            val file = DownloadFile()
            whenever(repository.download(url, interval)).thenReturn(DownloadState.Failed(""))
            useCase.download(url, interval)
            verify(repository, times(1)).download(url, interval)
            verify(repository, times(0)).addDownloadToDB(file.id, url, interval, 0,0)
        }
    }

    @Test
    fun `Should get the downloaded files from repo layer`() {
        testCoroutineDispatcher.runBlockingTest {
            val downloads = listOf(DownloadFile(), DownloadFile(), DownloadFile())
            whenever(repository.getDownloadedFiles()).thenReturn(downloads)
            val files = useCase.getDownloadedFiles()
            verify(repository, times(1)).getDownloadedFiles()
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
            val useCase = UseCaseImpl(repository, context)
            val downloadingState = mutableListOf<DownloadingState>()

            val jobs = launch {
                useCase.getProgress(downloadId).collect{
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
    fun `Should open the download files from repo layer`() {
        testCoroutineDispatcher.runBlockingTest {
            val fileName = "Test"
            useCase.openFile(fileName)
            verify(repository, times(1)).openFile(fileName)

        }
    }
    @Test
    fun `Should return the valid downloading state if download already started or initiated`() {
        testCoroutineDispatcher.runBlockingTest {
            val fileName = "Test"
            val downloadFile = DownloadFile()
            whenever(repository.getDownloadState(fileName)).thenReturn(DownloadingState.Downloading(10f, downloadFile))
            val state = useCase.getDownloadState(fileName)
            verify(repository, times(1)).getDownloadState(fileName)
            assert(state is DownloadingState.Downloading)
            assert((state as DownloadingState.Downloading).progress == 10f)
            assert(state.file == downloadFile)
        }
    }

    @Test
    fun `Should return the download not started state if download not initiated`() {
        testCoroutineDispatcher.runBlockingTest {
            val fileName = "Test"
            whenever(repository.getDownloadState(fileName)).thenReturn(null)
            val state = useCase.getDownloadState(fileName)
            verify(repository, times(1)).getDownloadState(fileName)
            assert(state is DownloadingState.NotStarted)
        }
    }
}

class FakeDownloadManager : RemoteDownloadManager {

    private val downloadingState = MutableSharedFlow<DownloadingState>()

    suspend fun emit(state: DownloadingState) = downloadingState.emit(state)
    override fun download(url: String): DownloadState {
        TODO("Not yet implemented")
    }


    override fun getProgress(downloadingId: Long): Flow<DownloadingState> {
        return downloadingState
    }

    override fun getDownloadedFiles(): List<DownloadFile> {
        TODO("Not yet implemented")
    }

    override fun openFile(fileName: String) {
        TODO("Not yet implemented")
    }

    override fun deleteDownloadById(downloadId: Long) {
        TODO("Not yet implemented")
    }

    override fun getLastUpdatedTimeStamp(downloadId: Long): Long {
        TODO("Not yet implemented")
    }

    override fun getDownloadState(downloadId: Long): DownloadingState {
        TODO("Not yet implemented")
    }
}