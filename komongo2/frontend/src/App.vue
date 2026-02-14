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
