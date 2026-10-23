<template>
  <router-view />
</template>

<script setup>
/** 根组件：页面由路由与布局承载，品牌名统一「云农智诊」。 */
</script>

<style>
:root {
  --brand-green: #2d6a4f;
  --brand-deep: #1b4332;
  --brand-soft: #e8f5e9;
}
* { box-sizing: border-box; }
body {
  margin: 0;
  font-family: "Segoe UI", "PingFang SC", "Microsoft YaHei", sans-serif;
  color: var(--brand-deep);
  background: #f7faf7;
}
a { color: inherit; }
</style>
