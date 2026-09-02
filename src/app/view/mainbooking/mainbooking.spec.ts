import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Mainbooking } from './mainbooking';

describe('Mainbooking', () => {
  let component: Mainbooking;
  let fixture: ComponentFixture<Mainbooking>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Mainbooking],
    }).compileComponents();

    fixture = TestBed.createComponent(Mainbooking);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
