import { createRouter, createWebHistory } from 'vue-router';
import DashboardView from '@/views/DashboardView.vue';
import DevicesNetworkView from '@/views/DevicesNetworkView.vue';

const routes = [
  {
    path: '/',
    redirect: '/dashboard',
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: DashboardView,
  },
  {
    path: '/devices-network',
    name: 'DevicesNetwork',
    component: DevicesNetworkView,
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

export default router;