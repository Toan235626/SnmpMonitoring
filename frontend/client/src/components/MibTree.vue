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
      console.log('MibTree - props.data:', props.data);
      console.log('MibTree - treeItems:', items);
      return items;
    });

    const handleSelectOid = (oid) => {
      selectedOid.value = oid;
      emit('select-oid', oid);
      console.log('MibTree - emitted OID:', oid);
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
  padding: 10px;
  font-family: 'Courier New', Courier, monospace;
  background: #ff0; /* Debug: màu vàng để dễ thấy */
}
ul {
  list-style: none;
  padding: 0;
  border: 1px solid #000; /* Debug: viền để kiểm tra */
}
</style>