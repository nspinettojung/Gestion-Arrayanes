import { HttpErrorResponse } from '@angular/common/http';

function extractBackendMessage(body: unknown): string | null {
  if (typeof body === 'string' && body.trim() && !body.trim().startsWith('<')) {
    return body.trim();
  }

  if (body && typeof body === 'object') {
    const record = body as Record<string, unknown>;
    for (const key of ['message', 'error', 'detail']) {
      const value = record[key];
      if (typeof value === 'string' && value.trim()) {
        return value.trim();
      }
    }
  }

  return null;
}

/**
 * Convierte cualquier error HTTP en un mensaje legible para el usuario.
 * Nunca lanza: los handlers pueden usarlo sin riesgo de romper el flujo.
 */
export function describeHttpError(error: unknown): string {
  if (!(error instanceof HttpErrorResponse)) {
    return error instanceof Error && error.message ? error.message : 'Ocurrió un error inesperado.';
  }

  if (error.status === 0) {
    return 'No se pudo conectar con la API. Verificá que el servidor esté funcionando.';
  }

  const backendMessage = extractBackendMessage(error.error);
  if (backendMessage) {
    return backendMessage;
  }

  switch (error.status) {
    case 400:
      return 'Los datos enviados no son válidos.';
    case 403:
      return 'No tenés permisos para realizar esta acción.';
    case 404:
      return 'No se encontró el recurso solicitado.';
    case 409:
      return 'La operación entra en conflicto con datos existentes.';
    case 500:
    case 502:
    case 503:
      return 'El servidor tuvo un problema. Intentá nuevamente en unos segundos.';
    default:
      return `Ocurrió un error inesperado (${error.status}).`;
  }
}
