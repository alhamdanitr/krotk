const { Pool } = require('pg');

const pool = new Pool({
  connectionString: 'postgresql://postgres:postgres@localhost:5432/postgres'
});

async function run() {
  try {
    await pool.query('SELECT 1');
    console.log("DB connection OK");
  } catch (e) {
    console.log("DB Error:", e);
  }
  process.exit();
}
run();
