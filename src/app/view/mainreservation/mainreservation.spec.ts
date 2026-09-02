import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Mainreservation } from './mainreservation';

describe('Mainreservation', () => {
  let component: Mainreservation;
  let fixture: ComponentFixture<Mainreservation>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Mainreservation],
    }).compileComponents();

    fixture = TestBed.createComponent(Mainreservation);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
