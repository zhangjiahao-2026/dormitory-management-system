<template>
  <el-container class="shell">
    <el-aside width="240px" class="shell-aside">
      <Aside/>
    </el-aside>
    <el-container class="shell-body">
      <el-header class="shell-header">
        <Header/>
      </el-header>
      <el-main class="shell-main">
        <router-view/>
      </el-main>
    </el-container>
  </el-container>
</template>

<script>
import Header from "@/components/Header";
import Aside from "@/components/Aside";

export default {
  name: "layout",
  components: { Header, Aside },
}
</script>

<style scoped>
.shell {
  height: 100vh;
  overflow: hidden;
}

.shell-aside {
  background: var(--sidebar-bg);
  position: relative;
  overflow: hidden;
}

/* noise grain overlay on sidebar */
.shell-aside::after {
  content: '';
  position: absolute;
  inset: 0;
  background: url("data:image/svg+xml,%3Csvg viewBox='0 0 256 256' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='n'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='.85' numOctaves='4' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23n)' opacity='.03'/%3E%3C/svg%3E");
  pointer-events: none;
  z-index: 1;
}

.shell-body {
  display: flex;
  flex-direction: column;
}

.shell-header {
  height: 60px !important;
  background: var(--bg-card);
  border-bottom: 1px solid var(--border-light);
  display: flex;
  align-items: center;
  padding: 0 28px;
  z-index: 10;
  flex-shrink: 0;
}

.shell-main {
  padding: 0;
  background: var(--bg-page);
  overflow-y: auto;
  flex: 1;
}
</style>
