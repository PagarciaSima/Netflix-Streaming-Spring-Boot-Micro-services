import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

/**
 * Response interface for the Streaming Service getStreamingUrl endpoint.
 */
export interface StreamingResponse {
  /** Unique identifier of the movie. */
  movieId: string;
  /** Presigned URL of the HLS master playlist. */
  streamingUrl: string;
  /** Available quality levels (e.g., "1080p, 720p, 480p, 360p"). */
  qualities: string;
  /** URL expiration time in minutes. */
  expiresIn: number;
}

@Injectable({
  providedIn: 'root'
})
export class StreamingService {
  /** Base URL for the Streaming Service stream endpoints. */
  private readonly baseUrl = 'http://localhost:8084/api/v1/stream';

  /**
   * Creates an instance of StreamingService.
   * @param http - Angular HttpClient for making HTTP requests.
   */
  constructor(private http: HttpClient) {}

  /**
   * Retrieves the presigned streaming URL for a movie from the Streaming Service.
   * Checks Redis cache first; if not cached, generates a new presigned URL.
   * @param movieId - Unique identifier of the movie.
   * @returns Observable emitting the StreamingResponse with streaming URL and metadata.
   */
  getStreamingUrl(movieId: string): Observable<StreamingResponse> {
    return this.http.get<StreamingResponse>(`${this.baseUrl}/${movieId}`);
  }

  /**
   * Retrieves the signed m3u8 playlist content with presigned segment URLs.
   * Used by the HLS player to fetch individual video segments securely.
   * @param movieId - Unique identifier of the movie.
   * @param path - S3 path to the master playlist (e.g., encoded/movieId/master.m3u8).
   * @returns Observable emitting the playlist content as text.
   */
  getSignedPlaylist(movieId: string, path: string): Observable<string> {
    const encodedPath = encodeURIComponent(path);
    return this.http.get(
      `${this.baseUrl}/${movieId}/playlist?path=${encodedPath}`,
      { responseType: 'text' }
    );
  }
}