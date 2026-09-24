import { ChangeDetectionStrategy, Component, OnInit, inject, viewChild } from '@angular/core';
import { TarjetaFormComponent } from '@/tarjeta/components/tarjeta-form/tarjeta-form.component';
import { ApiError, TarjetaRequest, TipoTarjeta } from '@/tarjeta/models/tarjeta.model';
import { TarjetaService } from '@/tarjeta/services/tarjeta.service';

@Component({
  selector: 'app-tarjeta-create-page',
  imports: [TarjetaFormComponent],
  templateUrl: './tarjeta-create.page.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TarjetaCreatePage implements OnInit {
  private readonly tarjetaService = inject(TarjetaService);
  private readonly formulario = viewChild(TarjetaFormComponent);

  tipos: TipoTarjeta[] = [];
  enviando = false;
  confirmacion: string | null = null;
  errorCodigo: string | null = null;
  errorMensaje: string | null = null;

  ngOnInit(): void {
    this.tarjetaService.listarTiposTarjeta().subscribe({
      next: (tipos) => {
        this.tipos = tipos;
      },
      error: (error: ApiError) => this.mostrarError(error),
    });
  }

  onSubmitRequest(request: TarjetaRequest): void {
    if (this.enviando) {
      return;
    }
    this.enviando = true;
    this.limpiarMensajes();

    this.tarjetaService.crearTarjeta(request).subscribe({
      next: (tarjeta) => {
        this.confirmacion = `Tarjeta creada con id ${tarjeta.id}`;
        this.enviando = false;
        this.formulario()?.reset();
      },
      error: (error: ApiError) => {
        this.mostrarError(error);
        this.enviando = false;
      },
    });
  }

  onCancelar(): void {
    this.limpiarMensajes();
    this.formulario()?.reset();
  }

  private mostrarError(error: ApiError): void {
    this.errorCodigo = error.codigo;
    this.errorMensaje = error.message;
  }

  private limpiarMensajes(): void {
    this.confirmacion = null;
    this.errorCodigo = null;
    this.errorMensaje = null;
  }
}