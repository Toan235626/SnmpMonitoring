<template>
  <div>
    <h3>MIB Tree</h3>
    <v-alert v-if="!data || !Object.keys(data).length" type="info">
      No MIB data available.
    </v-alert>

    <v-treeview v-else :items="treeItems" item-key="oid" item-text="oid" activatable>
      <template v-slot:label="{ item }">
        {{ item.oid }} ({{ item.value || 'No value' }})
      </template>
    </v-treeview>
  </div>
</template>

<script>

export default {
};
</script>

<style scoped>
div {
  padding: 20px;
}
</style>