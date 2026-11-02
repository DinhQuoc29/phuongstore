import { Component, OnInit, inject, input, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { OrderService } from '../../../core/services/order.service';
import { ProductService } from '../../../core/services/product.service';
import { ToastService } from '../../../core/services/toast.service';
import { ConfirmDialogComponent } from '../../../shared/components/confirm-dialog/confirm-dialog.component';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge.component';
import {
  Order, OrderItem, PaymentStatus, ShippingStatus,
  PAYMENT_STATUS_LABELS, SHIPPING_STATUS_LABELS
} from '../../../core/models';
import { Product } from '../../../core/models/product.model';

interface EditableItem {
  id: number;
  productId: number;
  productName: string;
  productCode: string;
  quantity: number;
  pickedQuantity: number;
  shippedQuantity: number;
  costPrice: number;
  unitPrice: number;
  lineTotal: number;
  lineCostTotal?: number;
  lineProfit?: number;
  editing: boolean;
  // edit form state
  editProductSearch: string;
  editQty: number;
  editPickedQty: number;
  editCostPrice: number;
  editPrice: number;
  editProductId: number;
  editProductName: string;
}

@Component({
  selector: 'app-order-detail',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, ConfirmDialogComponent, StatusBadgeComponent],
  templateUrl: './order-detail.component.html',
  styleUrl: './order-detail.component.css'
})
export class OrderDetailComponent implements OnInit {
  id = input.required<string>();

  private readonly orderService   = inject(OrderService);
  private readonly productService = inject(ProductService);
  private readonly toast          = inject(ToastService);
  private readonly router         = inject(Router);

  order   = signal<Order | null>(null);
  loading = signal(true);
  showDeleteDialog = signal(false);

  // Products for search
  allProducts = signal<Product[]>([]);

  // Editable items list
  editableItems = signal<EditableItem[]>([]);

  // Per-item update trackers
  updatingItem = signal<number | null>(null);
  updatingPickedItem = signal<number | null>(null);

  // Payment & Shipping status
  updatingPayment = signal(false);
  updatingShipping = signal(false);

  // Deposit editing
  editingDeposit = signal(false);
  editDepositValue = signal(0);
  updatingDeposit = signal(false);
  depositError = signal<string | null>(null);

  // Add item form
  showAddItem      = signal(false);
  addProductSearch = signal('');
  addProductId     = signal<number | null>(null);
  addProductName   = signal('');
  addQty           = signal(1);
  addCostPrice     = signal(0);
  addPrice         = signal(0);
  addingItem       = signal(false);
  showAddDrop      = signal(false);

  readonly paymentOptions: { value: PaymentStatus; label: string }[] =
    (Object.keys(PAYMENT_STATUS_LABELS) as PaymentStatus[])
      .map(k => ({ value: k, label: PAYMENT_STATUS_LABELS[k] }));

  readonly shippingOptions: { value: ShippingStatus; label: string }[] =
    (Object.keys(SHIPPING_STATUS_LABELS) as ShippingStatus[])
      .map(k => ({ value: k, label: SHIPPING_STATUS_LABELS[k] }));

  // Filtered products for add search
  filteredAddProducts = computed(() => {
    const kw = this.addProductSearch().toLowerCase().trim();
    const all = this.allProducts();
    if (!kw) return all.slice(0, 8);
    return all.filter(p =>
      p.productName.toLowerCase().includes(kw) || p.productCode?.toLowerCase().includes(kw)
    ).slice(0, 8);
  });

  // Filtered products for inline edit search
  editProductSearchTerm = signal('');
  editingItemId = signal<number | null>(null);
  filteredEditProducts = computed(() => {
    const kw = this.editProductSearchTerm().toLowerCase().trim();
    const all = this.allProducts();
    if (!kw) return all.slice(0, 8);
    return all.filter(p =>
      p.productName.toLowerCase().includes(kw) || p.productCode?.toLowerCase().includes(kw)
    ).slice(0, 8);
  });

  ngOnInit() {
    this.loadOrder();
    this.productService.getAll().subscribe(ps => this.allProducts.set(ps));
  }

  loadOrder() {
    this.loading.set(true);
    this.orderService.getOrderById(+this.id()).subscribe({
      next: o => {
        this.order.set(o);
        this.editableItems.set(this.toEditableItems(o.items));
        this.loading.set(false);
      },
      error: () => { this.loading.set(false); this.router.navigate(['/orders']); }
    });
  }

  private toEditableItems(items: OrderItem[]): EditableItem[] {
    return items.map(i => ({
      id: i.id,
      productId: i.productId,
      productName: i.productName,
      productCode: i.productCode,
      quantity: i.quantity,
      pickedQuantity: i.pickedQuantity,
      shippedQuantity: i.shippedQuantity,
      costPrice: i.costPrice || 0,
      unitPrice: i.unitPrice,
      lineTotal: i.lineTotal,
      lineCostTotal: i.lineCostTotal ?? ((i.costPrice || 0) * i.quantity),
      lineProfit: i.lineProfit ?? (i.lineTotal - (i.costPrice || 0) * i.quantity),
      editing: false,
      editProductSearch: i.productName,
      editQty: i.quantity,
      editPickedQty: i.pickedQuantity,
      editCostPrice: i.costPrice || 0,
      editPrice: i.unitPrice,
      editProductId: i.productId,
      editProductName: i.productName,
    }));
  }

  private syncFromOrder(o: Order) {
    this.order.set(o);
    this.editableItems.set(this.toEditableItems(o.items));
  }

  // ── Picked quantity ──────────────────────────────────────────

  increasePicked(item: EditableItem): void {
    if (item.pickedQuantity >= item.quantity) return;
    this.patchPicked(item, item.pickedQuantity + 1);
  }

  decreasePicked(item: EditableItem): void {
    if (item.pickedQuantity <= 0) return;
    this.patchPicked(item, item.pickedQuantity - 1);
  }

  quickSetPicked(item: EditableItem): void {
    const input = window.prompt(
      `Nhập số lượng đã lấy cho "${item.productName}" (từ 0 đến ${item.quantity}):`,
      item.pickedQuantity.toString()
    );
    if (input === null) return;
    const newVal = parseInt(input.trim(), 10);
    if (isNaN(newVal)) {
      this.toast.error('Vui lòng nhập số hợp lệ.');
      return;
    }
    if (newVal < 0 || newVal > item.quantity) {
      this.toast.error(`Số lượng phải từ 0 đến ${item.quantity}.`);
      return;
    }
    if (newVal === item.pickedQuantity) return;
    this.patchPicked(item, newVal);
  }

  private patchPicked(item: EditableItem, newVal: number): void {
    this.updatingPickedItem.set(item.id);
    this.orderService.updatePickedQuantity(item.id, { pickedQuantity: newVal }).subscribe({
      next: () => {
        this.orderService.getOrderById(+this.id()).subscribe({
          next: full => {
            this.syncFromOrder(full);
            this.updatingPickedItem.set(null);
            this.toast.success('Đã cập nhật số lượng đã lấy!');
          },
          error: () => this.updatingPickedItem.set(null)
        });
      },
      error: (err) => {
        const msg = err?.error?.message || 'Không thể cập nhật số lượng đã lấy. Vui lòng thử lại.';
        this.toast.error(msg);
        this.updatingPickedItem.set(null);
      }
    });
  }

  // ── Payment & Shipping status ─────────────────────────────────

  setPaymentStatus(status: PaymentStatus) {
    this.updatingPayment.set(true);
    this.orderService.updatePaymentStatus(+this.id(), { paymentStatus: status }).subscribe({
      next: o => {
        this.syncFromOrder(o);
        this.updatingPayment.set(false);
        this.toast.success('Đã cập nhật trạng thái thanh toán!');
      },
      error: () => this.updatingPayment.set(false)
    });
  }

  setShippingStatus(status: ShippingStatus) {
    this.updatingShipping.set(true);
    this.orderService.updateShippingStatus(+this.id(), { shippingStatus: status }).subscribe({
      next: o => {
        this.syncFromOrder(o);
        this.updatingShipping.set(false);
        this.toast.success('Đã cập nhật trạng thái vận chuyển!');
      },
      error: () => this.updatingShipping.set(false)
    });
  }

  // ── Deposit editing ──────────────────────────────────────────

  startEditDeposit() {
    this.editDepositValue.set(this.depositAmount());
    this.depositError.set(null);
    this.editingDeposit.set(true);
  }

  cancelEditDeposit() {
    this.editingDeposit.set(false);
    this.depositError.set(null);
  }

  setDepositPreset(amount: number) {
    this.editDepositValue.set(amount);
    this.validateDeposit(amount);
  }

  onDepositInputChange(val: any) {
    const num = val === '' || val === null || val === undefined ? 0 : +val;
    this.editDepositValue.set(num);
    this.validateDeposit(num);
  }

  validateDeposit(val: number): boolean {
    if (val < 0) {
      this.depositError.set('Tiền đặt cọc không được âm.');
      return false;
    }
    if (val > this.totalAmount()) {
      this.depositError.set(`Tiền đặt cọc không được lớn hơn tổng tiền (${this.formatCurrency(this.totalAmount())}).`);
      return false;
    }
    this.depositError.set(null);
    return true;
  }

  saveDeposit() {
    const val = this.editDepositValue();
    if (!this.validateDeposit(val)) return;

    if (val === this.depositAmount()) {
      this.editingDeposit.set(false);
      return;
    }

    this.updatingDeposit.set(true);
    this.orderService.updateDeposit(+this.id(), { depositAmount: val }).subscribe({
      next: updatedOrder => {
        this.syncFromOrder(updatedOrder);
        this.editingDeposit.set(false);
        this.updatingDeposit.set(false);
        this.toast.success('Đã cập nhật tiền đặt cọc!');
      },
      error: err => {
        const msg = err?.error?.message || 'Không thể cập nhật tiền đặt cọc. Vui lòng thử lại.';
        this.depositError.set(msg);
        this.toast.error(msg);
        this.updatingDeposit.set(false);
      }
    });
  }

  // ── Inline item edit ─────────────────────────────────────────

  startEdit(item: EditableItem) {
    // Close other open edits
    this.editableItems.update(items => items.map(i => ({ ...i, editing: false })));
    this.editingItemId.set(item.id);
    this.editProductSearchTerm.set(item.productName);
    this.editableItems.update(items => items.map(i =>
      i.id === item.id
        ? {
            ...i,
            editing: true,
            editProductSearch: i.productName,
            editQty: i.quantity,
            editPickedQty: i.pickedQuantity,
            editCostPrice: i.costPrice || 0,
            editPrice: i.unitPrice,
            editProductId: i.productId,
            editProductName: i.productName
          }
        : i
    ));
  }

  cancelEdit(item: EditableItem) {
    this.editableItems.update(items => items.map(i =>
      i.id === item.id ? { ...i, editing: false } : i
    ));
    this.editingItemId.set(null);
  }

  selectEditProduct(item: EditableItem, p: Product) {
    this.editableItems.update(items => items.map(i =>
      i.id === item.id
        ? { ...i, editProductId: p.id, editProductName: p.productName, editProductSearch: p.productName }
        : i
    ));
    this.editProductSearchTerm.set(p.productName);
  }

  saveEdit(item: EditableItem) {
    this.updatingItem.set(item.id);
    const needUpdatePicked = item.editPickedQty !== undefined && item.editPickedQty !== item.pickedQuantity;
    this.orderService.updateOrderItem(item.id, {
      productId: item.editProductId,
      quantity: item.editQty,
      costPrice: item.editCostPrice || 0,
      unitPrice: item.editPrice
    }).subscribe({
      next: (order) => {
        if (needUpdatePicked) {
          const clampedPicked = Math.min(Math.max(item.editPickedQty, 0), item.editQty);
          this.orderService.updatePickedQuantity(item.id, { pickedQuantity: clampedPicked }).subscribe({
            next: () => {
              this.orderService.getOrderById(+this.id()).subscribe({
                next: full => {
                  this.syncFromOrder(full);
                  this.editingItemId.set(null);
                  this.updatingItem.set(null);
                  this.toast.success('Đã cập nhật sản phẩm!');
                },
                error: () => {
                  this.editingItemId.set(null);
                  this.updatingItem.set(null);
                }
              });
            },
            error: () => {
              this.syncFromOrder(order);
              this.editingItemId.set(null);
              this.updatingItem.set(null);
              this.toast.warning('Đã cập nhật sản phẩm nhưng không thể cập nhật số lượng đã lấy.');
            }
          });
        } else {
          this.syncFromOrder(order);
          this.editingItemId.set(null);
          this.updatingItem.set(null);
          this.toast.success('Đã cập nhật sản phẩm!');
        }
      },
      error: () => {
        this.toast.error('Không thể cập nhật sản phẩm. Vui lòng thử lại.');
        this.updatingItem.set(null);
      }
    });
  }

  deleteItem(item: EditableItem) {
    if (this.editableItems().length <= 1) {
      this.toast.error('Đơn hàng phải có ít nhất một sản phẩm.');
      return;
    }
    this.updatingItem.set(item.id);
    this.orderService.deleteOrderItem(item.id).subscribe({
      next: o => {
        this.syncFromOrder(o);
        this.updatingItem.set(null);
        this.toast.success('Đã xóa sản phẩm!');
      },
      error: () => {
        this.toast.error('Không thể xóa sản phẩm. Vui lòng thử lại.');
        this.updatingItem.set(null);
      }
    });
  }

  // ── Add item ─────────────────────────────────────────────────

  selectAddProduct(p: Product) {
    this.addProductId.set(p.id);
    this.addProductName.set(p.productName);
    this.addProductSearch.set(p.productName);
    this.showAddDrop.set(false);
  }

  submitAddItem() {
    const pid = this.addProductId();
    if (!pid) { this.toast.error('Vui lòng chọn sản phẩm.'); return; }
    if (this.addQty() < 1) { this.toast.error('Số lượng đặt phải từ 1 trở lên.'); return; }
    if (this.addPrice() <= 0) { this.toast.error('Đơn giá bán phải lớn hơn 0.'); return; }

    this.addingItem.set(true);
    this.orderService.addOrderItem(+this.id(), {
      productId: pid,
      quantity: this.addQty(),
      costPrice: this.addCostPrice() || 0,
      unitPrice: this.addPrice()
    }).subscribe({
      next: o => {
        this.syncFromOrder(o);
        this.addingItem.set(false);
        this.showAddItem.set(false);
        this.resetAddForm();
        this.toast.success('Đã thêm sản phẩm!');
      },
      error: () => {
        this.toast.error('Không thể thêm sản phẩm. Vui lòng thử lại.');
        this.addingItem.set(false);
      }
    });
  }

  resetAddForm() {
    this.addProductId.set(null);
    this.addProductName.set('');
    this.addProductSearch.set('');
    this.addQty.set(1);
    this.addCostPrice.set(0);
    this.addPrice.set(0);
  }

  // ── Delete order ─────────────────────────────────────────────

  doDelete() {
    this.orderService.deleteOrder(+this.id()).subscribe({
      next: () => { this.toast.success('Đã xóa đơn hàng.'); this.router.navigate(['/orders']); }
    });
  }

  // ── Aggregates ───────────────────────────────────────────────

  totalOrdered(): number  { return this.editableItems().reduce((s, i) => s + i.quantity, 0); }
  totalPicked(): number   { return this.editableItems().reduce((s, i) => s + i.pickedQuantity, 0); }
  totalAmount(): number   { return this.editableItems().reduce((s, i) => s + i.lineTotal, 0); }
  totalCostAmount(): number { return this.editableItems().reduce((s, i) => s + (i.costPrice || 0) * i.quantity, 0); }
  profitAmount(): number  { return this.totalAmount() - this.totalCostAmount(); }
  depositAmount(): number { return this.order()?.depositAmount ?? 0; }
  remainingAmount(): number {
    if (this.order()?.paymentStatus === 'PAID') return 0;
    return Math.max(0, this.totalAmount() - this.depositAmount());
  }

  // ── Helpers ──────────────────────────────────────────────────

  formatCurrency(n: number): string {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND', maximumFractionDigits: 0 }).format(n);
  }

  formatDate(d: string): string {
    return new Date(d).toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric' });
  }

  readonly setTimeout = (fn: () => void, ms: number) => window.setTimeout(fn, ms);
  readonly Math = Math;
}
