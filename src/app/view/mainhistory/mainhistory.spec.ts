import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Mainhistory } from './mainhistory';

describe('Mainhistory', () => {
  let component: Mainhistory;
  let fixture: ComponentFixture<Mainhistory>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Mainhistory],
    }).compileComponents();

    fixture = TestBed.createComponent(Mainhistory);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
