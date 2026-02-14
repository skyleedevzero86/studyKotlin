<script setup lang="ts">
</script>

<template>
  <div id="app">
    <router-view />
  </div>
</template>

<style>
:root {
  --bg: #0f0f12;
  --hover: #1a1a1f;
  --border: #2a2a32;
  --muted: #888;
  --primary: #6366f1;
  --danger: #ef4444;
  --warning: #eab308;
  --success: #16a34a;
  --pagination-bg: #1a1a1f;
  --pagination-text: #a1a1aa;
  --pagination-btn-bg: #25252b;
}
*,
*::before,
*::after {
  box-sizing: border-box;
}
body {
  margin: 0;
  font-family: 'DM Sans', system-ui, sans-serif;
  background: var(--bg);
  color: #e4e4e7;
  line-height: 1.5;
  -webkit-font-smoothing: antialiased;
}
#app {
  min-height: 100vh;
}
</style>
