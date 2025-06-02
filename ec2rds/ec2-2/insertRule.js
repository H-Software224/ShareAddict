const express = require('express');
const router = express.Router();
const mysql = require('mysql2/promise');
require('dotenv').config();
const crypto = require('crypto');
const API_KEY = process.env.AYRSHARE_API_KEY;

router.post('/app-rule', async (req, res) => {
    try {
        const {
            packageName, startTimeMillis, endTimeMillis, appName,
            startHour, startMinute, endHour, endMinute, rules
        } = req.body;

        const conn = await mysql.createConnection({
            host: process.env.DB_HOST,
            user: process.env.DB_USER,
            password: process.env.DB_PASSWORD,
            database: process.env.DB_NAME,
        });

        await conn.execute(
            `INSERT INTO usage_rules 
            (id, start_year, start_month, start_day, start_hour, start_minute, start_second,
            end_year, end_month, end_day, end_hour, end_minute, end_second,
            app_name, rule, violation, ayrshare_id) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)`,
            [
                crypto.randomUUID(),
                2025, 1, 1, startHour, startMinute, 0,
                2025, 6, 22, endHour, endMinute, 0,
                appName, rules, true, 'BBD76341-EAD84EFB-A162CA36-8FDE22B3'
            ]
        );

        await conn.end();
        res.status(200).json({message: '✅ 규칙 저장 성공'});
    } catch (error) {
        console.error("❌ DB 저장 실패:", error);
        res.status(500).json({error: 'DB 저장 실패'});
    }
});

module.exports = router;