import { createRouter, createWebHistory } from "vue-router";
import DashboardView from "@/views/DashboardView.vue";
import DevicesNetworkView from "@/views/DevicesNetworkView.vue";
import MIBTreeView from "@/views/MIBTreeView.vue";
import SearchOIDView from "@/views/SearchOIDView.vue";
import TrapView from "@/views/TrapView.vue";

const routes = [
  {
    path: "/",
    redirect: "/devices-network",
  },
  {
    path: "/dashboard",
    name: "Dashboard",
    component: DashboardView,
  },
  {
    path: "/devices-network",
    name: "DevicesNetwork",
    component: DevicesNetworkView,
  },
  {
    path: "/mib-tree",
    name: "MIB Tree",
    component: MIBTreeView,
  },
  {
    path: "/search-oid",
    name: "Search OID",
    component: SearchOIDView,
  },
  {
    path: "/trap",
    name: "trap",
    component: TrapView,
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

export default router;
