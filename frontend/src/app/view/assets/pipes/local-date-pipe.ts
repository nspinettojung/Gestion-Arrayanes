import { Pipe, PipeTransform } from '@angular/core';
import { parseLocalDate } from '../../../core/utils/date';

const LONG_FORMATTER = new Intl.DateTimeFormat('es-AR', {
  day: 'numeric',
  month: 'long',
});

const SHORT_FORMATTER = new Intl.DateTimeFormat('es-AR', {
  day: '2-digit',
  month: '2-digit',
  year: 'numeric',
});

/**
 * Formatea fechas `yyyy-MM-dd` interpretándolas en horario local.
 *
 * Usar `DatePipe` con estas cadenas las interpreta como UTC y en Argentina
 * (UTC-3) muestra el día anterior.
 */
@Pipe({
  name: 'localDate',
})
export class LocalDatePipe implements PipeTransform {
  transform(value: string | Date | null | undefined, style: 'long' | 'short' = 'long'): string {
    if (!value) {
      return '—';
    }

    const date = typeof value === 'string' ? parseLocalDate(value) : value;

    if (Number.isNaN(date.getTime())) {
      return '—';
    }

    return style === 'short' ? SHORT_FORMATTER.format(date) : LONG_FORMATTER.format(date);
  }
}
