<template>
  <div class="mib-tree">
    <v-treeview
      :items="treeItems"
      item-key="oid"
      item-text="name"
      item-children="children"
      activatable
      dense
    >
      <template v-slot:label="{ item }">
        <div>
          <span>{{ item.name }}</span>
          <span v-if="item.value" class="value"> ({{ item.value }})</span>
        </div>
      </template>
    </v-treeview>
  </div>
</template>

<script>
export default {
  props: {
    data: {
      type: [Object, Array],
      required: true,
    },
  },
  computed: {
    treeItems() {
      if (Array.isArray(this.data)) {
        return this.data;
      }
      return Object.keys(this.data).length ? [this.data] : [];
    },
  },
};
</script>

<style scoped>
.mib-tree {
  padding: 10px;
}
.value {
  color: #666;
  font-size: 0.9em;
}
</style>