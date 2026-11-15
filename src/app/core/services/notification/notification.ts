import { inject, Injectable } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';

export type NotificationKind = 'success' | 'error' | 'info';

const DEDUPE_WINDOW_MS = 3000;

@Injectable({
  providedIn: 'root',
})
export class NotificationService {
  private snackBar = inject(MatSnackBar);
  private lastMessage = '';
  private lastShownAt = 0;

  success(message: string): void {
    this.show(message, 'success', 3500);
  }

  error(message: string): void {
    this.show(message, 'error', 6000);
  }

  info(message: string): void {
    this.show(message, 'info', 4000);
  }

  private show(message: string, kind: NotificationKind, duration: number): void {
    const text = message.trim();
    if (!text) {
      return;
    }

    // Varias requests pueden fallar a la vez (por ejemplo, al cargar el dashboard).
    // Evitamos apilar el mismo mensaje repetido.
    const now = Date.now();
    if (text === this.lastMessage && now - this.lastShownAt < DEDUPE_WINDOW_MS) {
      return;
    }
    this.lastMessage = text;
    this.lastShownAt = now;

    this.snackBar.open(text, 'Cerrar', {
      duration,
      horizontalPosition: 'right',
      verticalPosition: 'bottom',
      panelClass: [`gda-snack-${kind}`],
    });
  }
}
