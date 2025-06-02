const express = require("express");
const cors = require("cors");
const app = express();
require("dotenv").config();

const insertRuleRouter = require("./insertRule");
const sendViolations = require("./sendViolationsToEC2_1");

app.use(cors());
app.use(express.json());
app.use("/rules", insertRuleRouter);

(async () => {
    try {
        const results = await sendViolations();
        console.log("✅ EC2-1로 전송 완료:", results);
    } catch (err) {
        console.error("❌ 서버 시작 시 전송 실패:", err);
    }
})();

const PORT = process.env.PORT || 3000;
app.listen(PORT, "0.0.0.0", () => {
    console.log(`✅ EC2-2 서버 실행 중: http://43.203.174.53:${PORT}`);
});