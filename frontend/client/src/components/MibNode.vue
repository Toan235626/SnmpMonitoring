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
    margin: 8px 0;
    border: none; /* Remove default border for a cleaner look */
    position: relative;
    padding-left: 10px; /* Indent for hierarchy */
    transition: all 0.3s ease; /* Smooth transition for all changes */
  }
  li:before {
    content: '';
    position: absolute;
    left: 0;
    top: 0;
    height: 100%;
    width: 2px;
    background: linear-gradient(to bottom, #007bff, #00c4cc); /* Gradient line for hierarchy */
    opacity: 0.7;
  }
  span {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 10px 15px;
    cursor: pointer;
    font-size: 14px;
    color: #ffffff;
    background: linear-gradient(135deg, #2c3e50, #34495e); /* Dark gradient background */
    border-radius: 8px;
    box-shadow: 0 4px 6px rgba(0, 0, 0, 0.2);
    transition: all 0.3s ease; /* Smooth hover and active effects */
    font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; /* Modern font */
  }
  span:hover {
    background: linear-gradient(135deg, #3498db, #2980b9); /* Vibrant hover gradient */
    transform: translateY(-2px); /* Lift effect on hover */
    box-shadow: 0 6px 12px rgba(0, 0, 0, 0.3);
  }
  .node-selected {
    font-weight: 600;
    color: #ffffff; /* Gold for selected node */
    background: linear-gradient(135deg, #2c3e50, #34495e); /* Fiery gradient for selected */
    box-shadow: 0 0 15px rgba(0, 0, 0, 0.2); /* Glow effect */
    transform: scale(1.02); /* Slight scale for emphasis */
  }
  i {
    font-size: 14px;
    color: #00c4cc; /* Cyan for icons */
    transition: transform 0.3s ease; /* Smooth icon rotation */
  }
  span:hover i {
    transform: rotate(90deg); /* Rotate chevron on hover for dynamic feel */
  }
  ul {
    list-style: none;
    margin-left: 25px;
    padding: 5px 0;
  }
  .value {
    color: #00ff88; /* Neon green for values */
    font-size: 1em;
    font-style: italic;
    background: rgba(0, 255, 136, 0.1); /* Subtle glow background */
    padding: 2px 6px;
    border-radius: 4px;
  }
  .details {
    padding: 10px 15px;
    background: linear-gradient(145deg, #ffffff, #e6e6e6); /* Subtle white gradient */
    margin: 8px 15px;
    font-size: 13px;
    color: #2c3e50; /* Darker text for contrast */
    border-radius: 6px;
    box-shadow: 0 4px 10px rgba(0, 0, 0, 0.15);
    border-left: 4px solid #007bff; /* Blue accent border */
    animation: fadeIn 0.3s ease-in; /* Fade-in animation */
  }
  @keyframes fadeIn {
    from {
      opacity: 0;
      transform: translateY(10px);
    }
    to {
      opacity: 1;
      transform: translateY(0);
    }
  }
</style>