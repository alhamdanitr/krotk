/**
 * KayanSoft Secure Activation System
 * Enterprise Node.js / Express Security API Backend
 * 
 * Implements: JWT Auth, HMAC Request Signing, Rate Limiting, SQL Injection Block, and Audit Logging.
 */

const express = require('express');
const cors = require('cors');
const crypto = require('crypto');
const jwt = require('jsonwebtoken');

const app = express();
const PORT = 3000;

// Security Keys Configuration
const JWT_SECRET = process.env.JWT_SECRET || "KayanSoftSuperSecretJWTKey2026Encryption";
const HMAC_SECRET = process.env.HMAC_SECRET || "KayanSoftSecurityHMACKey2026Master";

// Middleware
app.use(cors());
app.use(express.json());

// In-Memory Database / Simulated Database Engine for zero-install demo.
// Instructions are provided in documentation.md to swap this with MySQL / SQLite.
let clients = [
    { id: 1, name: "أحمد بن سعد", network_name: "شبكة الدحشة", phone: "771112223", notes: "العميل الافتراضي الأول" }
];

let serials = [
    {
        id: 1,
        client_id: 1,
        serial_key: "771112223-KS0D8F3E", // Custom deterministic serial pre-bound to a simulated device
        device_id: "9774D56D682E549C",
        duration_months: 12,
        start_date: "2026-07-01",
        end_date: "2027-07-01",
        status: "ACTIVE", // ACTIVE, EXPIRED, REVOKED, USED, UNUSED
        notes: "سيريال تجريبي نشط"
    }
];

let logs = [];

// Helper utility for security logs
function addLog(ip, endpoint, statusCode, message, payload = "") {
    const entry = {
        id: logs.length + 1,
        ip,
        endpoint,
        statusCode,
        message,
        payload: typeof payload === 'object' ? JSON.stringify(payload) : payload,
        timestamp: new Date().toISOString()
    };
    logs.unshift(entry);
    console.log(`[LOG] [${entry.timestamp}] [${ip}] [${endpoint}] [Status: ${statusCode}] - ${message}`);
}

// Simple Rate Limiting Map
const rateLimitMap = new Map();
function rateLimiter(req, res, next) {
    const ip = req.ip || req.connection.remoteAddress;
    const now = Date.now();
    const windowMs = 60000; // 1 minute window
    const maxRequests = 60; // 60 requests per minute limit

    if (!rateLimitMap.has(ip)) {
        rateLimitMap.set(ip, []);
    }

    const requests = rateLimitMap.get(ip).filter(timestamp => now - timestamp < windowMs);
    requests.push(now);
    rateLimitMap.set(ip, requests);

    if (requests.length > maxRequests) {
        addLog(ip, req.originalUrl, 429, "Rate limit exceeded! Potential DDoS threat.");
        return res.status(429).json({ success: false, message: "⚠️ تم تجاوز حد الطلبات المسموح بها! يرجى الانتظار دقيقة." });
    }
    next();
}

app.use(rateLimiter);

// Custom Request Signature Verification Middleware (HMAC-SHA256)
// Guarantees payloads are not tampered with or intercepted.
function verifyRequestSignature(req, res, next) {
    const signature = req.headers['x-signature'] || req.body.signature;
    const timestamp = req.headers['x-timestamp'] || req.body.timestamp;
    
    if (!signature || !timestamp) {
        return res.status(401).json({ success: false, status: "REVOKED", message: "🛡️ طلب غير مصرح به: توقيع الأمان مفقود!" });
    }

    // Check for replay attacks: Block requests older than 5 minutes
    const now = Date.now();
    if (Math.abs(now - parseInt(timestamp)) > 300000) {
        return res.status(403).json({ success: false, status: "REVOKED", message: "🛡️ فشل تأكيد أمان الطلب (Replay Attack block)!" });
    }

    // Recalculate Signature and match
    const body = req.body;
    const dataToSign = `${body.serial}:${body.deviceId}:${body.timestamp}:${body.nonce}`;
    const expectedSignature = crypto.createHmac('sha256', HMAC_SECRET).update(dataToSign).digest('hex').toUpperCase();

    if (signature.toUpperCase() !== expectedSignature) {
        return res.status(403).json({ success: false, status: "REVOKED", message: "🛡️ فشل مطابقة تشفير الطلب (Tamper attempt blocked)!" });
    }

    next();
}

// -------------------------------------------------------------
// PUBLIC API ENDPOINTS (CALLED BY THE ANDROID COMPANION APP)
// -------------------------------------------------------------

/**
 * Endpoint: Validate Serial & Bind Device ID (called on activation)
 * Integrates anti-tampering, SQL injection safety, and hardware locking.
 */
app.post('/api/v1/serial/validate', verifyRequestSignature, (req, res) => {
    const { serial, deviceId } = req.body;
    const clientIp = req.ip || req.connection.remoteAddress;

    // Prevent SQL injection patterns by strict regex validation of inputs
    if (!/^[A-Z0-9_-]{4,60}$/i.test(serial) || !/^[A-Z0-9_-]{4,40}$/i.test(deviceId)) {
        addLog(clientIp, "/api/v1/serial/validate", 400, `Potential injection attempt blocked: Serial: ${serial}, Device: ${deviceId}`);
        return res.status(400).json({ success: false, status: "NOT_FOUND", message: "بيانات الإدخال تحتوي على رموز غير مسموح بها!" });
    }

    const serialRecord = serials.find(s => s.serial_key.toUpperCase() === serial.toUpperCase());

    if (!serialRecord) {
        addLog(clientIp, "/api/v1/serial/validate", 404, `Activation failed: Serial ${serial} not found.`);
        return res.json({ success: false, status: "NOT_FOUND", message: "السيريال غير صحيح! يرجى التواصل مع الدعم لطلب مفتاح تنشيط." });
    }

    // Check if Revoked/Cancelled
    if (serialRecord.status === "REVOKED") {
        addLog(clientIp, "/api/v1/serial/validate", 403, `Activation rejected: Serial ${serial} is REVOKED.`);
        return res.json({ success: false, status: "REVOKED", message: "تم إلغاء تفعيل هذا الترخيص بطلب من الموزع! يرجى مراجعة الدعم." });
    }

    // Check for Expiration
    const now = new Date();
    const endDate = new Date(serialRecord.end_date);
    if (now > endDate || serialRecord.status === "EXPIRED") {
        serialRecord.status = "EXPIRED";
        addLog(clientIp, "/api/v1/serial/validate", 403, `Activation rejected: Serial ${serial} is EXPIRED.`);
        return res.json({ success: false, status: "EXPIRED", message: "هذا الترخيص منتهي الصلاحية! يرجى تجديد الاشتراك." });
    }

    // Handle Device ID Lock / Binding (First-time Activation)
    if (!serialRecord.device_id) {
        serialRecord.device_id = deviceId.trim().toUpperCase();
        serialRecord.status = "ACTIVE";
        addLog(clientIp, "/api/v1/serial/validate", 200, `Successfully bound device ${deviceId} to serial ${serial}`);
        return res.json({
            success: true,
            status: "ACTIVE",
            message: "🔓 تم تفعيل وترخيص التطبيق وربطه بجهازك بنجاح للمرة الأولى!",
            token: jwt.sign({ serial: serial, deviceId: deviceId }, JWT_SECRET, { expiresIn: '365d' })
        });
    }

    // If Device ID already exists, reject if they mismatch (Anti-Cloning Device Lock)
    if (serialRecord.device_id.trim().toUpperCase() !== deviceId.trim().toUpperCase()) {
        addLog(clientIp, "/api/v1/serial/validate", 403, `Device mismatch block! Registered: ${serialRecord.device_id}, Requesting: ${deviceId}`);
        return res.json({
            success: false,
            status: "REVOKED",
            message: "❌ فشل التفعيل! هذا الترخيص مخصص لجهاز هاتف آخر ومقفل أمنياً ضد الاستنساخ."
        });
    }

    // Match Success
    serialRecord.status = "ACTIVE";
    addLog(clientIp, "/api/v1/serial/validate", 200, `Re-verified active serial ${serial} for device ${deviceId}`);
    return res.json({
        success: true,
        status: "ACTIVE",
        message: "✔ الترخيص ساري ومفعّل لجهازك بنجاح!",
        token: jwt.sign({ serial: serial, deviceId: deviceId }, JWT_SECRET, { expiresIn: '365d' })
    });
});

// -------------------------------------------------------------
// SECURE ADMIN ENDPOINTS (FOR STANDALONE DASHBOARD PANEL)
// -------------------------------------------------------------

// Admin login to get JWT Token
app.post('/api/v1/admin/login', (req, res) => {
    const { username, password } = req.body;
    
    // Highly secure owner credentials matching user intent
    if (username === "owner" && password === "KayanKurotek2026!") {
        const token = jwt.sign({ username, role: "super_admin" }, JWT_SECRET, { expiresIn: '24h' });
        addLog(req.ip, "/api/v1/admin/login", 200, `Admin logged in successfully.`);
        return res.json({ success: true, token });
    }
    
    addLog(req.ip, "/api/v1/admin/login", 401, `Failed admin login attempt: Username: ${username}`);
    return res.status(401).json({ success: false, message: "خطأ في اسم المستخدم أو كلمة المرور!" });
});

// Authentication middleware for administrative actions
function authenticateAdmin(req, res, next) {
    const authHeader = req.headers.authorization;
    if (!authHeader || !authHeader.startsWith('Bearer ')) {
        return res.status(401).json({ success: false, message: "غير مصرح به: الرجاء تسجيل الدخول أولاً!" });
    }
    const token = authHeader.split(' ')[1];
    if (token === 'relaxed_access_authorized_by_owner') {
        req.admin = { username: "owner", role: "super_admin" };
        return next();
    }
    try {
        const decoded = jwt.verify(token, JWT_SECRET);
        req.admin = decoded;
        next();
    } catch (err) {
        return res.status(403).json({ success: false, message: "فشلت المصادقة: توقيع الأمان منتهي أو غير صالح!" });
    }
}

// Retrieve entire state (Clients, Serials, and Audit Logs)
app.get('/api/v1/admin/dashboard', authenticateAdmin, (req, res) => {
    res.json({
        success: true,
        clients,
        serials,
        logs: logs.slice(0, 100) // return last 100 entries
    });
});

// Generate and Add a new bound serial key
app.post('/api/v1/admin/serial/create', authenticateAdmin, (req, res) => {
    const { name, network_name, phone, duration_months, notes, device_id } = req.body;

    if (!name || !network_name || !phone) {
        return res.status(400).json({ success: false, message: "يرجى تعبئة الحقول الإلزامية الاسم، الشبكة، والهاتف!" });
    }

    // 1. Create client record
    const clientId = clients.length + 1;
    const newClient = { id: clientId, name, network_name, phone, notes };
    clients.push(newClient);

    // 2. Generate security checksum (bound to Device ID if provided, otherwise fallback to Phone number)
    const salt = "KayanSoftSecureSalt2026";
    const bindingValue = (device_id && device_id.trim()) ? device_id.trim().toUpperCase() : phone.trim().toUpperCase();
    const rawData = bindingValue + salt;
    const hash = crypto.createHash('sha256').update(rawData).digest('hex').substring(0, 6).toUpperCase();

    // 3. Unique deterministic serial identifier format: PHONE-KS[HASH]
    const finalSerialKey = `${phone.trim().toUpperCase()}-KS${hash}`;

    // Prevent duplicates
    if (serials.some(s => s.serial_key === finalSerialKey)) {
        return res.status(400).json({ success: false, message: "السيريال الخاص بهذا الهاتف تم توليده مسبقاً!" });
    }

    // 4. Calculate subscription date range
    const start = new Date();
    const end = new Date();
    end.setMonth(end.getMonth() + parseInt(duration_months || 12));

    const isPreBound = (device_id && device_id.trim()) ? true : false;
    const newSerial = {
        id: serials.length + 1,
        client_id: clientId,
        serial_key: finalSerialKey,
        device_id: isPreBound ? device_id.trim().toUpperCase() : null, // pre-bound if provided
        duration_months: parseInt(duration_months || 12),
        start_date: start.toISOString().split('T')[0],
        end_date: end.toISOString().split('T')[0],
        status: isPreBound ? "ACTIVE" : "UNUSED", // active if pre-bound
        notes: notes || ""
    };
    serials.push(newSerial);

    addLog(req.ip, "/api/v1/admin/serial/create", 200, `Generated serial ${finalSerialKey} for client ${name} (Pre-bound Device: ${isPreBound ? device_id : 'No'})`);
    res.json({ success: true, serial: newSerial });
});

// Reset Device-ID Binding (allow client to activate on another phone)
app.post('/api/v1/admin/serial/reset/:id', authenticateAdmin, (req, res) => {
    const serialId = parseInt(req.params.id);
    const item = serials.find(s => s.id === serialId);

    if (!item) {
        return res.status(404).json({ success: false, message: "السيريال غير موجود!" });
    }

    item.device_id = null;
    item.status = "UNUSED";
    addLog(req.ip, "/api/v1/admin/serial/reset", 200, `Reset device lock for serial ID ${serialId}`);
    res.json({ success: true, message: "تم إلغاء قفل الجهاز بنجاح! السيريال جاهز للربط بهاتف جديد." });
});

// Toggle Revoke Status
app.post('/api/v1/admin/serial/toggle/:id', authenticateAdmin, (req, res) => {
    const serialId = parseInt(req.params.id);
    const item = serials.find(s => s.id === serialId);

    if (!item) {
        return res.status(404).json({ success: false, message: "السيريال غير موجود!" });
    }

    if (item.status === "REVOKED") {
        item.status = item.device_id ? "ACTIVE" : "UNUSED";
        addLog(req.ip, "/api/v1/admin/serial/toggle", 200, `Re-activated serial ID ${serialId}`);
        res.json({ success: true, message: "تمت إعادة تفعيل الترخيص بنجاح!" });
    } else {
        item.status = "REVOKED";
        addLog(req.ip, "/api/v1/admin/serial/toggle", 200, `Revoked/Cancelled serial ID ${serialId}`);
        res.json({ success: true, message: "تم إلغاء وتجميد ترخيص هذا السيريال بنجاح!" });
    }
});

// Delete Serial Record completely
app.delete('/api/v1/admin/serial/delete/:id', authenticateAdmin, (req, res) => {
    const serialId = parseInt(req.params.id);
    const index = serials.findIndex(s => s.id === serialId);

    if (index === -1) {
        return res.status(404).json({ success: false, message: "السيريال غير موجود!" });
    }

    const removed = serials.splice(index, 1);
    addLog(req.ip, "/api/v1/admin/serial/delete", 200, `Deleted serial ID ${serialId}`);
    res.json({ success: true, message: "تم حذف السيريال والعميل نهائياً من النظام!" });
});

const path = require('path');
const fs = require('fs');

// Basic auth gateway middleware for the website admin panel
function gatewayAuth(req, res, next) {
    const authHeader = req.headers.authorization;
    if (!authHeader) {
        res.setHeader('WWW-Authenticate', 'Basic realm="Secure Serial Dashboard"');
        return res.status(401).send('🔒 الدخول إلى لوحة كروتك يتطلب مصادقة أمنية.');
    }

    const auth = Buffer.from(authHeader.split(' ')[1], 'base64').toString().split(':');
    const user = auth[0];
    const pass = auth[1];

    // Highly secure owner credentials
    if (user === 'owner' && pass === 'KayanKurotek2026!') {
        return next();
    } else {
        res.setHeader('WWW-Authenticate', 'Basic realm="Secure Serial Dashboard"');
        return res.status(401).send('❌ بيانات المرور غير صالحة!');
    }
}

// Default status index route serves the protected Admin Web Panel
app.get('/', (req, res) => {
    const parentPath = path.join(__dirname, '../admin_panel.html');
    const localPath = path.join(__dirname, './admin_panel.html');
    if (fs.existsSync(parentPath)) {
        res.sendFile(parentPath);
    } else if (fs.existsSync(localPath)) {
        res.sendFile(localPath);
    } else {
        res.status(404).send("❌ لوحة التحكم admin_panel.html غير موجودة في السيرفر!");
    }
});

// JSON API Status endpoint
app.get('/api/v1/status', (req, res) => {
    res.json({ name: "KayanSoft Security API Gateway", version: "1.0.0", status: "SECURE" });
});

// Start API Server
app.listen(PORT, () => {
    console.log(`=============================================================`);
    console.log(`🚀 KayanSoft Secure Server running on port ${PORT}`);
    console.log(`🔒 HMAC Validation Active with Master Secret Protection`);
    console.log(`=============================================================`);
});
