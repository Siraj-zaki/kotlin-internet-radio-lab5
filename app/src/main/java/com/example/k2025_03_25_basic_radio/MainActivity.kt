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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import com.example.k2025_03_25_basic_radio.ui.theme.K2025_03_25_basic_radioTheme

class MainActivity : ComponentActivity() {
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var handlerThread: HandlerThread
    private lateinit var handler: Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handlerThread = HandlerThread("RadioThread").also { it.start() }
        handler = Handler(handlerThread.looper)

        setContent {
            K2025_03_25_basic_radioTheme {
                var currentStation by remember { mutableStateOf<RadioStation?>(null) }
                var isPlaying by remember { mutableStateOf(false) }
                var volume by remember { mutableFloatStateOf(0.5f) }

                RadioUI(
                    currentStation = currentStation,
                    isPlaying = isPlaying,
                    volume = volume,
                    onStationSelected = { station ->
                        handler.post {
                            if (currentStation?.url == station.url && isPlaying) {
                                pauseRadio()
                                isPlaying = false
                            } else {
                                playRadio(station, volume)
                                currentStation = station
                                isPlaying = true
                            }
                        }
                    },
                    onVolumeChanged = { newVolume ->
                        volume = newVolume
                        handler.post {
                            mediaPlayer?.setVolume(newVolume, newVolume)
                        }
                    },
                    onPlayPauseToggle = {
                        handler.post {
                            if (isPlaying) {
                                pauseRadio()
                            } else {
                                currentStation?.let { station ->
                                    playRadio(station, volume)
                                }
                            }
                            isPlaying = !isPlaying
                        }
                    }
                )
            }
        }
    }

    private fun playRadio(station: RadioStation, volume: Float) {
        try {
            mediaPlayer?.release()

            mediaPlayer = MediaPlayer().apply {
                setAudioStreamType(AudioManager.STREAM_MUSIC)
                setDataSource(station.url)
                prepareAsync()
                setOnPreparedListener {
                    setVolume(volume, volume)
                    start()
                }
                setOnErrorListener { _, what, extra ->
                    android.util.Log.e("MediaPlayer", "Error: what=$what, extra=$extra")
                    false
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun pauseRadio() {
        mediaPlayer?.pause()
    }

    override fun onPause() {
        super.onPause()
        mediaPlayer?.pause()
    }

    override fun onResume() {
        super.onResume()
        // We don't need to check currentStation here since the MediaPlayer
        // will be null if no station is selected
        mediaPlayer?.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
        handlerThread.quitSafely()
    }
}

@Composable
fun RadioUI(
    currentStation: RadioStation?,
    isPlaying: Boolean,
    volume: Float,
    onStationSelected: (RadioStation) -> Unit,
    onVolumeChanged: (Float) -> Unit,
    onPlayPauseToggle: () -> Unit
) {
    val stations = listOf(
        RadioStation(
            "Radio Paradise",
            "https://stream.radioparadise.com/mp3-192",
            "https://yt3.googleusercontent.com/1-sb7SOHP7VCoEOhMhzJXX38QPzPjAdkA-4b8e3-cahWl5nDgSiG3YRnmm6ZXpWIRHqf5s4u=s900-c-k-c0x00ffffff-no-rj"
        ),
        RadioStation(
            "Jazz24",
            "https://live.wostreaming.net/direct/ppm-jazz24mp3-ibc1",
            "https://npr.brightspotcdn.com/dims4/default/032ce25/2147483647/strip/true/crop/900x900+0+0/resize/1760x1760!/format/webp/quality/90/?url=http%3A%2F%2Fnpr-brightspot.s3.amazonaws.com%2F98%2Ff7%2F48229ba341b0b1c8933834130d10%2Fjazz24.jpg"
        ),
        RadioStation(
            "BBC Radio 1",
            "http://stream.live.vc.bbcmedia.co.uk/bbc_radio_one",
            "https://ichef.bbci.co.uk/images/ic/1200x675/p0cbjbk8.jpg"
        ),
        RadioStation(
            "KEXP",
            "https://kexp-mp3-128.streamguys1.com/kexp128.mp3",
            "https://play-lh.googleusercontent.com/0LcDI5r90wP77ANQcXHl0gjg1GecYSBqcCKFsFxt1nq89EOXbw820UMaFW2z66RPIm4f"
        ),
        RadioStation(
            "NPR News",
            "https://npr-ice.streamguys1.com/live.mp3",
            "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSIVtlLTLsHc7I9jONXGElH-xgurhSFXeL0FA&s"
        ),
        RadioStation(
            "Classical KUSC",
            "https://kusc.streamguys1.com/kusc128.mp3",
            "https://play-lh.googleusercontent.com/T5tfBCj39SESBTj0Ot9KnbmsBcbpYBonJF7OflclVaoRocl6D0CO7osaijQlbLyKDPfP"
        ),
        RadioStation(
            "Radio Swiss Pop",
            "http://stream.srg-ssr.ch/m/pop/mp3_128",
            "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQJ3xidGVroH0sMeqv9KJ-k5cANPhA90QfVGQ&s"
        ),
        RadioStation(
            "Chilltrax",
            "https://ais-sa1.streamon.fm/7117_128k.aac",
            "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSs-922hj_HGmvrCtBb36dWSleU5JsnKiIOsQ&s"
        ),
        RadioStation(
            "Deep House Lounge",
            "http://198.58.98.83:8356/stream",
            "https://i1.sndcdn.com/avatars-000196423494-ggyazv-t1080x1080.jpg"
        ),
        RadioStation(
            "Dance Wave Retro",
            "https://stream.dancewave.online/retro",
            "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRb-tlvvRh5qtg9z8ywEpoXwxld_RjkbJkWuA&s"
        )
    )

    Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        // Now Playing section
        currentStation?.let { station ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("NOW PLAYING", style = MaterialTheme.typography.labelSmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    AsyncImage(
                        model = station.imageUrl,
                        contentDescription = station.name,
                        modifier = Modifier.size(120.dp),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = station.name,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    IconButton(
                        onClick = onPlayPauseToggle,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.ArrowBack else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }
        }

        // Volume control
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = "Volume",
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Slider(
                value = volume,
                onValueChange = onVolumeChanged,
                valueRange = 0f..1f,
                modifier = Modifier.weight(1f)
            )
        }

        // Station list
        Text(
            "Available Stations",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(8.dp)
        )
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(stations) { station ->
                StationItem(
                    station = station,
                    isSelected = currentStation?.url == station.url,
                    isPlaying = isPlaying && currentStation?.url == station.url,
                    onClick = { onStationSelected(station) }
                )
            }
        }
    }
}

@Composable
fun StationItem(
    station: RadioStation,
    isSelected: Boolean,
    isPlaying: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
    val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor,
            contentColor = contentColor
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = station.imageUrl,
                contentDescription = station.name,
                modifier = Modifier.size(56.dp),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = station.name,
                    style = MaterialTheme.typography.bodyLarge
                )
                if (isSelected) {
                    Text(
                        text = if (isPlaying) "▶ Playing" else "⏸ Paused",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

data class RadioStation(val name: String, val url: String, val imageUrl: String)