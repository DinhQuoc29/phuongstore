import { Component, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-confirm-dialog',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="overlay" (click)="onCancel()">
      <div class="dialog" (click)="$event.stopPropagation()" role="alertdialog">
        <div class="dialog__drag-handle"></div>
        <div class="dialog__icon"><i class="fa-solid fa-triangle-exclamation"></i></div>
        <h3 class="dialog__title">{{ title() }}</h3>
        <p class="dialog__message">{{ message() }}</p>
        <div class="dialog__actions">
          <button class="btn btn--ghost" (click)="onCancel()">Hủy</button>
          <button class="btn btn--danger" (click)="onConfirm()">{{ confirmLabel() }}</button>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .overlay {
      position: fixed; inset: 0;
      background: rgba(0, 0, 0, 0.45);
      display: flex; align-items: center; justify-content: center;
      z-index: 1000; padding: var(--space-4);
      animation: fadeIn 0.15s ease;
      backdrop-filter: blur(2px);
    }
    .dialog {
      background: #FFFFFF;
      border: 1px solid var(--color-border);
      border-radius: var(--radius-xl);
      padding: var(--space-6);
      max-width: 380px; width: 100%;
      text-align: center;
      animation: slideDown 0.18s ease;
      box-shadow: 0 12px 32px rgba(0,0,0,0.15);
    }
    .dialog__drag-handle {
      display: none;
    }
    .dialog__icon {
      font-size: 32px;
      margin-bottom: var(--space-3);
      color: #dc2626;
    }
    .dialog__title {
      font-size: 17px;
      font-weight: 700;
      margin-bottom: var(--space-2);
      color: var(--color-text-primary);
    }
    .dialog__message {
      color: var(--color-text-muted);
      font-size: var(--font-size-sm);
      margin-bottom: var(--space-5);
      line-height: 1.5;
    }
    .dialog__actions {
      display: flex;
      gap: var(--space-3);
      justify-content: center;
    }
    .btn--danger {
      background: #B91C1C;
      color: #FFFFFF;
      border: 1px solid #B91C1C;
      padding: 8px var(--space-5);
      border-radius: var(--radius-md);
      font-family: var(--font-family);
      font-size: var(--font-size-sm);
      font-weight: 600;
      cursor: pointer;
      transition: background var(--transition-fast);
    }
    .btn--danger:hover {
      background: #991B1B;
      border-color: #991B1B;
    }

    /* ─── Mobile: Bottom Sheet Pattern (< 768px) ─── */
    @media (max-width: 767px) {
      .overlay {
        align-items: flex-end;
        padding: 0;
      }
      .dialog {
        max-width: 100%;
        border-radius: 20px 20px 0 0;
        padding: 12px 20px calc(24px + env(safe-area-inset-bottom, 0px));
        animation: slideUpSheet 0.22s cubic-bezier(0.16, 1, 0.3, 1);
        border-bottom: none;
        border-left: none;
        border-right: none;
      }
      .dialog__drag-handle {
        display: block;
        width: 38px;
        height: 4px;
        background: #cbd5e1;
        border-radius: 9999px;
        margin: 0 auto 16px;
      }
      .dialog__actions {
        flex-direction: column-reverse;
        gap: 10px;
      }
      .dialog__actions .btn {
        width: 100%;
        height: 44px;
        font-size: 15px;
        font-weight: 600;
        display: flex;
        align-items: center;
        justify-content: center;
      }
    }

    @keyframes slideUpSheet {
      from { transform: translateY(100%); }
      to   { transform: translateY(0); }
    }
    @keyframes fadeIn {
      from { opacity: 0; }
      to   { opacity: 1; }
    }
    @keyframes slideDown {
      from { opacity: 0; transform: translateY(-12px); }
      to   { opacity: 1; transform: translateY(0); }
    }
  `]
})
export class ConfirmDialogComponent {
  title   = input('Xác nhận xóa');
  message = input('Bạn có chắc chắn muốn xóa? Hành động này không thể hoàn tác.');
  confirmLabel = input('Xóa');

  confirmed = output<void>();
  cancelled = output<void>();

  onConfirm() { this.confirmed.emit(); }
  onCancel()  { this.cancelled.emit(); }
}
