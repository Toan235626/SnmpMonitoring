<template>
  <div class="app-container">
    <Sidebar />
    <div class="main-content">
      <h1>SNMP Monitoring</h1>
      <router-view />
    </div>
  </div>
</template>

<script>
import Sidebar from '@/components/Sidebar.vue';

export default {
  components: { Sidebar },
};
</script>

<style>
.app-container {
  display: flex;
  min-height: 100vh;
}
.main-content {
  flex: 1;
  padding: 20px;
}
</style>