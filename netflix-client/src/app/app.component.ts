import { Component, AfterViewInit, OnDestroy, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterOutlet } from '@angular/router';
import { StreamingService, StreamingResponse } from './streaming.service';

declare var Hls: any;

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent implements OnInit, AfterViewInit, OnDestroy {
  private streamingService = inject(StreamingService);

  movieId = '';
  status = '';
  statusType: 'loading' | 'success' | 'error' | '' = '';
  isLoading = false;
  showInfoBox = false;

  streamingUrl = '';
  qualities = '1080p, 720p, 480p, 360p';
  expiresIn = 0;

  private video: HTMLVideoElement | null = null;
  private hlsInstance: any = null;
  private qualitySelect: HTMLSelectElement | null = null;
  private speedSelect: HTMLSelectElement | null = null;
  public currentMovieId = '';

  showStats = false;
  bandwidthEstimate = 0;
  bufferedSeconds = 0;
  activeQuality = 'Auto';

  private statsInterval: any = null;

  /**
   * Initializes the component.
   * Sets up keyboard shortcuts for video playback control.
   */
  ngOnInit(): void {
    this.setupKeyboardShortcuts();
  }

  /**
   * Initializes view child elements after Angular initializes the view.
   * Obtains references to the video element, quality select, and speed select DOM elements.
   */
  ngAfterViewInit(): void {
    this.video = document.getElementById('video') as HTMLVideoElement;
    this.qualitySelect = document.getElementById('qualitySelect') as HTMLSelectElement;
    this.speedSelect = document.getElementById('speedSelect') as HTMLSelectElement;
  }

  /**
   * Cleans up resources when the component is destroyed.
   * Destroys the HLS instance and clears the stats update interval.
   */
  ngOnDestroy(): void {
    this.destroyHls();
    if (this.statsInterval) {
      clearInterval(this.statsInterval);
    }
  }

  /**
   * Handles keyboard key press events in the movie ID input.
   * Triggers loadMovie when the Enter key is pressed.
   * @param event - The keyboard event from the input field.
   * @returns void
   */
  onKeyPress(event: KeyboardEvent): void {
    if (event.key === 'Enter') {
      this.loadMovie();
    }
  }

  /**
   * Loads a movie stream from the Streaming Service.
   * Validates the movie ID, destroys any existing HLS instance, and fetches the streaming URL.
   * On success, initializes HLS playback and shows the info box.
   * @returns void
   */
  loadMovie(): void {
    const id = this.movieId.trim();
    if (!id) {
      this.setStatus('❌ Please enter a Movie ID', 'error');
      return;
    }

    this.isLoading = true;
    this.statusType = 'loading';
    this.status = '⏳ Fetching stream from Streaming Service...';

    this.destroyHls();

    this.streamingService.getStreamingUrl(id).subscribe({
      next: (data: StreamingResponse) => {
        this.currentMovieId = id;
        this.streamingUrl = data.streamingUrl;
        this.qualities = data.qualities;
        this.expiresIn = data.expiresIn || 0;
        this.showInfoBox = true;
        this.setStatus(`✅ Stream loaded! Playing in ${data.qualities}`, 'success');
        this.isLoading = false;
        this.playHLS(data.streamingUrl);
      },
      error: (err) => {
        this.setStatus('❌ Movie not found or not ready for streaming', 'error');
        this.isLoading = false;
      }
    });
  }

  /**
   * Initializes HLS.js playback for the given streaming URL.
   * Sets up XHR routing for m3u8 playlist requests through the signing endpoint.
   * Configures manifest parsed and level switched event handlers.
   * @param streamUrl - The presigned HLS master playlist URL from the Streaming Service.
   * @returns void
   */
  private playHLS(streamUrl: string): void {
    if (!this.video) return;

    if (Hls.isSupported()) {
      this.hlsInstance = new Hls({
        xhrSetup: (xhr: any, url: string) => {
          if (url.includes('.m3u8') && !url.includes('X-Amz')) {
            const encodedPath = encodeURIComponent(
              url.split('.amazonaws.com/')[1] || ''
            );
            xhr.open('GET',
              `http://localhost:8084/api/v1/stream/${this.currentMovieId}/playlist?path=${encodedPath}`
            );
          }
        }
      });

      this.hlsInstance.loadSource(streamUrl);
      this.hlsInstance.attachMedia(this.video);

      this.hlsInstance.on(Hls.Events.MANIFEST_PARSED, (_event: any, data: any) => {
        this.setupQualitySelector(data.levels);
        this.restorePlaybackTime();

        this.video!.play().catch(() => {
          // Autoplay may be blocked by browser
        });

        this.startStatsInterval();
      });

      this.hlsInstance.on(Hls.Events.LEVEL_SWITCHED, (_event: any, data: any) => {
        const level = data.level;
        const hlsLevel = this.hlsInstance.levels[level];
        if (hlsLevel) {
          this.activeQuality = `${hlsLevel.height}p`;
        }
      });
    } else if (this.video.canPlayType('application/vnd.apple.mpegURL')) {
      this.video.src = streamUrl;
      this.video.play().catch(() => {});
    }
  }

  /**
   * Builds the quality selector dropdown from HLS manifest levels.
   * Populates options with resolution and bitrate info, plus an Auto option.
   * @param levels - Array of HLS quality levels from the parsed manifest.
   * @returns void
   */
  private setupQualitySelector(levels: any[]): void {
    if (!this.qualitySelect) return;

    this.qualitySelect.innerHTML = '<option value="-1">Auto (Adaptativo)</option>';

    levels.forEach((level, index) => {
      const option = document.createElement('option');
      option.value = String(index);
      option.textContent = `${level.height}p (${Math.round(level.bitrate / 1000)} kbps)`;
      this.qualitySelect!.appendChild(option);
    });

    this.qualitySelect.onchange = () => {
      const val = parseInt(this.qualitySelect!.value, 10);
      if (this.hlsInstance) {
        this.hlsInstance.currentLevel = val;
      }
      if (val === -1) {
        this.activeQuality = 'Auto';
      } else {
        this.activeQuality = `${levels[val].height}p`;
      }
    };
  }

  /**
   * Handles quality selector change events.
   * Switches HLS playback to the selected quality level or Auto.
   * @returns void
   */
  onQualityChange(): void {
    if (!this.qualitySelect || !this.hlsInstance) return;
    const val = parseInt(this.qualitySelect.value, 10);
    this.hlsInstance.currentLevel = val;
    if (val === -1) {
      this.activeQuality = 'Auto';
    } else {
      this.activeQuality = `${val}p`;
    }
  }

  /**
   * Handles playback speed selector change events.
   * Updates the video element playbackRate to the selected value.
   * @returns void
   */
  onSpeedChange(): void {
    if (!this.speedSelect || !this.video) return;
    this.video.playbackRate = parseFloat(this.speedSelect.value);
  }

  /**
   * Handles video timeupdate events.
   * Saves the current playback time to localStorage for resume functionality.
   * @returns void
   */
  onTimeUpdate(): void {
    this.savePlaybackTime();
  }

  /**
   * Restores saved playback time from localStorage for the current movie.
   * Seeks the video to the previously saved position if available.
   * @returns void
   */
  private restorePlaybackTime(): void {
    const saved = localStorage.getItem(`resume_${this.currentMovieId}`);
    if (saved && this.video) {
      this.video.currentTime = parseFloat(saved);
    }
  }

  /**
   * Saves the current playback time to localStorage for the current movie.
   * Enables resume functionality when the user returns to the same movie.
   * @returns void
   */
  private savePlaybackTime(): void {
    if (!this.video || !this.currentMovieId) return;
    localStorage.setItem(`resume_${this.currentMovieId}`, String(this.video.currentTime));
  }

  /**
   * Starts a 1-second interval to update streaming statistics.
   * Captures bandwidth estimate, buffered seconds, and active quality.
   * @returns void
   */
  private startStatsInterval(): void {
    if (this.statsInterval) clearInterval(this.statsInterval);
    this.statsInterval = setInterval(() => {
      if (this.hlsInstance && this.video) {
        this.bandwidthEstimate = this.hlsInstance.bandwidthEstimate || 0;
        const buffered = this.video.buffered;
        if (buffered.length > 0) {
          this.bufferedSeconds = buffered.end(0) - this.video.currentTime;
        }
      }
    }, 1000);
  }

  /**
   * Stops the stats update interval and clears the reference.
   * @returns void
   */
  private stopStatsInterval(): void {
    if (this.statsInterval) {
      clearInterval(this.statsInterval);
      this.statsInterval = null;
    }
  }

  /**
   * Destroys the HLS instance and releases video resources.
   * Stops the stats interval, destroys HLS, and clears the video source.
   * @returns void
   */
  private destroyHls(): void {
    this.stopStatsInterval();
    if (this.hlsInstance) {
      this.hlsInstance.destroy();
      this.hlsInstance = null;
    }
    if (this.video) {
      this.video.src = '';
      this.video.load();
    }
  }

  /**
   * Sets up global keyboard shortcuts for video playback control.
   * Supported keys: Space (play/pause), ArrowLeft/Right (±10s), F (fullscreen), M (mute).
   * Ignores events when the input field is focused.
   * @returns void
   */
  private setupKeyboardShortcuts(): void {
    document.addEventListener('keydown', (e: KeyboardEvent) => {
      if ((document.activeElement as HTMLElement).tagName === 'INPUT') return;

      if (!this.video) return;

      if (e.code === 'Space') {
        e.preventDefault();
        if (this.video.paused) {
          this.video.play();
        } else {
          this.video.pause();
        }
      } else if (e.code === 'ArrowRight') {
        this.video.currentTime += 10;
      } else if (e.code === 'ArrowLeft') {
        this.video.currentTime -= 10;
      } else if (e.code === 'KeyF') {
        if (!document.fullscreenElement) {
          this.video.requestFullscreen();
        } else {
          document.exitFullscreen();
        }
      } else if (e.code === 'KeyM') {
        this.video.muted = !this.video.muted;
      }
    });
  }

  /**
   * Sets the status message and its type for display in the UI.
   * @param message - The status message text to display.
   * @param type - The status type: 'loading', 'success', 'error', or empty.
   * @returns void
   */
  private setStatus(message: string, type: 'loading' | 'success' | 'error' | ''): void {
    this.status = message;
    this.statusType = type;
  }

  /**
   * Toggles the visibility of the stats overlay.
   * @returns void
   */
  toggleStats(): void {
    this.showStats = !this.showStats;
  }

  /**
   * Formats a bandwidth value in bits per second to a human-readable string.
   * @param bps - The bandwidth value in bits per second.
   * @returns A formatted string (e.g., "1500 kbps" or "2.5 Mbps").
   */
  formatBandwidth(bps: number): string {
    if (bps < 1000) return `${bps} bps`;
    if (bps < 1000000) return `${(bps / 1000).toFixed(1)} kbps`;
    return `${(bps / 1000000).toFixed(1)} Mbps`;
  }

  /**
   * Returns the current video playback time formatted as MM:SS.
   * @returns The formatted time string (e.g., "5:30") or "0:00" if no video element.
   */
  videoCurrentTime(): string {
    if (!this.video) return '0:00';
    const t = this.video.currentTime;
    const m = Math.floor(t / 60);
    const s = Math.floor(t % 60);
    return `${m}:${s.toString().padStart(2, '0')}`;
  }
}
