<template>
  <li ref="nodeRef">
    <div class="node-container"
      @mouseenter="showDetails = true"
      @mouseleave="showDetails = false"
    >
      <span
        :class="{ 'node-selected': selectedOid === node.oid, 'node-highlighted': highlightedOid === node.oid }"
        @click="handleNodeClick"
      >
        <i
          :class="node.expanded ? 'fas fa-chevron-down' : 'fas fa-chevron-right'"
        ></i>
        {{ node.name || 'Unnamed' }} ({{ node.oid || 'N/A' }})
        <div v-if="node.value" class="value">
          {{ node.value }}
        </div>
      </span>
      <div v-if="showDetails && (node.description || node.value)" class="details">
        <p v-if="node.description"><strong>Description:</strong> {{ node.description }}</p>
        <p v-if="node.value"><strong>Value:</strong> {{ node.value }}</p>
        <p v-if="node.syntax"><strong>Syntax:</strong> {{ node.syntax }}</p>
        <p v-if="node.access"><strong>Access:</strong> {{ node.access }}</p>
      </div>
    </div>
    <ul v-if="node.expanded && node.children && node.children.length">
      <MibNode
        v-for="child in node.children"
        :key="child.oid"
        :node="child"
        :selected-oid="selectedOid"
        :highlighted-oid="highlightedOid"
        @select-oid="$emit('select-oid', $event)"
      />
    </ul>
  </li>
</template>

<script>
import { ref } from "vue";
import { mibTreeStore } from "@/stores/mibtree";

export default {
  name: "MibNode",
  props: {
    node: {
      type: Object,
      required: true,
    },
    selectedOid: {
      type: String,
      default: "",
    },
    highlightedOid: {
      type: String,
      default: "",
    },
  },
  emits: ["select-oid"],
  setup(props, { emit }) {
    if (!Object.prototype.hasOwnProperty.call(props.node, "expanded")) {
      props.node.expanded = false;
    }
    const showDetails = ref(false);
    const mibTree = mibTreeStore();
    const nodeRef = ref(null);

    const handleNodeClick = () => {
      if (props.node.oid && props.node.category === "scalar") {
        emit("select-oid", props.node.oid);
      }
      if (props.node.children && props.node.children.length > 0) {
        props.node.expanded = !props.node.expanded;
      }
      // Reset highlight nếu click vào node đang được highlight
      if (props.node.oid === mibTree.highlightedOid) {
        mibTree.resetHighlight();
      }
    };

    return {
      showDetails,
      handleNodeClick,
      nodeRef,
    };
  },
};
</script>

<style scoped>
.node-container {
  position: relative;
  display: inline-block;
}
li {
  margin: 8px 0;
  border: none;
  position: relative;
  padding-left: 10px;
  transition: all 0.3s ease;
}
li:before {
  content: "";
  position: absolute;
  left: 0;
  top: 0;
  height: 100%;
  width: 2px;
  background: linear-gradient(
    to bottom,
    #007bff,
    #00c4cc
  );
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
  background: linear-gradient(
    135deg,
    #2c3e50,
    #34495e
  );
  border-radius: 8px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.2);
  transition: all 0.3s ease;
  font-family: "Segoe UI", Tahoma, Geneva, Verdana, sans-serif;
}
span:hover {
  background: linear-gradient(
    135deg,
    #3498db,
    #2980b9
  );
  transform: translateY(-2px);
  box-shadow: 0 6px 12px rgba(0, 0, 0, 0.3);
}
.node-selected {
  font-weight: 600;
  color: #ffffff;
  background: linear-gradient(
    135deg,
    #2c3e50,
    #34495e
  );
  box-shadow: 0 0 15px rgba(0, 0, 0, 0.2);
  transform: scale(1.02);
}
.node-highlighted {
  background: #ffeb3b !important; /* Màu vàng sáng để highlight */
  color: #2c3e50 !important; /* Màu chữ tối để dễ đọc */
  box-shadow: 0 0 20px rgba(255, 235, 59, 0.5) !important;
}
i {
  font-size: 14px;
  color: #00c4cc;
  transition: transform 0.3s ease;
}
span:hover i {
  transform: rotate(90deg);
}
ul {
  list-style: none;
  margin-left: 25px;
  padding: 5px 0;
}
.value {
  font-weight: 500;
  color: #000000;
  background: linear-gradient(
    135deg,
    #ffffff,
    #ffffff
  );
  padding: 5px 10px;
  border-radius: 6px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}
.details {
  padding: 10px 15px;
  background: linear-gradient(
    145deg,
    #ffffff,
    #e6e6e6
  );
  margin: 8px 15px;
  font-size: 13px;
  color: #2c3e50;
  border-radius: 6px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.15);
  border-left: 4px solid #007bff;
  animation: fadeIn 0.3s ease-in;
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