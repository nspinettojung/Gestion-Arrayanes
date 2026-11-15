import { Component, input } from '@angular/core';

@Component({
  selector: 'gda-card',
  imports: [],
  templateUrl: './card.html',
  styleUrl: './card.scss',
})
export class Card {
  title = input<string>('');
  dataNumber = input<number | string>('—');
  dataIcon = input<string>('');
  emojiIcon = input<string>('');
  dataPercentage = input<number | string>();
  classText = input<string>('');
  barPercentage = input<number>(0);
  showBar = input<boolean>(false);
}
