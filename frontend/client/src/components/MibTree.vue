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
  padding: 10px;
  background: rgb(255, 255, 255);
}
ul {
  list-style: none;
  padding: 0;
}
</style>