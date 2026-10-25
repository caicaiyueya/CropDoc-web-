<template>
  <router-view />
</template>

<script setup>
/** 云农智诊根组件：普通页由浏览器自然滚动；问诊页自管高度 */
</script>

<style>
:root {
  --brand: #28b446;
  --brand-dark: #1f8f36;
  --brand-soft: #eaf8ee;
  --bg: #f7f8fa;
  --text: #1f2937;
  --muted: #6b7280;
  --card: #ffffff;
  --radius: 16px;
  --shadow: 0 8px 24px rgba(15, 23, 42, 0.06);
  --shadow-hover: 0 12px 28px rgba(40, 180, 70, 0.16);
  --nav-height: 68px;
  --chat-footer-height: 120px;
}
* { box-sizing: border-box; }
html,
body {
  margin: 0;
  min-height: 100%;
}
body {
  font-family: "Segoe UI", "PingFang SC", "Microsoft YaHei", sans-serif;
  color: var(--text);
  background: var(--bg);
  -webkit-font-smoothing: antialiased;
}
#app {
  min-height: 100%;
}
a { color: inherit; text-decoration: none; }
button { font-family: inherit; }
.el-button--primary {
  --el-button-bg-color: var(--brand);
  --el-button-border-color: var(--brand);
  --el-button-hover-bg-color: var(--brand-dark);
  --el-button-hover-border-color: var(--brand-dark);
}
</style>
