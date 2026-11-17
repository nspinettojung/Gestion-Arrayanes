// @ts-check
const eslint = require('@eslint/js');
const { defineConfig } = require('eslint/config');
const tseslint = require('typescript-eslint');
const angular = require('angular-eslint');

module.exports = defineConfig([
  // Contrato y tipos generados por `pnpm api`: no se editan a mano.
  { ignores: ['src/app/core/api/api-types.ts'] },
  {
    files: ['**/*.ts'],
    extends: [
      eslint.configs.recommended,
      tseslint.configs.recommended,
      tseslint.configs.stylistic,
      angular.configs.tsRecommended,
    ],
    processor: angular.processInlineTemplates,
    rules: {
      '@angular-eslint/directive-selector': [
        'error',
        {
          type: 'attribute',
          prefix: 'app',
          style: 'camelCase',
        },
      ],
      // Los componentes usan selectores con prefijo "gda-" y algunos sin prefijo.
      // Unificarlos requiere renombrar templates: queda como deuda técnica.
      '@angular-eslint/component-selector': 'warn',
      '@angular-eslint/no-output-native': 'warn',
      '@angular-eslint/prefer-inject': 'warn',
    },
  },
  {
    files: ['**/*.html'],
    extends: [angular.configs.templateRecommended, angular.configs.templateAccessibility],
    rules: {
      // Reglas de accesibilidad detectadas en la UI actual. No bloquean el CI
      // todavía; se corrigen junto con los formularios en una fase posterior.
      '@angular-eslint/template/label-has-associated-control': 'warn',
      '@angular-eslint/template/click-events-have-key-events': 'warn',
      '@angular-eslint/template/interactive-supports-focus': 'warn',
      '@angular-eslint/template/no-autofocus': 'warn',
    },
  },
]);
