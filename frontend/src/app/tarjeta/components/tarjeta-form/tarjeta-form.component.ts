import {
  AbstractControl,
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  ValidationErrors,
  Validators,
} from '@angular/forms';
import {
  ChangeDetectionStrategy,
  Component,
  EventEmitter,
  Input,
  Output,
} from '@angular/core';
import { TarjetaRequest, TipoTarjeta } from '@/tarjeta/models/tarjeta.model';

function nombrePropietarioNoVacio(
  control: AbstractControl<string | null>,
): ValidationErrors | null {
  const valor = control.value?.trim() ?? '';
  return valor.length === 0 ? { nombreObligatorio: true } : null;
}

@Component({
  selector: 'app-tarjeta-form',
  imports: [ReactiveFormsModule],
  templateUrl: './tarjeta-form.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TarjetaFormComponent {
  @Input() tipos: TipoTarjeta[] = [];
  @Input() enviando = false;
  @Output() submitRequest = new EventEmitter<TarjetaRequest>();
  @Output() cancelar = new EventEmitter<void>();

  readonly formulario = new FormGroup({
    nombrePropietario: new FormControl<string | null>('', [
      Validators.required,
      nombrePropietarioNoVacio,
    ]),
    tipo: new FormControl<string | null>(null),
  });

  get nombrePropietario(): FormControl<string | null> {
    return this.formulario.controls.nombrePropietario;
  }

  enviar(): void {
    this.formulario.markAllAsTouched();
    if (this.formulario.invalid) {
      return;
    }

    const { nombrePropietario, tipo } = this.formulario.getRawValue();
    this.submitRequest.emit({ nombrePropietario: nombrePropietario ?? '', tipo });
  }

  reset(): void {
    this.formulario.reset({ nombrePropietario: '', tipo: null });
  }
}