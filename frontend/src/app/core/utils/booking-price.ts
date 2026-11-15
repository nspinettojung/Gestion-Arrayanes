/** Recargo por persona adicional a la primera, por noche. */
export const EXTRA_PERSON_PRICE = 5000;

/**
 * Calcula el precio total de una reserva replicando la regla del backend
 * (`BookingPrice`): precio base por noche más el recargo por personas extra.
 */
export function calculateBookingPrice(
  basePrice: number | string,
  amountPeople: number | string,
  nights: number | string,
): number {
  const price = Number(basePrice);
  const people = Number(amountPeople);
  const stayNights = Number(nights);

  if (
    !Number.isFinite(price) ||
    !Number.isFinite(people) ||
    !Number.isFinite(stayNights) ||
    stayNights <= 0
  ) {
    return 0;
  }

  return (price + Math.max(0, people - 1) * EXTRA_PERSON_PRICE) * stayNights;
}
