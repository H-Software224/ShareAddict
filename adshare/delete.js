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

router.delete("/:ayrshare_id", async (req, res) => {
  const { ayrshare_id } = req.params;

  try {
    const conn = await mysql.createConnection(DB_CONFIG);

    const [rows] = await conn.execute(
      "SELECT * FROM usage_rules WHERE ayrshare_id = ?",
      [ayrshare_id]
    );

    if (rows.length === 0) {
      await conn.end();
      return res.status(404).json({ error: "해당 ayrshare_id를 가진 데이터가 없습니다." });
    }

    // ✅ Ayrshare는 이 방식으로 삭제함
    const response = await fetch("https://api.ayrshare.com/api/post", {
      method: "DELETE",
      headers: {
        "Authorization": `Bearer ${API_KEY}`,
        "Content-Type": "application/json"
      },
      body: JSON.stringify({ id: ayrshare_id })
    });

    const result = await response.json();
    console.log("🗑️ Ayrshare 응답:", result);

    if (result.status === "success") {
      await conn.execute(
        "UPDATE usage_rules SET ayrshare_id = NULL WHERE ayrshare_id = ?",
        [ayrshare_id]
      );
    }

    await conn.end();
    return res.json(result);
  } catch (err) {
    console.error("❌ 삭제 중 오류:", err);
    return res.status(500).json({ error: "삭제 처리 중 서버 오류" });
  }
});


module.exports = router;
