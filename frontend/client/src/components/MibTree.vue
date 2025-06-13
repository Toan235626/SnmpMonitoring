<template>
  <div class="mib-tree">
    <p v-if="!treeItems.length">No nodes to display</p>
    <ul v-else>
      <MibNode
        v-for="item in treeItems"
        :key="item.oid"
        :node="item"
        :selected-oid="selectedOid"
        @select-oid="handleSelectOid"
      />
    </ul>
  </div>
</template>

<script>
import { ref, computed } from "vue";
import MibNode from "@/components/MibNode.vue";

export default {
  name: "MibTree",
  components: { MibNode },
  props: {
    data: {
      type: [Object, Array],
      required: true,
    },
  },
  emits: ["select-oid"],
  setup(props, { emit }) {
    const selectedOid = ref("");

    const treeItems = computed(() => {
      const items = Array.isArray(props.data)
        ? props.data
        : Object.keys(props.data).length
        ? [props.data]
        : [];
      return items;
    });

    const handleSelectOid = (oid) => {
      selectedOid.value = oid;
      emit("select-oid", oid);
    };

    return {
      treeItems,
      selectedOid,
      handleSelectOid,
    };
  },
};
</script>

<style scoped>
.mib-tree {
  padding: 20px;
  background: linear-gradient(
    135deg,
    #676790,
    #2c2c54
  ); 
  min-height: 100vh; 
  border-radius: 12px; 
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.3),
    inset 0 0 10px rgba(255, 255, 255, 0.1); 
  position: relative;
  overflow: hidden; 
  font-family: "Poppins", "Segoe UI", sans-serif; 
}
.mib-tree::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: radial-gradient(
    circle,
    rgba(0, 123, 255, 0.2),
    transparent 70%
  ); 
  opacity: 0.3;
  pointer-events: none; 
}
ul {
  list-style: none;
  padding: 0;
  margin: 0;
  position: relative;
}
li {
  margin: 10px 0;
  padding-left: 15px; 
  position: relative;
  transition: all 0.4s ease; 
}
li:hover {
  transform: translateX(5px); 
}
li::before {
  content: "";
  position: absolute;
  left: 0;
  top: 0;
  height: 100%;
  width: 2px;
  background: linear-gradient(
    to bottom,
    #00ddeb,
    #ff6f61
  ); 
  opacity: 0.8;
  border-radius: 2px;
}
li:last-child::before {
  height: 50%; 
}
</style>
