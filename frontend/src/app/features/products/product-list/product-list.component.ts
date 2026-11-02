import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ProductService } from '../../../core/services/product.service';
import { ToastService } from '../../../core/services/toast.service';
import { Product, ProductRequest } from '../../../core/models/product.model';
import { ConfirmDialogComponent } from '../../../shared/components/confirm-dialog/confirm-dialog.component';

@Component({
  selector: 'app-product-list',
  standalone: true,
  imports: [CommonModule, FormsModule, ConfirmDialogComponent],
  templateUrl: './product-list.component.html',
  styleUrl: './product-list.component.css'
})
export class ProductListComponent implements OnInit {
  private readonly productService = inject(ProductService);
  private readonly toast = inject(ToastService);

  products      = signal<Product[]>([]);
  loading       = signal(true);
  submitting    = signal(false);
  keyword       = signal('');
  currentPage   = signal(0);
  totalPages    = signal(0);
  totalElements = signal(0);
  readonly pageSize = 15;

  showModal  = signal(false);
  editingId  = signal<number | null>(null);
  deletingId = signal<number | null>(null);

  // Form
  formName     = signal('');
  formImageUrl = signal('');
  formErrors   = signal<Record<string, string>>({});

  isEditing   = computed(() => this.editingId() !== null);
  modalTitle  = computed(() => this.isEditing() ? 'Cập nhật sản phẩm' : 'Thêm sản phẩm mới');
  deletingCode= computed(() => this.products().find(p => p.id === this.deletingId())?.productCode ?? '');

  ngOnInit() { this.load(); }

  load() {
    this.loading.set(true);
    this.productService.search(this.keyword(), this.currentPage(), this.pageSize).subscribe({
      next: p => {
        this.products.set(p.content);
        this.totalPages.set(p.totalPages);
        this.totalElements.set(p.totalElements);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  onSearch(kw: string) {
    this.keyword.set(kw);
    this.currentPage.set(0);
    this.load();
  }

  changePage(p: number) {
    this.currentPage.set(p);
    this.load();
  }

  openCreate() {
    this.editingId.set(null);
    this.formName.set('');
    this.formImageUrl.set('');
    this.formErrors.set({});
    this.showModal.set(true);
  }

  openEdit(p: Product) {
    this.editingId.set(p.id);
    this.formName.set(p.productName);
    this.formImageUrl.set(p.imageUrl ?? '');
    this.formErrors.set({});
    this.showModal.set(true);
  }

  closeModal() { this.showModal.set(false); }

  submit() {
    if (!this.validate()) return;
    const req: ProductRequest = {
      productName: this.formName().trim(),
      imageUrl: this.formImageUrl().trim() || null
    };
    this.submitting.set(true);
    const obs = this.isEditing()
      ? this.productService.update(this.editingId()!, req)
      : this.productService.create(req);
    obs.subscribe({
      next: () => {
        this.toast.success(this.isEditing() ? 'Đã cập nhật sản phẩm!' : 'Đã thêm sản phẩm mới!');
        this.closeModal();
        this.load();
        this.submitting.set(false);
      },
      error: (err) => {
        this.toast.error(err?.error?.message || 'Có lỗi xảy ra khi lưu sản phẩm. Vui lòng thử lại.');
        this.submitting.set(false);
      }
    });
  }

  confirmDelete(id: number) { this.deletingId.set(id); }
  cancelDelete()             { this.deletingId.set(null); }
  doDelete() {
    const id = this.deletingId();
    if (!id) return;
    this.productService.delete(id).subscribe({
      next: () => {
        this.toast.success('Đã xóa sản phẩm.');
        this.deletingId.set(null);
        this.load();
      },
      error: (err) => {
        this.toast.error(err?.error?.message || 'Không thể xóa sản phẩm. Vui lòng thử lại.');
      }
    });
  }

  private validate(): boolean {
    const errs: Record<string, string> = {};
    if (!this.formName().trim()) errs['productName'] = 'Tên sản phẩm không được để trống.';
    this.formErrors.set(errs);
    return Object.keys(errs).length === 0;
  }

  pages() { return Array.from({ length: this.totalPages() }, (_, i) => i); }
}
