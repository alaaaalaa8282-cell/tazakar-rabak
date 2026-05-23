package com.alaaeltaweel.thikrallah.presentation.screen.radio

import android.media.AudioManager
import android.media.MediaPlayer
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun RadioScreen(
    onBackClick: () -> Unit = {},
    viewModel: RadioViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var currentPlayingId by remember { mutableIntStateOf(-1) }

    LaunchedEffect(state.channels) {
        val (url, _) = viewModel.consumePlayUrl() ?: return@LaunchedEffect
        val id = state.channels.firstOrNull { it.streamUrl == url }?.id ?: return@LaunchedEffect

        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        currentPlayingId = id

        val mp = MediaPlayer().apply {
            @Suppress("DEPRECATION")
            setAudioStreamType(AudioManager.STREAM_MUSIC)
            setDataSource(url)
            setOnPreparedListener {
                it.start()
                viewModel.onChannelReady(id)
            }
            setOnErrorListener { _, _, _ ->
                viewModel.onPlayerError()
                true
            }
            prepareAsync()
        }
        mediaPlayer = mp
    }

    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "إذاعات القرآن الكريم",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1B5E20),
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            items(state.channels, key = { it.id }) { channel ->
                RadioChannelItem(
                    channel = channel,
                    onPlayClick = { viewModel.onPlayClick(channel.id) },
                    onPauseClick = {
                        mediaPlayer?.stop()
                        mediaPlayer?.release()
                        mediaPlayer = null
                        currentPlayingId = -1
                        viewModel.onPauseClick(channel.id)
                    }
                )
                HorizontalDivider(color = Color(0xFFE0E0E0))
            }
        }
    }
}

@Composable
private fun RadioChannelItem(
    channel: RadioUiState.RadioChannelUiState,
    onPlayClick: () -> Unit,
    onPauseClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { if (channel.isPlaying) onPauseClick() else onPlayClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Box(
            modifier = Modifier.size(40.dp),
            contentAlignment = Alignment.Center
        ) {
            when {
                channel.isLoading -> CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color(0xFF1B5E20),
                    strokeWidth = 2.dp
                )
                channel.isPlaying -> IconButton(onClick = onPauseClick) {
                    Icon(Icons.Default.Pause, contentDescription = "إيقاف", tint = Color(0xFF1B5E20))
                }
                else -> IconButton(onClick = onPlayClick) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "تشغيل", tint = Color(0xFF1B5E20))
                }
            }
        }

        Text(
            text = channel.nameAr,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.End,
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp)
        )
    }
}
