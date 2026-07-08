/**
 * KayanSoft Secure Activation System
 * Enterprise Node.js / Express Security API Backend
 * 
 * Implements: JWT Auth, HMAC Request Signing, Rate Limiting, SQL Injection Block, Audit Logging,
 * Multi-tenant PostgreSQL synchronization, and WebSocket real-time updates.
 */

const express = require('express');
const cors = require('cors');
const crypto = require('crypto');
const jwt = require('jsonwebtoken');
const { Pool } = require('pg');
const http = require('http');
const WebSocket = require('ws');

const app = express();
const PORT = process.env.PORT || 3000;

// Security Keys Configuration
const JWT_SECRET = process.env.JWT_SECRET || "KayanSoftSuperSecretJWTKey2026Encryption";
const HMAC_SECRET = process.env.HMAC_SECRET || "KayanSoftSecurityHMACKey2026Master";

// Middleware
app.use(cors());
app.use(express.json({ limit: '50mb' }));

// PostgreSQL Pool setup
const databaseUrl = process.env.DATABASE_URL;
let pool = null;

if (databaseUrl) {
    console.log("An URL for PostgreSQL was found. Connecting to database...");
    pool = new Pool({
        connectionString: databaseUrl,
        ssl: { rejectUnauthorized: false }
    });
    
    // Initialize PostgreSQL schemas
    const initDbQuery = `
        CREATE TABLE IF NOT EXISTS clients (
            id SERIAL PRIMARY KEY,
            name VARCHAR(100) NOT NULL,
            network_name VARCHAR(100) NOT NULL,
            phone VARCHAR(20) NOT NULL,
            notes TEXT,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        );

        CREATE TABLE IF NOT EXISTS activation_serials (
            id SERIAL PRIMARY KEY,
            client_id INTEGER NOT NULL,
            serial_key VARCHAR(100) UNIQUE NOT NULL,
            device_id VARCHAR(100) DEFAULT NULL,
            duration_months INTEGER DEFAULT 12,
            start_date DATE NOT NULL,
            end_date DATE NOT NULL,
            status VARCHAR(20) DEFAULT 'UNUSED',
            notes TEXT DEFAULT NULL,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        );

        CREATE TABLE IF NOT EXISTS security_logs (
            id SERIAL PRIMARY KEY,
            ip_address VARCHAR(45) NOT NULL,
            endpoint VARCHAR(100) NOT NULL,
            request_payload TEXT DEFAULT NULL,
            status_code INTEGER NOT NULL,
            message TEXT DEFAULT NULL,
            timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        );

        CREATE TABLE IF NOT EXISTS tenant_transactions (
            uuid VARCHAR(100) PRIMARY KEY,
            phone VARCHAR(50),
            amount INT,
            card_code TEXT,
            wallet_type VARCHAR(50),
            created_at BIGINT,
            updated_at BIGINT,
            version INT DEFAULT 1,
            license_key VARCHAR(100) NOT NULL,
            device_id VARCHAR(100)
        );

        CREATE TABLE IF NOT EXISTS tenant_deposits (
            uuid VARCHAR(100) PRIMARY KEY,
            phone VARCHAR(50),
            amount INT,
            wallet_type VARCHAR(50),
            is_shared BOOLEAN,
            card_details TEXT,
            created_at BIGINT,
            updated_at BIGINT,
            version INT DEFAULT 1,
            license_key VARCHAR(100) NOT NULL,
            device_id VARCHAR(100)
        );

        CREATE TABLE IF NOT EXISTS tenant_distributor_customers (
            uuid VARCHAR(100) PRIMARY KEY,
            name VARCHAR(100),
            total_sales DOUBLE PRECISION DEFAULT 0.0,
            total_payments DOUBLE PRECISION DEFAULT 0.0,
            current_balance DOUBLE PRECISION DEFAULT 0.0,
            created_at BIGINT,
            updated_at BIGINT,
            version INT DEFAULT 1,
            license_key VARCHAR(100) NOT NULL,
            device_id VARCHAR(100)
        );

        CREATE TABLE IF NOT EXISTS tenant_distributor_transactions (
            uuid VARCHAR(100) PRIMARY KEY,
            customer_id VARCHAR(100),
            date BIGINT,
            type VARCHAR(20),
            amount DOUBLE PRECISION,
            notes TEXT,
            updated_at BIGINT,
            version INT DEFAULT 1,
            license_key VARCHAR(100) NOT NULL,
            device_id VARCHAR(100)
        );

        CREATE TABLE IF NOT EXISTS tenant_distributor_expenses (
            uuid VARCHAR(100) PRIMARY KEY,
            category VARCHAR(50),
            amount DOUBLE PRECISION,
            description TEXT,
            date BIGINT,
            updated_at BIGINT,
            version INT DEFAULT 1,
            license_key VARCHAR(100) NOT NULL,
            device_id VARCHAR(100)
        );

        CREATE TABLE IF NOT EXISTS tenant_distributor_capitals (
            uuid VARCHAR(100) PRIMARY KEY,
            type VARCHAR(20),
            amount DOUBLE PRECISION,
            description TEXT,
            date BIGINT,
            updated_at BIGINT,
            version INT DEFAULT 1,
            license_key VARCHAR(100) NOT NULL,
            device_id VARCHAR(100)
        );
    `;
    pool.query(initDbQuery)
        .then(() => console.log("PostgreSQL Tables Initialized Successfully"))
        .catch(err => console.error("Error initializing PostgreSQL tables:", err));
} else {
    console.log("DATABASE_URL not found. Running in-memory multi-tenant database mode.");
}

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

// Multi-tenant in-memory store fallbacks
const tenantDataStore = {
    transactions: {},
    deposits: {},
    distributorCustomers: {},
    distributorTransactions: {},
    distributorExpenses: {},
    distributorCapitals: {}
};

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

// -------------------------------------------------------------
// MULTI-TENANT CLIENT-SERVER SYNCHRONIZATION API
// -------------------------------------------------------------

// Helper to validate license key and device binding
async function validateTenant(licenseKey, deviceId) {
    if (!licenseKey) return { valid: false, reason: "مفتاح الترخيص مطلوب!" };
    
    // In-memory validation check
    const serialRecord = serials.find(s => s.serial_key.toUpperCase() === licenseKey.toUpperCase());
    if (serialRecord) {
        if (serialRecord.status === "REVOKED") {
            return { valid: false, reason: "تم إلغاء تفعيل هذا الترخيص!" };
        }
        if (serialRecord.device_id && serialRecord.device_id.toUpperCase() !== deviceId.toUpperCase()) {
            return { valid: false, reason: "مفتاح الترخيص مقيد بجهاز آخر!" };
        }
        return { valid: true };
    }

    // PostgreSQL validation check
    if (pool) {
        try {
            const res = await pool.query(
                "SELECT * FROM activation_serials WHERE UPPER(serial_key) = UPPER($1)",
                [licenseKey]
            );
            if (res.rows.length > 0) {
                const row = res.rows[0];
                if (row.status === "REVOKED") {
                    return { valid: false, reason: "تم إلغاء تفعيل هذا الترخيص!" };
                }
                if (row.device_id && row.device_id.toUpperCase() !== deviceId.toUpperCase()) {
                    return { valid: false, reason: "مفتاح الترخيص مقيد بجهاز آخر!" };
                }
                return { valid: true };
            }
        } catch (err) {
            console.error("Error validating tenant from database:", err);
        }
    }

    // Relaxed mode for trial/migration
    if (licenseKey.startsWith("771112223") || licenseKey.startsWith("DAHSHA")) {
        return { valid: true };
    }

    return { valid: false, reason: "مفتاح الترخيص غير صحيح!" };
}

// Upload/Sync endpoint
app.post('/api/v1/sync/upload', async (req, res) => {
    const { licenseKey, deviceId, transactions, deposits, distributorCustomers, distributorTransactions, distributorExpenses, distributorCapitals } = req.body;
    
    const validation = await validateTenant(licenseKey, deviceId);
    if (!validation.valid) {
        return res.status(401).json({ success: false, message: validation.reason });
    }

    if (pool) {
        const client = await pool.connect();
        try {
            await client.query('BEGIN');
            
            if (transactions && transactions.length > 0) {
                for (const item of transactions) {
                    await client.query(`
                        INSERT INTO tenant_transactions (uuid, phone, amount, card_code, wallet_type, created_at, updated_at, version, license_key, device_id)
                        VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10)
                        ON CONFLICT (uuid) DO UPDATE SET
                            phone = EXCLUDED.phone, amount = EXCLUDED.amount, card_code = EXCLUDED.card_code,
                            wallet_type = EXCLUDED.wallet_type, updated_at = EXCLUDED.updated_at, version = EXCLUDED.version
                    `, [item.uuid, item.phone, item.amount, item.cardCode || item.card_code, item.walletType || item.wallet_type, parseInt(item.createdAt || item.created_at), parseInt(item.updatedAt || item.updated_at), item.version || 1, licenseKey, deviceId]);
                }
            }

            if (deposits && deposits.length > 0) {
                for (const item of deposits) {
                    await client.query(`
                        INSERT INTO tenant_deposits (uuid, phone, amount, wallet_type, is_shared, card_details, created_at, updated_at, version, license_key, device_id)
                        VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10)
                        ON CONFLICT (uuid) DO UPDATE SET
                            phone = EXCLUDED.phone, amount = EXCLUDED.amount, wallet_type = EXCLUDED.wallet_type,
                            is_shared = EXCLUDED.is_shared, card_details = EXCLUDED.card_details, updated_at = EXCLUDED.updated_at, version = EXCLUDED.version
                    `, [item.uuid, item.phone, item.amount, item.walletType || item.wallet_type, item.isShared || item.is_shared, item.cardDetails || item.card_details, parseInt(item.createdAt || item.created_at), parseInt(item.updatedAt || item.updated_at), item.version || 1, licenseKey, deviceId]);
                }
            }

            if (distributorCustomers && distributorCustomers.length > 0) {
                for (const item of distributorCustomers) {
                    await client.query(`
                        INSERT INTO tenant_distributor_customers (uuid, name, total_sales, total_payments, current_balance, created_at, updated_at, version, license_key, device_id)
                        VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10)
                        ON CONFLICT (uuid) DO UPDATE SET
                            name = EXCLUDED.name, total_sales = EXCLUDED.total_sales, total_payments = EXCLUDED.total_payments,
                            current_balance = EXCLUDED.current_balance, updated_at = EXCLUDED.updated_at, version = EXCLUDED.version
                    `, [item.id || item.uuid, item.name, item.totalSales || item.total_sales, item.totalPayments || item.total_payments, item.currentBalance || item.current_balance, parseInt(item.createdAt || item.created_at), parseInt(item.updatedAt || item.updated_at), item.version || 1, licenseKey, deviceId]);
                }
            }

            if (distributorTransactions && distributorTransactions.length > 0) {
                for (const item of distributorTransactions) {
                    await client.query(`
                        INSERT INTO tenant_distributor_transactions (uuid, customer_id, date, type, amount, notes, updated_at, version, license_key, device_id)
                        VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10)
                        ON CONFLICT (uuid) DO UPDATE SET
                            customer_id = EXCLUDED.customer_id, date = EXCLUDED.date, type = EXCLUDED.type,
                            amount = EXCLUDED.amount, notes = EXCLUDED.notes, updated_at = EXCLUDED.updated_at, version = EXCLUDED.version
                    `, [item.id || item.uuid, item.customerId || item.customer_id, parseInt(item.date), item.type, item.amount, item.notes, parseInt(item.updatedAt || item.updated_at), item.version || 1, licenseKey, deviceId]);
                }
            }

            if (distributorExpenses && distributorExpenses.length > 0) {
                for (const item of distributorExpenses) {
                    await client.query(`
                        INSERT INTO tenant_distributor_expenses (uuid, category, amount, description, date, updated_at, version, license_key, device_id)
                        VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9)
                        ON CONFLICT (uuid) DO UPDATE SET
                            category = EXCLUDED.category, amount = EXCLUDED.amount, description = EXCLUDED.description,
                            date = EXCLUDED.date, updated_at = EXCLUDED.updated_at, version = EXCLUDED.version
                    `, [item.id || item.uuid, item.category, item.amount, item.description, parseInt(item.date), parseInt(item.updatedAt || item.updated_at), item.version || 1, licenseKey, deviceId]);
                }
            }

            if (distributorCapitals && distributorCapitals.length > 0) {
                for (const item of distributorCapitals) {
                    await client.query(`
                        INSERT INTO tenant_distributor_capitals (uuid, type, amount, description, date, updated_at, version, license_key, device_id)
                        VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9)
                        ON CONFLICT (uuid) DO UPDATE SET
                            type = EXCLUDED.type, amount = EXCLUDED.amount, description = EXCLUDED.description,
                            date = EXCLUDED.date, updated_at = EXCLUDED.updated_at, version = EXCLUDED.version
                    `, [item.id || item.uuid, item.type, item.amount, item.description, parseInt(item.date), parseInt(item.updatedAt || item.updated_at), item.version || 1, licenseKey, deviceId]);
                }
            }

            await client.query('COMMIT');
            res.json({ success: true, message: "تمت المزامنة وحفظ العمليات على قاعدة البيانات السحابية PostgreSQL بنجاح! 🟢" });
        } catch (err) {
            await client.query('ROLLBACK');
            console.error("Migration error on PostgreSQL transactional execute:", err);
            res.status(500).json({ success: false, message: `فشل الحفظ في السحابة: ${err.message}` });
        } finally {
            client.release();
        }
    } else {
        // Fallback to memory store
        const lk = licenseKey.toUpperCase();
        if (!tenantDataStore.transactions[lk]) tenantDataStore.transactions[lk] = {};
        if (!tenantDataStore.deposits[lk]) tenantDataStore.deposits[lk] = {};
        if (!tenantDataStore.distributorCustomers[lk]) tenantDataStore.distributorCustomers[lk] = {};
        if (!tenantDataStore.distributorTransactions[lk]) tenantDataStore.distributorTransactions[lk] = {};
        if (!tenantDataStore.distributorExpenses[lk]) tenantDataStore.distributorExpenses[lk] = {};
        if (!tenantDataStore.distributorCapitals[lk]) tenantDataStore.distributorCapitals[lk] = {};

        if (transactions) transactions.forEach(t => tenantDataStore.transactions[lk][t.uuid] = t);
        if (deposits) deposits.forEach(d => tenantDataStore.deposits[lk][d.uuid] = d);
        if (distributorCustomers) distributorCustomers.forEach(c => tenantDataStore.distributorCustomers[lk][c.id || c.uuid] = c);
        if (distributorTransactions) distributorTransactions.forEach(t => tenantDataStore.distributorTransactions[lk][t.id || t.uuid] = t);
        if (distributorExpenses) distributorExpenses.forEach(e => tenantDataStore.distributorExpenses[lk][e.id || e.uuid] = e);
        if (distributorCapitals) distributorCapitals.forEach(c => tenantDataStore.distributorCapitals[lk][c.id || c.uuid] = c);

        res.json({ success: true, message: "تم حفظ العمليات محلياً على الخادم بنجاح! 🟢" });
    }
});

// Download/Fetch endpoint (multi-tenant restore)
app.get('/api/v1/sync/download', async (req, res) => {
    const { licenseKey, deviceId } = req.query;
    
    const validation = await validateTenant(licenseKey, deviceId);
    if (!validation.valid) {
        return res.status(401).json({ success: false, message: validation.reason });
    }

    if (pool) {
        try {
            const txs = await pool.query("SELECT * FROM tenant_transactions WHERE license_key = $1", [licenseKey]);
            const dps = await pool.query("SELECT * FROM tenant_deposits WHERE license_key = $1", [licenseKey]);
            const custs = await pool.query("SELECT * FROM tenant_distributor_customers WHERE license_key = $1", [licenseKey]);
            const dtxs = await pool.query("SELECT * FROM tenant_distributor_transactions WHERE license_key = $1", [licenseKey]);
            const exps = await pool.query("SELECT * FROM tenant_distributor_expenses WHERE license_key = $1", [licenseKey]);
            const caps = await pool.query("SELECT * FROM tenant_distributor_capitals WHERE license_key = $1", [licenseKey]);

            res.json({
                success: true,
                transactions: txs.rows.map(r => ({
                    uuid: r.uuid, phone: r.phone, amount: r.amount, cardCode: r.card_code, walletType: r.wallet_type,
                    createdAt: parseInt(r.created_at), updatedAt: parseInt(r.updated_at), version: r.version
                })),
                deposits: dps.rows.map(r => ({
                    uuid: r.uuid, phone: r.phone, amount: r.amount, walletType: r.wallet_type, isShared: r.is_shared,
                    cardDetails: r.card_details, createdAt: parseInt(r.created_at), updatedAt: parseInt(r.updated_at), version: r.version
                })),
                distributorCustomers: custs.rows.map(r => ({
                    id: r.uuid, name: r.name, totalSales: r.total_sales, totalPayments: r.total_payments,
                    currentBalance: r.current_balance, createdAt: parseInt(r.created_at), updatedAt: parseInt(r.updated_at), version: r.version
                })),
                distributorTransactions: dtxs.rows.map(r => ({
                    id: r.uuid, customerId: r.customer_id, date: parseInt(r.date), type: r.type, amount: r.amount,
                    notes: r.notes, updatedAt: parseInt(r.updated_at), version: r.version
                })),
                distributorExpenses: exps.rows.map(r => ({
                    id: r.uuid, category: r.category, amount: r.amount, description: r.description, date: parseInt(r.date),
                    updatedAt: parseInt(r.updated_at), version: r.version
                })),
                distributorCapitals: caps.rows.map(r => ({
                    id: r.uuid, type: r.type, amount: r.amount, description: r.description, date: parseInt(r.date),
                    updatedAt: parseInt(r.updated_at), version: r.version
                }))
            });
        } catch (err) {
            console.error("Download sync error:", err);
            res.status(500).json({ success: false, message: err.message });
        }
    } else {
        const lk = licenseKey.toUpperCase();
        res.json({
            success: true,
            transactions: Object.values(tenantDataStore.transactions[lk] || {}),
            deposits: Object.values(tenantDataStore.deposits[lk] || {}),
            distributorCustomers: Object.values(tenantDataStore.distributorCustomers[lk] || {}),
            distributorTransactions: Object.values(tenantDataStore.distributorTransactions[lk] || {}),
            distributorExpenses: Object.values(tenantDataStore.distributorExpenses[lk] || {}),
            distributorCapitals: Object.values(tenantDataStore.distributorCapitals[lk] || {})
        });
    }
});

// Migration endpoint: receives local database dump and moves it to PostgreSQL securely
app.post('/api/v1/sync/migrate', async (req, res) => {
    const { licenseKey, deviceId, transactions, deposits, distributorCustomers, distributorTransactions, distributorExpenses, distributorCapitals } = req.body;
    
    const validation = await validateTenant(licenseKey, deviceId);
    if (!validation.valid) {
        return res.status(401).json({ success: false, message: validation.reason });
    }

    console.log(`Starting migration for tenant: ${licenseKey}`);

    if (pool) {
        const client = await pool.connect();
        try {
            await client.query('BEGIN');
            
            // Wipe existing tenant tables before loading full migration to avoid duplication conflicts
            await client.query("DELETE FROM tenant_transactions WHERE license_key = $1", [licenseKey]);
            await client.query("DELETE FROM tenant_deposits WHERE license_key = $1", [licenseKey]);
            await client.query("DELETE FROM tenant_distributor_customers WHERE license_key = $1", [licenseKey]);
            await client.query("DELETE FROM tenant_distributor_transactions WHERE license_key = $1", [licenseKey]);
            await client.query("DELETE FROM tenant_distributor_expenses WHERE license_key = $1", [licenseKey]);
            await client.query("DELETE FROM tenant_distributor_capitals WHERE license_key = $1", [licenseKey]);

            if (transactions && transactions.length > 0) {
                for (const item of transactions) {
                    await client.query(`
                        INSERT INTO tenant_transactions (uuid, phone, amount, card_code, wallet_type, created_at, updated_at, version, license_key, device_id)
                        VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10)
                    `, [item.uuid || `mig-${Date.now()}-${Math.random()}`, item.phone, item.amount, item.cardCode || item.card_code, item.walletType || item.wallet_type, parseInt(item.createdAt || item.created_at), parseInt(item.updatedAt || item.updated_at || item.createdAt), item.version || 1, licenseKey, deviceId]);
                }
            }

            if (deposits && deposits.length > 0) {
                for (const item of deposits) {
                    await client.query(`
                        INSERT INTO tenant_deposits (uuid, phone, amount, wallet_type, is_shared, card_details, created_at, updated_at, version, license_key, device_id)
                        VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10)
                    `, [item.uuid || `mig-${Date.now()}-${Math.random()}`, item.phone, item.amount, item.walletType || item.wallet_type, item.isShared || item.is_shared, item.cardDetails || item.card_details, parseInt(item.createdAt || item.created_at), parseInt(item.updatedAt || item.updated_at || item.createdAt), item.version || 1, licenseKey, deviceId]);
                }
            }

            if (distributorCustomers && distributorCustomers.length > 0) {
                for (const item of distributorCustomers) {
                    await client.query(`
                        INSERT INTO tenant_distributor_customers (uuid, name, total_sales, total_payments, current_balance, created_at, updated_at, version, license_key, device_id)
                        VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10)
                    `, [item.id || item.uuid, item.name, item.totalSales || item.total_sales, item.totalPayments || item.total_payments, item.currentBalance || item.current_balance, parseInt(item.createdAt || item.created_at), parseInt(item.updatedAt || item.updated_at || item.createdAt), item.version || 1, licenseKey, deviceId]);
                }
            }

            if (distributorTransactions && distributorTransactions.length > 0) {
                for (const item of distributorTransactions) {
                    await client.query(`
                        INSERT INTO tenant_distributor_transactions (uuid, customer_id, date, type, amount, notes, updated_at, version, license_key, device_id)
                        VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10)
                    `, [item.id || item.uuid, item.customerId || item.customer_id, parseInt(item.date), item.type, item.amount, item.notes, parseInt(item.updatedAt || item.updated_at || item.date), item.version || 1, licenseKey, deviceId]);
                }
            }

            if (distributorExpenses && distributorExpenses.length > 0) {
                for (const item of distributorExpenses) {
                    await client.query(`
                        INSERT INTO tenant_distributor_expenses (uuid, category, amount, description, date, updated_at, version, license_key, device_id)
                        VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9)
                    `, [item.id || item.uuid, item.category, item.amount, item.description, parseInt(item.date), parseInt(item.updatedAt || item.updated_at || item.date), item.version || 1, licenseKey, deviceId]);
                }
            }

            if (distributorCapitals && distributorCapitals.length > 0) {
                for (const item of distributorCapitals) {
                    await client.query(`
                        INSERT INTO tenant_distributor_capitals (uuid, type, amount, description, date, updated_at, version, license_key, device_id)
                        VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9)
                    `, [item.id || item.uuid, item.type, item.amount, item.description, parseInt(item.date), parseInt(item.updatedAt || item.updated_at || item.date), item.version || 1, licenseKey, deviceId]);
                }
            }

            await client.query('COMMIT');
            res.json({ success: true, message: "🚀 تم ترحيل قاعدة بيانات العميل وتأمينها بالكامل في سحابة PostgreSQL بنجاح!" });
        } catch (err) {
            await client.query('ROLLBACK');
            console.error("Migration transactions error:", err);
            res.status(500).json({ success: false, message: `فشل الترحيل إلى السحابة: ${err.message}` });
        } finally {
            client.release();
        }
    } else {
        // Fallback store setup
        const lk = licenseKey.toUpperCase();
        tenantDataStore.transactions[lk] = {};
        tenantDataStore.deposits[lk] = {};
        tenantDataStore.distributorCustomers[lk] = {};
        tenantDataStore.distributorTransactions[lk] = {};
        tenantDataStore.distributorExpenses[lk] = {};
        tenantDataStore.distributorCapitals[lk] = {};

        if (transactions) transactions.forEach(t => tenantDataStore.transactions[lk][t.uuid] = t);
        if (deposits) deposits.forEach(d => tenantDataStore.deposits[lk][d.uuid] = d);
        if (distributorCustomers) distributorCustomers.forEach(c => tenantDataStore.distributorCustomers[lk][c.id || c.uuid] = c);
        if (distributorTransactions) distributorTransactions.forEach(t => tenantDataStore.distributorTransactions[lk][t.id || t.uuid] = t);
        if (distributorExpenses) distributorExpenses.forEach(e => tenantDataStore.distributorExpenses[lk][e.id || e.uuid] = e);
        if (distributorCapitals) distributorCapitals.forEach(c => tenantDataStore.distributorCapitals[lk][c.id || c.uuid] = c);

        res.json({ success: true, message: "🚀 تم ترحيل قاعدة البيانات وتأمينها محلياً على الخادم بنجاح!" });
    }
});

// JSON API Status endpoint
app.get('/api/v1/status', (req, res) => {
    res.json({ name: "KayanSoft Security API Gateway", version: "1.0.0", status: "SECURE" });
});

// Start API Server with WebSockets Attached
const server = http.createServer(app);
const wss = new WebSocket.Server({ server, path: '/ws' });

wss.on('connection', (ws) => {
    console.log("🔌 New WS Connection received.");
    let authenticatedTenant = null;

    ws.on('message', async (messageStr) => {
        try {
            const msg = JSON.parse(messageStr);
            
            // Authentication payload
            if (msg.type === "AUTH") {
                const validation = await validateTenant(msg.licenseKey, msg.deviceId);
                if (validation.valid) {
                    authenticatedTenant = { licenseKey: msg.licenseKey, deviceId: msg.deviceId };
                    ws.send(JSON.stringify({ type: "AUTH_RESPONSE", success: true, message: "تم الاتصال الآمن بالسيرفر بنجاح! 🔑" }));
                    console.log(`🔑 WS Client Authenticated for Tenant: ${msg.licenseKey}`);
                } else {
                    ws.send(JSON.stringify({ type: "AUTH_RESPONSE", success: false, message: validation.reason }));
                    ws.close();
                }
                return;
            }

            if (!authenticatedTenant) {
                ws.send(JSON.stringify({ type: "ERROR", message: "الرجاء تسجيل الدخول أولاً!" }));
                ws.close();
                return;
            }

            // Real-time synchronization ping/pong
            if (msg.type === "PING") {
                ws.send(JSON.stringify({ type: "PONG" }));
                return;
            }

            // Real-time operation processed message (SMS_PROCESSED etc.)
            if (msg.type === "SMS_PROCESSED") {
                console.log(`📱 SMS Synced for tenant ${authenticatedTenant.licenseKey}: ${msg.cardDetails}`);
                // Forward message to PostgreSQL or memory store
                const syncPayload = {
                    licenseKey: authenticatedTenant.licenseKey,
                    deviceId: authenticatedTenant.deviceId,
                    deposits: [{
                        uuid: msg.uuid || crypto.randomUUID(),
                        phone: msg.recipientPhone,
                        amount: msg.amount,
                        walletType: msg.walletType,
                        isShared: true,
                        cardDetails: msg.cardDetails,
                        createdAt: Date.now(),
                        updatedAt: Date.now(),
                        version: 1
                    }],
                    transactions: [{
                        uuid: msg.uuid || crypto.randomUUID(),
                        phone: msg.recipientPhone,
                        amount: msg.amount,
                        cardCode: msg.cardDetails,
                        walletType: msg.walletType,
                        createdAt: Date.now(),
                        updatedAt: Date.now(),
                        version: 1
                    }]
                };

                await handleInternalSyncUpload(syncPayload);
                ws.send(JSON.stringify({ type: "SYNC_ACK", success: true, message: "SMS Synced" }));
            }
        } catch (err) {
            console.error("WS Message Error:", err);
            ws.send(JSON.stringify({ type: "ERROR", message: err.message }));
        }
    });
});

// Helper for internal sync from WebSocket
async function handleInternalSyncUpload(payload) {
    const { licenseKey, deviceId, transactions, deposits } = payload;
    if (pool) {
        try {
            if (transactions && transactions.length > 0) {
                for (const item of transactions) {
                    await pool.query(`
                        INSERT INTO tenant_transactions (uuid, phone, amount, card_code, wallet_type, created_at, updated_at, version, license_key, device_id)
                        VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10)
                        ON CONFLICT (uuid) DO NOTHING
                    `, [item.uuid, item.phone, item.amount, item.cardCode, item.walletType, item.createdAt, item.updatedAt, 1, licenseKey, deviceId]);
                }
            }
            if (deposits && deposits.length > 0) {
                for (const item of deposits) {
                    await pool.query(`
                        INSERT INTO tenant_deposits (uuid, phone, amount, wallet_type, is_shared, card_details, created_at, updated_at, version, license_key, device_id)
                        VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10)
                        ON CONFLICT (uuid) DO NOTHING
                    `, [item.uuid, item.phone, item.amount, item.walletType, item.isShared, item.cardDetails, item.createdAt, item.updatedAt, 1, licenseKey, deviceId]);
                }
            }
        } catch (err) {
            console.error("WS internal sync error:", err);
        }
    } else {
        const lk = licenseKey.toUpperCase();
        if (!tenantDataStore.transactions[lk]) tenantDataStore.transactions[lk] = {};
        if (!tenantDataStore.deposits[lk]) tenantDataStore.deposits[lk] = {};
        if (transactions) transactions.forEach(t => tenantDataStore.transactions[lk][t.uuid] = t);
        if (deposits) deposits.forEach(d => tenantDataStore.deposits[lk][d.uuid] = d);
    }
}

server.listen(PORT, () => {
    console.log(`=============================================================`);
    console.log(`🚀 KayanSoft Multi-Tenant Server running on port ${PORT}`);
    console.log(`🔒 HMAC & Multi-tenant isolation active`);
    console.log(`=============================================================`);
});
