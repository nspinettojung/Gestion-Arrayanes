import { HttpErrorResponse } from '@angular/common/http';

function formatFieldError(error: unknown): string | null {
  if (!error || typeof error !== 'object') {
    return null;
  }

  const record = error as Record<string, unknown>;
  const field = record['field'];
  const message = record['message'];

  if (typeof field !== 'string' || typeof message !== 'string' || !message.trim()) {
    return null;
  }

  return `${field}: ${message.trim()}`;
}

function extractFieldErrors(record: Record<string, unknown>): string | null {
  const errors = record['errors'];

  if (!Array.isArray(errors)) {
    return null;
  }

  const joined = errors
    .map(formatFieldError)
    .filter((value): value is string => value !== null)
    .join('; ');

  return joined || null;
}

function extractBackendMessage(body: unknown): string | null {
  if (typeof body === 'string' && body.trim() && !body.trim().startsWith('<')) {
    return body.trim();
  }

  if (body && typeof body === 'object') {
    const record = body as Record<string, unknown>;
    const detail =
      ['detail', 'message', 'error']
        .map((key) => record[key])
        .find((value): value is string => typeof value === 'string' && value.trim().length > 0)
        ?.trim() ?? null;
    const fieldErrors = extractFieldErrors(record);

    if (detail && fieldErrors) {
      return `${detail} (${fieldErrors})`;
    }

    return detail ?? fieldErrors;
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
