<template>
  <div class="sidebar">
    <router-link to="/devices-network">Devices & Network</router-link>
    <router-link
      to="/dashboard"
      :class="{ highlight: highlightDashboard }"
      @click="resetDashboardHighlight"
    >Dashboard</router-link>
    <router-link
      to="/mib-tree"
      :class="{ highlight: highlightMibTree }"
      @click="resetMibTreeHighlight"
    >MIB Tree</router-link>
    <router-link to="/search-oid">Search for OID </router-link>
  </div>
</template>

<script>
import { networkStore } from "@/stores/network";
import { ref, computed, watch } from "vue";

export default {
  setup() {
    const store = networkStore();
    
    
    const highlightDashboard = ref(false);
    const highlightMibTree = ref(false);
    
    
    const scanSuccess = computed(() => store.scanDevicesSuccess);
    
    
    watch(scanSuccess, (newValue) => {
      if (newValue) {
        highlightDashboard.value = true;
        highlightMibTree.value = true;
      }
    }, { immediate: true }); 

    
    const resetDashboardHighlight = () => {
      highlightDashboard.value = false;
      store.scanDevicesSuccess = false; 
    };
    
    const resetMibTreeHighlight = () => {
      highlightMibTree.value = false;
      store.scanDevicesSuccess = false; 
    };

    return {
      highlightDashboard,
      highlightMibTree,
      resetDashboardHighlight,
      resetMibTreeHighlight,
    };
  },
};
</script>

<style scoped>
.sidebar {
  width: 220px; 
  background: linear-gradient(
    135deg,
    #2c3e50,
    #1a252f
  ); 
  padding: 25px;
  border-radius: 12px; 
  box-shadow: 0 6px 15px rgba(0, 0, 0, 0.3), inset 0 0 10px rgba(0, 0, 0, 0.2); 
  position: relative;
  overflow: hidden; 
  font-family: "Poppins", "Segoe UI", sans-serif; 
}
.sidebar::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: radial-gradient(
    circle,
    rgba(0, 184, 212, 0.25),
    transparent 70%
  ); 
  opacity: 0.2;
  pointer-events: none; 
}
.sidebar a {
  display: block;
  padding: 15px 20px; 
  text-decoration: none;
  color: #ffffff; 
  font-weight: 500; 
  text-transform: uppercase; 
  letter-spacing: 1.2px; 
  border-radius: 8px; 
  transition: all 0.3s ease; 
  position: relative;
  overflow: hidden; 
  margin-bottom: 10px; 
}
.sidebar a:hover {
  background: linear-gradient(
    135deg,
    #00b8d4,
    #007bff
  ); 
  transform: translateX(5px); 
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.3); 
  color: #ffd700; 
}
.sidebar a.highlight {
  background: linear-gradient(
    135deg,
    #00b8d4,
    #007bff
  ); 
  transform: translateX(5px); 
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.3); 
  color: #ffd700; 
  animation: pulse 2s infinite; 
  margin-bottom: 10px; 
}
.sidebar a::after {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(
    90deg,
    transparent,
    rgba(255, 255, 255, 0.2),
    transparent
  ); 
  transform: translateX(-100%);
  transition: transform 0.5s ease;
}
.sidebar a:hover::after,
.sidebar a.highlight::after {
  transform: translateX(100%); 
}
@keyframes pulse {
  0% {
    box-shadow: 0 4px 10px rgba(0, 0, 0, 0.3);
  }
  50% {
    box-shadow: 0 6px 15px rgba(0, 184, 212, 0.5); 
  }
  100% {
    box-shadow: 0 4px 10px rgba(0, 0, 0, 0.3);
  }
}
</style>