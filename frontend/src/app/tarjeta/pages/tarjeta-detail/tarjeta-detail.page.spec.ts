import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { Observable, of, throwError } from 'rxjs';
import { ApiError, Tarjeta } from '@/tarjeta/models/tarjeta.model';
import { TarjetaService } from '@/tarjeta/services/tarjeta.service';
import { TarjetaDetailPage } from './tarjeta-detail.page';

interface MockTarjetaService {
  obtenerTarjeta: (id: number) => Observable<Tarjeta>;
}

interface MockParamMap {
  get: (key: string) => string | null;
}

interface MockActivatedRoute {
  snapshot: { paramMap: MockParamMap };
}

describe('TarjetaDetailPage', () => {
  let fixture: ComponentFixture<TarjetaDetailPage>;
  let component: TarjetaDetailPage;
  let serviceMock: MockTarjetaService;
  let rutaMock: MockActivatedRoute;

  const TARJETA_CON_TIPO: Tarjeta = {
    id: 7,
    nombrePropietario: 'Ana Pérez',
    tipo: 'Crédito',
  };

  function configurarRuta(id: string | null): void {
    rutaMock = {
      snapshot: {
        paramMap: {
          get: (key: string) => (key === 'id' ? id : null),
        },
      },
    };
  }

  async function crearComponente(tarjetaEmision: Observable<Tarjeta>): Promise<void> {
    serviceMock = {
      obtenerTarjeta: vi.fn(() => tarjetaEmision),
    };

    await TestBed.configureTestingModule({
      imports: [TarjetaDetailPage],
      providers: [
        { provide: TarjetaService, useValue: serviceMock },
        { provide: ActivatedRoute, useValue: rutaMock },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(TarjetaDetailPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }

  it('consulta el id de la ruta y llama obtenerTarjeta con ese id', async () => {
    configurarRuta('7');
    await crearComponente(of(TARJETA_CON_TIPO));

    expect(serviceMock.obtenerTarjeta).toHaveBeenCalledTimes(1);
    expect(serviceMock.obtenerTarjeta).toHaveBeenCalledWith(7);
  });

  it('al éxito con tipo muestra el propietario y el tipo en MAYÚSCULAS y negrita (3.2)', async () => {
    configurarRuta('7');
    await crearComponente(of(TARJETA_CON_TIPO));

    const propietario = fixture.nativeElement.querySelector(
      '[data-testid="propietario"]',
    ) as HTMLElement;
    expect(propietario.textContent).toContain('Ana Pérez');

    const tipo = fixture.nativeElement.querySelector(
      '[data-testid="tipo"] span',
    ) as HTMLElement;
    expect(tipo).toBeTruthy();
    expect(tipo.textContent).toBe('CRÉDITO');
    expect(tipo.classList.contains('text-uppercase')).toBe(true);
    expect(tipo.classList.contains('fw-bold')).toBe(true);
  });

  it('cuando la tarjeta no existe muestra el mensaje y el código del ApiError (3.1)', async () => {
    configurarRuta('999');
    await crearComponente(
      throwError(() => new ApiError('TARJETA_NO_ENCONTRADA', 'Tarjeta no encontrada')),
    );

    const error = fixture.nativeElement.querySelector(
      '[data-testid="error-consulta"]',
    ) as HTMLElement;
    expect(error).toBeTruthy();
    expect(error.textContent).toContain('TARJETA_NO_ENCONTRADA');
    expect(error.textContent).toContain('Tarjeta no encontrada');
    expect(component.cargando).toBe(false);
  });

  it('cuando el tipo es null muestra el placeholder (SIN TIPO) en el mismo formato', async () => {
    configurarRuta('8');
    await crearComponente(
      of({ id: 8, nombrePropietario: 'Luis Gómez', tipo: null }),
    );

    const tipo = fixture.nativeElement.querySelector(
      '[data-testid="tipo"] span',
    ) as HTMLElement;
    expect(tipo).toBeTruthy();
    expect(tipo.textContent).toBe('(SIN TIPO)');
    expect(tipo.classList.contains('text-uppercase')).toBe(true);
    expect(tipo.classList.contains('fw-bold')).toBe(true);
  });
});