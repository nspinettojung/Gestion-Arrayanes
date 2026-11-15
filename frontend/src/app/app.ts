import { Component, signal } from '@angular/core';
import { Leftnavbar } from './view/leftnavbar/leftnavbar';
import { Navbar } from './view/navbar/navbar';
import { Main } from './view/main/main';

@Component({
  selector: 'app-root',
  imports: [Main, Leftnavbar, Navbar],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App {
  protected readonly title = signal('GestionArrayanes');
}
