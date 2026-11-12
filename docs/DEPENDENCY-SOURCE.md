# Copyleft dependency source references

The unmodified Jellyfin FFmpeg decoder is pinned to `org.jellyfin.media3:media3-ffmpeg-decoder:1.9.0+1`.

- Publication metadata: https://repo.maven.apache.org/maven2/org/jellyfin/media3/media3-ffmpeg-decoder/1.9.0+1/media3-ffmpeg-decoder-1.9.0+1.pom
- Corresponding wrapper/decoder source: https://github.com/jellyfin/jellyfin-androidx-media/tree/af9ee4e26b2045e3ea6f2ebf4a18ac8ebfeae396 (tag v1.9.0+1).
- This tag pins its `ffmpeg` submodule to `37c90f55d8b6c3948c79449b04755527b4ae1351` and `media` submodule to `7cc1056f840ce226598d3b990d4a6f7cd17e2831`. Retrieve recursively to include the full native code and scripts, not just the sources.jar.
- The published sources.jar is retained under licenses/dependencies as a convenience, but it explicitly excludes the FFmpeg submodule and is not by itself the full corresponding native source.

No decoder code is modified by this fork. Keep these references and the bundled dependency source archive with binary distribution. Android system libraries and standard build tools are separate prerequisites.
