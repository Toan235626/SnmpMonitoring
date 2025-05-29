<template>
    <teleport to="body">
      <div v-if="visible" class="overlay">
        <div class="modal">
          <h3>Quét các mạng khả dụng</h3>
  
          <div class="buttons">
            <button @click="scanNetworks" :disabled="loading">
              {{ loading ? "Đang quét..." : "Scan Networks" }}
            </button>
            <button @click="$emit('close')" :disabled="loading">Hủy</button>
          </div>
        </div>
      </div>
    </teleport>
  </template>  
  
  <script>
  import axios from "axios";
  
  export default {
    name: "ScanNetworkModal",
    props: ["visible"],
    emits: ["close", "networks-scanned"],
    data() {
      return {
        loading: false,
      };
    },
    methods: {
      async scanNetworks() {
        this.loading = true;
        try {
          const response = await axios.post("/api/device-scan/networks");
          const ips = response.data;
  
          const devices = ips.map(ip => ({
            deviceIp: ip,
            community: "public",
          }));
  
          this.$emit("networks-scanned", devices);
          this.$emit("close");
        } catch (err) {
          console.error("Scan failed", err);
          alert("Không thể quét networks.");
        } finally {
          this.loading = false;
        }
      },
    },
  };
  </script>  
  
  <style scoped>
  .overlay {
    position: fixed;
    inset: 0;
    background-color: rgba(0, 0, 0, 0.6);
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 10000;
  }
  
  .modal {
    background: #ffffff;
    border-radius: 16px;
    padding: 28px 32px;
    width: 100%;
    max-width: 420px;
    box-shadow: 0 20px 40px rgba(0, 0, 0, 0.25);
    text-align: left;
    animation: fadeIn 0.3s ease-out;
    font-family: 'Segoe UI', sans-serif;
    display: flex;
    flex-direction: column;
    gap: 16px;
  }
  
  .modal h3 {
    font-size: 20px;
    margin-bottom: 8px;
    color: #333;
    text-align: center;
  }
  
  .modal input {
    padding: 12px 14px;
    font-size: 16px;
    border-radius: 8px;
    border: 1px solid #ccc;
    box-sizing: border-box;
    width: 100%;
    margin-bottom: 8px;
  }
  
  .buttons {
    display: flex;
    flex-direction: column;
    gap: 12px;
    margin-top: 12px;
  }
  
  .buttons button {
    padding: 12px;
    border: none;
    border-radius: 8px;
    font-size: 15px;
    font-weight: bold;
    cursor: pointer;
    width: 100%;
    transition: background-color 0.2s ease;
  }
  
  .buttons button:first-child {
    background-color: #28a745;
    color: #fff;
  }
  
  .buttons button:first-child:hover {
    background-color: #218838;
  }
  
  .buttons button:last-child {
    background-color: #e0e0e0;
    color: #333;
  }
  
  .buttons button:last-child:hover {
    background-color: #ccc;
  }
  
  @keyframes fadeIn {
    from {
      opacity: 0;
      transform: scale(0.95);
    }
    to {
      opacity: 1;
      transform: scale(1);
    }
  }
  </style>
  
  