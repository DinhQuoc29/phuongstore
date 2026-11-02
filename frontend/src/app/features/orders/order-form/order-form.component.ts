import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { CustomerService } from '../../../core/services/customer.service';
import { ProductService } from '../../../core/services/product.service';
import { OrderService } from '../../../core/services/order.service';
import { ToastService } from '../../../core/services/toast.service';
import { Customer, CustomerRequest } from '../../../core/models/customer.model';
import { Product, ProductRequest } from '../../../core/models/product.model';
import { OrderRequest, OrderItemRequest } from '../../../core/models/order.model';

interface LineItem {
  product: Product;
  quantity: number;
  costPrice: number;   // Giá gốc (giá vốn)
  unitPrice: number;   // Giá bán
}

@Component({
  selector: 'app-order-form',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './order-form.component.html',
  styleUrl: './order-form.component.css'
})
export class OrderFormComponent implements OnInit {
  private readonly customerService = inject(CustomerService);
  private readonly productService = inject(ProductService);
  private readonly orderService = inject(OrderService);
  private readonly toast = inject(ToastService);
  private readonly router = inject(Router);

  // ── Data ──────────────────────────────────────────────────────────
  customers = signal<Customer[]>([]);
  products = signal<Product[]>([]);
  loadingData = signal(true);
  submitting = signal(false);

  // ── Customer picker ────────────────────────────────────────────────
  selectedCustomer = signal<Customer | null>(null);
  customerSearch = signal('');
  showCustomerDrop = signal(false);

  // Customer modal (thêm mới)
  showCustomerModal = signal(false);
  newCustName = signal('');
  newCustPhone = signal('');
  newCustFb = signal('');
  newCustAddress = signal('');
  savingCustomer = signal(false);
  custErrors = signal<Record<string, string>>({});

  // ── Product picker ─────────────────────────────────────────────────
  lineItems = signal<LineItem[]>([]);
  productSearch = signal('');
  showProductDrop = signal(false);

  // Product modal (thêm mới)
  showProductModal = signal(false);
  newProdName = signal('');
  newProdImageUrl = signal('');
  savingProduct = signal(false);
  prodErrors = signal<Record<string, string>>({});

  // ── Order details ─────────────────────────────────────────────────
  orderDate = signal(new Date().toISOString().split('T')[0]);
  depositAmount = signal(0);
  engraving = signal('');
  note = signal('');

  // ── Form validation ────────────────────────────────────────────────
  formErrors = signal<Record<string, string>>({});

  // ── Computed ──────────────────────────────────────────────────────
  filteredCustomers = computed(() => {
    const kw = this.customerSearch().toLowerCase().trim();
    const all = this.customers();
    if (!kw) return all.slice(0, 8);
    return all.filter(c =>
      c.fullName.toLowerCase().includes(kw) || c.phoneNumber?.includes(kw)
    ).slice(0, 8);
  });

  filteredProducts = computed(() => {
    const kw = this.productSearch().toLowerCase().trim();
    const selected = new Set(this.lineItems().map(l => l.product.id));
    const all = this.products().filter(p => !selected.has(p.id));
    if (!kw) return all.slice(0, 8);
    return all.filter(p =>
      p.productName.toLowerCase().includes(kw) || p.productCode.toLowerCase().includes(kw)
    ).slice(0, 8);
  });

  totalAmount = computed(() =>
    this.lineItems().reduce((sum, l) => sum + l.quantity * l.unitPrice, 0)
  );

  totalCostAmount = computed(() =>
    this.lineItems().reduce((sum, l) => sum + l.quantity * (l.costPrice || 0), 0)
  );

  profitAmount = computed(() =>
    this.totalAmount() - this.totalCostAmount()
  );

  remainingAmount = computed(() =>
    Math.max(0, this.totalAmount() - this.depositAmount())
  );

  // ── Lifecycle ─────────────────────────────────────────────────────
  ngOnInit() {
    Promise.all([
      this.customerService.getAll().toPromise(),
      this.productService.getAll().toPromise()
    ]).then(([customers, products]) => {
      this.customers.set(customers ?? []);
      this.products.set(products ?? []);
      this.loadingData.set(false);
    }).catch(() => this.loadingData.set(false));
  }

  // ── Customer picker actions ────────────────────────────────────────
  selectCustomer(c: Customer) {
    this.selectedCustomer.set(c);
    this.customerSearch.set('');
    this.showCustomerDrop.set(false);
    this.formErrors.update(e => { const n = { ...e }; delete n['customer']; return n; });
  }

  clearCustomer() {
    this.selectedCustomer.set(null);
  }

  // ── Customer modal actions ─────────────────────────────────────────
  openCustomerModal() {
    this.newCustName.set(this.customerSearch());
    this.newCustPhone.set('');
    this.newCustFb.set('');
    this.newCustAddress.set('');
    this.custErrors.set({});
    this.showCustomerDrop.set(false);
    this.showCustomerModal.set(true);
  }

  closeCustomerModal() {
    this.showCustomerModal.set(false);
  }

  saveNewCustomer() {
    const errs: Record<string, string> = {};
    if (!this.newCustName().trim()) errs['fullName'] = 'Họ tên không được để trống.';
    const phone = this.newCustPhone().trim();
    if (phone) {
      if (!/^(\+84|0)[0-9]{8,10}$/.test(phone)) {
        errs['phone'] = 'Số điện thoại không hợp lệ (VD: 0901234567).';
      } else {
        const dup = this.customers().find(c => c.phoneNumber && c.phoneNumber.trim() === phone);
        if (dup) {
          errs['phone'] = `Số điện thoại đã tồn tại (khách: ${dup.fullName}).`;
        }
      }
    }
    this.custErrors.set(errs);
    if (Object.keys(errs).length > 0) return;

    this.savingCustomer.set(true);
    const req: CustomerRequest = {
      fullName: this.newCustName().trim(),
      phoneNumber: this.newCustPhone().trim() || null,
      facebookUrl: this.newCustFb().trim() || null,
      address: this.newCustAddress().trim() || null
    };
    this.customerService.create(req).subscribe({
      next: (c) => {
        this.customers.update(list => [c, ...list]);
        this.closeCustomerModal();
        this.selectCustomer(c);
        this.savingCustomer.set(false);
        this.toast.success('Đã thêm khách hàng mới!');
      },
      error: (err) => {
        const msg = err?.error?.message || 'Có lỗi xảy ra khi thêm khách hàng. Vui lòng thử lại.';
        this.toast.error(msg);
        if (msg.toLowerCase().includes('số điện thoại')) {
          this.custErrors.update(e => ({ ...e, phone: msg }));
        }
        this.savingCustomer.set(false);
      }
    });
  }

  // ── Product picker actions ─────────────────────────────────────────
  addProduct(p: Product) {
    this.lineItems.update(items => [...items, { product: p, quantity: 1, costPrice: 0, unitPrice: 0 }]);
    this.productSearch.set('');
    this.showProductDrop.set(false);
    this.formErrors.update(e => { const n = { ...e }; delete n['items']; return n; });
  }

  removeItem(idx: number) {
    this.lineItems.update(items => items.filter((_, i) => i !== idx));
  }

  updateQty(idx: number, val: string | number) {
    const q = Math.max(1, Math.floor(Number(val)));
    this.lineItems.update(items => items.map((l, i) => i === idx ? { ...l, quantity: q } : l));
  }

  updateCostPrice(idx: number, val: string | number) {
    const p = Math.max(0, Number(val));
    this.lineItems.update(items => items.map((l, i) => i === idx ? { ...l, costPrice: p } : l));
  }

  updatePrice(idx: number, val: string | number) {
    const p = Math.max(0, Number(val));
    this.lineItems.update(items => items.map((l, i) => i === idx ? { ...l, unitPrice: p } : l));
  }

  lineTotal(l: LineItem) { return l.quantity * l.unitPrice; }
  lineCostTotal(l: LineItem) { return l.quantity * (l.costPrice || 0); }
  lineProfit(l: LineItem) { return this.lineTotal(l) - this.lineCostTotal(l); }

  // ── Product modal actions ──────────────────────────────────────────
  openProductModal() {
    this.newProdName.set(this.productSearch());
    this.newProdImageUrl.set('');
    this.prodErrors.set({});
    this.showProductDrop.set(false);
    this.showProductModal.set(true);
  }

  closeProductModal() {
    this.showProductModal.set(false);
  }

  saveNewProduct() {
    const errs: Record<string, string> = {};
    if (!this.newProdName().trim()) errs['productName'] = 'Tên sản phẩm không được để trống.';
    this.prodErrors.set(errs);
    if (Object.keys(errs).length > 0) return;

    this.savingProduct.set(true);
    const req: ProductRequest = {
      productName: this.newProdName().trim(),
      imageUrl: this.newProdImageUrl().trim() || null
    };
    this.productService.create(req).subscribe({
      next: (p) => {
        this.products.update(list => [p, ...list]);
        this.closeProductModal();
        this.addProduct(p);
        this.savingProduct.set(false);
        this.toast.success('Đã thêm sản phẩm mới!');
      },
      error: (err) => {
        this.toast.error(err?.error?.message || 'Có lỗi xảy ra khi thêm sản phẩm. Vui lòng thử lại.');
        this.savingProduct.set(false);
      }
    });
  }

  // ── Submit ────────────────────────────────────────────────────────
  submit() {
    if (!this.validate()) return;
    const req: OrderRequest = {
      customerId: this.selectedCustomer()!.id,
      orderDate: this.orderDate(),
      engraving: this.engraving().trim() || null,
      note: this.note().trim() || null,
      depositAmount: this.depositAmount(),
      items: this.lineItems().map(l => ({
        productId: l.product.id,
        quantity: l.quantity,
        costPrice: l.costPrice || 0,
        unitPrice: l.unitPrice
      } as OrderItemRequest))
    };
    this.submitting.set(true);
    this.orderService.createOrder(req).subscribe({
      next: order => {
        this.toast.success(`Đã tạo đơn hàng #${order.id}!`);
        this.router.navigate(['/orders', order.id]);
      },
      error: (err) => {
        this.toast.error(err?.error?.message || 'Có lỗi xảy ra khi tạo đơn hàng. Vui lòng thử lại.');
        this.submitting.set(false);
      }
    });
  }

  private validate(): boolean {
    const errs: Record<string, string> = {};
    if (!this.selectedCustomer()) errs['customer'] = 'Vui lòng chọn khách hàng.';
    if (this.lineItems().length === 0) errs['items'] = 'Đơn hàng phải có ít nhất 1 sản phẩm.';
    const hasZeroPrice = this.lineItems().some(l => l.unitPrice <= 0);
    if (hasZeroPrice) errs['price'] = 'Tất cả sản phẩm phải có đơn giá bán lớn hơn 0.';
    const hasNegativeCost = this.lineItems().some(l => (l.costPrice || 0) < 0);
    if (hasNegativeCost) errs['cost'] = 'Giá gốc không được âm.';
    if (this.depositAmount() < 0) errs['deposit'] = 'Tiền đặt cọc không được âm.';
    this.formErrors.set(errs);
    return Object.keys(errs).length === 0;
  }

  // ── Helpers ───────────────────────────────────────────────────────
  formatCurrency(n: number) {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency', currency: 'VND', maximumFractionDigits: 0
    }).format(n);
  }

  readonly setTimeout = (fn: () => void, ms: number) => window.setTimeout(fn, ms);
}
