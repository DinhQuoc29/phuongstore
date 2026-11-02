import { Routes } from '@angular/router';

/**
 * Routing chính của ứng dụng.
 * Tất cả feature modules dùng lazy loading để tối ưu bundle size.
 */
export const routes: Routes = [
  // Redirect mặc định về trang đơn hàng
  {
    path: '',
    redirectTo: 'orders',
    pathMatch: 'full'
  },

  // ---------------------------------------------------------------
  // Orders
  // ---------------------------------------------------------------
  {
    path: 'orders',
    loadComponent: () =>
      import('./features/orders/order-list/order-list.component').then(m => m.OrderListComponent),
    title: 'Danh sách đơn hàng — Phương Store'
  },
  {
    path: 'orders/new',
    loadComponent: () =>
      import('./features/orders/order-form/order-form.component').then(m => m.OrderFormComponent),
    title: 'Tạo đơn hàng mới — Phương Store'
  },
  {
    path: 'orders/:id',
    loadComponent: () =>
      import('./features/orders/order-detail/order-detail.component').then(m => m.OrderDetailComponent),
    title: 'Chi tiết đơn hàng — Phương Store'
  },

  // ---------------------------------------------------------------
  // Pickup Checklist (màn hình gom hàng tại store)
  // ---------------------------------------------------------------
  {
    path: 'pickup',
    loadComponent: () =>
      import('./features/pickup/pickup-checklist/pickup-checklist.component').then(m => m.PickupChecklistComponent),
    title: 'Gom hàng — Phương Store'
  },

  // ---------------------------------------------------------------
  // Customers
  // ---------------------------------------------------------------
  {
    path: 'customers',
    loadComponent: () =>
      import('./features/customers/customer-list/customer-list.component').then(m => m.CustomerListComponent),
    title: 'Quản lý khách hàng — Phương Store'
  },

  // ---------------------------------------------------------------
  // Products
  // ---------------------------------------------------------------
  {
    path: 'products',
    loadComponent: () =>
      import('./features/products/product-list/product-list.component').then(m => m.ProductListComponent),
    title: 'Quản lý sản phẩm — Phương Store'
  },

  // Wildcard — 404
  {
    path: '**',
    redirectTo: 'orders'
  }
];
