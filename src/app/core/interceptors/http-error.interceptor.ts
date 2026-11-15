import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { NotificationService } from '../services/notification/notification';
import { describeHttpError } from '../utils/http-error';

/**
 * Muestra un aviso consistente cuando falla una request y deja pasar el error
 * original (con `status` y `error`) para que cada componente decida qué hacer.
 */
export const httpErrorInterceptor: HttpInterceptorFn = (request, next) => {
  const notification = inject(NotificationService);

  return next(request).pipe(
    catchError((error: unknown) => {
      if (error instanceof HttpErrorResponse) {
        notification.error(describeHttpError(error));
      }
      return throwError(() => error);
    }),
  );
};
