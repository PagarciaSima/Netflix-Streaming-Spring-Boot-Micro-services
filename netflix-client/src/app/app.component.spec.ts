import { TestBed } from '@angular/core/testing';
import { AppComponent } from './app.component';
import { StreamingService } from './streaming.service';
import { of } from 'rxjs';

describe('AppComponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AppComponent],
      providers: [
        {
          provide: StreamingService,
          useValue: {
            getStreamingUrl: jasmine.createSpy('getStreamingUrl').and.returnValue(of({
              movieId: 'test-id',
              streamingUrl: 'http://example.com/stream.m3u8',
              qualities: '1080p, 720p',
              expiresIn: 55
            }))
          }
        }
      ]
    }).compileComponents();
  });

  it('should create the app', () => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.componentInstance;
    expect(app).toBeTruthy();
  });

  it('should initialize with default property values', () => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.componentInstance;
    expect(app.movieId).toEqual('');
    expect(app.status).toEqual('');
    expect(app.isLoading).toBeFalse();
    expect(app.showInfoBox).toBeFalse();
    expect(app.streamingUrl).toEqual('');
    expect(app.expiresIn).toEqual(0);
    expect(app.activeQuality).toEqual('Auto');
    expect(app.showStats).toBeFalse();
  });

  it('should format bandwidth correctly', () => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.componentInstance;
    expect(app.formatBandwidth(500)).toEqual('500 bps');
    expect(app.formatBandwidth(1500000)).toEqual('1500.0 kbps');
    expect(app.formatBandwidth(50000000)).toEqual('50.0 Mbps');
  });

  it('should return 0:00 for videoCurrentTime when no video element', () => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.componentInstance;
    expect(app.videoCurrentTime()).toEqual('0:00');
  });

  it('should set error status when movieId is empty', () => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.componentInstance;
    app.movieId = '';
    app.loadMovie();
    expect(app.statusType).toEqual('error');
    expect(app.status).toContain('Please enter a Movie ID');
  });

  it('should toggle stats visibility', () => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.componentInstance;
    expect(app.showStats).toBeFalse();
    app.toggleStats();
    expect(app.showStats).toBeTrue();
    app.toggleStats();
    expect(app.showStats).toBeFalse();
  });
});