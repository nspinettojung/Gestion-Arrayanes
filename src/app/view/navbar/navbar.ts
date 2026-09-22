import { Component, DestroyRef, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, NavigationEnd, Router, RouterLink } from '@angular/router';
import { filter, map, mergeMap } from 'rxjs';

@Component({
  selector: 'gda-navbar',
  imports: [RouterLink],
  templateUrl: './navbar.html',
  styleUrl: './navbar.css',
})
export class Navbar {
  private destroyRef = inject(DestroyRef);
  pageTitle: string = 'Mi App';
  pageIcon: string = '';

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
}
