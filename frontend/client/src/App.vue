<template>
  <div class="app-container">
    <Sidebar />
    <div class="main-content">
      <div class="logo-container">
        <img src="@/assets/logo-removebg-preview.png" alt="Logo" class="logo" />
        <h1>SNMP Monitoring</h1>
      </div>
      <router-view />
    </div>
  </div>
</template>

<script>
import Sidebar from "@/components/Sidebar.vue";

export default {
  components: { Sidebar },
};
</script>

<style>
.app-container {
  display: flex;
  min-height: 100vh;
  gap: 8px;
  background-color: #f0f2f5;
  padding: 20px;
}

.main-content {
  flex: 1;
  padding: 20px;
  background-color: #ffffff;
  border-radius: 8px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.logo-container {
  display: flex; 
  align-items: center; 
  gap: 10px; 
}

.logo {
  width: 80px; 
}
</style>
