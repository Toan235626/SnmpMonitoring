<template>
    <div class="trap-container">
        <h2>SNMP Trap Management</h2>

        <!-- Tabs -->
        <div class="tabs">
            <button
                :class="{ active: activeTab === 'latest' }"
                @click="activeTab = 'latest'"
            >
                Latest Trap
            </button>
            <button
                :class="{ active: activeTab === 'history' }"
                @click="activeTab = 'history'"
            >
                Trap History
            </button>
        </div>

        <!-- Tab Content -->
        <div v-show="activeTab === 'latest'" class="tab-content">
            <h3>Latest Trap</h3>
            <div v-if="latestTrap">
                <!-- <p><strong>Raw PDU:</strong> {{ latestTrap.rawPdu }}</p> -->
                <p>
                    <strong>SNMP Version:</strong> {{ latestTrap.snmpVersion }}
                </p>
                <h4>Variables:</h4>
                <table class="trap-table">
                    <thead>
                        <tr>
                            <th>OID</th>
                            <th>Value</th>
                            <th>Info</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr
                            v-for="(variable, index) in latestTrap.variables"
                            :key="index"
                        >
                            <td>{{ Object.values(variable)[1] }}</td>
                            <td>{{ Object.values(variable)[2] }}</td>
                            <td>{{ Object.values(variable)[0] }}</td>
                        </tr>
                    </tbody>
                </table>
            </div>
            <p v-else>No new Trap available.</p>
        </div>

        <div v-show="activeTab === 'history'" class="tab-content">
            <h3>Trap History (Total: {{ trapHistory.length }})</h3>
            <table class="trap-table">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>sysTem Up Time</th>
                        <th>Trap OID</th>
                        <th>Sender's Ip</th>
                        <th>SNMP Version</th>
                        <th>Details</th>
                    </tr>
                </thead>
                <tbody>
                    <tr v-for="(trap, index) in trapHistory" :key="index">
                        <td>{{ index + 1 }}</td>
                        <td>
                            {{ Object.values(trap.variables[0])[2] || 'N/A' }}
                        </td>
                        <td>
                            {{ Object.values(trap.variables[1])[2] || 'N/A' }}
                        </td>
                        <td>
                            {{ trap.senderIp || 'N/A' }}
                        </td>
                        <td>{{ trap.snmpVersion }}</td>
                        <td>
                            <button @click="showTrapDetails(trap)">
                                View Details
                            </button>
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>

        <!-- Trap Details Modal -->
        <div v-if="selectedTrap" class="modal">
            <div class="modal-content">
                <h3>Trap Details</h3>
                <p><strong>Raw PDU:</strong> {{ selectedTrap.rawPdu }}</p>
                <p>
                    <strong>SNMP Version:</strong>
                    {{ selectedTrap.snmpVersion }}
                </p>
                <h4>Variables:</h4>
                <table class="trap-table">
                    <thead>
                        <tr>
                            <th>OID</th>
                            <th>Value</th>
                            <th>Info</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr
                            v-for="(variable, index) in selectedTrap.variables"
                            :key="index"
                        >
                            <td>{{ Object.values(variable)[1] }}</td>
                            <td>{{ Object.values(variable)[2] }}</td>
                            <td>{{ Object.values(variable)[0] }}</td>
                        </tr>
                    </tbody>
                </table>
                <button @click="selectedTrap = null">Close</button>
            </div>
        </div>
    </div>
</template>

<script>
import axios from '@/axios.js';
import { useToast } from 'vue-toastification';

export default {
    name: 'TrapView',
    setup() {
        const toast = useToast();
        return { toast };
    },
    data() {
        return {
            activeTab: 'latest',
            latestTrap: null,
            trapHistory: [],
            selectedTrap: null,
            pollingInterval: null,
            lastNotifiedTrapId: null,
        };
    },
    mounted() {
        this.startPolling();
    },
    beforeDestroy() {
        this.stopPolling();
    },
    methods: {
        async fetchTraps() {
            try {
                const response = await axios.post('/traps');
                const traps = response.data;

                if (traps.length > 0) {
                    const newLatestTrap = traps[traps.length - 1];
                    if (
                        !this.latestTrap ||
                        this.latestTrap.rawPdu !== newLatestTrap.rawPdu
                    ) {
                        this.latestTrap = newLatestTrap;

                        if (this.lastNotifiedTrapId !== newLatestTrap.rawPdu) {
                            this.toast.success('New Trap received', {
                                timeout: 5000,
                            });
                            this.lastNotifiedTrapId = newLatestTrap.rawPdu;
                        }
                    }

                    traps.forEach((trap) => {
                        if (
                            !this.trapHistory.some(
                                (t) => t.rawPdu === trap.rawPdu
                            )
                        ) {
                            this.trapHistory.unshift({
                                ...trap,
                                timestamp: new Date(),
                            });
                        }
                    });
                }
            } catch (error) {
                console.error('Error fetching Trap data:', error);
            }
        },
        startPolling() {
            this.pollingInterval = setInterval(this.fetchTraps, 1000);
        },
        stopPolling() {
            if (this.pollingInterval) {
                clearInterval(this.pollingInterval);
            }
        },
        truncateRawPdu(rawPdu) {
            return rawPdu.length > 50
                ? rawPdu.substring(0, 50) + '...'
                : rawPdu;
        },
        showTrapDetails(trap) {
            this.selectedTrap = trap;
        },
    },
};
</script>

<style scoped>
.trap-container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 20px;
    background: rgba(255, 255, 255, 0.95);
    border-radius: 12px;
    box-shadow: 0 6px 15px rgba(0, 0, 0, 0.2);
    animation: fadeIn 0.5s ease-in;
}

h2 {
    font-size: 24px;
    font-weight: 600;
    color: #2c3e50;
    text-align: center;
    margin-bottom: 20px;
    background: linear-gradient(135deg, #1e88e5, #43a047);
    background-clip: text;
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
}

h3 {
    font-size: 20px;
    font-weight: 500;
    color: #2c3e50;
    margin-bottom: 15px;
    text-transform: uppercase;
    letter-spacing: 0.5px;
}

.tabs {
    display: flex;
    gap: 10px;
    margin-bottom: 20px;
    justify-content: center;
}

.tabs button {
    padding: 12px 20px;
    border: none;
    background: linear-gradient(135deg, #43a047, #1e88e5);
    color: #ffffff;
    font-size: 16px;
    font-weight: 500;
    text-transform: uppercase;
    letter-spacing: 1px;
    cursor: pointer;
    border-radius: 8px;
    box-shadow: 0 4px 10px rgba(0, 0, 0, 0.2);
    transition: all 0.3s ease;
}

.tabs button.active {
    background: linear-gradient(135deg, #1e88e5, #43a047);
    transform: scale(1.05);
    box-shadow: 0 6px 15px rgba(0, 0, 0, 0.3);
}

.tabs button:hover:not(.active) {
    background: linear-gradient(135deg, #1e88e5, #43a047);
    transform: scale(1.05);
    box-shadow: 0 6px 15px rgba(0, 0, 0, 0.3);
}

.tab-content {
    background: rgba(255, 255, 255, 0.9);
    padding: 20px;
    border-radius: 10px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
    overflow: hidden;
}
.trap-table td button {
    padding: 10px 16px;
    background: linear-gradient(135deg, #1e88e5, #43a047);
    color: #ffffff;
    border: none;
    border-radius: 8px;
    font-size: 14px;
    font-weight: 600;
    text-transform: uppercase;
    letter-spacing: 1px;
    cursor: pointer;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
    transition: all 0.3s ease;
}

.trap-table td button:hover {
    background: linear-gradient(135deg, #1e88e5, #43a047);
    transform: scale(1.1);
    box-shadow: 0 6px 18px rgba(0, 0, 0, 0.4);
}
.trap-table {
    width: 100%;
    border-collapse: collapse;
    margin-top: 10px;
    background: rgba(255, 255, 255, 0.9);
    border-radius: 10px;
    overflow: hidden;
}

.trap-table th {
    background: linear-gradient(135deg, #1e88e5, #43a047);
    color: #ffffff;
    padding: 15px 18px;
    text-align: left;
    font-weight: 600;
    text-transform: uppercase;
    letter-spacing: 1px;
    border-bottom: 2px solid rgba(255, 255, 255, 0.2);
}

.trap-table td {
    padding: 15px 18px;
    border-bottom: 1px solid rgba(0, 0, 0, 0.1);
    color: #2c3e50;
    transition: background 0.3s ease;
}

.trap-table tr:hover {
    background: rgba(227, 242, 253, 0.9);
}

.modal {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: rgba(0, 0, 0, 0.5);
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 1000;
}

.modal-content {
    background: rgba(255, 255, 255, 0.95);
    padding: 20px;
    border-radius: 12px;
    max-width: 800px;
    width: 90%;
    max-height: 80vh;
    overflow-y: auto;
    box-shadow: 0 6px 15px rgba(0, 0, 0, 0.2);
    animation: fadeIn 0.5s ease-in;
}

.modal-content button {
    margin-top: 10px;
    padding: 12px 20px;
    background: linear-gradient(135deg, #43a047, #1e88e5);
    color: #ffffff;
    border: none;
    border-radius: 8px;
    font-size: 16px;
    font-weight: 500;
    text-transform: uppercase;
    letter-spacing: 1px;
    cursor: pointer;
    box-shadow: 0 4px 10px rgba(0, 0, 0, 0.2);
    transition: all 0.3s ease;
}

.modal-content button:hover {
    background: linear-gradient(135deg, #1e88e5, #43a047);
    transform: scale(1.05);
    box-shadow: 0 6px 15px rgba(0, 0, 0, 0.3);
}

p.no-trap {
    margin-top: 20px;
    padding: 15px;
    background: rgba(255, 235, 238, 0.9);
    border-radius: 8px;
    color: #d32f2f;
    text-align: center;
    box-shadow: 0 4px 10px rgba(0, 0, 0, 0.15);
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

@media (max-width: 600px) {
    .trap-container {
        padding: 10px;
    }

    .tabs {
        flex-direction: column;
        gap: 8px;
    }

    .tabs button {
        padding: 10px;
        font-size: 14px;
    }

    .trap-table th,
    .trap-table td {
        padding: 10px;
        font-size: 14px;
    }

    .modal-content {
        width: 95%;
        padding: 15px;
    }

    .modal-content button {
        padding: 10px;
        font-size: 14px;
    }
}
</style>
