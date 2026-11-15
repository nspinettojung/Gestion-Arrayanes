import { inject, Injectable } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { firstValueFrom } from 'rxjs';
import { map } from 'rxjs/operators';
import {
  ConfirmDialog,
  ConfirmDialogData,
} from '../../../view/assets/confirm-dialog/confirm-dialog';

@Injectable({
  providedIn: 'root',
})
export class ConfirmationService {
  private dialog = inject(MatDialog);

  confirm(data: ConfirmDialogData): Promise<boolean> {
    return firstValueFrom(
      this.dialog
        .open<ConfirmDialog, ConfirmDialogData, boolean>(ConfirmDialog, {
          data,
          width: '420px',
          maxWidth: '92vw',
          autoFocus: 'first-tabbable',
          restoreFocus: true,
          panelClass: 'gda-confirm-dialog',
        })
        .afterClosed()
        .pipe(map((result) => result === true)),
    );
  }

  confirmDelete(message: string, title = 'Confirmar eliminación'): Promise<boolean> {
    return this.confirm({
      title,
      message,
      confirmText: 'Eliminar',
      danger: true,
    });
  }
}
