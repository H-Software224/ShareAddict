const mysql = require("mysql2/promise");
const fetch = require("node-fetch");
require("dotenv").config();

const DB_CONFIG = {
    host: process.env.DB_HOST,
    user: process.env.DB_USER,
    password: process.env.DB_PASSWORD,
    database: process.env.DB_NAME,
};
const public_EC_1_IP = process.env.EC2_1_IP;
const EC2_1_ENDPOINT = "http://43.201.70.207:3000/rules/post_violations";

async function sendViolations() {
    const conn = await mysql.createConnection(DB_CONFIG);

    try {
        const [rows] = await conn.execute(
            "SELECT * FROM usage_rules WHERE violation = 1 AND ayrshare_id = 'BBD76341-EAD84EFB-A162CA36-8FDE22B3'"
        );

        if (rows.length == 0) {
            console.log("✅ 전송할 위반 항목이 없습니다.");
            return;
        }

        const results = [];

        for (const row of rows) {
            const payload = {
                id: row.id,
                app_name: row.app_name,
                start_hour: row.start_hour,
                start_minute: row.start_minute,
                end_hour: row.end_hour,
                end_minute: row.end_minute,
                rule: row.rule,
                violation: row.violation,
            };

            console.log("📤 전송 요청:", payload);

            const response = await fetch(EC2_1_ENDPOINT, {
                method: "POST",
                headers: {"Content-Type": "application/json"},
                body: JSON.stringify(payload),
            });

            let result;
            try {
                result = await response.json();
            } catch (jsonErr) {
                console.error("❌ JSON 파싱 실패:", jsonErr);
                continue;
            }

            console.log("✅ EC2-1 응답:", result);

            if (!result?.id || !row?.id) {
                console.warn("⚠️ 업데이트 스킵됨: result.id 또는 row.id가 없음", {result, row});
                continue;
            }

            try {
                await conn.execute(
                    "UPDATE usage_rules SET ayshare_id = ? WHERE id = ?",
                    [result.id, "BBD76341-EAD84EFB-A162CA36-8FDE22B3"]
                );
                console.log(`✅ DB 업데이트 완료: id=${row.id}, ayrshare_id=${result.id}`);
            } catch (updateErr) {
                console.error("❌ DB 업데이트 실패:", updateErr);
            }

            results.push({input: row.id, result});
        }

        console.log("🎉 전체 전송 결과:", results);
    } catch (err) {
        console.error("❌ 전송 오류:", err);
    } finally {
        await conn.end();
    }
}

module.exports = sendViolations;