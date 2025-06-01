require('dotenv').config();

const fetch = (...args) => import('node-fetch').then(({default: fetch}) => fetch(...args));

const API_KEY = process.env.AYRSHARE_API_KEY;

fetch("https://api.ayrshare.com/api/post", {
  method: "POST",
  headers: {
    "Content-Type": "application/json",
    "Authorization": `Bearer ${API_KEY}`
  },
  body: JSON.stringify({
    post: "앱: {YouTube} 시간대 시작: {12}:{00} 종료: {12}:{00} 규칙: {유튜브 보지 않기} 위반 유무: {위반함}",
    platforms: ["facebook", "instagram"],
    mediaUrls: [
      "https://search.pstatic.net/common/?src=http%3A%2F%2Fblogfiles.naver.net%2FMjAyNTA1MDhfMSAg%2FMDAxNzQ2Njg2NDI5ODQy.yk3R0vuDGWZNttHBHTq0WV4PKDokkxMn1FBJ9cf3CyUg.n06O2_b720YxEvlSzjqSuseUP3r0Na_CIzLWVnjRoGkg.PNG%2F%25C1%25D6%25C0%25C7%25BB%25E7%25C7%25D7.png&type=a340"
    ],
    scheduleDate: "2025-06-01T19:32:00Z",
    instagramOptions: {
      stories: true
    }
  }),
})
.then(res => res.json())
.then(json => console.log(json))
.catch(console.error);
