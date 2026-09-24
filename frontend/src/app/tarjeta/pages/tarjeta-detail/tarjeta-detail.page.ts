import { ChangeDetectionStrategy, Component } from '@angular/core';

@Component({
  selector: 'app-tarjeta-detail-page',
  template: `<p>Página de consulta del tipo de tarjeta (por construir).</p>`,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TarjetaDetailPage {}