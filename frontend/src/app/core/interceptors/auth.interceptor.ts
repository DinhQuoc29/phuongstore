import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';
import { ToastService } from '../services/toast.service';

/**
 * Functional HTTP Interceptor tự động gắn Bearer Token vào headers
 * và xử lý lỗi 401 khi token hết hạn.
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const toast = inject(ToastService);
  const token = authService.getToken();

  // Bỏ qua gắn Authorization header cho request đăng nhập
  const isLoginRequest = req.url.includes('/auth/login');

  let authReq = req;
  if (token && !isLoginRequest) {
    authReq = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }

  return next(authReq).pipe(
    catchError(err => {
      // Khi token hết hạn hoặc không hợp lệ (401)
      if (err.status === 401 && !isLoginRequest) {
        toast.warning('Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.');
        authService.logout();
      }
      return throwError(() => err);
    })
  );
};

