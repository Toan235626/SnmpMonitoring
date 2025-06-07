<template>
    <div class="sidebar">
      <router-link to="/dashboard">Dashboard</router-link>
      <router-link to="/devices-network">Devices & Network</router-link>
      <router-link to="/mib-tree">MIB Tree</router-link>
    </div>
  </template>
  
  <style scoped>
  .sidebar {
    width: 200px;
    background: #f4f4f4;
    padding: 20px;
  }
  .sidebar a {
    display: block;
    padding: 10px;
    text-decoration: none;
    color: #333;
  }
  .sidebar a:hover {
    background: #ddd;
  }
  </style>