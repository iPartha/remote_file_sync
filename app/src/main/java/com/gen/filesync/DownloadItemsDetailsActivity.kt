package com.gen.filesync


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.gen.filesync.Constants.Companion.KEY_DOWNLOAD_URL
import com.gen.filesync.ui.theme.FileSyncTheme
import com.gen.remotesync.model.DownloadFile
import com.gen.remotesync.model.DownloadState
import com.gen.remotesync.model.DownloadingState
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch


class DownloadItemsDetailsActivity : ComponentActivity() {

    private val viewModel by viewModels<DownloadItemDetailsViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val url = intent.extras?.run {
            getString(KEY_DOWNLOAD_URL)
        } ?: kotlin.run {
            ""
        }

        setContent {
            FileSyncTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ItemsView(url, viewModel)
                }
            }
        }

    }

    @Composable
    private fun ItemsView(downloadUrl: String, viewModel: DownloadItemDetailsViewModel) {

        viewModel.isDownloaded(downloadUrl)

        val uiState = viewModel.downloads.collectAsState(UIState.Loading)

        if (uiState.value is UIState.Loading) {

            Box(
                modifier = Modifier
                    .size(76.dp)
                    .background(
                        color = Color.White,
                        shape = RoundedCornerShape(4.dp)
                    )
            ) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(
                            Alignment.Center
                        ),
                    color = Color.Red
                )
            }

        } else if (uiState.value is UIState.Success) {

            val lifecycleScope = rememberCoroutineScope()

            val syncIntervalOption = viewModel.getAvailableSyncInterval()
            val (selectedOption, onOptionSelected) = remember { mutableStateOf(syncIntervalOption[0]) }

            Column(Modifier.padding(24.dp)) {
                Image( // The Image component to load the image with the Coil library
                    painter = rememberImagePainter(data = "https://picsum.photos/500/600"),
                    contentDescription = null,
                    modifier = Modifier
                        .size(500.dp, 500.dp)
                        .clickable {

                        }
                )

                Spacer(modifier = Modifier.height(14.dp))

                DownloadButton(downloadUrl = downloadUrl, viewModel, selectedOption, (uiState.value as UIState.Success).data as DownloadingState)

                Spacer(modifier = Modifier.height(14.dp))

                Text(text = "Select download sync interval")

                Spacer(modifier = Modifier.height(4.dp))

                Column(Modifier.selectableGroup()) {
                    syncIntervalOption.forEach { text ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .selectable(
                                    selected = (text == selectedOption),
                                    onClick = { onOptionSelected(text) },
                                    role = Role.RadioButton
                                )
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (text == selectedOption),
                                onClick = null
                            )
                            Text(
                                text = text,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(start = 16.dp)
                            )
                        }
                    }
                }
            }

        }

    }


    @Composable
    private fun DownloadButton(downloadUrl: String, viewModel: DownloadItemDetailsViewModel, syncInterval: String, state: DownloadingState) {

        var downloadingState by remember {
            mutableStateOf(state)
        }

        val lifecycleScope = rememberCoroutineScope()
        if (downloadingState is DownloadingState.Downloading) {
            LaunchedEffect(key1 = downloadingState) {
                lifecycleScope.launch {
                    viewModel.getDownloadingState((downloadingState as DownloadingState.Downloading).file.id).cancellable().collect{
                        downloadingState = it
                    }

                }
            }
        }

        if (downloadingState is DownloadingState.Completed || downloadingState is DownloadingState.NotStarted) {
            Button(
                {
                    if (downloadingState is DownloadingState.NotStarted) {
                        val downloadState = viewModel.download(url = downloadUrl, syncInterval)
                        if (downloadState is DownloadState.Started) {
                            val downloadId = downloadState.file.id
                            lifecycleScope.launch {
                                viewModel.getDownloadingState(downloadId).cancellable().collect {state->
                                    downloadingState = state
                                }
                            }

                        }
                    } else {
                        viewModel.openFile((downloadingState as DownloadingState.Completed).file.fileUrl)
                    }

                },
                modifier = Modifier.fillMaxWidth(),) {
                if (downloadingState is DownloadingState.Completed) {
                    Text("Play")
                } else{
                    Text("Download")
                }
            }
        } else if (downloadingState is DownloadingState.Downloading){
            ProgressBar(
                (downloadingState as DownloadingState.Downloading).progress/100,
                Modifier
                    .fillMaxWidth()
                    .height(20.dp))
        }

    }

    @Composable
    fun ProgressBar(progress: Float, modifier: Modifier = Modifier) {
        LinearProgressIndicator(
            progress = progress,
            modifier = modifier,
        )
    }
}