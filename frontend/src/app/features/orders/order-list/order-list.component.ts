import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { OrderService } from '../../../core/services/order.service';
import { ToastService } from '../../../core/services/toast.service';
import { InvoiceExportService } from '../../../core/services/invoice-export.service';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge.component';
import { ConfirmDialogComponent } from '../../../shared/components/confirm-dialog/confirm-dialog.component';
import {
  OrderSummary, OrderFilterParams,
  PickupStatus, PaymentStatus, ShippingStatus,
  PICKUP_STATUS_LABELS, PAYMENT_STATUS_LABELS, SHIPPING_STATUS_LABELS
} from '../../../core/models/order.model';

@Component({
  selector: 'app-order-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, StatusBadgeComponent, ConfirmDialogComponent],
  templateUrl: './order-list.component.html',
  styleUrl: './order-list.component.css'
})
export class OrderListComponent implements OnInit {
  private readonly orderService = inject(OrderService);
  private readonly toast        = inject(ToastService);
  private readonly invoiceExportService = inject(InvoiceExportService);

  exportingId   = signal<number | null>(null);

  orders        = signal<OrderSummary[]>([]);
  loading       = signal(true);
  totalPages    = signal(0);
  totalElements = signal(0);
  currentPage   = signal(0);
  readonly pageSize = 15;

  // Filters
  keyword        = signal('');
  filterPickup   = signal<PickupStatus | ''>('');
  filterPayment  = signal<PaymentStatus | ''>('');
  filterShipping = signal<ShippingStatus | ''>('');
  filterFrom     = signal('');
  filterTo       = signal('');
  filterHasNote  = signal<boolean | ''>('');
  showFilters    = signal(false);

  deletingId     = signal<number | null>(null);
  deletingLabel  = computed(() => {
    const o = this.orders().find(o => o.id === this.deletingId());
    return o ? `Đơn #${o.id} — ${o.customerName}` : '';
  });

  readonly pickupOptions:   { value: PickupStatus;   label: string }[] =
    (Object.keys(PICKUP_STATUS_LABELS) as PickupStatus[]).map(k => ({ value: k, label: PICKUP_STATUS_LABELS[k] }));
  readonly paymentOptions:  { value: PaymentStatus;  label: string }[] =
    (Object.keys(PAYMENT_STATUS_LABELS) as PaymentStatus[]).map(k => ({ value: k, label: PAYMENT_STATUS_LABELS[k] }));
  readonly shippingOptions: { value: ShippingStatus; label: string }[] =
    (Object.keys(SHIPPING_STATUS_LABELS) as ShippingStatus[]).map(k => ({ value: k, label: SHIPPING_STATUS_LABELS[k] }));

  hasActiveFilters = computed(() =>
    !!(this.filterPickup() || this.filterPayment() || this.filterShipping() ||
       this.filterFrom() || this.filterTo() || this.keyword() || this.filterHasNote() !== '')
  );

  ngOnInit() { this.load(); }

  load() {
    this.loading.set(true);
    const params: OrderFilterParams = {
      page: this.currentPage(), size: this.pageSize,
      keyword:        this.keyword()        || undefined,
      pickupStatus:   (this.filterPickup()   as PickupStatus)   || undefined,
      paymentStatus:  (this.filterPayment()  as PaymentStatus)  || undefined,
      shippingStatus: (this.filterShipping() as ShippingStatus) || undefined,
      fromDate:       this.filterFrom()      || undefined,
      toDate:         this.filterTo()        || undefined,
      hasNote:        this.filterHasNote() !== '' ? (this.filterHasNote() as boolean) : undefined,
    };
    this.orderService.searchOrders(params).subscribe({
      next: p => { this.orders.set(p.content); this.totalPages.set(p.totalPages); this.totalElements.set(p.totalElements); this.loading.set(false); },
      error: () => this.loading.set(false)
    });
  }

  toggleHasNote() {
    this.filterHasNote.update(v => v === true ? '' : true);
    this.search();
  }

  search() { this.currentPage.set(0); this.load(); }
  clearFilters() {
    this.keyword.set(''); this.filterPickup.set(''); this.filterPayment.set('');
    this.filterShipping.set(''); this.filterFrom.set(''); this.filterTo.set('');
    this.filterHasNote.set('');
    this.currentPage.set(0); this.load();
  }
  changePage(p: number) { this.currentPage.set(p); this.load(); }
  toggleFilters()       { this.showFilters.update(v => !v); }

    exportInvoice(id: number, event?: Event) {
    if (event) event.stopPropagation();
    this.exportingId.set(id);
    this.orderService.getOrderById(id).subscribe({
      next: order => {
        this.invoiceExportService.exportOrder(order);
        this.toast.success('Đã xuất hóa đơn ảnh thành công!');
        this.exportingId.set(null);
      },
      error: () => {
        this.toast.error('Không thể tải chi tiết đơn hàng để xuất hóa đơn');
        this.exportingId.set(null);
      }
    });
  }
  confirmDelete(id: number) { this.deletingId.set(id); }
  cancelDelete()             { this.deletingId.set(null); }
  doDelete() {
    const id = this.deletingId(); if (!id) return;
    this.orderService.deleteOrder(id).subscribe({
      next: () => { this.toast.success('Đã xóa đơn hàng.'); this.deletingId.set(null); this.load(); }
    });
  }

  formatCurrency(n: number): string {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND', maximumFractionDigits: 0 }).format(n);
  }

  pages() { return Array.from({ length: this.totalPages() }, (_, i) => i); }
}
