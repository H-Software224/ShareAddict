const express = require('express');
const router = express.Router();
const mysql = require('mysql2/promise');
const fetch = (...args) => import('node-fetch').then(({default: fetch}) => fetch(...args));
require('dotenv').config();

router.post('/post_violations', async (req, res) => {
    try {
        const conn = await mysql.createConnection({
            host: process.env.DB_HOST,
            user: process.env.DB_USER,
            password: process.env.DB_PASSWORD,
            database: process.env.DB_NAME,
        });

        const [rules] = await conn.execute(
            'SELECT * FROM usage_rules WHERE violation = true AND ayrshare_id = "BBD76341-EAD84EFB-A162CA36-8FDE22B3"'
        );

        const postedResults = [];

        for (const rule of rules) {
            const postMessage = `앱: ${rule.app_name} 시간대 시작: ${rule.start_hour}:${rule.start_minute} 종료: ${rule.end_hour}:${rule.end_minute} 규칙: ${rule.rule} 위반 유무: ${rule.violation ? "위반" : "정상"}`;
            const scheduleDate = new Date().toISOString().split(".")[0] + "Z";
            const response = await.fetch("https://api.ayrshare.com/api/post", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    "Authorization": `Bearer ${process.env.AYRSHARE_API_KEY}`
                },
                body: JSON.stringify({
                    post: postMessage,
                    platforms: ["facebook", "instagram"],
                    mediaUrls: ["https://search.pstatic.net/common/?src=http%3A%2F%2Fblogfiles.naver.net%2FMjAyNTA1MDhfMSAg%2FMDAxNzQ2Njg2NDI5ODQy.yk3R0vuDGWZNttHBHTq0WV4PKDokkxMn1FBJ9cf3CyUg.n06O2_b720YxEvlSzjqSuseUP3r0Na_CIzLWVnjRoGkg.PNG%2F%25C1%25D6%25C0%25C7%25BB%25E7%25C7%25D7.png&type=a340"],
                    scheduleDate: scheduleDate
                }),
            });
            
            const result = await response.json();
            console.log("📤 Ayrshare 응답:", result);

            if (result.id) {
                await conn.execute(
                    'UPDATE usage_rules SET ayrshare_id = ? WHERE ayrshare_id = "BBD76341-EAD84EFB-A162CA36-8FDE22B3"',
                    [result.id]
                );
                postedResults.push({id: rule.id, status: 'posted', ayrshare_id: result.id});
            } else {
                postedResults.push({id: rule.id, status: 'failed', error: result.errors});
            }
        }

        await conn.end();
        res.json({results: postedResults});
    } catch (err) {
        console.error("❌ 오류:", err);
        res.status(500).json({error: '포스트 중 오류 발생'});
    }
});

module.exports = router;