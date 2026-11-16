import { HttpErrorResponse } from '@angular/common/http';

import { describeHttpError } from './http-error';

function problemBody(overrides: Record<string, unknown> = {}) {
  return {
    type: 'about:blank',
    title: 'Bad Request',
    status: 400,
    detail: 'La solicitud contiene datos inválidos.',
    requestId: '4c5f3b2a-1111-4abc-9def-1234567890ab',
    timestamp: '2026-11-16T19:00:00',
    ...overrides,
  };
}

function httpError(status: number, body: unknown): HttpErrorResponse {
  return new HttpErrorResponse({ status, error: body });
}

describe('describeHttpError', () => {
  it('devuelve el cuerpo de texto plano del backend', () => {
    expect(describeHttpError(httpError(400, 'Datos inválidos'))).toBe('Datos inválidos');
  });

  it('ignora respuestas HTML del fallback', () => {
    expect(describeHttpError(httpError(404, '<html>index</html>'))).toBe(
      'No se encontró el recurso solicitado.',
    );
  });

  it('usa el detail del ProblemDetail RFC 9457', () => {
    expect(describeHttpError(httpError(400, problemBody()))).toBe(
      'La solicitud contiene datos inválidos.',
    );
  });

  it('lista los errores por campo del ProblemDetail', () => {
    const body = problemBody({
      errors: [{ field: 'clientName', message: 'no puede estar vacío' }],
    });

    expect(describeHttpError(httpError(400, body))).toBe(
      'La solicitud contiene datos inválidos. (clientName: no puede estar vacío)',
    );
  });

  it('muestra los errores por campo aunque falte el detail', () => {
    const body = problemBody({
      detail: undefined,
      errors: [
        { field: 'dni', message: 'no puede estar vacío' },
        { field: 'phone', message: 'formato inválido' },
      ],
    });

    expect(describeHttpError(httpError(400, body))).toBe(
      'dni: no puede estar vacío; phone: formato inválido',
    );
  });

  it('status 0 devuelve el mensaje de conexión', () => {
    expect(describeHttpError(httpError(0, null))).toBe(
      'No se pudo conectar con la API. Verificá que el servidor esté funcionando.',
    );
  });

  it('sin cuerpo del backend usa el fallback según el status', () => {
    expect(describeHttpError(httpError(409, null))).toBe(
      'La operación entra en conflicto con datos existentes.',
    );
    expect(describeHttpError(httpError(500, null))).toBe(
      'El servidor tuvo un problema. Intentá nuevamente en unos segundos.',
    );
  });

  it('con errores que no son HttpErrorResponse devuelve genérico o el mensaje', () => {
    expect(describeHttpError(new Error('boom'))).toBe('boom');
    expect(describeHttpError('algo')).toBe('Ocurrió un error inesperado.');
  });
});
