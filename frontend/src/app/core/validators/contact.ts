import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

/**
 * DNI argentino (7 u 8 dígitos, con o sin puntos) o pasaporte alfanumérico.
 * Debe coincidir con la validación del backend (`ClientRequestDTO`).
 */
export const DNI_OR_PASSPORT_PATTERN = /^[0-9A-Za-z.-]{6,15}$/;

/**
 * Teléfono con entre 6 y 20 caracteres y al menos 6 dígitos.
 * Admite +, espacios, guiones y paréntesis.
 */
export const PHONE_PATTERN = /^(?=(?:\D*\d){6,})[+0-9()\s-]{6,20}$/;

export const DNI_OR_PASSPORT_MESSAGE =
  'El DNI o pasaporte debe tener entre 6 y 15 caracteres alfanuméricos.';
export const PHONE_MESSAGE = 'El teléfono debe tener entre 6 y 20 caracteres y al menos 6 dígitos.';

export const dniOrPassportValidator: ValidatorFn = (
  control: AbstractControl,
): ValidationErrors | null => {
  const value = typeof control.value === 'string' ? control.value.trim() : '';

  return !value || DNI_OR_PASSPORT_PATTERN.test(value) ? null : { dniOrPassport: true };
};

export const phoneValidator: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {
  const value = typeof control.value === 'string' ? control.value.trim() : '';

  return !value || PHONE_PATTERN.test(value) ? null : { phone: true };
};
