import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PickupService } from '../../../core/services/pickup.service';
import { OrderService } from '../../../core/services/order.service';
import { ToastService } from '../../../core/services/toast.service';
import {
  PickupProductCard,
  PickupAllocationItem,
  PICKUP_STATUS_LABELS
} from '../../../core/models';

@Component({
  selector: 'app-pickup-checklist',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './pickup-checklist.component.html',
  styleUrl: './pickup-checklist.component.css'
})
export class PickupChecklistComponent implements OnInit {

  // ---------------------------------------------------------------
  // DI
  // ---------------------------------------------------------------
  private readonly pickupService = inject(PickupService);
  private readonly orderService  = inject(OrderService);
  private readonly toast         = inject(ToastService);

  // ---------------------------------------------------------------
  // Signals — Reactive State
  // ---------------------------------------------------------------

  /** Danh sách card sản phẩm từ API */
  readonly checklist = signal<PickupProductCard[]>([]);

  /** Trạng thái loading ban đầu */
  readonly loading = signal(true);

  /** ID các card đang mở (detail rows) */
  readonly expandedCards = signal<Set<number>>(new Set());

  /** Set các orderItemId đang được cập nhật (hiện spinner) */
  readonly updatingItems = signal<Set<number>>(new Set());

  // ---------------------------------------------------------------
  // Pagination (client-side)
  // ---------------------------------------------------------------

  readonly currentPickupPage = signal(0);
  readonly pickupPageSize = 15;

  readonly totalPickupPages = computed(() =>
    Math.ceil(this.filteredChecklist().length / this.pickupPageSize)
  );

  readonly pagedChecklist = computed(() => {
    const start = this.currentPickupPage() * this.pickupPageSize;
    return this.filteredChecklist().slice(start, start + this.pickupPageSize);
  });

  pickupPages(): number[] {
    return Array.from({ length: this.totalPickupPages() }, (_, i) => i);
  }

  changePickupPage(p: number): void {
    this.currentPickupPage.set(p);
    this.expandedCards.set(new Set());
  }

  // ---------------------------------------------------------------
  // Computed
  // ---------------------------------------------------------------

  /** Tổng số sản phẩm cần gom (không trùng) */
  readonly totalProductTypes = computed(() => this.checklist().length);

  /** Tổng tất cả items cần lấy */
  readonly totalItemsNeeded = computed(() =>
    this.checklist().reduce((sum, card) => sum + card.totalNeeded, 0)
  );

  /** Tổng đã lấy */
  readonly totalItemsPicked = computed(() =>
    this.checklist().reduce((sum, card) => sum + card.totalPicked, 0)
  );

  /** % hoàn thành tổng thể */
  readonly overallProgress = computed(() => {
    const needed = this.totalItemsNeeded();
    if (needed === 0) return 100;
    return Math.round((this.totalItemsPicked() / needed) * 100);
  });

  /** Số card đã hoàn thành (totalPicked >= totalNeeded) */
  readonly completedCards = computed(() =>
    this.checklist().filter(c => c.totalPicked >= c.totalNeeded).length
  );

  // ---------------------------------------------------------------
  // Lifecycle
  // ---------------------------------------------------------------

  ngOnInit(): void {
    this.loadChecklist();
  }

  // ---------------------------------------------------------------
  // Data loading
  // ---------------------------------------------------------------

  loadChecklist(): void {
    this.loading.set(true);
    this.pickupService.getPickupChecklist().subscribe({
      next: (data) => {
        this.checklist.set(data);
        this.currentPickupPage.set(0);
        this.expandedCards.set(new Set());
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.toast.error('Không thể tải danh sách gom hàng.');
      }
    });
  }

  // ---------------------------------------------------------------
  // Accordion / Detail Row Toggle
  // ---------------------------------------------------------------

  toggleCard(productId: number): void {
    this.expandedCards.update(set => {
      const next = new Set(set);
      if (next.has(productId)) {
        next.delete(productId);
      } else {
        next.add(productId);
      }
      return next;
    });
  }

  isExpanded(productId: number): boolean {
    return this.expandedCards().has(productId);
  }

  expandAll(): void {
    const allIds = new Set(this.checklist().map(c => c.productId));
    this.expandedCards.set(allIds);
  }

  collapseAll(): void {
    this.expandedCards.set(new Set());
  }

  // ---------------------------------------------------------------
  // Picked Quantity Update (core business action)
  // ---------------------------------------------------------------

  /**
   * Cập nhật picked_quantity cho một OrderItem.
   * @param allocation  Dòng phân bổ của khách hàng cụ thể
   * @param card        Card sản phẩm cha (để biết quantity tối đa)
   * @param delta       +1 hoặc -1
   */
  updatePicked(
    allocation: PickupAllocationItem,
    card: PickupProductCard,
    delta: number
  ): void {
    const newPicked = allocation.pickedQuantity + delta;

    // Guard: không vượt quá số đặt, không dưới 0
    if (newPicked < 0 || newPicked > allocation.quantity) return;

    // Đánh dấu item đang loading
    this.updatingItems.update(set => new Set(set).add(allocation.orderItemId));

    this.orderService.updatePickedQuantity(allocation.orderItemId, { pickedQuantity: newPicked })
      .subscribe({
        next: (updatedItem) => {
          // Cập nhật signal checklist tại chỗ (immutable update)
          this.checklist.update(list =>
            list.map(c => {
              if (c.productId !== card.productId) return c;

              const updatedAllocations = c.allocations.map(a =>
                a.orderItemId === allocation.orderItemId
                  ? { ...a, pickedQuantity: updatedItem.pickedQuantity }
                  : a
              );

              const newTotalPicked = updatedAllocations
                .reduce((sum, a) => sum + a.pickedQuantity, 0);

              return {
                ...c,
                allocations: updatedAllocations,
                totalPicked: newTotalPicked
              };
            })
          );

          // Thông báo khi lấy xong hoàn toàn một sản phẩm
          if (newPicked === allocation.quantity) {
            this.toast.success(`Đã lấy đủ cho ${allocation.customerName}`);
          }
        },
        error: () => {
          this.toast.error('Không thể cập nhật số lượng. Vui lòng thử lại.');
        },
        complete: () => {
          // Xóa trạng thái loading
          this.updatingItems.update(set => {
            const next = new Set(set);
            next.delete(allocation.orderItemId);
            return next;
          });
        }
      });
  }

  isItemUpdating(orderItemId: number): boolean {
    return this.updatingItems().has(orderItemId);
  }

  /**
   * Xử lý khi người dùng nhập trực tiếp số vào ô input "Đã lấy".
   * Clamp giá trị giữa 0 và alloc.quantity, chỉ gọi API nếu giá trị thay đổi.
   */
  onPickedInputChange(
    event: Event,
    allocation: PickupAllocationItem,
    card: PickupProductCard
  ): void {
    const inputEl = event.target as HTMLInputElement;
    let newValue = parseInt(inputEl.value, 10);

    // Nếu nhập không phải số → reset về giá trị cũ
    if (isNaN(newValue)) {
      inputEl.value = String(allocation.pickedQuantity);
      return;
    }

    // Clamp giữa 0 và quantity
    newValue = Math.max(0, Math.min(newValue, allocation.quantity));
    inputEl.value = String(newValue);

    // Không thay đổi → bỏ qua
    if (newValue === allocation.pickedQuantity) return;

    // Đánh dấu item đang loading
    this.updatingItems.update(set => new Set(set).add(allocation.orderItemId));

    this.orderService.updatePickedQuantity(allocation.orderItemId, { pickedQuantity: newValue })
      .subscribe({
        next: (updatedItem) => {
          this.checklist.update(list =>
            list.map(c => {
              if (c.productId !== card.productId) return c;

              const updatedAllocations = c.allocations.map(a =>
                a.orderItemId === allocation.orderItemId
                  ? { ...a, pickedQuantity: updatedItem.pickedQuantity }
                  : a
              );

              const newTotalPicked = updatedAllocations
                .reduce((sum, a) => sum + a.pickedQuantity, 0);

              return {
                ...c,
                allocations: updatedAllocations,
                totalPicked: newTotalPicked
              };
            })
          );

          if (newValue === allocation.quantity) {
            this.toast.success(`Đã lấy đủ cho ${allocation.customerName}`);
          }
        },
        error: () => {
          // Reset input về giá trị cũ khi lỗi
          inputEl.value = String(allocation.pickedQuantity);
          this.toast.error('Không thể cập nhật số lượng. Vui lòng thử lại.');
        },
        complete: () => {
          this.updatingItems.update(set => {
            const next = new Set(set);
            next.delete(allocation.orderItemId);
            return next;
          });
        }
      });
  }

  // ---------------------------------------------------------------
  // UI Helpers
  // ---------------------------------------------------------------

  getProgressPercent(card: PickupProductCard): number {
    if (card.totalNeeded === 0) return 100;
    return Math.round((card.totalPicked / card.totalNeeded) * 100);
  }

  getProgressClass(card: PickupProductCard): string {
    const pct = this.getProgressPercent(card);
    if (pct === 100) return 'progress--complete';
    if (pct > 0)    return 'progress--partial';
    return 'progress--empty';
  }

  getCardStatusClass(card: PickupProductCard): string {
    if (card.totalPicked >= card.totalNeeded) return 'card--complete';
    if (card.totalPicked > 0)                return 'card--partial';
    return 'card--pending';
  }

  getStatusEmoji(card: PickupProductCard): string {
    if (card.totalPicked >= card.totalNeeded) return '✅';
    if (card.totalPicked > 0)                return '🔄';
    return '⏳';
  }

  // Lọc chỉ hiển thị chưa hoàn thành (toggle filter)
  readonly showOnlyPending = signal(false);

  // Từ khóa tìm kiếm
  readonly searchKeyword = signal('');

  onSearch(kw: string): void {
    this.searchKeyword.set(kw);
    this.currentPickupPage.set(0);
  }

  clearSearch(): void {
    this.searchKeyword.set('');
    this.currentPickupPage.set(0);
  }

  readonly filteredChecklist = computed(() => {
    const list = this.checklist();
    const kw = this.searchKeyword().toLowerCase().trim();
    if (!kw) return list;

    return list.filter(c => {
      const matchName = c.productName?.toLowerCase().includes(kw);
      const matchCode = c.productCode?.toLowerCase().includes(kw);
      const matchCustomer = c.allocations?.some(a =>
        a.customerName?.toLowerCase().includes(kw) ||
        (a.customerPhone && a.customerPhone.includes(kw))
      );
      return matchName || matchCode || matchCustomer;
    });
  });

  stepPickedQuantity(allocation: PickupAllocationItem, card: PickupProductCard, delta: number): void {
    const newValue = Math.max(0, Math.min(allocation.pickedQuantity + delta, allocation.quantity));
    if (newValue === allocation.pickedQuantity) return;

    this.updatingItems.update(set => new Set(set).add(allocation.orderItemId));
    this.orderService.updatePickedQuantity(allocation.orderItemId, { pickedQuantity: newValue })
      .subscribe({
        next: (updatedItem) => {
          this.checklist.update(list =>
            list.map(c => {
              if (c.productId !== card.productId) return c;
              const updatedAllocations = c.allocations.map(a =>
                a.orderItemId === allocation.orderItemId
                  ? { ...a, pickedQuantity: updatedItem.pickedQuantity }
                  : a
              );
              const newTotalPicked = updatedAllocations.reduce((s, a) => s + a.pickedQuantity, 0);
              return {
                ...c,
                totalPicked: newTotalPicked,
                allocations: updatedAllocations
              };
            })
          );
          if (newValue === allocation.quantity) {
            this.toast.success(`Đã lấy đủ cho ${allocation.customerName}`);
          }
          this.updatingItems.update(set => {
            const next = new Set(set);
            next.delete(allocation.orderItemId);
            return next;
          });
        },
        error: () => {
          this.toast.error('Không thể cập nhật số lượng đã lấy');
          this.updatingItems.update(set => {
            const next = new Set(set);
            next.delete(allocation.orderItemId);
            return next;
          });
        }
      });
  }
}
