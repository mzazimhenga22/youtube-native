package com.youtubekids.youtube.di

import android.content.Context
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PlayerModule {

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    @Provides
    @Singleton
    fun provideExoPlayer(@ApplicationContext context: Context): ExoPlayer {
        // 1. Hardware Acceleration Renderers
        val renderersFactory = androidx.media3.exoplayer.DefaultRenderersFactory(context)
            .setExtensionRendererMode(androidx.media3.exoplayer.DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER)
            .setEnableDecoderFallback(true)

        // 2. Track Selection — let ExoPlayer pick the best track from the stream
        //    We already select stream quality in InnerTubeClient.chooseStream(),
        //    so don't restrict resolution here.
        val trackSelector = DefaultTrackSelector(context).apply {
            setParameters(
                buildUponParameters()
                    .setPreferredAudioLanguage("en")
                    .setForceHighestSupportedBitrate(true)
            )
        }

        // 3. Aggressive Buffering for 4K/HDR (Custom LoadControl)
        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                60000, // Min buffer 60s
                180000, // Max buffer 180s (3 mins for 4K stability)
                2000, // Playback start buffer 2s
                5000 // Rebuffer 5s
            )
            .setBackBuffer(30000, true) // Enable 30s back-buffer for instant re-seeking
            .build()

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
            .build()

        return ExoPlayer.Builder(context, renderersFactory)
            .setTrackSelector(trackSelector)
            .setLoadControl(loadControl)
            .setAudioAttributes(audioAttributes, true)
            .setHandleAudioBecomingNoisy(true)
            .build()
    }
}
