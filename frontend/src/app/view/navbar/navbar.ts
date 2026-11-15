import { Component, DestroyRef, inject, signal, OnInit } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, NavigationEnd, Router, RouterLink } from '@angular/router';
import { filter, finalize, map, mergeMap } from 'rxjs';
import { SystemService } from '../../core/services/system/system-service';
import { ConfirmationService } from '../../core/services/confirmation/confirmation';

@Component({
  selector: 'gda-navbar',
  imports: [RouterLink],
  templateUrl: './navbar.html',
  styleUrl: './navbar.css',
})
export class Navbar implements OnInit {
  private destroyRef = inject(DestroyRef);
  private systemService = inject(SystemService);
  private confirmation = inject(ConfirmationService);

  pageTitle = 'Mi App';
  pageIcon = '';
  closing = signal(false);
  closed = signal(false);

  constructor(
    private router: Router,
    private activatedRoute: ActivatedRoute,
  ) {}

  ngOnInit() {
    this.router.events
      .pipe(
        filter((event) => event instanceof NavigationEnd),
        map(() => this.activatedRoute),
        map((route) => {
          while (route.firstChild) {
            route = route.firstChild;
          }
          return route;
        }),
        mergeMap((route) => route.data),
      )
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((data) => {
        this.pageTitle = data['title'] || 'Bienvenido';
        this.pageIcon = data['icon'];
      });
  }

  async closeSystem(): Promise<void> {
    if (this.closing()) {
      return;
    }

    const confirmed = await this.confirmation.confirm({
      title: 'Salir de la aplicación',
      message: '¿Seguro que querés cerrar la aplicación? Se cerrarán las ventanas abiertas.',
      confirmText: 'Salir',
    });

    if (!confirmed) {
      return;
    }

    this.closing.set(true);
    this.systemService
      .closeApp()
      .pipe(finalize(() => this.closing.set(false)))
      .subscribe({
        next: () => {
          this.tryCloseWindow();
        },
        error: () => {
          // Aunque falle la respuesta por el apagado repentino, intentamos cerrar igual.
          this.tryCloseWindow();
        },
      });
  }

  private tryCloseWindow() {
    // En la ventana en modo app el cierre funciona; si el navegador lo bloquea,
    // mostramos una pantalla de despedida para que el usuario cierre la ventana.
    window.close();

    setTimeout(() => {
      if (!window.closed) {
        this.closed.set(true);
      }
    }, 600);
  }
}
