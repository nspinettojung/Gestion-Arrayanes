import { Component, inject, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { BuildInfoService } from '../../core/services/build-info/build-info';

@Component({
  selector: 'gda-leftnavbar',
  imports: [RouterLink],
  templateUrl: './leftnavbar.html',
  styleUrl: './leftnavbar.scss',
})
export class Leftnavbar implements OnInit {
  private buildInfoService = inject(BuildInfoService);

  readonly buildInfo = this.buildInfoService.buildInfo;
  readonly buildLabel = this.buildInfoService.label;
  readonly builtAtLabel = this.buildInfoService.builtAtLabel;

  ngOnInit(): void {
    this.buildInfoService.load();
  }
}
