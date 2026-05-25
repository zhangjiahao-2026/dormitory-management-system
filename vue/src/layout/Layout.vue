<template>
  <el-container class="shell">
    <el-aside width="184px" class="shell-aside">
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
  background:
      linear-gradient(180deg, rgba(233,251,255,.86), rgba(214,245,239,.82)),
      url("~@/assets/images/campus-bg.png") left center / cover no-repeat;
  position: relative;
  overflow: hidden;
}

/* noise grain overlay on sidebar */
.shell-aside::after {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, rgba(255,255,255,.16), rgba(255,255,255,0));
  pointer-events: none;
  z-index: 1;
}

.shell-body {
  display: flex;
  flex-direction: column;
}

.shell-header {
  height: 54px !important;
  background:
      linear-gradient(180deg, rgba(224,249,255,.92), rgba(255,255,255,.88)),
      url("~@/assets/images/campus-bg.png") top right / cover no-repeat;
  backdrop-filter: blur(16px);
  border-bottom: 1px solid rgba(215,236,234,.72);
  display: flex;
  align-items: center;
  padding: 0 26px;
  z-index: 10;
  flex-shrink: 0;
  box-shadow: 0 10px 28px rgba(20,83,92,.05);
}

.shell-main {
  padding: 0;
  background:
      linear-gradient(180deg, rgba(255,255,255,.78), rgba(255,255,255,.64)),
      url("~@/assets/images/campus-bg.png") center / cover no-repeat;
  overflow-y: auto;
  flex: 1;
}
</style>
