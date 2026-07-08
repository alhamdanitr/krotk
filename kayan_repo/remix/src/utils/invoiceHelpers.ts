import { jsPDF } from 'jspdf';
import { CalculatorType, SaleRecord } from '../types';
import { INITIAL_PRICES } from '../constants';

// --- Sound Chime System (Web Audio API) ---
export const playSuccessSound = (isSoundEnabled: boolean) => {
  if (!isSoundEnabled) return;
  try {
    const AudioCtx = window.AudioContext || (window as any).webkitAudioContext;
    if (!AudioCtx) return;
    const audioCtx = new AudioCtx();
    
    const playBeep = (freq: number, duration: number, delayRef: number) => {
      const osc = audioCtx.createOscillator();
      const gainNode = audioCtx.createGain();
      osc.connect(gainNode);
      gainNode.connect(audioCtx.destination);
      
      osc.type = 'sine';
      osc.frequency.setValueAtTime(freq, audioCtx.currentTime + delayRef);
      
      gainNode.gain.setValueAtTime(0, audioCtx.currentTime + delayRef);
      gainNode.gain.linearRampToValueAtTime(0.12, audioCtx.currentTime + delayRef + 0.03);
      gainNode.gain.exponentialRampToValueAtTime(0.001, audioCtx.currentTime + delayRef + duration);
      
      osc.start(audioCtx.currentTime + delayRef);
      osc.stop(audioCtx.currentTime + delayRef + duration);
    };
    
    // Satisfying cash register double chime:
    playBeep(587.33, 0.15, 0);       // D5
    playBeep(880.00, 0.22, 0.08);     // A5
  } catch (e) {
    console.warn("Audio Context feedback failed:", e);
  }
};

// --- Official Vector Logo drawing function ---
export const drawDahshahLogo = (
  ctx: CanvasRenderingContext2D,
  x: number,
  y: number,
  width: number,
  height: number,
  isThermal: boolean = false
) => {
  ctx.save();
  
  if (isThermal) {
    // Monochrome, crisp line-art representation for thermal printing
    ctx.strokeStyle = '#000000';
    ctx.lineWidth = 1.5;
    
    // Outline box (instead of solid heavy dark fill to conserve printer ink)
    ctx.strokeRect(x, y, width, height);
    
    ctx.fillStyle = '#000000';
    ctx.textAlign = 'center';
    ctx.textBaseline = 'middle';
    ctx.direction = 'rtl';
    
    // Draw "إستراحة"
    ctx.font = 'bold 11px sans-serif';
    ctx.fillText('إسـتـراحـة', x + width / 2 + 15, y + 20);
    
    // Draw "الـدحـشـة"
    ctx.font = 'bold 20px sans-serif';
    ctx.fillText('الـدحـشـة', x + width / 2 + 15, y + 43);
    
    // Draw wifi waves on left
    const centerX = x + 35;
    const centerY = y + 36;
    
    ctx.lineWidth = 2;
    ctx.beginPath();
    ctx.arc(centerX, centerY, 6, Math.PI * 1.15, Math.PI * 1.85);
    ctx.stroke();
    ctx.beginPath();
    ctx.arc(centerX, centerY, 11, Math.PI * 1.15, Math.PI * 1.85);
    ctx.stroke();
    ctx.beginPath();
    ctx.arc(centerX, centerY, 16, Math.PI * 1.15, Math.PI * 1.85);
    ctx.stroke();
    
    // Core dot
    ctx.beginPath();
    ctx.arc(centerX, centerY, 2.5, 0, Math.PI * 2);
    ctx.fill();
    
    // Arrows
    // Down Arrow (left of dot)
    ctx.beginPath();
    ctx.moveTo(centerX - 8, centerY + 8);
    ctx.lineTo(centerX - 8, centerY + 18);
    ctx.stroke();
    // arrow head down
    ctx.beginPath();
    ctx.moveTo(centerX - 11, centerY + 14);
    ctx.lineTo(centerX - 8, centerY + 18);
    ctx.lineTo(centerX - 5, centerY + 14);
    ctx.stroke();
    
    // Up Arrow (right of dot)
    ctx.beginPath();
    ctx.moveTo(centerX + 8, centerY + 18);
    ctx.lineTo(centerX + 8, centerY + 8);
    ctx.stroke();
    // arrow head up
    ctx.beginPath();
    ctx.moveTo(centerX + 5, centerY + 12);
    ctx.lineTo(centerX + 8, centerY + 8);
    ctx.lineTo(centerX + 11, centerY + 12);
    ctx.stroke();

    // Subtitle
    ctx.font = 'bold 7px sans-serif';
    ctx.fillText('ESTRAHA AL-DAHSHAH NET', x + width / 2, y + 71);
    
  } else {
    // --- Premium Full color, styled visual brand banner ---
    
    // Rounded charcoal BG box
    const r = 12; // corner radius
    ctx.beginPath();
    ctx.moveTo(x + r, y);
    ctx.lineTo(x + width - r, y);
    ctx.quadraticCurveTo(x + width, y, x + width, y + r);
    ctx.lineTo(x + width, y + height - r);
    ctx.quadraticCurveTo(x + width, y + height, x + width - r, y + height);
    ctx.lineTo(x + r, y + height);
    ctx.quadraticCurveTo(x, y + height, x, y + height - r);
    ctx.lineTo(x, y + r);
    ctx.quadraticCurveTo(x, y, x + r, y);
    ctx.closePath();
    
    // Create elegant dark gradient background
    const bgGrad = ctx.createLinearGradient(x, y, x, y + height);
    bgGrad.addColorStop(0, '#2d2e2f');
    bgGrad.addColorStop(1, '#1e2021');
    ctx.fillStyle = bgGrad;
    ctx.fill();
    
    // Draw decorative corner gold visual elements matching original logo style
    // Top-left gold splash
    ctx.beginPath();
    ctx.moveTo(x, y + 25);
    ctx.bezierCurveTo(x + 10, y + 20, x + 20, y + 10, x + 25, y);
    ctx.lineTo(x, y);
    ctx.closePath();
    ctx.fillStyle = '#facc15';
    ctx.fill();
    
    // Bottom-right gold splash
    ctx.beginPath();
    ctx.moveTo(x + width, y + height - 25);
    ctx.bezierCurveTo(x + width - 10, y + height - 20, x + width - 20, y + height - 10, x + width - 25, y + height);
    ctx.lineTo(x + width, y + height);
    ctx.closePath();
    ctx.fillStyle = '#facc15';
    ctx.fill();

    ctx.textAlign = 'center';
    ctx.textBaseline = 'middle';
    ctx.direction = 'rtl';
    
    // Draw "إستراحة" in shining gold/yellow
    ctx.shadowColor = 'rgba(0,0,0,0.4)';
    ctx.shadowBlur = 3;
    ctx.fillStyle = '#facc15';
    ctx.font = 'bold 11px sans-serif';
    ctx.fillText('إسـتـراحـة', x + width / 2 + 15, y + 20);
    
    // Draw "الـدحـشـة" in premium white with clean outlines
    ctx.fillStyle = '#ffffff';
    ctx.font = 'bold 21px sans-serif';
    ctx.strokeStyle = '#000000';
    ctx.lineWidth = 1.2;
    ctx.strokeText('الـدحـشـة', x + width / 2 + 15, y + 43);
    ctx.fillText('الـدحـشـة', x + width / 2 + 15, y + 43);
    
    // 3 small golden square accent dots (mimicking accents)
    ctx.shadowBlur = 0; // reset shadow
    ctx.fillStyle = '#facc15';
    ctx.fillRect(x + width / 2 + 3, y + 27, 3, 3);
    ctx.fillRect(x + width / 2 + 9, y + 27, 3, 3);
    
    // Draw Wifi network wave concentric circles (on left)
    const centerX = x + 35;
    const centerY = y + 36;
    
    ctx.lineWidth = 3.2;
    ctx.strokeStyle = '#facc15'; // gold wave color
    
    ctx.beginPath();
    ctx.arc(centerX, centerY, 7, Math.PI * 1.15, Math.PI * 1.85);
    ctx.stroke();
    
    ctx.beginPath();
    ctx.arc(centerX, centerY, 13, Math.PI * 1.15, Math.PI * 1.85);
    ctx.stroke();
    
    ctx.beginPath();
    ctx.arc(centerX, centerY, 19, Math.PI * 1.15, Math.PI * 1.85);
    ctx.stroke();
    
    // Core dot
    ctx.fillStyle = '#facc15';
    ctx.beginPath();
    ctx.arc(centerX, centerY, 3, 0, Math.PI * 2);
    ctx.fill();
    
    // Standard white/silver down arrow and gold up arrow
    ctx.lineWidth = 1.8;
    ctx.strokeStyle = '#ffffff';
    ctx.beginPath();
    ctx.moveTo(centerX - 8, centerY + 8);
    ctx.lineTo(centerX - 8, centerY + 18);
    ctx.stroke();
    
    // Down Arrowhead icon shape
    ctx.fillStyle = '#ffffff';
    ctx.beginPath();
    ctx.moveTo(centerX - 11, centerY + 14);
    ctx.lineTo(centerX - 8, centerY + 18);
    ctx.lineTo(centerX - 5, centerY + 14);
    ctx.fill();
    
    // Gold up arrow icon shape
    ctx.strokeStyle = '#facc15';
    ctx.beginPath();
    ctx.moveTo(centerX + 8, centerY + 18);
    ctx.lineTo(centerX + 8, centerY + 8);
    ctx.stroke();
    
    // Up Arrowhead shape
    ctx.fillStyle = '#facc15';
    ctx.beginPath();
    ctx.moveTo(centerX + 5, centerY + 12);
    ctx.lineTo(centerX + 8, centerY + 8);
    ctx.lineTo(centerX + 11, centerY + 12);
    ctx.fill();

    // Subtitle text
    ctx.fillStyle = '#ffffff';
    ctx.font = 'bold 7px sans-serif';
    ctx.fillText('ESTRAHA AL-DAHSHAH NET', x + width / 2, y + 71);
  }
  
  ctx.restore();
};

// --- Single Invoice Receipt Canvas Drawing & PDF Conversion ---
export const drawInvoiceOnCanvas = (summary: {
  items: { label: string; category: number; quantity: number; price: number; total: number }[];
  totalAmount: number;
  receivedAmount: number;
  remainingAmount: number;
  type: CalculatorType;
  date: string;
  shopName?: string;
}, isThermal: boolean = false): HTMLCanvasElement | null => {
  const canvas = document.createElement('canvas');
  const ctx = canvas.getContext('2d');
  if (!ctx) return null;
  
  const items = summary.items;
  const headerHeight = 165;
  const itemRowHeight = 42;
  const footerHeight = 225;
  const canvasWidth = 500;
  const canvasHeight = headerHeight + (items.length * itemRowHeight) + footerHeight;
  
  canvas.width = canvasWidth;
  canvas.height = canvasHeight;
  
  // Fill background
  ctx.fillStyle = '#ffffff';
  ctx.fillRect(0, 0, canvasWidth, canvasHeight);
  
  if (isThermal) {
    // --- POS Thermal Printer Mode: Pure monochrome high-contrast, no heavy black fills ---
    ctx.strokeStyle = '#000000';
    ctx.lineWidth = 2;
    ctx.strokeRect(10, 10, canvasWidth - 20, canvasHeight - 20);
    
    const drawDashLine = (y: number) => {
      ctx.strokeStyle = '#000000';
      ctx.lineWidth = 1.5;
      ctx.setLineDash([5, 5]);
      ctx.beginPath();
      ctx.moveTo(15, y);
      ctx.lineTo(canvasWidth - 15, y);
      ctx.stroke();
      ctx.setLineDash([]);
    };

    // Draw monochrome black/white network logo
    drawDahshahLogo(ctx, 22, 23, 140, 72, true);

    ctx.fillStyle = '#000000';
    ctx.textBaseline = 'middle';
    ctx.direction = 'rtl';
    
    ctx.textAlign = 'right';
    ctx.font = 'bold 21px sans-serif';
    ctx.fillText('شبكة الدحشة اللاسلكية', canvasWidth - 30, 46);
    ctx.font = 'bold 12px sans-serif';
    ctx.fillText('فاتورة مبيعات كروت اتصالات وموزعين', canvasWidth - 30, 73);
    
    drawDashLine(100);
    
    // Meta details with shop name custom
    ctx.font = 'bold 11px sans-serif';
    ctx.textAlign = 'right';
    ctx.fillText(`التاريخ: ${summary.date}  |  ${summary.type === CalculatorType.PRO ? 'فئة برو Pro' : 'فئة عادية'}`, canvasWidth - 30, 118);
    
    ctx.textAlign = 'left';
    ctx.fillText(`العميل / البقالة: ${summary.shopName || 'بقالة عامة'}`, 30, 118);
    
    drawDashLine(135);
    
    // Table Headers
    let currentY = 158;
    ctx.fillStyle = '#000000';
    ctx.font = 'bold 12px sans-serif';
    ctx.textAlign = 'center';
    ctx.fillText('الرقم', 450, currentY);
    ctx.fillText('أسم المنتج', 320, currentY);
    ctx.fillText('السعر', 205, currentY);
    ctx.fillText('الكمية', 135, currentY);
    ctx.fillText('اجمالي', 65, currentY);
    
    ctx.strokeStyle = '#000000';
    ctx.lineWidth = 1.5;
    ctx.beginPath();
    ctx.moveTo(25, 172);
    ctx.lineTo(canvasWidth - 25, 172);
    ctx.stroke();
    
    // Table Rows
    currentY = 195;
    items.forEach((item, idx) => {
      ctx.textAlign = 'center';
      ctx.fillText(String(idx + 1), 450, currentY);
      ctx.fillText(item.label, 320, currentY);
      ctx.fillText(`${item.price} ريال`, 205, currentY);
      ctx.fillText(String(item.quantity), 135, currentY);
      ctx.fillText(`${item.total} ريال`, 65, currentY);
      
      ctx.strokeStyle = '#cccccc';
      ctx.lineWidth = 1;
      ctx.beginPath();
      ctx.moveTo(25, currentY + 15);
      ctx.lineTo(canvasWidth - 25, currentY + 15);
      ctx.stroke();
      
      currentY += itemRowHeight;
    });
    
    // Total summaries
    drawDashLine(currentY - 5);
    currentY += 20;
    
    ctx.font = 'bold 12px sans-serif';
    ctx.textAlign = 'right';
    ctx.fillText('إجمالي الفاتورة:', canvasWidth - 40, currentY);
    ctx.textAlign = 'left';
    ctx.fillText(`${summary.totalAmount} ريال`, 40, currentY);
    
    currentY += 25;
    ctx.textAlign = 'right';
    ctx.fillText('المبلغ المستلم:', canvasWidth - 40, currentY);
    ctx.textAlign = 'left';
    ctx.fillText(`${summary.receivedAmount} ريال`, 40, currentY);
    
    currentY += 25;
    ctx.textAlign = 'right';
    ctx.fillText('المتبقي للزبون:', canvasWidth - 40, currentY);
    ctx.textAlign = 'left';
    ctx.fillText(`${summary.remainingAmount} ريال`, 40, currentY);
    
    currentY += 20;
    ctx.strokeStyle = '#000000';
    ctx.lineWidth = 1.5;
    drawDashLine(currentY);
    currentY += 28;
    
    ctx.textAlign = 'center';
    ctx.font = 'bold 12px sans-serif';
    ctx.fillText('الموزع والوكيل: أحمد المنتصر - هاتف: 773086403', canvasWidth / 2, currentY);
    currentY += 20;
    ctx.font = '10px sans-serif';
    ctx.fillText('نشكر ثقتكم بنا - شبكة الدحشة اللاسلكية', canvasWidth / 2, currentY);
    
  } else {
    // --- Premium Standard Layout: Clean, Professional, Monochrome Black Texts with Red Bold Figures ---
    
    // Clean thin black framing border
    ctx.strokeStyle = '#000000';
    ctx.lineWidth = 1.5;
    ctx.strokeRect(15, 15, canvasWidth - 30, canvasHeight - 30);
    
    ctx.fillStyle = '#000000';
    ctx.textBaseline = 'middle';
    ctx.direction = 'rtl';
    
    // Center Title "شبكة الدحشة اللاسلكية" at top (No logo as requested)
    ctx.textAlign = 'center';
    ctx.font = 'bold 22px sans-serif';
    ctx.fillText('شبكة الدحشة اللاسلكية', canvasWidth / 2, 50);
    
    ctx.font = 'bold 12px sans-serif';
    ctx.fillText('فاتــورة مبيــعـات كروت اتصالات وموزعين كاش', canvasWidth / 2, 78);
    
    // Simple thin horizontal separator line
    ctx.strokeStyle = '#000000';
    ctx.lineWidth = 1;
    ctx.beginPath();
    ctx.moveTo(35, 102);
    ctx.lineTo(canvasWidth - 35, 102);
    ctx.stroke();
    
    // Metadata block
    ctx.font = 'bold 11px sans-serif';
    ctx.textAlign = 'right';
    ctx.fillText(`تاريخ الفاتورة: ${summary.date}  |  ${summary.type === CalculatorType.PRO ? 'حساب فئة برو Pro' : 'حساب كروت عادية'}`, canvasWidth - 40, 122);
    
    ctx.textAlign = 'left';
    ctx.fillText(`العميل / البقالة: ${summary.shopName || 'بقالة عامة'}`, 40, 122);
    
    // Table Header Area
    let currentY = 155;
    
    const drawHeaderBox = (xStart: number, width: number, label: string) => {
      ctx.strokeStyle = '#000000';
      ctx.lineWidth = 1;
      ctx.strokeRect(xStart, currentY - 11, width, 22);
      ctx.fillStyle = '#000000';
      ctx.font = 'bold 11px sans-serif';
      ctx.textAlign = 'center';
      ctx.fillText(label, xStart + (width / 2), currentY + 1);
    };
    
    drawHeaderBox(440, 30, 'م');
    drawHeaderBox(240, 200, 'أسم المنتج / الفئة');
    drawHeaderBox(165, 75, 'السعر');
    drawHeaderBox(105, 60, 'الكمية');
    drawHeaderBox(30, 75, 'الاجمالي');
    
    // Row listings
    currentY = 182;
    items.forEach((item, idx) => {
      currentY += itemRowHeight;
      
      // Horizontal row separator line
      ctx.strokeStyle = '#cbd5e1';
      ctx.lineWidth = 0.5;
      ctx.beginPath();
      ctx.moveTo(30, currentY + 12);
      ctx.lineTo(canvasWidth - 30, currentY + 12);
      ctx.stroke();
      
      ctx.font = 'bold 12.5px sans-serif';
      
      // ID Row (Black)
      ctx.fillStyle = '#000000';
      ctx.textAlign = 'center';
      ctx.fillText(String(idx + 1), 455, currentY);
      
      // Item Name (Black)
      ctx.textAlign = 'right';
      ctx.fillText(item.label, 420, currentY);
      
      // Price (Red)
      ctx.fillStyle = '#dc2626';
      ctx.textAlign = 'center';
      ctx.fillText(`${item.price} ريال`, 202, currentY);
      
      // Quantity (Black)
      ctx.fillStyle = '#000000';
      ctx.fillText(String(item.quantity), 135, currentY);
      
      // Row Total (Red)
      ctx.fillStyle = '#dc2626';
      ctx.fillText(`${item.total} ريال`, 67, currentY);
    });
    
    currentY += 40;
    
    // Bottom separator
    ctx.strokeStyle = '#e2e8f0';
    ctx.lineWidth = 1;
    ctx.beginPath();
    ctx.moveTo(30, currentY - 12);
    ctx.lineTo(canvasWidth - 30, currentY - 12);
    ctx.stroke();
    
    // Summary values
    ctx.fillStyle = '#000000';
    ctx.font = 'bold 12px sans-serif';
    
    ctx.textAlign = 'right';
    ctx.fillText('الإجمالي الفرعي للفاتورة:', canvasWidth - 180, currentY);
    ctx.fillStyle = '#dc2626'; // Red
    ctx.textAlign = 'left';
    ctx.fillText(`${summary.totalAmount} ريال`, 45, currentY);
    
    currentY += 24;
    ctx.fillStyle = '#000000';
    ctx.textAlign = 'right';
    ctx.fillText('المبلغ المقبوض للزبون:', canvasWidth - 180, currentY);
    ctx.fillStyle = '#dc2626'; // Red
    ctx.textAlign = 'left';
    ctx.fillText(`${summary.receivedAmount} ريال`, 45, currentY);
    
    currentY += 24;
    ctx.fillStyle = '#000000';
    ctx.textAlign = 'right';
    ctx.fillText('المجموع الإجمالي المطلوب:', canvasWidth - 180, currentY);
    ctx.fillStyle = '#dc2626'; // Red
    ctx.textAlign = 'left';
    ctx.fillText(`${summary.totalAmount} ريال`, 45, currentY);
    
    // Outstanding Due Section prominently colored deep red
    currentY += 38;
    ctx.fillStyle = '#000000';
    ctx.font = 'bold 12px sans-serif';
    ctx.textAlign = 'right';
    ctx.fillText('المبلغ المتبقي للعميل:', canvasWidth - 40, currentY);
    
    ctx.fillStyle = '#dc2626'; // Bold red display value
    ctx.font = 'bold 22px sans-serif';
    ctx.fillText(`${summary.remainingAmount} ريال`, canvasWidth - 40, currentY + 24);
    
    // Signature block
    ctx.fillStyle = '#000000';
    ctx.font = 'bold 12px sans-serif';
    ctx.textAlign = 'left';
    ctx.fillText('توقيع الموزع والوكيل المعتمد', 165, currentY + 10);
    
    ctx.strokeStyle = '#000000';
    ctx.lineWidth = 0.8;
    ctx.beginPath();
    ctx.moveTo(35, currentY + 34);
    ctx.lineTo(190, currentY + 34);
    ctx.stroke();
    
    // Distributor details
    currentY += 80;
    ctx.fillStyle = '#000000';
    ctx.textAlign = 'right';
    ctx.font = 'bold 11.5px sans-serif';
    ctx.fillText('الموزع المعترف به والوكيل الحصري: أحمد المنتصر', canvasWidth - 40, currentY);
    
    ctx.font = 'bold 10.5px sans-serif';
    ctx.fillText('للتواصل ومبيعات الجوال: 773086403', canvasWidth - 40, currentY + 16);
    
    ctx.textAlign = 'left';
    ctx.fillText('شبكة الدحشة اللاسلكية - خدمة متميزة وعمل مستقر', canvasWidth - 280, currentY + 8);
  }
  
  return canvas;
};

// PDF Exporter for Single Invoice
export const downloadInvoicePDF = (summary: {
  items: { label: string; category: number; quantity: number; price: number; total: number }[];
  totalAmount: number;
  receivedAmount: number;
  remainingAmount: number;
  type: CalculatorType;
  date: string;
  shopName?: string;
}, isThermal: boolean = false) => {
  const canvas = drawInvoiceOnCanvas(summary, isThermal);
  if (!canvas) return;
  
  const imgData = canvas.toDataURL('image/jpeg', 1.0);
  const canvasWidth = canvas.width;
  const canvasHeight = canvas.height;
  
  const pdfWidth = canvasWidth * 0.264583; // px to mm conversion
  const pdfHeight = canvasHeight * 0.264583;
  
  const pdf = new jsPDF({
    orientation: pdfWidth > pdfHeight ? 'l' : 'p',
    unit: 'mm',
    format: [pdfWidth, pdfHeight]
  });
  
  pdf.addImage(imgData, 'JPEG', 0, 0, pdfWidth, pdfHeight);
  pdf.save(`فاتورة_مبيعات_${summary.date}.pdf`);
};

// Image (PNG) Exporter for Single Invoice
export const downloadInvoicePNG = (summary: {
  items: { label: string; category: number; quantity: number; price: number; total: number }[];
  totalAmount: number;
  receivedAmount: number;
  remainingAmount: number;
  type: CalculatorType;
  date: string;
  shopName?: string;
}, isThermal: boolean = false) => {
  const canvas = drawInvoiceOnCanvas(summary, isThermal);
  if (!canvas) return;
  
  const imgData = canvas.toDataURL('image/png', 1.0);
  const link = document.createElement('a');
  link.setAttribute('href', imgData);
  link.setAttribute('download', `فاتورة_${summary.date}.png`);
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
};

// --- Daily Financial Summary Canvas PDF Drawing ---
export const downloadDailyReportPDF = (report: any, history: SaleRecord[]) => {
  const canvas = document.createElement('canvas');
  const ctx = canvas.getContext('2d');
  if (!ctx) return;
  
  const today = new Date().toISOString().split('T')[0];
  const canvasWidth = 600;
  
  // Collect actual sales segments
  const regularItems = INITIAL_PRICES[CalculatorType.REGULAR].map(p => ({
    label: p.label,
    quantity: report[CalculatorType.REGULAR][p.id]?.quantity || 0,
    amount: report[CalculatorType.REGULAR][p.id]?.amount || 0
  })).filter(item => item.quantity > 0);

  const proItems = INITIAL_PRICES[CalculatorType.PRO].map(p => ({
    label: p.label,
    quantity: report[CalculatorType.PRO][p.id]?.quantity || 0,
    amount: report[CalculatorType.PRO][p.id]?.amount || 0
  })).filter(item => item.quantity > 0);

  const totalSalesCount = history.filter(s => s.date === today).reduce((sum, s) => sum + s.quantity, 0);

  const headerHeight = 150;
  const itemRowHeight = 35;
  const footerHeight = 150;
  
  const totalRows = regularItems.length + proItems.length + 
                    (regularItems.length > 0 ? 1 : 0) + 
                    (proItems.length > 0 ? 1 : 0);
  
  const canvasHeight = headerHeight + (totalRows * itemRowHeight) + footerHeight;
  
  canvas.width = canvasWidth;
  canvas.height = canvasHeight;
  
  // Fill background
  ctx.fillStyle = '#ffffff';
  ctx.fillRect(0, 0, canvasWidth, canvasHeight);
  
  // Clean thin border frame
  ctx.strokeStyle = '#000000';
  ctx.lineWidth = 1.5;
  ctx.strokeRect(15, 15, canvasWidth - 30, canvasHeight - 30);
  
  ctx.textBaseline = 'middle';
  ctx.direction = 'rtl';
  
  // Center Title "شبكة الدحشة اللاسلكية" (No logo as requested)
  ctx.fillStyle = '#000000';
  ctx.font = 'bold 22px sans-serif';
  ctx.textAlign = 'center';
  ctx.fillText('شبكة الدحشة اللاسلكية', canvasWidth / 2, 50);
  
  ctx.font = 'bold 13px sans-serif';
  ctx.fillText('التقرير الحسابي والمالي اليومي الموحد', canvasWidth / 2, 80);
  
  ctx.font = 'bold 11px sans-serif';
  ctx.fillText(`تاريخ التقرير اليومي: ${today}`, canvasWidth / 2, 108);
  
  // Simple divider
  ctx.strokeStyle = '#000000';
  ctx.lineWidth = 1.2;
  ctx.beginPath();
  ctx.moveTo(30, 125);
  ctx.lineTo(canvasWidth - 30, 125);
  ctx.stroke();
  
  let currentY = 150;
  
  const drawSectionHeader = (y: number, title: string) => {
    ctx.fillStyle = '#000000';
    ctx.fillRect(30, y - 12, canvasWidth - 60, 22);
    ctx.fillStyle = '#ffffff';
    ctx.font = 'bold 11px sans-serif';
    ctx.textAlign = 'right';
    ctx.fillText(`  ${title}`, canvasWidth - 45, y);
    ctx.textAlign = 'center';
    ctx.fillText('الكمية المباعة', canvasWidth / 2, y);
    ctx.textAlign = 'left';
    ctx.fillText('إجمالي المبلغ  ', 45, y);
  };
  
  // Draw Regular Section
  if (regularItems.length > 0) {
    drawSectionHeader(currentY, 'قسم كروت الفئات العادية');
    currentY += 30;
    
    regularItems.forEach(item => {
      ctx.strokeStyle = '#cbd5e1';
      ctx.lineWidth = 0.5;
      ctx.beginPath();
      ctx.moveTo(30, currentY + 12);
      ctx.lineTo(canvasWidth - 30, currentY + 12);
      ctx.stroke();
      
      ctx.fillStyle = '#000000';
      ctx.font = 'bold 12px sans-serif';
      
      ctx.textAlign = 'right';
      ctx.fillText(item.label, canvasWidth - 45, currentY);
      
      ctx.textAlign = 'center';
      ctx.fillText(String(item.quantity), canvasWidth / 2, currentY);
      
      ctx.fillStyle = '#dc2626'; // Red
      ctx.textAlign = 'left';
      ctx.fillText(`${item.amount} ريال`, 45, currentY);
      
      currentY += itemRowHeight;
    });
    currentY += 15;
  }
  
  // Draw Pro Section
  if (proItems.length > 0) {
    drawSectionHeader(currentY, 'قسم كروت فئة برو Pro');
    currentY += 30;
    
    proItems.forEach(item => {
      ctx.strokeStyle = '#cbd5e1';
      ctx.lineWidth = 0.5;
      ctx.beginPath();
      ctx.moveTo(30, currentY + 12);
      ctx.lineTo(canvasWidth - 30, currentY + 12);
      ctx.stroke();
      
      ctx.fillStyle = '#000000';
      ctx.font = 'bold 12px sans-serif';
      
      ctx.textAlign = 'right';
      ctx.fillText(item.label, canvasWidth - 45, currentY);
      
      ctx.textAlign = 'center';
      ctx.fillText(String(item.quantity), canvasWidth / 2, currentY);
      
      ctx.fillStyle = '#dc2626'; // Red
      ctx.textAlign = 'left';
      ctx.fillText(`${item.amount} ريال`, 45, currentY);
      
      currentY += itemRowHeight;
    });
    currentY += 15;
  }
  
  // Separator before stats
  ctx.strokeStyle = '#000000';
  ctx.lineWidth = 1;
  ctx.beginPath();
  ctx.moveTo(35, currentY - 15);
  ctx.lineTo(canvasWidth - 35, currentY - 15);
  ctx.stroke();
  
  // Statistics Section
  ctx.fillStyle = '#000000';
  ctx.font = 'bold 12px sans-serif';
  ctx.textAlign = 'right';
  ctx.fillText('إجمالي المبيعات والمدخول المالي الكلي لليوم:', canvasWidth - 50, currentY);
  ctx.fillStyle = '#dc2626'; // Red
  ctx.font = 'bold 16px sans-serif';
  ctx.textAlign = 'left';
  ctx.fillText(`${report.totalAmount} ريال يمني كامل`, 50, currentY);
  
  currentY += 25;
  ctx.fillStyle = '#000000';
  ctx.font = 'bold 12px sans-serif';
  ctx.textAlign = 'right';
  ctx.fillText('حجم الكروت الكلية المباعة اليوم ككل:', canvasWidth - 50, currentY);
  ctx.fillStyle = '#dc2626'; // Red
  ctx.textAlign = 'left';
  ctx.fillText(`${totalSalesCount} كرت`, 50, currentY);
  
  currentY += 22;
  ctx.fillStyle = '#000000';
  ctx.font = 'bold 11px sans-serif';
  ctx.textAlign = 'right';
  ctx.fillText('توقيع المحاسب / الموزع المسؤول:', canvasWidth - 50, currentY);
  ctx.textAlign = 'left';
  ctx.fillText('توقيع الوكيل المعتمد لشمال اليمن', 190, currentY);
  
  ctx.strokeStyle = '#000000';
  ctx.lineWidth = 0.8;
  ctx.beginPath();
  ctx.moveTo(40, currentY + 18);
  ctx.lineTo(165, currentY + 18);
  ctx.stroke();
  
  currentY += 55;
  ctx.fillStyle = '#000000';
  ctx.font = 'bold 10px sans-serif';
  ctx.textAlign = 'center';
  ctx.fillText('هذا كشف مالي رسمي مبسط ملخص لعمل شبكة الدحشة اللاسلكية - أحمد المنتصر (773086403)', canvasWidth / 2, currentY);
  
  const imgData = canvas.toDataURL('image/jpeg', 1.0);
  const pdfWidth = canvasWidth * 0.264583;
  const pdfHeight = canvasHeight * 0.264583;
  const pdf = new jsPDF({
    orientation: pdfWidth > pdfHeight ? 'l' : 'p',
    unit: 'mm',
    format: [pdfWidth, pdfHeight]
  });
  pdf.addImage(imgData, 'JPEG', 0, 0, pdfWidth, pdfHeight);
  pdf.save(`تقرير_الربح_اليومي_الكامل_${today}.pdf`);
};

// --- Detailed Grocery Accounts Statement PDF Rendering & Download Helpers ---
export interface ShopAccountDetail {
  id: string;
  name: string;
  totalSales: number;
  totalPayments: number;
  currentBalance: number;
  createdAt: string;
  transactions: { id: string; date: string; type: 'sale' | 'payment'; amount: number; notes: string }[];
}

export const drawShopStatementOnCanvas = (account: ShopAccountDetail): HTMLCanvasElement | null => {
  const canvas = document.createElement('canvas');
  const ctx = canvas.getContext('2d');
  if (!ctx) return null;
  
  const transactions = account.transactions || [];
  const headerHeight = 290; // Expanded to accommodate distinct header and metadata box
  const rowHeight = 35;
  const footerHeight = 150;
  const canvasWidth = 650;
  const canvasHeight = headerHeight + (transactions.length * rowHeight) + footerHeight;
  
  canvas.width = canvasWidth;
  canvas.height = canvasHeight;
  
  // Fill background
  ctx.fillStyle = '#ffffff';
  ctx.fillRect(0, 0, canvasWidth, canvasHeight);
  
  // Double-border official layout frame
  ctx.strokeStyle = '#000000';
  ctx.lineWidth = 1.5;
  ctx.strokeRect(15, 15, canvasWidth - 30, canvasHeight - 30);
  
  ctx.strokeStyle = '#000000';
  ctx.lineWidth = 0.5;
  ctx.strokeRect(20, 20, canvasWidth - 40, canvasHeight - 40);
  
  // Set default direction and baseline
  ctx.textBaseline = 'middle';
  ctx.direction = 'rtl';
  
  // Center Title "شبكة الدحشة اللاسلكية" with dual lines
  ctx.fillStyle = '#000000';
  ctx.font = 'bold 24px sans-serif';
  ctx.textAlign = 'center';
  ctx.fillText('شبكة الدحشة اللاسلكية', canvasWidth / 2, 50);
  
  ctx.font = 'bold 12px sans-serif';
  ctx.fillText('كشف حساب ومطابقة حسابات البقالات والعملاء المعتمدين', canvasWidth / 2, 80);
  
  // Title Parallel Divider lines
  ctx.strokeStyle = '#000000';
  ctx.lineWidth = 1.2;
  ctx.beginPath();
  ctx.moveTo(40, 96);
  ctx.lineTo(canvasWidth - 40, 96);
  ctx.stroke();
  
  ctx.lineWidth = 0.5;
  ctx.beginPath();
  ctx.moveTo(50, 100);
  ctx.lineTo(canvasWidth - 50, 100);
  ctx.stroke();
  
  // --- Beautiful Metadata Box Grid (Bookkeeping Style) ---
  const metaY = 115;
  const metaHeight = 70;
  const metaWidth = 590; // canvasWidth - 60
  
  // Draw metadata bounding box
  ctx.strokeStyle = '#000000';
  ctx.lineWidth = 1;
  ctx.strokeRect(30, metaY, metaWidth, metaHeight);
  
  // Draw vertical dividers inside metadata box
  ctx.beginPath();
  ctx.moveTo(30 + 200, metaY);
  ctx.lineTo(30 + 200, metaY + metaHeight);
  ctx.moveTo(30 + 400, metaY);
  ctx.lineTo(30 + 400, metaY + metaHeight);
  ctx.stroke();
  
  // Draw horizontal divider inside metadata box
  ctx.beginPath();
  ctx.moveTo(30, metaY + 30);
  ctx.lineTo(30 + metaWidth, metaY + 30);
  ctx.stroke();
  
  // Fill cells titles background
  ctx.fillStyle = '#f8fafc';
  ctx.fillRect(31, metaY + 1, 198, 28);
  ctx.fillRect(231, metaY + 1, 198, 28);
  ctx.fillRect(431, metaY + 1, 188, 28);
  
  ctx.fillStyle = '#000000';
  ctx.font = 'bold 11px sans-serif';
  
  // Column 1 Titles
  ctx.textAlign = 'center';
  ctx.fillText('العميل المستعلم وحساب البقالة', 30 + 495, metaY + 15);
  ctx.fillText('تاريخ كشف الحساب والمسؤول', 30 + 300, metaY + 15);
  ctx.fillText('الرصيد والمستحقات الكلية', 30 + 100, metaY + 15);
  
  // Values
  ctx.fillStyle = '#000000';
  ctx.font = 'bold 12px sans-serif';
  // Client values
  ctx.fillText(account.name, 30 + 495, metaY + 48);
  ctx.font = '900 10.5px sans-serif';
  ctx.fillText(`رقم الحساب: ${account.id}`, 30 + 495, metaY + 60);
  
  // Date values
  ctx.fillText(new Date().toISOString().split('T')[0], 30 + 300, metaY + 48);
  ctx.font = '900 10.5px sans-serif';
  ctx.fillText('الموزع: أحمد المنتصر', 30 + 300, metaY + 60);
  
  // Money values (Highlighted in RED)
  ctx.fillStyle = '#dc2626';
  ctx.font = 'bold 14px sans-serif';
  ctx.fillText(`${account.currentBalance} ريال`, 30 + 100, metaY + 44);
  ctx.font = 'bold 10px sans-serif';
  ctx.fillText(`إجمالي المبيعات: ${account.totalSales} ريال`, 30 + 100, metaY + 60);
  
  // --- Authentic Accounting Ledger Sheet Grid ---
  let currentY = 205;
  const tableStartY = currentY;
  
  // Column bounds definitions (Left-to-right)
  const cols = [
    { name: 'الرصيد التراكمي', x: 30, width: 85, align: 'center', isAction: false },
    { name: 'المبلغ', x: 115, width: 85, align: 'center', isAction: true },
    { name: 'تفاصيل الحركة والبيان', x: 200, width: 190, align: 'right', isAction: false },
    { name: 'نوع العملية', x: 390, width: 95, align: 'right', isAction: false },
    { name: 'التاريخ واليوم', x: 485, width: 100, align: 'center', isAction: false },
    { name: 'م', x: 585, width: 35, align: 'center', isAction: false }
  ];
  
  // Draw header row background
  ctx.fillStyle = '#000000';
  ctx.fillRect(30, currentY - 12, 590, 24);
  
  // Draw column labels
  ctx.fillStyle = '#ffffff';
  ctx.font = 'bold 11px sans-serif';
  cols.forEach(col => {
    ctx.textAlign = col.align as CanvasTextAlign;
    const textX = col.align === 'center' ? col.x + col.width / 2 : col.x + col.width - 12;
    ctx.fillText(col.name, textX, currentY);
  });
  
  // Update starting line post header
  currentY += 12;
  
  let tempRunning = 0;
  
  // Transactions rows loop
  transactions.forEach((tx, idx) => {
    currentY += rowHeight;
    
    // Draw row background alternating highlights
    ctx.fillStyle = idx % 2 === 0 ? '#ffffff' : '#fcfcfc';
    ctx.fillRect(31, currentY - rowHeight + 1, 588, rowHeight - 1);
    
    // Draw row bottom line
    ctx.strokeStyle = '#000000';
    ctx.lineWidth = 0.5;
    ctx.beginPath();
    ctx.moveTo(30, currentY);
    ctx.lineTo(620, currentY);
    ctx.stroke();
    
    // Calculate running balance
    if (tx.type === 'sale') {
      tempRunning += tx.amount;
    } else {
      tempRunning -= tx.amount;
    }
    
    const opLabel = tx.type === 'sale' ? 'فاتورة مبيعات' : 'سداد نقد مستلم';
    
    // Row column values representation
    cols.forEach(col => {
      ctx.textAlign = col.align as CanvasTextAlign;
      const textX = col.align === 'center' ? col.x + col.width / 2 : col.x + col.width - 12;
      
      ctx.font = 'bold 11.5px sans-serif';
      
      if (col.name === 'م') {
        ctx.fillStyle = '#000000';
        ctx.fillText(String(idx + 1), textX, currentY - rowHeight / 2);
      } else if (col.name === 'التاريخ واليوم') {
        ctx.fillStyle = '#000000';
        ctx.fillText(tx.date, textX, currentY - rowHeight / 2);
      } else if (col.name === 'نوع العملية') {
        ctx.fillStyle = tx.type === 'sale' ? '#000000' : '#000000';
        ctx.fillText(opLabel, textX, currentY - rowHeight / 2);
      } else if (col.name === 'تفاصيل الحركة والبيان') {
        ctx.fillStyle = '#000000';
        ctx.fillText(tx.notes || '', textX, currentY - rowHeight / 2);
      } else if (col.name === 'المبلغ') {
        ctx.fillStyle = '#dc2626'; // Red for monetary figure
        const numSign = tx.type === 'sale' ? '+' : '-';
        ctx.fillText(`${numSign}${tx.amount} ريال`, textX, currentY - rowHeight / 2);
      } else if (col.name === 'الرصيد التراكمي') {
        ctx.fillStyle = '#dc2626'; // Red for running balance
        ctx.fillText(`${tempRunning} ريال`, textX, currentY - rowHeight / 2);
      }
    });
  });
  
  // Draw the full bookkeeping vertical grid lines
  ctx.strokeStyle = '#000000';
  ctx.lineWidth = 0.8;
  cols.forEach(col => {
    ctx.beginPath();
    ctx.moveTo(col.x, tableStartY - 12);
    ctx.lineTo(col.x, currentY);
    ctx.stroke();
  });
  // Rightmost end boundary line
  ctx.beginPath();
  ctx.moveTo(620, tableStartY - 12);
  ctx.lineTo(620, currentY);
  ctx.stroke();
  
  // Add some space for the formal signature ledger block
  currentY += 45;
  
  // Bottom summary divider line
  ctx.strokeStyle = '#000000';
  ctx.lineWidth = 1.2;
  ctx.beginPath();
  ctx.moveTo(30, currentY);
  ctx.lineTo(canvasWidth - 30, currentY);
  ctx.stroke();
  
  // Current outstanding highlighted block
  ctx.fillStyle = '#000000';
  ctx.font = 'bold 12.5px sans-serif';
  ctx.textAlign = 'right';
  ctx.fillText('بصمة رصيد حساب البقالة النهائي المطالب بدفعه وتصفيته حالياً:', canvasWidth - 45, currentY + 22);
  
  ctx.font = 'bold 20px sans-serif';
  ctx.fillStyle = '#dc2626'; // Red
  ctx.textAlign = 'left';
  ctx.fillText(`${account.currentBalance} ريال يمني مستحق للشبكة`, 45, currentY + 22);
  
  // Signatures segment
  currentY += 70;
  
  ctx.strokeStyle = '#000000';
  ctx.lineWidth = 0.5;
  
  // Accountant Signature
  ctx.fillStyle = '#000000';
  ctx.font = 'bold 11px sans-serif';
  ctx.textAlign = 'right';
  ctx.fillText('توقيع وختم الوكيل المعتمد (أحمد المنتصر):', canvasWidth - 45, currentY);
  
  ctx.beginPath();
  ctx.moveTo(canvasWidth - 110, currentY + 28);
  ctx.lineTo(canvasWidth - 280, currentY + 28);
  ctx.stroke();
  
  // Customer Signature
  ctx.textAlign = 'left';
  ctx.fillText('توقيع وختم العميل / صاحب البقالة:', 45, currentY);
  
  ctx.beginPath();
  ctx.moveTo(110, currentY + 28);
  ctx.lineTo(280, currentY + 28);
  ctx.stroke();
  
  currentY += 60;
  ctx.textAlign = 'center';
  ctx.font = 'bold 10.5px sans-serif';
  ctx.fillText('شبكة الدحشة اللاسلكية - خدمة متميزة وعمل مستمر هاتف: 773086403', canvasWidth / 2, currentY);
  
  return canvas;
};

export const downloadShopStatementPDF = (account: ShopAccountDetail) => {
  const canvas = drawShopStatementOnCanvas(account);
  if (!canvas) return;
  
  const imgData = canvas.toDataURL('image/jpeg', 1.0);
  const canvasWidth = canvas.width;
  const canvasHeight = canvas.height;
  
  const pdfWidth = canvasWidth * 0.264583; // px to mm conversion
  const pdfHeight = canvasHeight * 0.264583;
  
  const pdf = new jsPDF({
    orientation: pdfWidth > pdfHeight ? 'l' : 'p',
    unit: 'mm',
    format: [pdfWidth, pdfHeight]
  });
  
  pdf.addImage(imgData, 'JPEG', 0, 0, pdfWidth, pdfHeight);
  pdf.save(`كشف_حساب_رسمي_${account.name}_${new Date().toISOString().split('T')[0]}.pdf`);
};

// --- formatted Text sharing generators ---
export const handleWhatsAppSingleShare = (summary: {
  items: { label: string; category: number; quantity: number; price: number; total: number }[];
  totalAmount: number;
  receivedAmount: number;
  remainingAmount: number;
  type: CalculatorType;
  date: string;
  shopName?: string;
}, isThermal: boolean = false) => {
  // 1. Download PDF in parallel
  downloadInvoicePDF(summary, isThermal);
  
  // 2. Prepare text content
  const timestamp = new Date().toLocaleTimeString('ar-YE', { hour: '2-digit', minute: '2-digit' });
  const typeText = summary.type === CalculatorType.PRO ? 'كروت برو PRO🚀' : 'كروت عادية🔹';
  
  let msg = `*📄 فاتورة مبيعات شبكة الدحشة اللاسلكية 📄*\n`;
  msg += `*العميل / البقالة:* ${summary.shopName || 'بقالة عامة'}\n`;
  msg += `*التاريخ:* ${summary.date} - *الوقت:* ${timestamp}\n`;
  msg += `*نوع الحساب:* ${typeText}\n`;
  msg += `------------------------------------\n`;
  
  summary.items.forEach(item => {
    msg += `🔹 *${item.quantity} أبو ${item.category}* = ${item.total} ريال\n`;
  });
  
  msg += `------------------------------------\n`;
  msg += `💵 *الإجمالي الكلي:* *${summary.totalAmount} ريال*\n`;
  msg += `📥 *المستلم:* ${summary.receivedAmount} ريال\n`;
  msg += `🔄 *الباقي للزبون:* *${summary.remainingAmount} ريال*\n`;
  msg += `------------------------------------\n`;
  msg += `📌 _تم حفظ نسخة PDF الفاتورة على جهازك ويمكنك ارفاق ملف الـ PDF الآن في المحادثة مباشرة!_`;
  
  const enc = encodeURIComponent(msg);
  window.open(`https://wa.me/?text=${enc}`, '_blank');
};

export const handleSMSSingleShare = (summary: {
  items: { label: string; category: number; quantity: number; price: number; total: number }[];
  totalAmount: number;
}) => {
  // Format exact text according to user request:
  // (عدد الكروت ) أبو (فءه الكرت ) = ( اجمالي سعر الفءه فقط)
  // ـــــــــــــــــــــ
  // الإجمالي ( الإجمالي الكلي )
  const lines = summary.items.map((item) => `${item.quantity} أبو ${item.category} = ${item.total}`);
  const text = `${lines.join('\n')}\n ـــــــــــــــــــــ\nالإجمالي ${summary.totalAmount}`;
  
  const enc = encodeURIComponent(text);
  window.open(`sms:?body=${enc}`, '_blank');
};

// Share WhatsApp full day report
export const handleWhatsAppReportShare = (report: any, history: SaleRecord[]) => {
  const today = new Date().toISOString().split('T')[0];
  const totalSalesCount = history.filter(s => s.date === today).reduce((sum, s) => sum + s.quantity, 0);
  
  let msg = `*📊 تقرير حركة المبيعات اليومي - شبكة الدحشة اللاسلكية 📊*\n`;
  msg += `*اليوم:* ${today}\n`;
  msg += `------------------------------------\n`;
  
  // Regular
  let hasReg = false;
  let regStr = `*💸 الكروت العادية:* \n`;
  INITIAL_PRICES[CalculatorType.REGULAR].forEach(p => {
    const stats = report[CalculatorType.REGULAR][p.id];
    if (stats && stats.quantity > 0) {
      regStr += `  ▫️ *فئة ${p.id}:* باع ${stats.quantity} | إجمالي ${stats.amount} ريال\n`;
      hasReg = true;
    }
  });
  if (hasReg) msg += regStr + `\n`;
  
  // Pro
  let hasPro = false;
  let proStr = `*⚡ كروت Pro:* \n`;
  INITIAL_PRICES[CalculatorType.PRO].forEach(p => {
    const stats = report[CalculatorType.PRO][p.id];
    if (stats && stats.quantity > 0) {
      proStr += `  ▫️ *فئة ${p.id}:* باع ${stats.quantity} | إجمالي ${stats.amount} ريال\n`;
      hasPro = true;
    }
  });
  if (hasPro) msg += proStr + `\n`;
  
  msg += `------------------------------------\n`;
  msg += `📦 *إجمالي الكروت المباعة:* ${totalSalesCount} كرت\n`;
  msg += `💰 *إجمالي دخل اليوم كلياً:* *${report.totalAmount} ريال يمني*\n`;
  msg += `------------------------------------\n`;
  msg += `📥 _تم استخراج ملف PDF مالي تفصيلي لحساب اليوم المجموع على جهازك!_`;

  downloadDailyReportPDF(report, history);
  
  const enc = encodeURIComponent(msg);
  window.open(`https://wa.me/?text=${enc}`, '_blank');
};

// Share SMS full day report
export const handleSMSReportShare = (report: any, history: SaleRecord[]) => {
  const today = new Date().toISOString().split('T')[0];
  const totalSalesCount = history.filter(s => s.date === today).reduce((sum, s) => sum + s.quantity, 0);
  
  let text = `كشف اليوم مبيعات شبكة الدحشة اللاسلكية ${today}\n`;
  text += `إجمالي المبيعات: ${report.totalAmount} ريال\n`;
  text += `حجم الكروت: ${totalSalesCount} كروت\n`;
  
  const enc = encodeURIComponent(text);
  window.open(`sms:?body=${enc}`, '_blank');
};
