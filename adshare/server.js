require('dotenv').config(); // .env 불러오기
const express = require('express');
const app = express();
const port = process.env.PORT || 3000;
const public_address = process.env.PUBLIC_ADDRESS

// POST, GET, DELETE API 파일 연결
app.use(express.json());
app.use("/rules", require("./index"));   // POST /rules/:id/share
app.use("/rules", require("./get"));     // GET /rules/:id
app.use("/rules", require("./delete"));  // DELETE /rules/:id

app.listen(port, () => {
  console.log(`✅ Server running at http://${public_address}:${port}`);
});

app.get("/", (req, res) => {
  res.send("👍 API 서버 정상 작동 중입니다.");
});