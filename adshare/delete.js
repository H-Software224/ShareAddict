require('dotenv').config();

const fetch = (...args) => import('node-fetch').then(({default: fetch}) => fetch(...args));

const API_KEY = process.env.AYRSHARE_API_KEY;
const id = process.env.POSTID;

fetch("https://api.ayrshare.com/api/post", {
      method: "DELETE",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${API_KEY}`
      },
      body: JSON.stringify({ id }),
    })
      .then((res) => res.json())
      .then((json) => console.log(json))
      .catch(console.error);