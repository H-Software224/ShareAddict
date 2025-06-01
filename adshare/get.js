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

    const response = await fetch(`https://api.ayrshare.com/api/post/${POST_ID}`, {
      method: "GET",
      headers: {
        "Authorization": `Bearer ${API_KEY}`
      }
    });

    const result = await response.json();
    console.log("✅ 게시물 정보:", result);
    await conn.end();
  } catch (err) {
    console.error("❌ 오류:", err);
  }
})();
