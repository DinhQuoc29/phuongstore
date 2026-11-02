import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';

/**
 * Routing chính của ứng dụng.
 * Tất cả feature modules dùng lazy loading để tối ưu bundle size.
 */
export const routes: Routes = [
  // Trang Đăng nhập (Công khai)
  {
    path: 'login',
    loadComponent: () =>
      import('./features/auth/login/login.component').then(m => m.LoginComponent),
    title: 'Đăng nhập — Phương Store'
  },

  // Redirect mặc định về trang đơn hàng
  {
    path: '',
    redirectTo: 'orders',
    pathMatch: 'full'
  },

  // ---------------------------------------------------------------
  // Orders (Yêu cầu đăng nhập)
  // ---------------------------------------------------------------
  {
    path: 'orders',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/orders/order-list/order-list.component').then(m => m.OrderListComponent),
    title: 'Danh sách đơn hàng — Phương Store'
  },
  {
    path: 'orders/new',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/orders/order-form/order-form.component').then(m => m.OrderFormComponent),
    title: 'Tạo đơn hàng mới — Phương Store'
  },
  {
    path: 'orders/:id',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/orders/order-detail/order-detail.component').then(m => m.OrderDetailComponent),
    title: 'Chi tiết đơn hàng — Phương Store'
  },

  // ---------------------------------------------------------------
  // Pickup Checklist (màn hình gom hàng tại store - Yêu cầu đăng nhập)
  // ---------------------------------------------------------------
  {
    path: 'pickup',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/pickup/pickup-checklist/pickup-checklist.component').then(m => m.PickupChecklistComponent),
    title: 'Gom hàng — Phương Store'
  },

  // ---------------------------------------------------------------
  // Customers (Yêu cầu đăng nhập)
  // ---------------------------------------------------------------
  {
    path: 'customers',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/customers/customer-list/customer-list.component').then(m => m.CustomerListComponent),
    title: 'Quản lý khách hàng — Phương Store'
  },

  // ---------------------------------------------------------------
  // Products (Yêu cầu đăng nhập)
  // ---------------------------------------------------------------
  {
    path: 'products',
    canActivate: [authGuard],
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
