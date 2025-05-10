<template>
    <div class="app-container">
        <!-- Header -->
        <header class="header">
            <h1>SNMP Data Management</h1>
            <img src="/src/assets/Main.png" alt="logo" />
        </header>

        <!-- Sidebar -->
        <aside class="sidebar">
            <nav>
                <ul>
                    <li>
                        <router-link to="/" class="active">Home</router-link>
                    </li>
                    <li><router-link to="/devices">Devices</router-link></li>
                    <li><router-link to="/traps">Traps</router-link></li>
                    <li><router-link to="/analysis">Analysis</router-link></li>
                </ul>
            </nav>
        </aside>

        <!-- Main Content -->
        <main class="main-content">
            <div class="card">
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
                            {{ loading ? 'Fetching...' : 'Get SNMP Data' }}
                        </button>
                        <button
                            type="button"
                            @click="clearForm"
                            :disabled="loading"
                        >
                            Clear
                        </button>
                    </div>
                </form>
                <div v-if="result" class="result">
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
                </div>
                <div v-if="error" class="error">
                    <h3>Error</h3>
                    <p>{{ error }}</p>
                </div>
            </div>
        </main>
    </div>
</template>

<script>
import { ref } from 'vue';
import axios from 'axios'; // THÊM dòng này ở đầu file <script>

export default {
    name: 'App',
    setup() {
        // Form data
        const form = ref({
            ip: '',
            oid: '',
            community: '',
        });

        // State for result, error, and loading
        const result = ref(null);
        const error = ref(null);
        const loading = ref(false);

        // Function to fetch SNMP data (mock data)
        const fetchSnmpData = async () => {
            loading.value = true;
            result.value = null;
            error.value = null;

            try {
                await new Promise((resolve) => setTimeout(resolve, 1000));
                const response = await axios.post(`/snmp/get`, {
                    oid: form.value.oid,
                    ip: form.value.ip,
                    community: form.value.community,
                });
                result.value = response.data;
            } catch (err) {
                error.value = 'An error occurred while fetching SNMP data.';
                console.log(err);
            } finally {
                loading.value = false;
            }
        };

        // Function to clear form
        const clearForm = () => {
            form.value.ip = '';
            form.value.oid = '';
            form.value.community = '';
            result.value = null;
            error.value = null;
        };

        return {
            form,
            result,
            error,
            loading,
            fetchSnmpData,
            clearForm,
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
    min-height: 80vh;
    background-color: #f0f2f5;
    font-family: Arial, sans-serif;
}

.header {
    background-color: #2c3e50;
    color: white;
    padding: 1rem;
    text-align: center;
}

.header img {
    width: 500px;
    height: auto;
    margin-top: 50px;
}

.sidebar {
    width: 200px;
    background-color: #34495e;
    color: white;
    padding: 1rem;
}

.sidebar ul {
    list-style: none;
}

.sidebar li {
    margin: 1rem 0;
}

.sidebar a {
    color: white;
    text-decoration: none;
    padding: 0.5rem;
}

.sidebar a.router-link-active,
.sidebar a:hover {
    background-color: #465c71;
    border-radius: 4px;
}

.main-content {
    flex: 1;
    padding: 20px;
}

.card {
    background: white;
    padding: 20px;
    border-radius: 8px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
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
    padding: 8px;
    border: 1px solid #ddd;
    border-radius: 4px;
    font-size: 14px;
}

.button-group {
    display: flex;
    gap: 10px;
}

button {
    padding: 10px;
    border: none;
    border-radius: 4px;
    cursor: pointer;
    font-size: 14px;
    transition: background-color 0.3s;
}

button:first-child {
    background-color: #007bff;
    color: white;
    flex: 2;
}

button:last-child {
    background-color: #6c757d;
    color: white;
    flex: 1;
}

button:hover:not(:disabled) {
    opacity: 0.9;
}

button:disabled {
    background-color: #cccccc;
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

th,
td {
    padding: 8px;
    text-align: left;
    border-bottom: 1px solid #ddd;
}

th {
    background-color: #f2f2f2;
}

/* Responsive Design */
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
        padding: 10px;
    }

    .button-group {
        flex-direction: column;
    }

    button {
        width: 100%;
    }
}
</style>
