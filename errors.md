It was likely two issues combined, not just a UI color problem.

1. The main `VideoPlayerScreen` was applying fixed overlay padding: `end = 400.dp` and `bottom = 520.dp`. On smaller/narrower screens that can shrink the actual `PlayerView` surface so much that you effectively see the black background while audio keeps playing.

2. Stream setup was fragile for YouTube adaptive streams. Audio and video often arrive as separate URLs, and the player setup was not consistently resetting stale media or using the same header-aware Media3 source factory for those URLs. That can lead to audio playing while the video track/surface fails.

I patched:
- [PlayerStream.kt](</mnt/data/D:/youtube-native/android-native/app/src/main/java/com/youtubekids/youtube/ui/player/PlayerStream.kt>) to reset media, use request headers, follow redirects, and build proper media sources.
- [VideoPlayerScreen.kt](</mnt/data/D:/youtube-native/android-native/app/src/main/java/com/youtubekids/youtube/ui/screens/VideoPlayerScreen.kt>) to use proportional padding so the video surface cannot collapse.
- [InnerTubeClient.kt](</mnt/data/D:/youtube-native/android-native/app/src/main/java/com/youtubekids/youtube/data/remote/InnerTubeClient.kt>) to prefer muxed/adaptive playable streams before manifest fallback and prefer H.264/MP4 video for device compatibility.

I started a compile check, but it was interrupted, so I have not verified the build yet.