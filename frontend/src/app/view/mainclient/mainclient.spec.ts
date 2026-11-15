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
});
