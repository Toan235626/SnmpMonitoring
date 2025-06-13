# 🛰️ SNMP Monitoring Web Application

Hệ thống giám sát thiết bị mạng thông qua giao thức SNMP, gồm:

-   ✅ Backend: Spring Boot (SNMP4J, REST API)
-   ✅ Frontend: Vue.js 3 + Vite + Vuetify
-   ✅ Giao tiếp qua API RESTful
-   ✅ Hỗ trợ SNMPv1, v2c, v3

---

## 📦 Cấu trúc thư mục

```
.
├── backend/              # Spring Boot (SNMP API)
├── frontend/client/      # Vue 3 + Vuetify (UI)
├── docker-compose.yml    # Chạy cả frontend/backend bằng Docker
├── Main.java
└── README.md
```

---

## 🚀 Cách chạy

### ✅ Cách 1: Dùng Docker (khuyên dùng)

> Không cần cài Java, Maven hay Node

```bash
git clone https://github.com/Toan235626/SnmpMonitoring.git
cd SnmpMonitoring
docker-compose up --build
```

-   Backend: http://localhost:8086/api
-   Frontend: http://localhost:5173

---

### ✅ Cách 2: Chạy bằng file `Main.java` (gộp cả frontend + backend)

> Dành cho người đã có sẵn Java trên máy (không cần mở nhiều terminal)

```bash
cd snmp-monitoring
javac Main.java
java Main
```

-   File `Main.java` sẽ tự động:
    -   chạy `backend/` bằng Maven wrapper (`mvnw` hoặc `mvn`)
    -   chạy `frontend/client/` bằng `npm run dev`

### ✅ Cách 3: Chạy thủ công

#### 🧩 Backend

Yêu cầu: Java 17+, Maven

```bash
cd backend
./mvnw spring-boot:run     # Linux/macOS
mvnw.cmd spring-boot:run   # Windows
```

API chạy tại: http://localhost:8086/api

#### 🎨 Frontend

Yêu cầu: Node.js v18+

```bash
cd frontend/client
npm install
npm run dev
```

App chạy tại: http://localhost:5173

---

## 🛠 Tính năng

-   🔍 SNMP Walk, GetNext, GetBulk
-   📊 MIB Tree & Trap Viewer
-   🔐 Hỗ trợ bảo mật SNMPv3
-   🌐 UI hiện đại với Vue + Vuetify
-   📁 Hỗ trợ phân loại theo vendor

---

## 📚 Công nghệ sử dụng

| Layer    | Tech                   |
| -------- | ---------------------- |
| Frontend | Vue.js, Vuetify, Vite  |
| Backend  | Spring Boot, SNMP4J    |
| Database | H2 (in-memory)         |
| SNMP     | SNMPv1, v2c, v3        |
| DevOps   | Docker, Docker Compose |

---

## 📌 Tác giả

-   **Tên:** [Nguyễn Khánh Toàn, Dương Ngô Hoàng Vũ, Chu Văn An]
-   **GitHub:** [github.com/Toan235626]
-   **Liên hệ:** [Toan.NK235626@sis.hust.edu.vn]

---

> ⭐ Hãy star dự án nếu bạn thấy hữu ích!
