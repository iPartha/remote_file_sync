package com.gen.filesync

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import com.gen.filesync.ui.theme.FileSyncTheme

class MainActivity : ComponentActivity() {

    private val filesList = listOf<String>("https://sample-videos.com/video321/mp4/720/big_buck_bunny_720p_30mb.mp4",
        "https://sample-videos.com/video321/mp4/480/big_buck_bunny_480p_30mb.mp4",
        "https://sample-videos.com/video321/mp4/720/big_buck_bunny_720p_30mb.mp4",
        "https://sample-videos.com/video321/mp4/480/big_buck_bunny_480p_30mb.mp4",
        "https://sample-videos.com/video321/mp4/720/big_buck_bunny_720p_30mb.mp4",
        "https://sample-videos.com/video321/mp4/480/big_buck_bunny_480p_30mb.mp4",
        "https://sample-videos.com/video321/mp4/720/big_buck_bunny_720p_30mb.mp4",
        "https://sample-videos.com/video321/mp4/480/big_buck_bunny_480p_30mb.mp4",
        "https://sample-videos.com/video321/mp4/720/big_buck_bunny_720p_30mb.mp4",
        "https://sample-videos.com/video321/mp4/480/big_buck_bunny_480p_30mb.mp4",
        "https://sample-videos.com/video321/mp4/720/big_buck_bunny_720p_30mb.mp4",
        "https://sample-videos.com/video321/mp4/480/big_buck_bunny_480p_30mb.mp4",
        "https://sample-videos.com/video321/mp4/720/big_buck_bunny_720p_30mb.mp4",
        "https://sample-videos.com/video321/mp4/480/big_buck_bunny_480p_30mb.mp4")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

         setContent {
            FileSyncTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {

                    DownloadItems(filesList)

                }
            }

        }
    }
}

@Composable
fun DownloadItems(downloadList : List<String>) {

    val context = LocalContext.current

    Column() {
        Row(Modifier.padding(16.dp)) {
            Text(
                "Available for downloads",
                style = TextStyle(
                    fontSize = 16.sp,
                )
                )
            Spacer(Modifier.weight(1f))
            ClickableText(
                text = AnnotatedString("View Downloads") ,
                style = TextStyle(
                    color = Color.Blue,
                    fontSize = 16.sp,
                ),
                onClick = {
                   Intent(context, ViewDownloadsActivity::class.java).run {
                       context.startActivity(this)
                   }
                }
            )
        }

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 128.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(downloadList) { index, item ->

                    Image( // The Image component to load the image with the Coil library
                        painter = rememberImagePainter(data = "https://picsum.photos/200/300"),
                        contentDescription = null,
                        modifier = Modifier.size(200.dp, 200.dp).clickable {
                            Intent(context, DownloadItemsDetailsActivity::class.java).run {
                                putExtra(Constants.KEY_DOWNLOAD_URL, item)
                                context.startActivity(this)
                            }
                        }
                    )

            }
        }

    }

}

