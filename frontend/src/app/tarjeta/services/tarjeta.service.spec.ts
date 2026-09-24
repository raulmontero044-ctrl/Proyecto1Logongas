import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { API_URL } from '@/environment';
import { ApiError, Tarjeta, TarjetaRequest, TipoTarjeta } from '@/tarjeta/models/tarjeta.model';

import { TarjetaService } from '@/tarjeta/services/tarjeta.service';

describe('TarjetaService', () => {
  let service: TarjetaService;
  let httpTesting: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(TarjetaService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
  });

  it('crea una tarjeta con POST /api/tarjetas y el cuerpo TarjetaRequest (1.1)', () => {
    const request: TarjetaRequest = {
      nombrePropietario: 'Ana Pérez',
      tipo: 'Débito',
    };
    const response: Tarjeta = {
      id: 1,
      nombrePropietario: 'Ana Pérez',
      tipo: 'Débito',
    };

    let result: Tarjeta | undefined;
    service.crearTarjeta(request).subscribe((tarjeta) => {
      result = tarjeta;
    });

    const req = httpTesting.expectOne(`${API_URL}/tarjetas`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(request);
    req.flush(response);

    expect(result).toEqual(response);
  });

  it('obtiene una tarjeta por id con GET /api/tarjetas/7 (3.1)', () => {
    const response: Tarjeta = {
      id: 7,
      nombrePropietario: 'Luis Gómez',
      tipo: 'Crédito',
    };

    let result: Tarjeta | undefined;
    service.obtenerTarjeta(7).subscribe((tarjeta) => {
      result = tarjeta;
    });

    const req = httpTesting.expectOne(`${API_URL}/tarjetas/7`);
    expect(req.request.method).toBe('GET');
    req.flush(response);

    expect(result).toEqual(response);
  });

  it('lista el catálogo de tipos con GET /api/tipos-tarjetas', () => {
    const response: TipoTarjeta[] = [
      { id: 1, nombre: 'Débito' },
      { id: 2, nombre: 'Crédito' },
    ];

    let result: TipoTarjeta[] | undefined;
    service.listarTiposTarjeta().subscribe((tipos) => {
      result = tipos;
    });

    const req = httpTesting.expectOne(`${API_URL}/tipos-tarjetas`);
    expect(req.request.method).toBe('GET');
    req.flush(response);

    expect(result).toEqual(response);
  });

  it('convierte un error 400 con body {codigo, mensaje} en un ApiError tipado', () => {
    const errorBody = {
      codigo: 'NOMBRE_REQUERIDO',
      mensaje: 'El nombre del propietario es obligatorio',
    };

    let actual: ApiError | undefined;
    service
      .crearTarjeta({ nombrePropietario: '', tipo: null })
      .subscribe({ error: (err: ApiError) => (actual = err) });

    const req = httpTesting.expectOne(`${API_URL}/tarjetas`);
    req.flush(errorBody, { status: 400, statusText: 'Bad Request' });

    expect(actual).toBeInstanceOf(ApiError);
    expect(actual?.codigo).toBe('NOMBRE_REQUERIDO');
    expect(actual?.message).toBe('El nombre del propietario es obligatorio');
  });

  it('emite un ApiError genérico cuando el body del error no está estructurado', () => {
    let actual: ApiError | undefined;
    service.listarTiposTarjeta().subscribe({
      error: (err: ApiError) => (actual = err),
    });

    const req = httpTesting.expectOne(`${API_URL}/tipos-tarjetas`);
    req.flush('respuesta inesperada', { status: 500, statusText: 'Server Error' });

    expect(actual).toBeInstanceOf(ApiError);
    expect(actual?.codigo).toBe('ERROR');
    expect(actual?.message).toBe('Error inesperado');
  });
});