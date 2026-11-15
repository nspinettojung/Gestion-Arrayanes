import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Mainapartment } from './mainapartment';

describe('Mainapartment', () => {
  let component: Mainapartment;
  let fixture: ComponentFixture<Mainapartment>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Mainapartment],
    }).compileComponents();

    fixture = TestBed.createComponent(Mainapartment);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
