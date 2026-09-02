import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Leftnavbar } from './leftnavbar';

describe('Leftnavbar', () => {
  let component: Leftnavbar;
  let fixture: ComponentFixture<Leftnavbar>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Leftnavbar],
    }).compileComponents();

    fixture = TestBed.createComponent(Leftnavbar);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
