const { DateTime } = require("luxon");

const timestamp = "2025-06-01 10:20:07";

const isoString = DateTime.fromSQL(timestamp, { zone: 'utc' }).toISO();
const cleanISOString = isoString.replace('.000', '');
console.log(cleanISOString); // "2025-06-01T10:20:07Z"

