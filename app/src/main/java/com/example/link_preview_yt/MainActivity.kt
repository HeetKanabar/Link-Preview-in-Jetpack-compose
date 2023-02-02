package com.example.link_preview_yt

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.link_preview_yt.ui.theme.CardBackground
import com.squareup.picasso.Picasso
import org.jsoup.Jsoup
import java.util.concurrent.Executors

class MainActivity : ComponentActivity() {

    var linkPreview: LinkPreview? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

            Column(modifier = Modifier.fillMaxSize()) {


                var searchQuery by remember {
                    mutableStateOf("")
                }

                var linkp by remember {
                    mutableStateOf(linkPreview)
                }

                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth()
                )

                Button(onClick = {

                    var linkpw = LinkPreview()
                    val executer = Executors.newSingleThreadExecutor()


                    executer.execute {

                        try {

                            val response = Jsoup.connect(searchQuery)
                                .execute()

                            val docs = response.parse().getElementsByTag("meta")

                            for (element in docs) {

                                when {

                                    element.attr("property").equals("og:image") -> {
                                        val bitmap: Bitmap? =
                                            Picasso.get().load(element.attr("content")).get()
                                        linkpw.img = bitmap
                                    }

                                    element.attr("property").equals("og:title") -> {
                                        linkpw.title = element.attr("content").toString()
                                    }
                                    element.attr("property").equals("og:description") -> {
                                        linkpw.description = element.attr("content").toString()
                                    }
                                    element.attr("property").equals("og:url") -> {
                                        linkpw.url = element.attr("content").toString()

                                    }

                                }

                            }


                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                        linkp = linkpw
                    }


                }) {
                    Text(text = "Get Preview")
                }

                DrawPreview(linkPreview = linkp)

            }


        }
    }

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    fun DrawPreview(linkPreview: LinkPreview?) {

        linkPreview?.let {

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                backgroundColor = CardBackground,
                shape = RoundedCornerShape(16.dp),
                onClick = {
                    val intent = Intent()
                    intent.action = Intent.ACTION_VIEW
                    intent.data = Uri.parse(linkPreview.url)
                    startActivity(intent)
                }
            ) {

                Spacer(modifier = Modifier.height(10.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                ) {

                    it.img?.let {

                        Image(
                            bitmap = it.asImageBitmap(),
                            "Image",
                            contentScale = ContentScale.FillWidth
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    it.title?.let {
                        Text(text = it, color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    it.description?.let {
                        Text(text = it, maxLines = 2)
                    }
                }
            }
        }
    }
}
