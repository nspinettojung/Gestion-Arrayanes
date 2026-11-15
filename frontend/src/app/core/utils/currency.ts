const ARS_FORMATTER = new Intl.NumberFormat('es-AR', {
  style: 'currency',
  currency: 'ARS',
  minimumFractionDigits: 0,
  maximumFractionDigits: 0,
});

/** Formatea un monto en pesos argentinos, o devuelve un guion si no es válido. */
export function formatArs(value: number | string | null | undefined): string {
  if (value === null || value === undefined || value === '') {
    return '—';
  }

  const amount = typeof value === 'string' ? Number(value) : value;

  return Number.isFinite(amount) ? ARS_FORMATTER.format(amount) : '—';
}
