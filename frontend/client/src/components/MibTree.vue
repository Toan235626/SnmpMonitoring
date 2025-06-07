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
import { ref, computed } from 'vue';
import MibNode from '@/components/MibNode.vue';

export default {
  name: 'MibTree',
  components: { MibNode },
  props: {
    data: {
      type: [Object, Array],
      required: true,
    },
  },
  emits: ['select-oid'],
  setup(props, { emit }) {
    const selectedOid = ref('');

    const treeItems = computed(() => {
      const items = Array.isArray(props.data) ? props.data : Object.keys(props.data).length ? [props.data] : [];
      return items;
    });

    const handleSelectOid = (oid) => {
      selectedOid.value = oid;
      emit('select-oid', oid);
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
  background: linear-gradient(135deg, #676790, #2c2c54); /* Deep, cosmic gradient background */
  min-height: 100vh; /* Full viewport height for immersion */
  border-radius: 12px; /* Smooth, modern corners */
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.3), inset 0 0 10px rgba(255, 255, 255, 0.1); /* Dramatic shadow and inner glow */
  position: relative;
  overflow: hidden; /* Contain pseudo-elements */
  font-family: 'Poppins', 'Segoe UI', sans-serif; /* Premium, modern font */
}
.mib-tree::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: radial-gradient(circle, rgba(0, 123, 255, 0.2), transparent 70%); /* Subtle blue glow effect */
  opacity: 0.3;
  pointer-events: none; /* Allow interaction through overlay */
}
ul {
  list-style: none;
  padding: 0;
  margin: 0;
  position: relative;
}
li {
  margin: 10px 0;
  padding-left: 15px; /* Space for hierarchy lines */
  position: relative;
  transition: all 0.4s ease; /* Smooth transitions for all changes */
}
li:hover {
  transform: translateX(5px); /* Subtle slide effect on hover */
}
li::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  height: 100%;
  width: 2px;
  background: linear-gradient(to bottom, #00ddeb, #ff6f61); /* Vibrant gradient connector */
  opacity: 0.8;
  border-radius: 2px;
}
li:last-child::before {
  height: 50%; /* Shorten connector for last item */
}
</style>