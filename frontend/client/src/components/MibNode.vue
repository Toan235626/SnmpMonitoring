<template>
  <li>
    <span
      :class="{ 'node-selected': selectedOid === node.oid }"
      @click="handleNodeClick"
    >
      <i
        :class="node.expanded ? 'fas fa-chevron-down' : 'fas fa-chevron-right'"
      ></i>
      {{ node.name }} ({{ node.oid }})
    </span>
    <ul v-if="node.expanded && node.children && node.children.length">
      <MibNode
        v-for="child in node.children"
        :key="child.oid"
        :node="child"
        :selected-oid="selectedOid"
        @select-oid="$emit('select-oid', $event)"
      />
    </ul>
  </li>
</template>

<script>
export default {
  name: "MibNode",
  props: {
    node: {
      type: Object,
      required: true,
    },
    selectedOid: {
      type: String,
      required: true,
    },
  },
  emits: ["select-oid"],
  methods: {
    handleNodeClick() {
      this.$emit("select-oid", this.node.oid);
      if (this.node.children && this.node.children.length > 0) {
        this.node.expanded = !this.node.expanded;
      }
    },
  },
};
</script>

<style scoped>
span {
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 5px 10px;
  cursor: pointer;
  font-size: 12px;
  color: #333;
}

span:hover {
  background: #e0e0e0;
}

.node-selected {
  font-weight: bold;
  color: #007bff;
}

i {
  font-size: 12px;
}

ul {
  list-style: none;
  margin-left: 20px;
}
</style>
