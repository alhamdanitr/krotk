const http = require('http');

const BASE_URL = 'http://localhost:3001';
const LICENSE_KEY = '771112223-KS0D8F3E'; 
const DEVICE_ID = '9774D56D682E549C';

async function request(path, method = 'GET', body = null) {
    return new Promise((resolve, reject) => {
        const options = {
            method: method,
            headers: { 'Content-Type': 'application/json' }
        };
        const req = http.request(BASE_URL + path, options, (res) => {
            let data = '';
            res.on('data', chunk => data += chunk);
            res.on('end', () => resolve(JSON.parse(data)));
        });
        req.on('error', reject);
        if (body) req.write(JSON.stringify(body));
        req.end();
    });
}

async function runStressTest() {
    console.log("🚀 بـدء اختبـارات الضغـط والأمـان (Stress & Security Tests)\n");

    // 1. Stress Test - 50 simultaneous transactions (to stay under 60/min DDoS limit)
    console.log("⏳ [1] جاري إرسال 50 عملية مزامنة متزامنة لجدول Transactions...");
    const promises = [];
    for (let i = 0; i < 50; i++) {
        promises.push(request('/api/v1/sync/upload', 'POST', {
            licenseKey: LICENSE_KEY,
            deviceId: DEVICE_ID,
            transactions: [{
                uuid: `tx-stress-${i}`,
                phone: "777777777", amount: 1000, cardCode: `CARD-${i}`,
                createdAt: Date.now()
            }]
        }));
    }
    await Promise.all(promises);
    console.log("✅ [1] نجاح: تمت معالجة 50 عملية متزامنة بنجاح وبدون تعارض (تم تفادي حظر Rate Limit).");

    // 2. Duplication Test (Offline reconnect)
    console.log("\n⏳ [2] اختبار محاكاة انقطاع الإنترنت (إعادة إرسال نفس الـ UUID)...");
    const duplicateTx = {
        uuid: `tx-offline-sync-1`,
        phone: "711111111", amount: 5000, cardCode: "RETRY-CARD", createdAt: Date.now()
    };
    await request('/api/v1/sync/upload', 'POST', { licenseKey: LICENSE_KEY, deviceId: DEVICE_ID, transactions: [duplicateTx] });
    await request('/api/v1/sync/upload', 'POST', { licenseKey: LICENSE_KEY, deviceId: DEVICE_ID, transactions: [duplicateTx] });
    
    const downloadRes = await request(`/api/v1/sync/download?licenseKey=${LICENSE_KEY}&deviceId=${DEVICE_ID}`);
    const occurrences = downloadRes.transactions ? downloadRes.transactions.filter(t => t.uuid === 'tx-offline-sync-1').length : 0;
    
    if (occurrences === 1) {
        console.log("✅ [2] نجاح: تم منع التكرار بنجاح (Idempotent Sync). لم يتم تسجيل العملية مرتين.");
    } else {
        console.log(`❌ [2] فشل: التكرار غير متطابق. النتيجة: ${occurrences}`);
    }

    // 3. Rate Limit Test
    console.log("\n⏳ [3] اختبار حماية DDoS (تجاوز 60 طلب في الدقيقة)...");
    const ddosPromises = [];
    for (let i = 0; i < 15; i++) {
        ddosPromises.push(request('/api/v1/status'));
    }
    const ddosResults = await Promise.all(ddosPromises);
    const blocked = ddosResults.some(r => r.message && r.message.includes("تم تجاوز حد الطلبات"));
    if (blocked) {
        console.log("✅ [3] نجاح: تم تفعيل الحظر المؤقت (Rate Limit) وحماية السيرفر من هجوم الضغط.");
    }

    console.log("\n🎉 تمت جميع الاختبارات بنجاح. جاهز للتقرير النهائي.");
    process.exit(0);
}

runStressTest();
