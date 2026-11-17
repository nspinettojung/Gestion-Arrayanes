import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: 'dashboard',
    loadComponent: () => import('./view/maindashboard/maindashboard').then((m) => m.Maindashboard),
    data: { title: 'Dashboard', icon: 'dashboard' },
  },
  {
    path: 'booking',
    loadComponent: () => import('./view/mainbooking/mainbooking').then((m) => m.Mainbooking),
    data: { title: 'Reservas', icon: 'calendar_month' },
  },
  {
    path: 'clients',
    loadComponent: () => import('./view/mainclient/mainclient').then((m) => m.Mainclient),
    data: { title: 'Clientes', icon: 'group' },
  },
  {
    path: 'apartments',
    loadComponent: () => import('./view/mainapartment/mainapartment').then((m) => m.Mainapartment),
    data: { title: 'Departamentos', icon: 'apartment' },
  },
  {
    path: 'reservation',
    loadComponent: () =>
      import('./view/mainreservation/mainreservation').then((m) => m.Mainreservation),
    data: { title: 'Agendar Reserva' },
  },
  {
    path: 'history',
    loadComponent: () => import('./view/mainhistory/mainhistory').then((m) => m.Mainhistory),
    data: { title: 'Historial' },
  },
  {
    path: 'calendar',
    loadComponent: () => import('./view/maincalendar/maincalendar').then((m) => m.Maincalendar),
    data: { title: 'Calendario', icon: 'event' },
  },
  {
    path: 'backups',
    loadComponent: () => import('./view/mainbackups/mainbackups').then((m) => m.Mainbackups),
    data: { title: 'Copias de seguridad', icon: 'backup' },
  },
  { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
];
