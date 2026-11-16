import { FormControl } from '@angular/forms';

import { dniOrPassportValidator, phoneValidator } from './contact';

describe('validadores de contacto', () => {
  const validateDni = (value: string) => dniOrPassportValidator(new FormControl(value));
  const validatePhone = (value: string) => phoneValidator(new FormControl(value));

  it('acepta DNI con y sin puntos', () => {
    expect(validateDni('12345678')).toBeNull();
    expect(validateDni('12.345.678')).toBeNull();
  });

  it('acepta pasaportes alfanuméricos', () => {
    expect(validateDni('AB123456')).toBeNull();
  });

  it('rechaza un DNI demasiado corto o con caracteres inválidos', () => {
    expect(validateDni('123')).toEqual({ dniOrPassport: true });
    expect(validateDni('1234 5678')).toEqual({ dniOrPassport: true });
  });

  it('omite la validación cuando el campo está vacío', () => {
    expect(validateDni('')).toBeNull();
    expect(validatePhone('')).toBeNull();
  });

  it('acepta teléfonos locales e internacionales', () => {
    expect(validatePhone('555-1234')).toBeNull();
    expect(validatePhone('+54 9 11 5555-1234')).toBeNull();
    expect(validatePhone('(011) 4444-5555')).toBeNull();
  });

  it('rechaza teléfonos con pocos dígitos o caracteres inválidos', () => {
    expect(validatePhone('123')).toEqual({ phone: true });
    expect(validatePhone('abc123456')).toEqual({ phone: true });
    expect(validatePhone('123456789012345678901')).toEqual({ phone: true });
  });
});
