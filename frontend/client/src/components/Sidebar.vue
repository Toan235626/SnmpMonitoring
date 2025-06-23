<template>
    <div class="sidebar">
        <router-link
            :to="'/devices-network'"
            :class="getLinkClass('/devices-network')"
        >
            <Antenna class="icon" /> Devices & Network
        </router-link>

        <router-link
            to="/dashboard"
            :class="getLinkClass('/dashboard', highlightDashboard)"
        >
            <LayoutDashboard class="icon" /> Dashboard
        </router-link>

        <router-link
            to="/mib-tree"
            :class="getLinkClass('/mib-tree', highlightMibTree)"
        >
            <TreeDeciduous class="icon" /> MIB Tree
        </router-link>

        <router-link :to="'/search-oid'" :class="getLinkClass('/search-oid')">
            <Search class="icon" /> Search for OID
        </router-link>

        <router-link :to="'/trap'" :class="getLinkClass('/trap')">
            <Bell class="icon" /> Trap
        </router-link>
    </div>
</template>

<script setup>
import { ref, watch, computed } from 'vue';
import { useRoute } from 'vue-router';
import { networkStore } from '@/stores/network';

import {
    TreeDeciduous,
    LayoutDashboard,
    Antenna,
    Search,
    Bell,
} from 'lucide-vue-next';

const route = useRoute();
const store = networkStore();

const highlightDashboard = ref(false);
const highlightMibTree = ref(false);

const scanSuccess = computed(() => store.scanDevicesSuccess);

watch(scanSuccess, (newValue) => {
    if (newValue) {
        highlightDashboard.value = true;
        highlightMibTree.value = true;
        setTimeout(() => {
            highlightDashboard.value = false;
            highlightMibTree.value = false;
            store.scanDevicesSuccess = false;
        }, 2000);
    }
});

const getLinkClass = (path, highlight = false) => {
    return {
        highlight,
        active: route.path === path,
    };
};
</script>

<style scoped>
.sidebar {
    width: 220px;
    height: 100vh;
    background: linear-gradient(135deg, #2c3e50, #1a252f);
    padding: 25px;
    border-radius: 12px;
    box-shadow: 0 6px 15px rgba(0, 0, 0, 0.3), inset 0 0 10px rgba(0, 0, 0, 0.2);
    font-family: 'Poppins', 'Segoe UI', sans-serif;
    overflow-y: auto;
}
.sidebar a {
    display: flex;
    align-items: center;
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
.icon {
    width: 20px;
    height: 20px;
    margin-right: 10px;
    flex-shrink: 0;
}
.sidebar a:hover,
.sidebar a.active {
    background: linear-gradient(135deg, #00b8d4, #007bff);
    transform: translateX(5px);
    box-shadow: 0 4px 10px rgba(0, 0, 0, 0.3);
    color: #ffd700;
}
.sidebar a.highlight {
    background: linear-gradient(135deg, #00b8d4, #007bff);
    transform: translateX(5px);
    box-shadow: 0 4px 10px rgba(0, 0, 0, 0.3);
    color: #ffd700;
    animation: pulse 2s infinite;
}
.sidebar a::after {
    content: '';
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
