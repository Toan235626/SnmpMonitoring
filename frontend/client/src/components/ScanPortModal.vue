<!-- src/components/ScanPortModal.vue -->
<template>
    <div v-if="isOpen" class="modal-overlay" @click="$emit('close')">
      <div class="modal-content" @click.stop>
        <h2>Nhập Port để Scan</h2>
        <form @submit.prevent="handleSubmit">
          <div class="form-group">
            <label for="port">Port:</label>
            <input
              id="port"
              v-model="port"
              type="number"
              min="1"
              max="65535"
              placeholder="Nhập port (1-65535)"
              required
            />
          </div>
          <div class="button-group">
            <button type="submit" :disabled="loading">Scan</button>
            <button type="button" @click="$emit('close')">Hủy</button>
          </div>
        </form>
        <p v-if="error" class="error">{{ error }}</p>
      </div>
    </div>
  </template>
  
  <script>
  import { ref } from 'vue';
  import axios from 'axios';
  
  export default {
    name: 'ScanPortModal',
    props: {
      isOpen: {
        type: Boolean,
        default: false,
      },
      deviceIp: {
        type: String,
        default: '',
      },
    },
    emits: ['close', 'scan-success'],
    setup(props, { emit }) {
      const port = ref('');
      const loading = ref(false);
      const error = ref(null);
  
      const handleSubmit = async () => {
        if (!port.value || port.value < 1 || port.value > 65535) {
          error.value = 'Vui lòng nhập port hợp lệ (1-65535).';
          return;
        }
  
        loading.value = true;
        error.value = null;
  
        try {
          const response = await axios.post('/api/device-scan/scan-subnet', null, {
            params: {
              deviceIp: props.deviceIp,
              port: port.value,
            },
          });
  
          // Giả sử API trả về danh sách thiết bị, ví dụ: [{ ip, status, port }]
          emit('scan-success', {
            deviceIp: props.deviceIp,
            port: port.value,
            result: response.data,
          });
          emit('close');
          port.value = '';
        } catch (err) {
          error.value = err.response?.data?.error || 'Đã xảy ra lỗi khi scan mạng.';
        } finally {
          loading.value = false;
        }
      };
  
      return {
        port,
        loading,
        error,
        handleSubmit,
      };
    },
    mounted() {
      document.addEventListener('keydown', this.handleEsc);
    },
    beforeUnmount() {
      document.removeEventListener('keydown', this.handleEsc);
    },
    methods: {
      handleEsc(event) {
        if (event.key === 'Escape') {
          this.$emit('close');
        }
      },
    },
  };
  </script>
  
  <style scoped>
  .modal-overlay {
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
    background: #fff;
    padding: 20px;
    border: 1px solid #ccc;
    border-radius: 4px;
    max-width: 400px;
    width: 100%;
  }
  
  .form-group {
    margin-bottom: 15px;
  }
  
  label {
    display: block;
    margin-bottom: 5px;
    font-weight: bold;
  }
  
  input {
    width: 100%;
    padding: 6px;
    border: 1px solid #ccc;
    border-radius: 4px;
    font-size: 12px;
  }
  
  .button-group {
    display: flex;
    justify-content: flex-end;
    gap: 10px;
  }
  
  button {
    padding: 6px 12px;
    border: 1px solid #999;
    border-radius: 4px;
    cursor: pointer;
    font-size: 12px;
  }
  
  button[type="submit"] {
    background: #28a745;
    color: #fff;
  }
  
  button[type="submit"]:hover {
    background: #218838;
  }
  
  button[type="button"] {
    background: #dc3545;
    color: #fff;
  }
  
  button[type="button"]:hover {
    background: #c82333;
  }
  
  button:disabled {
    background: #ccc;
    cursor: not-allowed;
  }
  
  .error {
    color: #721c24;
    margin-top: 10px;
    font-size: 12px;
  }
  </style>