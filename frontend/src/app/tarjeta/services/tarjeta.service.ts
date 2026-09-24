import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { API_URL } from '@/environment';
import {
  ApiError,
  Tarjeta,
  TarjetaRequest,
  TipoTarjeta,
} from '@/tarjeta/models/tarjeta.model';

interface CuerpoError {
  codigo?: string;
  mensaje?: string;
}

function esCuerpoErrorEstructurado(
  body: CuerpoError | null | undefined,
): body is Omit<CuerpoError, 'codigo' | 'mensaje'> & {
  codigo: string;
  mensaje: string;
} {
  return (
    body !== null &&
    body !== undefined &&
    typeof body.codigo === 'string' &&
    typeof body.mensaje === 'string'
  );
}

@Injectable({ providedIn: 'root' })
export class TarjetaService {
  private readonly http = inject(HttpClient);

  crearTarjeta(request: TarjetaRequest): Observable<Tarjeta> {
    return this.http
      .post<Tarjeta>(`${API_URL}/tarjetas`, request)
      .pipe(catchError((error: HttpErrorResponse) => this.manejarError(error)));
  }

  obtenerTarjeta(id: number): Observable<Tarjeta> {
    return this.http
      .get<Tarjeta>(`${API_URL}/tarjetas/${id}`)
      .pipe(catchError((error: HttpErrorResponse) => this.manejarError(error)));
  }

  listarTiposTarjeta(): Observable<TipoTarjeta[]> {
    return this.http
      .get<TipoTarjeta[]>(`${API_URL}/tipos-tarjetas`)
      .pipe(catchError((error: HttpErrorResponse) => this.manejarError(error)));
  }

  private manejarError(error: HttpErrorResponse): Observable<never> {
    const body = error.error as CuerpoError | null;
    if (
      error.status !== 0 &&
      esCuerpoErrorEstructurado(body)
    ) {
      return throwError(() => new ApiError(body.codigo, body.mensaje));
    }
    return throwError(() => new ApiError('ERROR', 'Error inesperado'));
  }
}