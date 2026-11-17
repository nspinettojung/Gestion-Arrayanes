import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';

import { BackupInfo, BackupService } from '../../core/services/backup/backup';
import { ConfirmationService } from '../../core/services/confirmation/confirmation';
import { NotificationService } from '../../core/services/notification/notification';
import { SystemService } from '../../core/services/system/system-service';
import { Mainbackups } from './mainbackups';

const backup: BackupInfo = {
  name: 'backup-manual-20261117-030000.zip',
  kind: 'manual',
  createdAt: '2026-11-17T03:00:00Z',
  sizeBytes: 2048,
  pending: false,
};

const listMock = vi.fn(() => of({ backups: [backup], pendingRestore: null }));
const createMock = vi.fn(() => of(backup));
const restoreMock = vi.fn(() =>
  of({ message: 'Se aplicará al reiniciar', backupName: backup.name }),
);
const closeAppMock = vi.fn(() => of(undefined));
const confirmMock = vi.fn(async () => true);
const notificationMock = { success: vi.fn(), error: vi.fn(), info: vi.fn() };

describe('Mainbackups', () => {
  let component: Mainbackups;
  let fixture: ComponentFixture<Mainbackups>;

  beforeEach(async () => {
    vi.clearAllMocks();

    await TestBed.configureTestingModule({
      imports: [Mainbackups],
      providers: [
        {
          provide: BackupService,
          useValue: { list: listMock, create: createMock, restore: restoreMock },
        },
        { provide: SystemService, useValue: { closeApp: closeAppMock } },
        { provide: ConfirmationService, useValue: { confirm: confirmMock } },
        { provide: NotificationService, useValue: notificationMock },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(Mainbackups);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('carga el listado al iniciar', () => {
    expect(listMock).toHaveBeenCalledTimes(1);
    expect(component.backups()).toEqual([backup]);
  });

  it('crea una copia y vuelve a cargar el listado', () => {
    component.create();

    expect(createMock).toHaveBeenCalledTimes(1);
    expect(listMock).toHaveBeenCalledTimes(2);
    expect(notificationMock.success).toHaveBeenCalled();
  });

  it('marca la copia y cierra la app al restaurar', async () => {
    await component.restore(backup);

    expect(confirmMock).toHaveBeenCalledTimes(1);
    expect(restoreMock).toHaveBeenCalledWith(backup.name);
    expect(closeAppMock).toHaveBeenCalledTimes(1);
    expect(notificationMock.info).toHaveBeenCalledWith('Se aplicará al reiniciar');
  });

  it('no restaura si se cancela la confirmación', async () => {
    confirmMock.mockResolvedValueOnce(false);

    await component.restore(backup);

    expect(restoreMock).not.toHaveBeenCalled();
    expect(closeAppMock).not.toHaveBeenCalled();
  });

  it('formatea el tamaño y la etiqueta del tipo', () => {
    expect(component.kindLabel('diario')).toBe('Diaria');
    expect(component.kindLabel('cierre')).toBe('Al cerrar');
    expect(component.kindLabel('manual')).toBe('Manual');
    expect(component.sizeLabel(512)).toBe('512 B');
    expect(component.sizeLabel(2048)).toBe('2.0 KB');
  });
});
