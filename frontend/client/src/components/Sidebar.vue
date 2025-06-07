<template>
    <div class="sidebar">
      <router-link to="/dashboard">Dashboard</router-link>
      <router-link to="/devices-network">Devices & Network</router-link>
      <router-link to="/mib-tree">MIB Tree</router-link>
    </div>
  </template>
  
  <style scoped>
  .sidebar {
    width: 220px; /* Slightly wider for a premium feel */
    background: linear-gradient(135deg, #2c3e50, #1a252f); /* Deep, gradient background */
    padding: 25px;
    border-radius: 12px; /* Smooth, modern corners */
    box-shadow: 0 6px 15px rgba(0, 0, 0, 0.3), inset 0 0 10px rgba(0, 0, 0, 0.2); /* Dramatic shadow + inner glow */
    position: relative;
    overflow: hidden; /* Contain pseudo-elements */
    font-family: 'Poppins', 'Segoe UI', sans-serif; /* Elegant, modern font */
  }
  .sidebar::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: radial-gradient(circle, rgba(0, 184, 212, 0.25), transparent 70%); /* Subtle cyan glow */
    opacity: 0.2;
    pointer-events: none; /* Allow interaction through overlay */
  }
  .sidebar a {
    display: block;
    padding: 15px 20px; /* More padding for comfort */
    text-decoration: none;
    color: #ffffff; /* Crisp white text */
    font-weight: 500; /* Bold but refined */
    text-transform: uppercase; /* Elegant, professional look */
    letter-spacing: 1.2px; /* Spaced letters for sophistication */
    border-radius: 8px; /* Rounded links */
    transition: all 0.3s ease; /* Smooth transitions for all properties */
    position: relative;
    overflow: hidden; /* Contain hover effects */
  }
  .sidebar a:hover {
    background: linear-gradient(135deg, #00b8d4, #007bff); /* Vibrant gradient on hover */
    transform: translateX(5px); /* Subtle slide effect */
    box-shadow: 0 4px 10px rgba(0, 0, 0, 0.3); /* Glow-like shadow */
    color: #ffd700; /* Gold text on hover for VIP feel */
  }
  .sidebar a::after {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent); /* Shine effect */
    transform: translateX(-100%);
    transition: transform 0.5s ease;
  }
  .sidebar a:hover::after {
    transform: translateX(100%); /* Shine moves across on hover */
  }
  </style>