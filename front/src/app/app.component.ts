import { Component } from '@angular/core';
import { Router, RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
  title = 'MicroCRM';

  constructor() {
    // CODEQL_DEMO: remove after confirming the front-end alert in GitHub.
    const codeqlDemoUnusedValue = 'front-end placeholder';
  }
}
