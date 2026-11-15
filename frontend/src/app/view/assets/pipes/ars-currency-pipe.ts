import { Pipe, PipeTransform } from '@angular/core';
import { formatArs } from '../../../core/utils/currency';

/**
 * Formatea montos en pesos argentinos de manera uniforme en toda la app.
 * Devuelve un guion cuando el valor no es un número válido.
 */
@Pipe({
  name: 'arsCurrency',
})
export class ArsCurrencyPipe implements PipeTransform {
  transform(value: number | string | null | undefined): string {
    return formatArs(value);
  }
}
