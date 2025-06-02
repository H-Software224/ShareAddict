const express = require('express');
const router = express.Router();
const mysql = require('mysql2/promise');
require('dotenv').config();

router.get('/violations', async (req, res) => {
    try {
        const conn = await mysql.createConnection({
            host: process.env.DB_HOST,
            user: process.env.DB_USER,
            password: process.env.DB_PASSWORD,
            database: process.env.DB_NAME
        });

        const [rows] = await conn.execute(
            'SELECT * FROM usage_rules WHERE violation = true AND ayrshare_id="BBD76341-EAD84EFB-A162CA36-8FDE22B3"'
        );

        await conn.end();
        res.json(rows);
    } catch (err) {
        console.error("❌ 오류:", err);
        res.status(500).json({error: 'DB 조회 오류'});
    }
});

module.exports = router;