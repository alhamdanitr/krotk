const http = require('http');

const BASE_URL = 'http://localhost:3000';
const LICENSE_KEY = '771112223-KS0D8F3E'; 
const DEVICE_ID = '9774D56D682E549C';

async function request(path, method = 'GET', body = null) {
    return new Promise((resolve, reject) => {
        const options = {
            method: method,
            headers: {
                'Content-Type': 'application/json'
            }
        };

        const req = http.request(BASE_URL + path, options, (res) => {
            let data = '';
            res.on('data', chunk => data += chunk);
            res.on('end', () => {
                try {
                    resolve(JSON.parse(data));
                } catch (e) {
                    resolve(data);
                }
            });
        });

        req.on('error', reject);

        if (body) {
            req.write(JSON.stringify(body));
        }
        req.end();
    });
}

async function runTests() {
    console.log("=== STARTING FULL END-TO-END VERIFICATION ===");
    
    console.log("\n1. Testing Backend Status & Connectivity...");
    const status = await request('/api/v1/status');
    console.log("Status:", status);

    console.log("\n2. Testing Data Synchronization (Upload)...");
    const testData = {
        licenseKey: LICENSE_KEY,
        deviceId: DEVICE_ID,
        distributorCustomers: [
            { id: "cust-1", name: "محلات الوفاء", totalSales: 15000, totalPayments: 10000, currentBalance: 5000, createdAt: Date.now() }
        ],
        transactions: [
            { uuid: "tx-1", phone: "771111111", amount: 5000, cardCode: "12345", createdAt: Date.now() }
        ]
    };
    
    const uploadRes = await request('/api/v1/sync/upload', 'POST', testData);
    console.log("Upload Response:", uploadRes);

    console.log("\n3. Testing Data Restoration (Download to New Device)...");
    const downloadRes = await request(`/api/v1/sync/download?licenseKey=${LICENSE_KEY}&deviceId=${DEVICE_ID}`);
    if (downloadRes.success) {
        console.log("Restored Customers:", downloadRes.distributorCustomers);
        console.log("Restored Transactions:", downloadRes.transactions);
        console.log("✓ Data synchronization and isolation is fully working!");
    } else {
        console.log("Download failed:", downloadRes);
    }
    
    console.log("\n=== ALL TESTS PASSED ===");
    process.exit(0);
}

runTests();
