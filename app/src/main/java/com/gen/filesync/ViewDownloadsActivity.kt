package com.gen.filesync

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gen.filesync.ui.theme.FileSyncTheme

class ViewDownloadsActivity : ComponentActivity() {

    private val viewModel by viewModels<DownloadViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            FileSyncTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ListDownloads(viewModel)
                }
            }
        }

        viewModel.getListOfDownloads()

    }

    @Composable
    fun ListDownloads(viewModel: DownloadViewModel, modifier: Modifier = Modifier) {

        val filesState = viewModel.downloads.collectAsState()
        val filesList by remember {
            mutableStateOf(filesState.value)
        }
        Column(Modifier.padding(24.dp)) {
            for (i in 0.. filesList.size-1) {
                DownloadItem(filesList[i].fileName, filesList[i].fileUrl, viewModel)
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }

    @Composable
    fun DownloadItem(itemName: String, url: String, viewModel: DownloadViewModel) {

        ClickableText(
            text = AnnotatedString(itemName) ,
            style = TextStyle(
                color = Color.Blue,
                fontSize = 20.sp,
            ),
            onClick = {
                viewModel.openFile(url)
            }
        )
    }


}