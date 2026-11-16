import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Mainclient } from './mainclient';

describe('Mainclient', () => {
  let component: Mainclient;
  let fixture: ComponentFixture<Mainclient>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Mainclient],
    }).compileComponents();

    fixture = TestBed.createComponent(Mainclient);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('valida el formato del DNI y del teléfono', () => {
    component.buildForm();
    component.clientForm.patchValue({ clientDni: '12', clientPhone: 'abc' });

    expect(component.clientForm.get('clientDni')?.hasError('dniOrPassport')).toBe(true);
    expect(component.clientForm.get('clientPhone')?.hasError('phone')).toBe(true);
  });

  it('acepta un DNI con puntos y un teléfono internacional', () => {
    component.buildForm();
    component.clientForm.patchValue({
      clientName: 'Ana',
      clientLastName: 'Pérez',
      clientDni: '12.345.678',
      clientPhone: '+54 9 11 5555-1234',
    });

    expect(component.clientForm.valid).toBe(true);
  });
});
