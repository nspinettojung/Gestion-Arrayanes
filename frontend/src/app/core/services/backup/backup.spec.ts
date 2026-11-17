import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { BackupService } from './backup';

describe('BackupService', () => {
  let service: BackupService;
  let httpMock: HttpTestingController;

  const tokenUrl = '/api/v1/system/shutdown-token';
  const backupsUrl = '/api/v1/system/backups';

  function flushToken(): void {
    const request = httpMock.expectOne(tokenUrl);
    expect(request.request.method).toBe('GET');
    request.flush({ token: 'token-de-prueba' });
  }

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(BackupService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('lista las copias con el token local', () => {
    service.list().subscribe();

    flushToken();
    const request = httpMock.expectOne(backupsUrl);
    expect(request.request.method).toBe('GET');
    expect(request.request.headers.get('X-Shutdown-Token')).toBe('token-de-prueba');
    request.flush({ backups: [], pendingRestore: null });
  });

  it('crea una copia con el token local', () => {
    service.create().subscribe();

    flushToken();
    const request = httpMock.expectOne(backupsUrl);
    expect(request.request.method).toBe('POST');
    expect(request.request.headers.get('X-Shutdown-Token')).toBe('token-de-prueba');
    request.flush({ name: 'backup-manual.zip' });
  });

  it('marca una copia para restaurar con el token local', () => {
    service.restore('backup-manual-20261117-030000.zip').subscribe();

    flushToken();
    const request = httpMock.expectOne(`${backupsUrl}/backup-manual-20261117-030000.zip/restore`);
    expect(request.request.method).toBe('POST');
    expect(request.request.headers.get('X-Shutdown-Token')).toBe('token-de-prueba');
    request.flush({ message: 'ok', backupName: 'backup-manual-20261117-030000.zip' });
  });
});
