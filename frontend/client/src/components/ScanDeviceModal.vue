<template>
    <teleport to="body">
      <div v-if="visible" class="overlay">
        <div class="modal">
          <h3>Quét thiết bị trong subnet {{ ip }}</h3>
  
          <input v-model="port" placeholder="Port (mặc định: 161)" type="number" />
          <input v-model="community" placeholder="Community" />
  
          <div class="buttons">
            <button @click="scanDevice" :disabled="loading">
              {{ loading ? "Đang quét..." : "Scan Device" }}
            </button>
            <button @click="$emit('close')">Hủy</button>
          </div>
        </div>
      </div>
    </teleport>  
  </template>
  
  <script>
  import axios from "axios";
  
  export default {
    name: "ScanDeviceModal",
    props: ["visible", "ip"],
    emits: ["close", "devices-scanned"],
    data() {
      return {
        port: "161",
        community: "public",
        loading: false,
      };
    },
    methods: {
      async scanDevice() {
        this.loading = true;
        try {
          const response = await axios.post("/api/device-scan/scan-subnet", null, {
            params: {
              baseIp: this.ip,
              community: this.community,
              port: this.port,
            },
          });
  
          this.$emit("scan-device", response.data)
          this.$emit("close");
        } catch (err) {
          console.error("Lỗi quét subnet:", err);
          alert("Không thể quét subnet.");
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
    background: white;
    border-radius: 16px;
    padding: 32px 24px;
    width: 100%;
    max-width: 420px;
    box-shadow: 0 20px 40px rgba(0, 0, 0, 0.25);
    animation: fadeIn 0.3s ease-out;
    display: flex;
    flex-direction: column;
    gap: 16px;
    text-align: left;
  }
  
  .modal h3 {
    font-size: 20px;
    font-weight: bold;
    margin-bottom: 8px;
    text-align: center;
  }
  
  .modal input {
    width: 100%;
    padding: 12px 14px;
    font-size: 16px;
    border-radius: 8px;
    border: 1px solid #ccc;
    box-sizing: border-box;
  }
  
  .buttons {
    display: flex;
    flex-direction: column;
    gap: 12px;
    margin-top: 12px;
  }
  
  .buttons button {
    padding: 12px 18px;
    border: none;
    border-radius: 8px;
    font-weight: bold;
    cursor: pointer;
    font-size: 15px;
    width: 100%;
    transition: background-color 0.2s ease;
  }
  
  .buttons button:first-child {
    background-color: #007bff;
    color: white;
  }
  
  .buttons button:first-child:hover {
    background-color: #005fcc;
  }
  
  .buttons button:last-child {
    background-color: #e0e0e0;
  }
  
  .buttons button:last-child:hover {
    background-color: #c7c7c7;
  }
  
  /* Optional: Smooth fade-in animation */
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
  