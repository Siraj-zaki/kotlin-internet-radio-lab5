package com.example.k2025_03_25_basic_radio

import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
//import coil.compose.AsyncImage
import com.example.k2025_03_25_basic_radio.ui.theme.K2025_03_25_basic_radioTheme

class MainActivity : ComponentActivity() {
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var handlerThread: HandlerThread
    private lateinit var handler: Handler
    private var isPlaying = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handlerThread = HandlerThread("RadioThread").also { it.start() }
        handler = Handler(handlerThread.looper)

        setContent {
            K2025_03_25_basic_radioTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    var volume by remember { mutableFloatStateOf(1.0f) }
                    var currentUrl by remember { mutableStateOf<String?>(null) }

                    RadioUI(
                        currentUrl = currentUrl,
                        isPlaying = isPlaying,
                        onStationSelected = { url ->
                            handler.post {
                                if (currentUrl == url && isPlaying) {
                                    mediaPlayer?.pause()
                                    isPlaying = false
                                } else {
                                    playStream(url, volume)
                                    currentUrl = url
                                    isPlaying = true
                                }
                            }
                        },
                        onVolumeChanged = {
                            volume = it
                            mediaPlayer?.setVolume(it, it)
                        }
                    )
                }
            }
        }
    }

    private fun playStream(url: String, volume: Float) {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()

            mediaPlayer = MediaPlayer().apply {
                setAudioStreamType(AudioManager.STREAM_MUSIC)
                setDataSource(url)
                prepare()
                setVolume(volume, volume)
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        handlerThread.quitSafely()
    }
}

@Composable
fun RadioUI(
    currentUrl: String?,
    isPlaying: Boolean,
    onStationSelected: (String) -> Unit,
    onVolumeChanged: (Float) -> Unit
) {
    val stations = listOf(
        RadioStation("Radio Paradise", "https://stream.radioparadise.com/mp3-192", "https://placekitten.com/200/200"),
        RadioStation("Jazz24", "https://live.wostreaming.net/direct/ppm-jazz24mp3-ibc1", "https://placekitten.com/201/200"),
        RadioStation("BBC Radio 1", "http://stream.live.vc.bbcmedia.co.uk/bbc_radio_one", "https://placekitten.com/202/200"),
        RadioStation("KEXP", "https://kexp-mp3-128.streamguys1.com/kexp128.mp3", "https://placekitten.com/203/200"),
        RadioStation("NPR News", "https://npr-ice.streamguys1.com/live.mp3", "https://placekitten.com/204/200"),
        RadioStation("Classical KUSC", "https://kusc.streamguys1.com/kusc128.mp3", "https://placekitten.com/205/200"),
        RadioStation("Radio Swiss Pop", "http://stream.srg-ssr.ch/m/pop/mp3_128", "https://placekitten.com/206/200"),
        RadioStation("Chilltrax", "https://ais-sa1.streamon.fm/7117_128k.aac", "https://placekitten.com/207/200"),
        RadioStation("Deep House Lounge", "http://198.58.98.83:8356/stream", "https://placekitten.com/208/200"),
        RadioStation("Dance Wave Retro", "https://stream.dancewave.online/retro", "https://placekitten.com/209/200")
    )

    Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        Text("Select a Station", fontSize = 20.sp, modifier = Modifier.padding(8.dp))
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(stations) { station ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onStationSelected(station.url) }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
//                    AsyncImage(
//                        model = station.imageUrl,
//                        contentDescription = station.name,
//                        modifier = Modifier.size(64.dp)
//                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(text = station.name, style = MaterialTheme.typography.bodyLarge)
                        if (currentUrl == station.url && isPlaying) {
                            Text("Playing", style = MaterialTheme.typography.bodySmall)
                        } else if (currentUrl == station.url) {
                            Text("Paused", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text("Volume", modifier = Modifier.align(Alignment.CenterHorizontally))
        Slider(
            value = 1.0f,
            onValueChange = onVolumeChanged,
            valueRange = 0f..1f,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

data class RadioStation(val name: String, val url: String, val imageUrl: String)
