/**
 * Utilidades de fecha que trabajan siempre en horario local.
 *
 * `Date#toISOString()` convierte a UTC: en Argentina (UTC-3) un `new Date()`
 * de las 22:00 devuelve la fecha del día siguiente. Estas funciones evitan
 * ese corrimiento usando los componentes locales de la fecha.
 */

export function toLocalIsoDate(date: Date = new Date()): string {
  const year = date.getFullYear();
  const month = `${date.getMonth() + 1}`.padStart(2, '0');
  const day = `${date.getDate()}`.padStart(2, '0');

  return `${year}-${month}-${day}`;
}

export function parseLocalDate(value: string): Date {
  const [year, month, day] = value.split('-').map(Number);

  return new Date(year, month - 1, day);
}

export function addDays(value: string, days: number): string {
  const date = parseLocalDate(value);
  date.setDate(date.getDate() + days);

  return toLocalIsoDate(date);
}

export function daysBetween(start: string, end: string): number {
  const startDate = parseLocalDate(start);
  const endDate = parseLocalDate(end);
  const millisecondsPerDay = 24 * 60 * 60 * 1000;

  return Math.round((endDate.getTime() - startDate.getTime()) / millisecondsPerDay);
}
