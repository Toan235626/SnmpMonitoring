<template>
  <div class="app-container">
    <!-- Header -->
    <header class="header">
      <h1>SNMP Dashboard</h1>
      <div class="quick-stats">
        <div class="stat-card">
          <span>Requests</span>
          <p>{{ history.length }}</p>
        </div>
        <div class="stat-card">
          <span>Success Rate</span>
          <p>{{ successRate }}%</p>
        </div>
      </div>
      <img src="/src/assets/Main.png" alt="logo" />
    </header>

    <!-- Sidebar -->
    <aside class="sidebar">
      <nav>
        <ul>
          <li><router-link to="/" class="active">Dashboard</router-link></li>
          <li><router-link to="/devices">Devices</router-link></li>
          <li><router-link to="/traps">Traps</router-link></li>
          <li><router-link to="/analysis">Analysis</router-link></li>
        </ul>
      </nav>
    </aside>

    <!-- Main Content -->
    <main class="main-content">
      <div class="dashboard">
        <!-- SNMP Form -->
        <div class="card form-card">
          <h2>Retrieve SNMP Data</h2>
          <form @submit.prevent="fetchSnmpData" class="form">
            <div class="form-group">
              <label for="ip">Device IP:</label>
              <input
                type="text"
                id="ip"
                v-model="form.ip"
                placeholder="e.g., 192.168.1.20"
                required
              />
            </div>
            <div class="form-group">
              <label for="oid">OID:</label>
              <input
                type="text"
                id="oid"
                v-model="form.oid"
                placeholder="e.g., 1.3.6.1.2.1.1.3.0"
                required
              />
            </div>
            <div class="form-group">
              <label for="community">Community String:</label>
              <input
                type="text"
                id="community"
                v-model="form.community"
                placeholder="e.g., public"
                required
              />
            </div>
            <div class="button-group">
              <button type="submit" :disabled="loading">
                <span v-if="loading" class="spinner"></span>
                {{ loading ? "Fetching..." : "Get SNMP Data" }}
              </button>
              <button type="button" @click="clearForm" :disabled="loading">
                Clear
              </button>
            </div>
          </form>
          <div v-if="result" class="result animate-result">
            <h3>Result</h3>
            <table>
              <thead>
                <tr>
                  <th>Field</th>
                  <th>Value</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(value, key) in result" :key="key">
                  <td>{{ key }}</td>
                  <td>{{ value }}</td>
                </tr>
              </tbody>
            </table>
            <button class="copy-btn" @click="copyResult">Copy to Clipboard</button>
          </div>
          <div v-if="error" class="error animate-error">
            <h3>Error</h3>
            <p>{{ error }}</p>
          </div>
        </div>

        <!-- History Section -->
        <div class="card history-card">
          <h2>Request History</h2>
          <div class="search-bar">
            <input
              type="text"
              v-model="searchQuery"
              placeholder="Search by IP, OID, or Community..."
            />
          </div>
          <div class="history-list">
            <div
              v-for="(entry, index) in filteredHistory"
              :key="index"
              class="history-item"
            >
              <p><strong>IP:</strong> {{ entry.ip }}</p>
              <p><strong>OID:</strong> {{ entry.oid }}</p>
              <p><strong>Community:</strong> {{ entry.community }}</p>
              <p><strong>Result:</strong> {{ entry.result.value }}</p>
            </div>
            <p v-if="!filteredHistory.length">No history available.</p>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script>
import { ref, computed } from "vue";
import { useToast } from "vue-toastification";
import Clipboard from "clipboard";

export default {
  name: "App",
  setup() {
    const toast = useToast();

    // Form data
    const form = ref({
      ip: "",
      oid: "",
      community: "",
    });

    // State for result, error, loading, and history
    const result = ref(null);
    const error = ref(null);
    const loading = ref(false);
    const history = ref(
      JSON.parse(localStorage.getItem("snmpHistory")) || []
    );
    const searchQuery = ref("");

    // Computed property for success rate
    const successRate = computed(() => {
      if (!history.value.length) return 100;
      const successful = history.value.filter((entry) => !entry.error).length;
      return Math.round((successful / history.value.length) * 100);
    });

    // Computed property for filtered history
    const filteredHistory = computed(() => {
      if (!searchQuery.value) return history.value;
      const query = searchQuery.value.toLowerCase();
      return history.value.filter(
        (entry) =>
          entry.ip.toLowerCase().includes(query) ||
          entry.oid.toLowerCase().includes(query) ||
          entry.community.toLowerCase().includes(query)
      );
    });

    // Function to fetch SNMP data (mock data)
    const fetchSnmpData = async () => {
      loading.value = true;
      result.value = null;
      error.value = null;

      try {
        // Mock data with simulated delay
        await new Promise((resolve) => setTimeout(resolve, 1000));
        const mockResponse = {
          oid: form.value.oid,
          value: "MinhPC <3 NamPC",
          ip: form.value.ip,
          community: form.value.community,
        };
        result.value = mockResponse;

        // Add to history
        history.value.push({
          ip: form.value.ip,
          oid: form.value.oid,
          community: form.value.community,
          result: mockResponse,
          error: null,
        });
        localStorage.setItem("snmpHistory", JSON.stringify(history.value));
      } catch (err) {
        error.value = "An error occurred while fetching SNMP data.";
        history.value.push({
          ip: form.value.ip,
          oid: form.value.oid,
          community: form.value.community,
          result: null,
          error: error.value,
        });
        localStorage.setItem("snmpHistory", JSON.stringify(history.value));
      } finally {
        loading.value = false;
      }
    };

    // Function to clear form
    const clearForm = () => {
      form.value.ip = "";
      form.value.oid = "";
      form.value.community = "";
      result.value = null;
      error.value = null;
      toast.success("Form cleared!");
    };

    // Function to copy result to clipboard
    const copyResult = () => {
      const clipboard = new Clipboard(".copy-btn", {
        text: () => JSON.stringify(result.value, null, 2),
      });
      clipboard.on("success", () => {
        toast.success("Copied to clipboard!");
        clipboard.destroy();
      });
      clipboard.on("error", () => {
        toast.error("Failed to copy!");
        clipboard.destroy();
      });
    };

    return {
      form,
      result,
      error,
      loading,
      history,
      searchQuery,
      successRate,
      filteredHistory,
      fetchSnmpData,
      clearForm,
      copyResult,
    };
  },
};
</script>

<style>
/* Reset default styles */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

.app-container {
  display: flex;
  min-height: 100vh;
  background: linear-gradient(135deg, #f0f2f5, #e6e9ec);
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}

.header {
  background: linear-gradient(90deg, #2c3e50, #4a6278);
  color: white;
  padding: 1.5rem;
  text-align: center;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
}
.header img {
  width: 500px;
  height: auto;
  margin-top: 50px;
}

.quick-stats {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 10px;
}

.stat-card {
  background: rgba(255, 255, 255, 0.1);
  padding: 10px 20px;
  border-radius: 8px;
  text-align: center;
}

.stat-card span {
  display: block;
  font-size: 14px;
  opacity: 0.8;
}

.stat-card p {
  font-size: 18px;
  font-weight: bold;
  margin: 5px 0 0;
}

.sidebar {
  width: 220px;
  background: #34495e;
  color: white;
  padding: 1.5rem;
  box-shadow: 2px 0 10px rgba(0, 0, 0, 0.1);
}

.sidebar ul {
  list-style: none;
}

.sidebar li {
  margin: 1.2rem 0;
}

.sidebar a {
  color: white;
  text-decoration: none;
  padding: 0.7rem 1rem;
  display: block;
  border-radius: 4px;
  transition: background 0.3s;
}

.sidebar a.router-link-active,
.sidebar a:hover {
  background: #465c71;
}

.main-content {
  flex: 1;
  padding: 30px;
  overflow-y: auto;
}

.dashboard {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.card {
  background: white;
  padding: 20px;
  border-radius: 10px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s;
}

.card:hover {
  transform: translateY(-3px);
}

.form-card {
  grid-column: 1;
}

.history-card {
  grid-column: 2;
}

.form {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.form-group {
  display: flex;
  flex-direction: column;
}

label {
  margin-bottom: 5px;
  font-weight: bold;
  color: #333;
}

input {
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  transition: border-color 0.3s;
}

input:focus {
  border-color: #007bff;
  outline: none;
}

.button-group {
  display: flex;
  gap: 10px;
}

button {
  padding: 10px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.3s, transform 0.1s;
}

button:first-child {
  background: linear-gradient(90deg, #007bff, #0056b3);
  color: white;
  flex: 2;
}

button:last-child {
  background: linear-gradient(90deg, #6c757d, #5a6268);
  color: white;
  flex: 1;
}

button:hover:not(:disabled) {
  transform: scale(1.02);
}

button:disabled {
  background: #cccccc;
  cursor: not-allowed;
}

.spinner {
  display: inline-block;
  width: 14px;
  height: 14px;
  border: 2px solid #fff;
  border-top: 2px solid transparent;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-right: 5px;
  vertical-align: middle;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.result,
.error {
  margin-top: 20px;
  padding: 15px;
  border-radius: 8px;
}

.result {
  background: #e7f3fe;
  border: 1px solid #b3d4fc;
}

.error {
  background: #f8d7da;
  border: 1px solid #f5c6cb;
  color: #721c24;
}

table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 10px;
}

th, td {
  padding: 10px;
  text-align: left;
  border-bottom: 1px solid #ddd;
}

th {
  background: #f8f9fa;
  font-weight: bold;
}

.copy-btn {
  margin-top: 10px;
  background: linear-gradient(90deg, #28a745, #218838);
  color: white;
  width: 100%;
}

.history-card h2 {
  margin-bottom: 15px;
}

.search-bar input {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 6px;
  margin-bottom: 15px;
}

.history-list {
  max-height: 300px;
  overflow-y: auto;
  border: 1px solid #eee;
  border-radius: 6px;
  padding: 10px;
}

.history-item {
  padding: 10px;
  border-bottom: 1px solid #eee;
  background: #f8f9fa;
  border-radius: 4px;
  margin-bottom: 5px;
}

.history-item:last-child {
  border-bottom: none;
}

.history-item p {
  margin: 5px 0;
  font-size: 14px;
}

/* Animation for result and error */
.animate-result {
  animation: fadeIn 0.5s ease-in;
}

.animate-error {
  animation: fadeIn 0.5s ease-in;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* Responsive Design */
@media (max-width: 1024px) {
  .dashboard {
    grid-template-columns: 1fr;
  }

  .form-card,
  .history-card {
    grid-column: 1;
  }
}

@media (max-width: 768px) {
  .app-container {
    flex-direction: column;
  }

  .sidebar {
    width: 100%;
    padding: 10px;
  }

  .sidebar ul {
    display: flex;
    justify-content: space-around;
  }

  .main-content {
    padding: 15px;
  }

  .button-group {
    flex-direction: column;
  }

  button {
    width: 100%;
  }

  .quick-stats {
    flex-wrap: wrap;
  }

  .stat-card {
    flex: 1 1 45%;
  }
}

@media (max-width: 480px) {
  .header h1 {
    font-size: 1.2rem;
  }

  .stat-card {
    padding: 8px 10px;
  }

  .stat-card p {
    font-size: 16px;
  }
}
</style>