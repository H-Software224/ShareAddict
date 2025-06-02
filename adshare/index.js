const express = require("express");
const router = express.Router();
require("dotenv").config();
const mysql = require("mysql2/promise");
const fetch = (...args) => import("node-fetch").then(({ default: fetch }) => fetch(...args));

const DB_CONFIG = {
  host: process.env.DB_HOST,
  user: process.env.DB_USER,
  password: process.env.DB_PASSWORD,
  database: process.env.DB_NAME,
};

const API_KEY = process.env.AYRSHARE_API_KEY;

router.post(
  "/:app_name/:start_year/:start_month/:start_day/:start_hour/:start_minute/:start_second/:end_year/:end_month/:end_day/:end_hour/:end_minute/:end_second/share",
  async (req, res) => {
    const {
      app_name,
      start_year, start_month, start_day,
      start_hour, start_minute, start_second,
      end_year, end_month, end_day,
      end_hour, end_minute, end_second
    } = req.params;

    try {
      const conn = await mysql.createConnection(DB_CONFIG);

      // 해당 조건의 usage_rule 찾기
      const [rows] = await conn.execute(
        `SELECT * FROM usage_rules 
         WHERE app_name = ? AND
               start_year = ? AND start_month = ? AND start_day = ? AND
               start_hour = ? AND start_minute = ? AND start_second = ? AND
               end_year = ? AND end_month = ? AND end_day = ? AND
               end_hour = ? AND end_minute = ? AND end_second = ?`,
        [
          app_name,
          start_year, start_month, start_day,
          start_hour, start_minute, start_second,
          end_year, end_month, end_day,
          end_hour, end_minute, end_second
        ]
      );

      if (rows.length === 0) {
        await conn.end();
        return res.status(404).json({ error: "No matching usage rule found." });
      }

      const rule = rows[0];

      const postText = `앱: ${rule.app_name} 시간대 시작: ${rule.start_hour}:${rule.start_minute} 종료: ${rule.end_hour}:${rule.end_minute} 규칙: ${rule.rule} 위반 유무: ${rule.violation ? "위반" : "정상"}`;
      const currentTimeUTC = new Date().toISOString().split(".")[0] + "Z"; // ISO8601 형식

      const body = {
        post: postText,
        platforms: ["facebook", "instagram"],
        mediaUrls: [
          "https://search.pstatic.net/common/?src=http%3A%2F%2Fblogfiles.naver.net%2FMjAyNTA1MDhfMSAg%2FMDAxNzQ2Njg2NDI5ODQy.yk3R0vuDGWZNttHBHTq0WV4PKDokkxMn1FBJ9cf3CyUg.n06O2_b720YxEvlSzjqSuseUP3r0Na_CIzLWVnjRoGkg.PNG%2F%25C1%25D6%25C0%25C7%25BB%25E7%25C7%25D7.png&type=a340",
        ],
        scheduleDate: currentTimeUTC
      };

      const response = await fetch("https://api.ayrshare.com/api/post", {
        method: "POST",
        headers: {
          Authorization: `Bearer ${API_KEY}`,
          "Content-Type": "application/json"
        },
        body: JSON.stringify(body)
      });

      const result = await response.json();

      // API 결과에 따라 DB에 ayrshare_id 저장
      if (result.id) {
        console.log("id 저장 완료!");
        await conn.execute(
          "UPDATE usage_rules SET ayrshare_id = ? WHERE id = ?",
          [result.id, rule.id]
        );
      }

      await conn.end();
      return res.json(result);
    } catch (err) {
      console.error("❌ 오류:", err);
      return res.status(500).json({ error: "Server error" });
    }
  }
);

module.exports = router;
