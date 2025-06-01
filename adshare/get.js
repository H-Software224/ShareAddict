require('dotenv').config();

const fetch = (...args) => import('node-fetch').then(({default: fetch}) => fetch(...args));

const API_KEY = process.env.AYRSHARE_API_KEY;
const POST_ID = process.env.POSTID
fetch(`https://api.ayrshare.com/api/post/${POST_ID}`, {
      method: "GET",
      headers: {
        "Authorization": `Bearer ${API_KEY}`
      }
    })
      .then((res) => res.json())
      .then((json) => console.log(json))
      .catch(console.error);