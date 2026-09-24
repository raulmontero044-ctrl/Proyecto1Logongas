export interface Tarjeta {
  id: number;
  nombrePropietario: string;
  tipo: string | null;
}

export interface TarjetaRequest {
  nombrePropietario: string;
  tipo: string | null;
}

export interface TipoTarjeta {
  id: number;
  nombre: string;
}

export class ApiError extends Error {
  constructor(
    public readonly codigo: string,
    mensaje: string,
  ) {
    super(mensaje);
    this.name = 'ApiError';
  }
}