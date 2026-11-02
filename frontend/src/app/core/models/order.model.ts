// ============================================================
// Order Models — mirrors Spring Boot Order DTOs
// ============================================================

// ---------------------------------------------------------------
// Enums (union types)
// ---------------------------------------------------------------

/** Tương ứng PickupStatus.java */
export type PickupStatus = 'NOT_PICKED' | 'PARTIALLY_PICKED' | 'FULLY_PICKED';

/** Tương ứng PaymentStatus.java */
export type PaymentStatus = 'UNPAID' | 'PAID';

/** Tương ứng ShippingStatus.java */
export type ShippingStatus = 'NOT_SHIPPED' | 'SHIPPED';

// ---------------------------------------------------------------
// UI Labels (tiếng Việt)
// ---------------------------------------------------------------

export const PICKUP_STATUS_LABELS: Record<PickupStatus, string> = {
  NOT_PICKED: 'Chưa lấy',
  PARTIALLY_PICKED: 'Đang lấy',
  FULLY_PICKED: 'Đã lấy đủ'
};

export const PICKUP_STATUS_COLORS: Record<PickupStatus, string> = {
  NOT_PICKED:       '#BE123C',
  PARTIALLY_PICKED: '#B45309',
  FULLY_PICKED:     '#059669'
};

export const PAYMENT_STATUS_LABELS: Record<PaymentStatus, string> = {
  UNPAID: 'Chưa thanh toán',
  PAID: 'Đã thanh toán'
};

export const PAYMENT_STATUS_COLORS: Record<PaymentStatus, string> = {
  UNPAID: '#BE123C',
  PAID:   '#059669'
};

export const SHIPPING_STATUS_LABELS: Record<ShippingStatus, string> = {
  NOT_SHIPPED: 'Chưa gửi',
  SHIPPED: 'Đã gửi'
};

export const SHIPPING_STATUS_COLORS: Record<ShippingStatus, string> = {
  NOT_SHIPPED: '#BE123C',
  SHIPPED:     '#059669'
};

// ---------------------------------------------------------------
// OrderItem
// ---------------------------------------------------------------

/** Tương ứng OrderItemResponse.java */
export interface OrderItem {
  id: number;
  productId: number;
  productCode: string;
  productName: string;
  productImageUrl: string | null;
  quantity: number;
  pickedQuantity: number;
  shippedQuantity: number;
  unitPrice: number;        // Đơn giá bán
  costPrice: number;        // Giá gốc (giá vốn)
  lineTotal: number;        // Thành tiền bán
  lineCostTotal?: number;   // Thành tiền gốc
  lineProfit?: number;      // Tiền lời (lineTotal - lineCostTotal)
}

/** Tương ứng OrderItemRequest.java */
export interface OrderItemRequest {
  productId: number;
  quantity: number;
  unitPrice: number;
  costPrice?: number;
}

// ---------------------------------------------------------------
// Order
// ---------------------------------------------------------------

/** Tương ứng OrderResponse.java (chi tiết, có items) */
export interface Order {
  id: number;
  customerId: number;
  customerName: string;
  customerPhone: string | null;
  customerAddress: string | null;
  customerFacebookUrl: string | null;
  orderDate: string;
  note: string | null;
  engraving?: string | null;
  totalAmount: number;         // Tổng tiền bán
  totalCostAmount?: number;    // Tổng giá gốc
  profitAmount?: number;       // Tiền lời (totalAmount - totalCostAmount)
  depositAmount: number;
  remainingAmount: number;
  pickupStatus: PickupStatus;
  paymentStatus: PaymentStatus;
  shippingStatus: ShippingStatus;
  createdAt: string;
  items: OrderItem[];
}

/** Tương ứng OrderSummaryResponse.java (danh sách, không có items) */
export interface OrderSummary {
  id: number;
  customerId: number;
  customerName: string;
  customerPhone: string | null;
  orderDate: string;
  totalAmount: number;
  totalCostAmount?: number;
  profitAmount?: number;
  depositAmount: number;
  remainingAmount: number;
  pickupStatus: PickupStatus;
  paymentStatus: PaymentStatus;
  shippingStatus: ShippingStatus;
  itemCount: number;
  totalQuantity: number;
  totalPickedQuantity: number;
  note?: string | null;
  engraving?: string | null;
}

/** Tương ứng OrderRequest.java */
export interface OrderRequest {
  customerId: number;
  orderDate: string;
  note?: string | null;
  engraving?: string | null;
  depositAmount: number;
  items: OrderItemRequest[];
}

/** Tương ứng UpdateDepositRequest.java */
export interface UpdateDepositRequest {
  depositAmount: number;
}

/** Tương ứng UpdateShippingStatusRequest.java */
export interface UpdateShippingStatusRequest {
  shippingStatus: ShippingStatus;
}

/** Tương ứng UpdatePickedQuantityRequest.java */
export interface UpdatePickedQuantityRequest {
  pickedQuantity: number;
}

/** Tương ứng UpdateShippedQuantityRequest.java */
export interface UpdateShippedQuantityRequest {
  shippedQuantity: number;
}

/** Tương ứng UpdatePaymentStatusRequest.java */
export interface UpdatePaymentStatusRequest {
  paymentStatus: PaymentStatus;
}

/** Tương ứng UpdateOrderItemRequest.java */
export interface UpdateOrderItemRequest {
  productId: number;
  quantity: number;
  unitPrice: number;
  costPrice?: number;
}

// ---------------------------------------------------------------
// Pickup Screen
// ---------------------------------------------------------------

export interface PickupAllocationItem {
  orderItemId: number;
  orderId: number;
  customerId: number;
  customerName: string;
  customerPhone: string | null;
  quantity: number;
  pickedQuantity: number;
}

export interface PickupProductCard {
  productId: number;
  productCode: string;
  productName: string;
  imageUrl: string | null;
  totalNeeded: number;
  totalPicked: number;
  allocations: PickupAllocationItem[];
}

// ---------------------------------------------------------------
// Filter params
// ---------------------------------------------------------------

export interface OrderFilterParams {
  customerId?: number;
  pickupStatus?: PickupStatus;
  paymentStatus?: PaymentStatus;
  shippingStatus?: ShippingStatus;
  fromDate?: string;
  toDate?: string;
  hasNote?: boolean;
  hasEngraving?: boolean;
  keyword?: string;
  page?: number;
  size?: number;
}


export interface MonthlyStatisticsResponse {
  year: number;
  month: number;
  totalRevenue: number;
  totalProfit: number;
  paidOrderCount: number;
}
