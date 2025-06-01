require('dotenv').config();
const mysql = require('mysql2/promise');
const fetch = (...args) => import('node-fetch').then(({default: fetch}) => fetch(...args));

const API_KEY = process.env.AYRSHARE_API_KEY;
const DB_CONFIG = {
  host: process.env.DB_HOST,
  user: process.env.DB_USER,
  password: process.env.DB_PASSWORD,
  database: process.env.DB_NAME,
};
const RULE_ID = process.env.RULE_ID;

(async () => {
  try {
    const conn = await mysql.createConnection(DB_CONFIG);
    const [rows] = await conn.execute('SELECT * FROM usage_rules WHERE id = ?', [RULE_ID]);

    if (rows.length === 0) {
      console.log("❌ 해당 RULE_ID를 찾을 수 없습니다.");
      process.exit(1);
    }

    const rule = rows[0];
    const postMessage = `앱: ${rule.app_name} 시간대 시작: ${rule.start_hour}:${rule.start_minute} 종료: ${rule.end_hour}:${rule.end_minute} 규칙: ${rule.rule} 위반 유무: ${rule.violation ? "위반" : "정상"}`;
    const scheduleDate = new Date().toISOString().split(".")[0] + "Z";

    const response = await fetch("https://api.ayrshare.com/api/post", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${API_KEY}`
      },
      body: JSON.stringify({
        post: postMessage,
        platforms: ["facebook", "instagram"],
        mediaUrls: [
          "https://search.pstatic.net/common/?src=http%3A%2F%2Fblogfiles.naver.net%2FMjAyNTA1MDhfMSAg%2FMDAxNzQ2Njg2NDI5ODQy.yk3R0vuDGWZNttHBHTq0WV4PKDokkxMn1FBJ9cf3CyUg.n06O2_b720YxEvlSzjqSuseUP3r0Na_CIzLWVnjRoGkg.PNG%2F%25C1%25D6%25C0%25C7%25BB%25E7%25C7%25D7.png&type=a340"
        ],
        scheduleDate: scheduleDate,
        instagramOptions: { stories: true }
      }),
    });

    const result = await response.json();
    console.log("✅ Ayrshare 응답:", result);

    if (result?.id) {
      await conn.execute('UPDATE usage_rules SET ayrshare_id = ? WHERE id = ?', [result.id, RULE_ID]);
      console.log("✅ RDS에 ayrshare_id 업데이트 완료:", result.id);
    } else {
      console.log("⚠️ Ayrshare POST 응답에 id가 없습니다.");
    }

    await conn.end();
  } catch (err) {
    console.error("❌ 오류:", err);
  }
})();
