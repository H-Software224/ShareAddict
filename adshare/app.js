const express = require("express");
const app = express();
require("dotenv").config();

app.use(express.json());

app.use("/", require("./index"));   // POST /rules/:id/share
app.use("/", require("./get"));     // GET /rules
app.use("/", require("./delete"));  // DELETE /rules/:id

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => console.log(`Server running on port ${PORT}`));
