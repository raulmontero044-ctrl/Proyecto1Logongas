import { ComponentFixture, TestBed } from '@angular/core/testing';
import { TarjetaRequest, TipoTarjeta } from '@/tarjeta/models/tarjeta.model';
import { TarjetaFormComponent } from './tarjeta-form.component';

describe('TarjetaFormComponent', () => {
  const catalogo: TipoTarjeta[] = [
    { id: 1, nombre: 'Débito' },
    { id: 2, nombre: 'Crédito' },
  ];

  let fixture: ComponentFixture<TarjetaFormComponent>;
  let component: TarjetaFormComponent;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TarjetaFormComponent],
    }).compileComponents();
    fixture = TestBed.createComponent(TarjetaFormComponent);
    component = fixture.componentInstance;
    component.tipos = catalogo;
    fixture.detectChanges();
  });

  function enviarFormulario(): void {
    const form = fixture.nativeElement.querySelector('form') as HTMLFormElement;
    form.dispatchEvent(new Event('submit'));
    fixture.detectChanges();
  }

  it('renderiza una opción "Sin tipo" y una opción por cada tipo del catálogo entrante', () => {
    const opciones = Array.from(
      fixture.nativeElement.querySelectorAll('select option'),
    ) as HTMLOptionElement[];

    expect(opciones.length).toBe(catalogo.length + 1);
    expect(opciones[0].textContent?.trim()).toBe('Sin tipo');
    expect(opciones.map((o) => o.textContent?.trim())).toContain('Débito');
    expect(opciones.map((o) => o.textContent?.trim())).toContain('Crédito');
  });

  it('no emite submitRequest cuando el nombre está vacío o en blanco', () => {
    const emitido: TarjetaRequest[] = [];
    component.submitRequest.subscribe((req) => emitido.push(req));

    for (const valor of ['', '   ']) {
      component.formulario.setValue({ nombrePropietario: valor, tipo: null });
      enviarFormulario();
      expect(component.formulario.invalid).toBe(true);
    }

    expect(emitido).toHaveLength(0);
  });

  it('emite un TarjetaRequest con nombre y tipo al enviar un formulario válido', () => {
    const emitido: TarjetaRequest[] = [];
    component.submitRequest.subscribe((req) => emitido.push(req));

    component.formulario.setValue({ nombrePropietario: 'Ana Pérez', tipo: 'Débito' });
    enviarFormulario();

    expect(emitido).toEqual([{ nombrePropietario: 'Ana Pérez', tipo: 'Débito' }]);
  });

  it('emite tipo null al elegir "Sin tipo"', () => {
    const emitido: TarjetaRequest[] = [];
    component.submitRequest.subscribe((req) => emitido.push(req));

    component.formulario.setValue({ nombrePropietario: 'Luis Gómez', tipo: null });
    enviarFormulario();

    expect(emitido).toEqual([{ nombrePropietario: 'Luis Gómez', tipo: null }]);
  });
});