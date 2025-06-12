const express = require("express");
const cors = require("cors");
const app = express();
require("dotenv").config();

const insertRuleRouter = require("./insertRule");
const sendViolations = require("./sendViolationsToEC2_1");
const ec2_2_ip = process.env.AWS_PUBLIC_IP_ADDRESS;

app.use(cors());
app.use(express.json());
app.use("/rules", insertRuleRouter);

setInterval(async () => {
    try {
        const results = await sendViolations();
        console.log("✅ EC2-1로 전송 완료:", results);
    } catch (err) {
        console.error("❌ 서버 시작 시 전송 실패:", err);
    }
}, 30 * 1000);

const PORT = process.env.PORT || 3000;
app.listen(PORT, "0.0.0.0", () => {
    console.log(`✅ EC2-2 서버 실행 중: http://${ec2_2_ip}:${PORT}`);
});