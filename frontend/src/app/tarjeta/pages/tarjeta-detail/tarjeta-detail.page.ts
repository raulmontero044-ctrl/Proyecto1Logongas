import { ChangeDetectionStrategy, Component, OnInit, inject } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ApiError, Tarjeta } from '@/tarjeta/models/tarjeta.model';
import { TarjetaService } from '@/tarjeta/services/tarjeta.service';

const TIPO_INDEFINIDO = '(SIN TIPO)';

@Component({
  selector: 'app-tarjeta-detail-page',
  templateUrl: './tarjeta-detail.page.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TarjetaDetailPage implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly tarjetaService = inject(TarjetaService);

  cargando = true;
  tarjeta: Tarjeta | null = null;
  errorCodigo: string | null = null;
  errorMensaje: string | null = null;

  get tipoVisible(): string {
    return this.tarjeta?.tipo?.toUpperCase() ?? TIPO_INDEFINIDO;
  }

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));

    this.tarjetaService.obtenerTarjeta(id).subscribe({
      next: (tarjeta) => {
        this.tarjeta = tarjeta;
        this.cargando = false;
      },
      error: (error: ApiError) => {
        this.errorCodigo = error.codigo;
        this.errorMensaje = error.message;
        this.cargando = false;
      },
    });
  }
}