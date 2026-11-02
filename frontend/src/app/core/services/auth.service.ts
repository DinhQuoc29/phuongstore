import { Injectable, computed, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { LoginRequest, LoginResponse, UserProfile } from '../models/auth.model';

const TOKEN_KEY = 'phuongstore_auth_token';
const USER_KEY = 'phuongstore_auth_user';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly apiUrl = `${environment.apiBaseUrl}/auth`;

  // Reactive state với Angular Signals
  readonly currentUser = signal<UserProfile | null>(this.getStoredUser());
  readonly isAuthenticated = computed(() => !!this.currentUser());

  constructor() {
    // Tự động kiểm tra token hợp lệ khi khởi tạo ứng dụng
    const token = this.getToken();
    if (token && !this.currentUser()) {
      this.fetchCurrentUser().subscribe({
        error: () => this.clearStorage()
      });
    }
  }

  /**
   * Đăng nhập người dùng
   */
  login(request: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.apiUrl}/login`, request).pipe(
      tap(response => {
        const storage = request.rememberMe ? localStorage : sessionStorage;
        this.clearStorage();

        storage.setItem(TOKEN_KEY, response.token);
        const user: UserProfile = {
          username: response.username,
          fullName: response.fullName,
          role: response.role
        };
        storage.setItem(USER_KEY, JSON.stringify(user));
        this.currentUser.set(user);
      })
    );
  }

  /**
   * Đăng xuất người dùng và điều hướng về trang /login
   */
  logout(): void {
    this.clearStorage();
    this.currentUser.set(null);
    this.router.navigate(['/login']);
  }

  /**
   * Lấy token JWT hiện tại
   */
  getToken(): string | null {
    return localStorage.getItem(TOKEN_KEY) || sessionStorage.getItem(TOKEN_KEY);
  }

  /**
   * Tải lại thông tin người dùng từ backend
   */
  fetchCurrentUser(): Observable<UserProfile> {
    return this.http.get<UserProfile>(`${this.apiUrl}/me`).pipe(
      tap(user => {
        this.currentUser.set(user);
      })
    );
  }

  private getStoredUser(): UserProfile | null {
    try {
      const rawUser = localStorage.getItem(USER_KEY) || sessionStorage.getItem(USER_KEY);
      return rawUser ? JSON.parse(rawUser) : null;
    } catch {
      return null;
    }
  }

  private clearStorage(): void {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    sessionStorage.removeItem(TOKEN_KEY);
    sessionStorage.removeItem(USER_KEY);
  }
}

