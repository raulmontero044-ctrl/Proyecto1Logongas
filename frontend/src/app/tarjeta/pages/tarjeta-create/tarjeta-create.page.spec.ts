import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { Observable, Subject, of, throwError } from 'rxjs';
import { TarjetaFormComponent } from '@/tarjeta/components/tarjeta-form/tarjeta-form.component';
import {
  ApiError,
  Tarjeta,
  TarjetaRequest,
  TipoTarjeta,
} from '@/tarjeta/models/tarjeta.model';
import { TarjetaService } from '@/tarjeta/services/tarjeta.service';
import { TarjetaCreatePage } from './tarjeta-create.page';

interface MockTarjetaService {
  listarTiposTarjeta: () => Observable<TipoTarjeta[]>;
  crearTarjeta: (request: TarjetaRequest) => Observable<Tarjeta>;
  obtenerTarjeta: () => Observable<Tarjeta>;
}

const CATALOGO: TipoTarjeta[] = [
  { id: 1, nombre: 'Débito' },
  { id: 2, nombre: 'Crédito' },
];

const TARJETA_CREADA: Tarjeta = {
  id: 42,
  nombrePropietario: 'Ana Pérez',
  tipo: 'Débito',
};

describe('TarjetaCreatePage', () => {
  let fixture: ComponentFixture<TarjetaCreatePage>;
  let component: TarjetaCreatePage;
  let serviceMock: MockTarjetaService;
  let llamadasCrear: TarjetaRequest[];

  beforeEach(async () => {
    llamadasCrear = [];
    serviceMock = {
      listarTiposTarjeta: () => of(CATALOGO),
      crearTarjeta: (request: TarjetaRequest) => {
        llamadasCrear.push(request);
        return of(TARJETA_CREADA);
      },
      obtenerTarjeta: () => throwError(() => new ApiError('ERROR', 'Error inesperado')),
    };

    await TestBed.configureTestingModule({
      imports: [TarjetaCreatePage],
      providers: [{ provide: TarjetaService, useValue: serviceMock }],
    }).compileComponents();

    fixture = TestBed.createComponent(TarjetaCreatePage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  function obtenerFormulario(): TarjetaFormComponent {
    const debugElement = fixture.debugElement.query(By.directive(TarjetaFormComponent));
    return debugElement.componentInstance as TarjetaFormComponent;
  }

  it('al inicializar carga el catálogo y lo expone al formulario', () => {
    expect(component.tipos).toEqual(CATALOGO);
    expect(obtenerFormulario().tipos).toEqual(CATALOGO);
  });

  it('al recibir un TarjetaRequest llama crearTarjeta y al éxito muestra confirmación con el id y reinicia el form', () => {
    obtenerFormulario().submitRequest.emit({
      nombrePropietario: 'Ana Pérez',
      tipo: 'Débito',
    });
    fixture.detectChanges();

    expect(llamadasCrear).toEqual([
      { nombrePropietario: 'Ana Pérez', tipo: 'Débito' },
    ]);

    const alerta = fixture.nativeElement.querySelector(
      '[data-testid="confirmacion"]',
    ) as HTMLElement;
    expect(alerta).toBeTruthy();
    expect(alerta.textContent).toContain('42');
    expect(component.enviando).toBe(false);
    expect(obtenerFormulario().formulario.getRawValue()).toEqual({
      nombrePropietario: '',
      tipo: null,
    });
  });

  it('cuando crearTarjeta lanza un ApiError muestra el mensaje de validación en pantalla', () => {
    serviceMock.crearTarjeta = () =>
      throwError(
        () =>
          new ApiError('NOMBRE_REQUERIDO', 'El nombre del propietario es obligatorio'),
      );

    obtenerFormulario().submitRequest.emit({ nombrePropietario: '', tipo: null });
    fixture.detectChanges();

    const alerta = fixture.nativeElement.querySelector(
      '[data-testid="error-validacion"]',
    ) as HTMLElement;
    expect(alerta).toBeTruthy();
    expect(alerta.textContent).toContain('NOMBRE_REQUERIDO');
    expect(alerta.textContent).toContain('El nombre del propietario es obligatorio');
    expect(component.enviando).toBe(false);
  });

  it('no fuerza un segundo envío mientras la creación está en curso', () => {
    const crearSubject = new Subject<Tarjeta>();
    serviceMock.crearTarjeta = (request: TarjetaRequest) => {
      llamadasCrear.push(request);
      return crearSubject;
    };

    const request: TarjetaRequest = { nombrePropietario: 'Ana Pérez', tipo: null };
    const formulario = obtenerFormulario();
    formulario.submitRequest.emit(request);
    formulario.submitRequest.emit(request);
    fixture.detectChanges();

    expect(llamadasCrear).toHaveLength(1);
    expect(component.enviando).toBe(true);

    crearSubject.next(TARJETA_CREADA);
    crearSubject.complete();
    fixture.detectChanges();
    expect(component.enviando).toBe(false);
  });
});