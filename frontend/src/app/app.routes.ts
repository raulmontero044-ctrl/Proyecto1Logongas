import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    loadChildren: () =>
      import('@/tarjeta/tarjeta.routes').then((m) => m.tarjetaRoutes),
  },
];
