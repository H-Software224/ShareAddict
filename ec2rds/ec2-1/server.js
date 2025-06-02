require('dotenv').config()
const express = require('express');
const app = express();
const port = process.env.PORT || 3000;
const public_address = process.env.PUBLIC_ADDRESS;

app.use(express.json());
app.use("/rules", require("./index"));
app.use("/rules", require("./get"));

app.listen(port, "0.0.0.0", () => {
    console.log(`✅ Server running at http://${public_address}:${port}/post_violations`);
});

app.get("/", (req, res) => {
    res.send("👍 API 서버 정상 작동 중입니다.");
});