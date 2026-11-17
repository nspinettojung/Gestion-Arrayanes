import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { SystemService } from './system-service';

describe('SystemService', () => {
  let service: SystemService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(SystemService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('pide el token y lo envía al cerrar la aplicación', () => {
    const closed = vi.fn();
    service.closeApp().subscribe({ next: closed });

    const tokenRequest = httpMock.expectOne('/api/v1/system/shutdown-token');
    expect(tokenRequest.request.method).toBe('GET');
    tokenRequest.flush({ token: 'token-de-prueba' });

    const shutdownRequest = httpMock.expectOne('/api/v1/system/shutdown');
    expect(shutdownRequest.request.method).toBe('POST');
    expect(shutdownRequest.request.headers.get('X-Shutdown-Token')).toBe('token-de-prueba');
    shutdownRequest.flush(null);

    expect(closed).toHaveBeenCalledTimes(1);
  });
});
