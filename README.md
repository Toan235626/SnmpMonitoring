# 🛰️ SNMP Monitoring Web Application

A web-based system for monitoring network devices using SNMP, including:

- ✅ Backend: Spring Boot (SNMP4J, REST API)
- ✅ Frontend: Vue.js 3 + Vite + Vuetify
- ✅ Communication via RESTful APIs
- ✅ Support for SNMPv1, v2c, and v3

---

## 📦 Project Structure

```
.
├── backend/              # Spring Boot (SNMP API)
├── frontend/client/      # Vue 3 + Vuetify (UI)
├── docker-compose.yml    # Launches both frontend and backend
├── Main.java
└── README.md
```

---

## 🚀 How to Run

### ✅ Option 1: Using Docker (recommended)

> No need to install Java, Maven, or Node.js

```bash
git clone https://github.com/Toan235626/SnmpMonitoring.git
cd SnmpMonitoring
docker-compose up --build
```

- Backend: http://localhost:8086/api  
- Frontend: http://localhost:5173

---

### ✅ Option 2: Run using `Main.java` (automatically starts both frontend and backend)

> For users who already have Java installed (no need to open multiple terminals)

```bash
cd SnmpMonitoring
javac Main.java
java Main
```

- The `Main.java` file will automatically:
  - Start `backend/` using Maven Wrapper (`mvnw` or `mvn`)
  - Start `frontend/client/` using `npm run dev`

---

### ✅ Option 3: Run manually

```bash
cd SnmpMonitoring
```

#### 🧩 Backend

**Requirements:** Java 17+, Maven

```bash
cd backend
mvn spring-boot:run
```

API available at: http://localhost:8086/api

#### 🎨 Frontend

**Requirements:** Node.js v18+

```bash
cd frontend/client
npm install
npm run dev
```

App available at: http://localhost:5173

---

## 🛠 Features

- 🔍 SNMP Walk, GetNext, and GetBulk operations
- 📊 MIB Tree & Trap Viewer
- 🔐 SNMPv3 authentication support
- 🌐 Modern UI with Vue + Vuetify
- 📁 Vendor-based MIB classification

---

## 📚 Technologies Used

| Layer     | Technologies               |
|-----------|----------------------------|
| Frontend  | Vue.js, Vuetify, Vite      |
| Backend   | Spring Boot, SNMP4J        |
| Database  | H2 (in-memory)             |
| SNMP      | SNMPv1, v2c, v3            |
| DevOps    | Docker, Docker Compose     |

---

## 📌 Authors

- **Names:** Nguyễn Khánh Toàn, Dương Ngô Hoàng Vũ, Chu Văn An  
- **GitHub:** [github.com/Toan235626](https://github.com/Toan235626)  
- **Contact:** [Toan.NK235626@sis.hust.edu.vn](mailto:Toan.NK235626@sis.hust.edu.vn)

---

> ⭐ Star this project if you found it helpful!
