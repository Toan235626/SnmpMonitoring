<template>
    <li>
      <span
        :class="{ 'node-selected': selectedOid === node.oid }"
        @click="handleNodeClick"
        @mouseover="showDetails = true"
        @mouseout="showDetails = false"
      >
        <i
          :class="node.expanded ? 'fas fa-chevron-down' : 'fas fa-chevron-right'"
        ></i>
        {{ node.name || 'Unnamed' }} ({{ node.oid || 'N/A' }})
        <span v-if="node.value && node.category === 'scalar'" class="value">
          : {{ node.value }}
        </span>
      </span>
      <div v-if="showDetails && (node.description || node.value)" class="details">
        <p v-if="node.description"><strong>Description:</strong> {{ node.description }}</p>
        <p v-if="node.value"><strong>Value:</strong> {{ node.value }}</p>
        <p v-if="node.syntax"><strong>Syntax:</strong> {{ node.syntax }}</p>
        <p v-if="node.access"><strong>Access:</strong> {{ node.access }}</p>
      </div>
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
  import { ref } from 'vue';
  
  export default {
    name: 'MibNode',
    props: {
      node: {
        type: Object,
        required: true,
      },
      selectedOid: {
        type: String,
        default: '',
      },
    },
    emits: ['select-oid'],
    setup(props) {
      console.log('MibNode - node:', props.node);
      if (!Object.prototype.hasOwnProperty.call(props.node, 'expanded')) {
        props.node.expanded = false;
      }
      const showDetails = ref(false);
  
      const handleNodeClick = () => {
        if (props.node.oid && props.node.category === 'scalar') {
          this.$emit('select-oid', props.node.oid);
        }
        if (props.node.children && props.node.children.length > 0) {
          props.node.expanded = !props.node.expanded;
          console.log('MibNode - expanded:', props.node.expanded, 'for node:', props.node.name);
        }
      };
  
      return {
        showDetails,
        handleNodeClick,
      };
    },
  };
  </script>
  
  <style scoped>
  li {
    border: 1px solid #ccc; /* Debug: viền để kiểm tra */
    margin: 2px 0;
  }
  span {
    display: flex;
    align-items: center;
    gap: 5px;
    padding: 5px 10px;
    cursor: pointer;
    font-size: 12px;
    color: #333;
    font-family: 'Courier New', Courier, monospace;
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
  .value {
    color: #666;
    font-size: 0.9em;
  }
  .details {
    padding: 5px 10px;
    background: #f9f9f9;
    border: 1px solid #ddd;
    border-radius: 4px;
    margin: 5px 10px;
    font-size: 11px;
    color: #555;
  }
  </style>