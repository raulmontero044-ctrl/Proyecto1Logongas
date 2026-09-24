import { Routes } from '@angular/router';
import { TarjetaCreatePage } from '@/tarjeta/pages/tarjeta-create/tarjeta-create.page';
import { TarjetaDetailPage } from '@/tarjeta/pages/tarjeta-detail/tarjeta-detail.page';

export const tarjetaRoutes: Routes = [
  { path: 'tarjeta/nueva', component: TarjetaCreatePage },
  { path: 'tarjeta/:id', component: TarjetaDetailPage },
];