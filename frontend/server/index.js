const express = require("express");
const axios = require("axios");
const app = express();

// Middleware to parse JSON requests
app.use(express.json());

// Proxy endpoint to forward SNMP GET requests to Spring Boot
app.post("/snmp/get", async (req, res) => {
  try {
    const response = await axios.post("http://localhost:8081/api/snmp/get", req.body);
    res.json(response.data);
  } catch (error) {
    res.status(500).json({ error: error.message || "Failed to fetch SNMP data" });
  }
});

// Start the server
const PORT = 4000;
app.listen(PORT, () => {
  console.log(`Server running on http://localhost:${PORT}`);
});
