import { Injectable } from '@angular/core';
import { Order } from '../models/order.model';

@Injectable({
  providedIn: 'root'
})
export class InvoiceExportService {

  /**
   * Tạo hóa đơn dạng ảnh PNG và tải về máy
   */
  exportOrder(order: Order): void {
    const canvas = this.drawInvoice(order);
    const dataUrl = canvas.toDataURL('image/png');

    const link = document.createElement('a');
    link.href = dataUrl;
    link.download = `hoa-don-${order.id}.png`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  }

  /**
   * Vẽ hóa đơn trên Canvas với độ nét Retina (2x)
   */
  private drawInvoice(order: Order): HTMLCanvasElement {
    const logicalWidth = 540;
    const paddingX = 30;
    const contentWidth = logicalWidth - paddingX * 2;
    const scale = 2; // Độ phân giải Retina 2x sắc nét

    // Tính toán chiều cao cần thiết trước khi vẽ
    const estimatedHeight = this.calculateHeight(order, contentWidth);

    const canvas = document.createElement('canvas');
    canvas.width = logicalWidth * scale;
    canvas.height = estimatedHeight * scale;

    const ctx = canvas.getContext('2d');
    if (!ctx) throw new Error('Không thể khởi tạo Canvas 2D');

    ctx.scale(scale, scale);

    // Nền trắng
    ctx.fillStyle = '#ffffff';
    ctx.fillRect(0, 0, logicalWidth, estimatedHeight);

    let y = 38;
    ctx.fillStyle = '#000000';
    ctx.textBaseline = 'top';

    // ─── 1. Header ───
    // Tên thương hiệu
    ctx.font = 'bold 22px "Segoe UI", -apple-system, BlinkMacSystemFont, Roboto, sans-serif';
    ctx.textAlign = 'center';
    ctx.fillText('HATHITHUPHUONG', logicalWidth / 2, y);
    y += 36;

    // Tiêu đề HÓA ĐƠN BÁN HÀNG
    ctx.font = 'bold 28px "Segoe UI", -apple-system, BlinkMacSystemFont, Roboto, sans-serif';
    ctx.fillText('HÓA ĐƠN BÁN HÀNG', logicalWidth / 2, y);
    y += 46;

    // Ngày bán & Tên KH
    ctx.font = '500 17px "Segoe UI", -apple-system, BlinkMacSystemFont, Roboto, sans-serif';
    ctx.textAlign = 'left';

    const formattedDate = this.formatDate(order.createdAt || order.orderDate);
    const labelX = paddingX;
    const valX = paddingX + 90;

    ctx.fillText('Ngày bán', labelX, y);
    ctx.fillText(`:  ${formattedDate}`, valX - 10, y);
    y += 28;

    ctx.fillText('KH', labelX, y);
    ctx.fillText(`:  ${order.customerName}`, valX - 10, y);
    y += 36;

    // Đường kẻ trên bảng
    this.drawLine(ctx, paddingX, y, logicalWidth - paddingX, y);
    y += 14;

    // ─── 2. Tiêu đề bảng sản phẩm ───
    ctx.font = 'bold 18px "Segoe UI", -apple-system, BlinkMacSystemFont, Roboto, sans-serif';
    const colSlX = paddingX;
    const colDgX = paddingX + 180;
    const colTotalX = logicalWidth - paddingX;

    ctx.textAlign = 'left';
    ctx.fillText('SL', colSlX, y);
    ctx.fillText('ĐG', colDgX, y);

    ctx.textAlign = 'right';
    ctx.fillText('T.Tiền', colTotalX, y);
    y += 30;

    // Đường kẻ phân cách tiêu đề cột và hàng
    this.drawLine(ctx, paddingX, y, logicalWidth - paddingX, y, 1);
    y += 16;

    // ─── 3. Danh sách sản phẩm ───
    let totalQty = 0;
    const items = order.items && order.items.length > 0 ? order.items : [];

    for (const item of items) {
      totalQty += item.quantity;
      const fullItemName = item.colorCode
        ? `${item.productName || 'SẢN PHẨM'} ${item.colorCode}`
        : (item.productName || 'SẢN PHẨM');
      const itemName = fullItemName.toUpperCase();

      // Dòng 1: Tên sản phẩm
      ctx.font = '600 17px "Segoe UI", -apple-system, BlinkMacSystemFont, Roboto, sans-serif';
      ctx.textAlign = 'left';
      const nameLines = this.wrapText(ctx, itemName, contentWidth);
      for (const line of nameLines) {
        ctx.fillText(line, paddingX, y);
        y += 24;
      }
      y += 2;

      // Dòng 2: SL, ĐG, T.Tiền
      ctx.font = '400 17px "Segoe UI", -apple-system, BlinkMacSystemFont, Roboto, sans-serif';
      ctx.textAlign = 'left';
      ctx.fillText(String(item.quantity), colSlX, y);
      ctx.fillText(this.formatCurrency(item.unitPrice), colDgX, y);

      ctx.textAlign = 'right';
      ctx.fillText(this.formatCurrency(item.lineTotal || (item.quantity * item.unitPrice)), colTotalX, y);
      y += 32;
    }

    if (items.length === 0) {
      ctx.font = 'italic 16px "Segoe UI", -apple-system, BlinkMacSystemFont, Roboto, sans-serif';
      ctx.textAlign = 'center';
      ctx.fillText('(Không có sản phẩm)', logicalWidth / 2, y);
      y += 30;
    }

    // Đường kẻ dưới danh sách sản phẩm
    y += 4;
    this.drawLine(ctx, paddingX, y, logicalWidth - paddingX, y);
    y += 22;

    // ─── 4. Tổng kết ───
    ctx.font = '400 17px "Segoe UI", -apple-system, BlinkMacSystemFont, Roboto, sans-serif';

    // Tổng số lượng
    this.drawSummaryRow(ctx, 'Tổng số lượng', String(totalQty), paddingX, colTotalX, y);
    y += 32;

    // Tổng tiền hàng
    this.drawSummaryRow(ctx, 'Tổng tiền hàng', this.formatCurrency(order.totalAmount), paddingX, colTotalX, y);
    y += 32;

    // Tiền cọc
    const depositText = (order.depositAmount && order.depositAmount > 0)
      ? this.formatCurrency(order.depositAmount)
      : '0';
    this.drawSummaryRow(ctx, 'Tiền cọc', depositText, paddingX, colTotalX, y);
    y += 36;

    // Còn lại thanh toán (chữ to, bold)
    ctx.font = 'bold 22px "Segoe UI", -apple-system, BlinkMacSystemFont, Roboto, sans-serif';
    const remainingText = order.remainingAmount > 0
      ? this.formatCurrency(order.remainingAmount)
      : '0';
    this.drawSummaryRow(ctx, 'Còn lại thanh toán', remainingText, paddingX, colTotalX, y);
    y += 38;

    // Đường kẻ trước footer
    this.drawLine(ctx, paddingX, y, logicalWidth - paddingX, y);
    y += 32;

    // ─── 5. Footer ───
    ctx.font = '600 16px -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif';
    const footerText = 'Cảm ơn bạn đã tin tưởng ủng hộ shop ạ ❤️';
    // Loại bỏ ký tự ẩn variation selector (\uFE0F) tránh lỗi tính toán canvas trên iOS Safari
    const cleanFooterText = footerText.replace(/\uFE0F/g, '');
    const textWidth = ctx.measureText(cleanFooterText).width;
    const startX = Math.max(paddingX, (logicalWidth - textWidth) / 2);
    ctx.textAlign = 'left';
    ctx.fillText(cleanFooterText, startX, y);

    return canvas;
  }

  private drawSummaryRow(
    ctx: CanvasRenderingContext2D,
    label: string,
    value: string,
    leftX: number,
    rightX: number,
    y: number
  ): void {
    ctx.textAlign = 'left';
    ctx.fillText(label, leftX, y);
    ctx.textAlign = 'right';
    ctx.fillText(value, rightX, y);
  }

  private drawLine(
    ctx: CanvasRenderingContext2D,
    x1: number,
    y1: number,
    x2: number,
    y2: number,
    width = 1.5
  ): void {
    ctx.save();
    ctx.strokeStyle = '#374151';
    ctx.lineWidth = width;
    ctx.beginPath();
    ctx.moveTo(x1, y1);
    ctx.lineTo(x2, y2);
    ctx.stroke();
    ctx.restore();
  }

  private wrapText(
    ctx: CanvasRenderingContext2D,
    text: string,
    maxWidth: number
  ): string[] {
    const words = text.split(' ');
    const lines: string[] = [];
    let currentLine = '';

    for (const word of words) {
      const testLine = currentLine ? `${currentLine} ${word}` : word;
      const testWidth = ctx.measureText(testLine).width;
      if (testWidth > maxWidth && currentLine) {
        lines.push(currentLine);
        currentLine = word;
      } else {
        currentLine = testLine;
      }
    }
    if (currentLine) lines.push(currentLine);
    return lines;
  }

  private calculateHeight(order: Order, contentWidth: number): number {
    let height = 38 + 36 + 46 + 28 + 28 + 36 + 14 + 30 + 16; // Header + Table headers

    const items = order.items && order.items.length > 0 ? order.items : [];
    // Tạm tính số dòng của tên sản phẩm
    for (const item of items) {
      const fullItemName = item.colorCode ? `${item.productName || ''} ${item.colorCode}` : (item.productName || '');
      const charCount = fullItemName.length;
      const lines = Math.max(1, Math.ceil(charCount / 34));
      height += lines * 24 + 2 + 32;
    }
    if (items.length === 0) height += 30;

    height += 4 + 22; // Divider
    height += 32 + 32 + 36 + 38 + 32 + 32 + 45; // Summary + Footer + Bottom padding
    return Math.max(height, 500);
  }

  private formatDate(dateStr?: string | null): string {
    if (!dateStr) return '';
    const normalized = dateStr.includes(' ') ? dateStr.replace(' ', 'T') : dateStr;
    const d = new Date(normalized);
    if (isNaN(d.getTime())) return dateStr;

    const day = String(d.getDate()).padStart(2, '0');
    const month = String(d.getMonth() + 1).padStart(2, '0');
    const year = d.getFullYear();

    const hasTime = dateStr.includes('T') || dateStr.includes(':');
    if (!hasTime) {
      return `${day}/${month}/${year}`;
    }

    const hours = String(d.getHours()).padStart(2, '0');
    const mins = String(d.getMinutes()).padStart(2, '0');

    return `${day}/${month}/${year} - ${hours}:${mins}`;
  }

  private formatCurrency(amount: number): string {
    return new Intl.NumberFormat('en-US').format(amount);
  }
}

