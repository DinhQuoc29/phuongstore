import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { ToastService } from '../../../core/services/toast.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly toast = inject(ToastService);

  username = '';
  password = '';
  rememberMe = true;
  loading = false;
  errorMessage = '';

  constructor() {
    // Nếu đã đăng nhập sẵn rồi thì chuyển hướng thẳng vào trang chính
    if (this.authService.getToken()) {
      this.router.navigate(['/orders']);
    }
  }

  onSubmit(): void {
    if (!this.username.trim() || !this.password.trim()) {
      this.errorMessage = 'Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu.';
      return;
    }

    this.loading = true;
    this.errorMessage = '';

    this.authService.login({
      username: this.username.trim(),
      password: this.password,
      rememberMe: this.rememberMe
    }).subscribe({
      next: (res) => {
        this.loading = false;
        this.toast.success(`Chào mừng ${res.fullName || res.username} trở lại!`);
        const returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/orders';
        this.router.navigateByUrl(returnUrl);
      },
      error: (err) => {
        this.loading = false;
        if (err.status === 401) {
          this.errorMessage = 'Tên đăng nhập hoặc mật khẩu không chính xác.';
        } else if (err.error?.message) {
          this.errorMessage = err.error.message;
        } else {
          this.errorMessage = 'Không thể kết nối đến máy chủ. Vui lòng thử lại sau.';
        }
      }
    });
  }
}

