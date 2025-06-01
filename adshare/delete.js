require('dotenv').config();
const mysql = require('mysql2/promise');
const fetch = (...args) => import('node-fetch').then(({default: fetch}) => fetch(...args));

const API_KEY = process.env.AYRSHARE_API_KEY;
const RULE_ID = process.env.RULE_ID;

(async () => {
  try {
    const conn = await mysql.createConnection({
      host: process.env.DB_HOST,
      user: process.env.DB_USER,
      password: process.env.DB_PASSWORD,
      database: process.env.DB_NAME,
    });

    const [rows] = await conn.execute('SELECT ayrshare_id FROM usage_rules WHERE id = ?', [RULE_ID]);
    if (!rows.length || !rows[0].ayrshare_id) {
      console.log("❌ ayrshare_id가 없습니다.");
      return;
    }

    const POST_ID = rows[0].ayrshare_id;

    const response = await fetch("https://api.ayrshare.com/api/post", {
      method: "DELETE",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${API_KEY}`
      },
      body: JSON.stringify({ id: POST_ID }),
    });

    const result = await response.json();
    console.log("✅ 삭제 응답:", result);
    await conn.end();
  } catch (err) {
    console.error("❌ 오류:", err);
  }
})();
