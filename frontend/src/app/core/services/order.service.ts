import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  Order,
  OrderFilterParams,
  OrderItem,
  OrderItemRequest,
  OrderRequest,
  OrderSummary,
  UpdateDepositRequest,
  UpdateOrderItemRequest,
  UpdatePaymentStatusRequest,
  UpdatePickedQuantityRequest,
  UpdateShippingStatusRequest,
  UpdateShippedQuantityRequest,
  MonthlyStatisticsResponse
} from '../models/order.model';
import { PageResponse } from '../models/api.model';

@Injectable({ providedIn: 'root' })
export class OrderService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiBaseUrl}/orders`;

  // CREATE
  createOrder(request: OrderRequest): Observable<Order> {
    return this.http.post<Order>(this.base, request);
  }

  // READ
  getOrderById(id: number): Observable<Order> {
    return this.http.get<Order>(`${this.base}/${id}`);
  }

  searchOrders(filters: OrderFilterParams = {}): Observable<PageResponse<OrderSummary>> {
    let params = new HttpParams()
      .set('page', (filters.page ?? 0).toString())
      .set('size', (filters.size ?? 20).toString());

    if (filters.customerId) params = params.set('customerId', filters.customerId.toString());
    if (filters.pickupStatus) params = params.set('pickupStatus', filters.pickupStatus);
    if (filters.paymentStatus) params = params.set('paymentStatus', filters.paymentStatus);
    if (filters.shippingStatus) params = params.set('shippingStatus', filters.shippingStatus);
    if (filters.fromDate) params = params.set('fromDate', filters.fromDate);
    if (filters.toDate) params = params.set('toDate', filters.toDate);
    if (filters.hasNote !== undefined && filters.hasNote !== null) {
      params = params.set('hasNote', filters.hasNote.toString());
    }
    if (filters.hasEngraving !== undefined && filters.hasEngraving !== null) {
      params = params.set('hasEngraving', filters.hasEngraving.toString());
    }
    if (filters.keyword?.trim()) params = params.set('keyword', filters.keyword.trim());

    return this.http.get<PageResponse<OrderSummary>>(this.base, { params });
  }

  // UPDATE — Order level
  updateDeposit(orderId: number, request: UpdateDepositRequest): Observable<Order> {
    return this.http.patch<Order>(`${this.base}/${orderId}/deposit`, request);
  }

  updatePaymentStatus(orderId: number, request: UpdatePaymentStatusRequest): Observable<Order> {
    return this.http.patch<Order>(`${this.base}/${orderId}/payment-status`, request);
  }

  updateShippingStatus(orderId: number, request: UpdateShippingStatusRequest): Observable<Order> {
    return this.http.patch<Order>(`${this.base}/${orderId}/shipping-status`, request);
  }

  // UPDATE — Item level
  updatePickedQuantity(orderItemId: number, request: UpdatePickedQuantityRequest): Observable<OrderItem> {
    return this.http.patch<OrderItem>(`${this.base}/items/${orderItemId}/picked-quantity`, request);
  }

  updateShippedQuantity(orderItemId: number, request: UpdateShippedQuantityRequest): Observable<OrderItem> {
    return this.http.patch<OrderItem>(`${this.base}/items/${orderItemId}/shipped-quantity`, request);
  }

  updateOrderItem(orderItemId: number, request: UpdateOrderItemRequest): Observable<Order> {
    return this.http.patch<Order>(`${this.base}/items/${orderItemId}`, request);
  }

  // CREATE — Add item to order
  addOrderItem(orderId: number, request: OrderItemRequest): Observable<Order> {
    return this.http.post<Order>(`${this.base}/${orderId}/items`, request);
  }

  // DELETE — Item
  deleteOrderItem(orderItemId: number): Observable<Order> {
    return this.http.delete<Order>(`${this.base}/items/${orderItemId}`);
  }

  // DELETE — Order
  deleteOrder(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }

  // STATISTICS
  getMonthlyStatistics(year: number, month: number): Observable<MonthlyStatisticsResponse> {
    const params = new HttpParams()
      .set('year', year.toString())
      .set('month', month.toString());
    return this.http.get<MonthlyStatisticsResponse>(`${this.base}/statistics`, { params });
  }
}