<template>
  <nav class="sidebar">
    <!-- logo -->
    <div class="sidebar-brand">
      <div class="brand-icon">
        <img alt="" src="@/assets/logo.png"/>
      </div>
      <div class="brand-text">
        <span class="brand-name">宿舍管理系统</span>
        <span class="brand-sub">Dormitory Mgmt</span>
      </div>
    </div>

    <!-- navigation -->
    <el-menu
        :default-active="path"
        router
        class="sidebar-nav"
        unique-opened
        background-color="transparent"
        text-color="rgba(255,255,255,.72)"
        active-text-color="#ffffff"
    >
      <el-menu-item v-if="judgeIdentity()!==0" index="/home" class="nav-item">
        <el-icon><home-filled/></el-icon>
        <template #title>工作台</template>
      </el-menu-item>

      <div v-if="judgeIdentity()!==0" class="nav-label">管理</div>

      <el-sub-menu v-if="judgeIdentity()!==0" index="2">
        <template #title>
          <el-icon><user/></el-icon>
          <span>用户管理</span>
        </template>
        <el-menu-item v-if="judgeIdentity()!==0" index="/stuInfo">学生信息</el-menu-item>
        <el-menu-item v-if="judgeIdentity()===2" index="/dormManagerInfo">宿管信息</el-menu-item>
      </el-sub-menu>

      <el-sub-menu v-if="judgeIdentity()!==0" index="3">
        <template #title>
          <el-icon><office-building/></el-icon>
          <span>宿舍管理</span>
        </template>
        <el-menu-item v-if="judgeIdentity()!==0" index="/buildingInfo">楼宇信息</el-menu-item>
        <el-menu-item v-if="judgeIdentity()!==0" index="/roomInfo">房间信息</el-menu-item>
        <el-menu-item v-if="judgeIdentity()!==0" index="/utilityInfo">水电管理</el-menu-item>
      </el-sub-menu>

      <el-sub-menu v-if="judgeIdentity()!==0" index="4">
        <template #title>
          <el-icon><document/></el-icon>
          <span>信息管理</span>
        </template>
        <el-menu-item v-if="judgeIdentity()===2" index="/noticeInfo">公告信息</el-menu-item>
        <el-menu-item v-if="judgeIdentity()!==0" index="/repairInfo">报修信息</el-menu-item>
      </el-sub-menu>

      <el-sub-menu v-if="judgeIdentity()!==0" index="5">
        <template #title>
          <el-icon><operation/></el-icon>
          <span>申请管理</span>
        </template>
        <el-menu-item v-if="judgeIdentity()!==0" index="/adjustRoomInfo">调宿申请</el-menu-item>
      </el-sub-menu>

      <el-menu-item v-if="judgeIdentity()!==0" index="/visitorInfo" class="nav-item">
        <el-icon><user-filled/></el-icon>
        <template #title>访客管理</template>
      </el-menu-item>

      <el-menu-item v-if="judgeIdentity()!==0" index="/accessInfo" class="nav-item">
        <el-icon><connection/></el-icon>
        <template #title>出入管理</template>
      </el-menu-item>

      <!-- student menu -->
      <div v-if="judgeIdentity()===0" class="nav-label">我的</div>

      <el-menu-item v-if="judgeIdentity()===0" index="/myRoomInfo" class="nav-item">
        <el-icon><school/></el-icon>
        <template #title>我的宿舍</template>
      </el-menu-item>
      <el-menu-item v-if="judgeIdentity()===0" index="/applyChangeRoom" class="nav-item">
        <el-icon><sort/></el-icon>
        <template #title>申请调宿</template>
      </el-menu-item>
      <el-menu-item v-if="judgeIdentity()===0" index="/applyRepairInfo" class="nav-item">
        <el-icon><tools/></el-icon>
        <template #title>报修申请</template>
      </el-menu-item>

      <div class="nav-label">账户</div>
      <el-menu-item index="/selfInfo" class="nav-item">
        <el-icon><setting/></el-icon>
        <template #title>个人信息</template>
      </el-menu-item>
    </el-menu>
  </nav>
</template>

<script>
import request from "@/utils/request";
import {ElMessage} from "element-plus";

export default {
  name: "Aside",
  data() {
    return {
      user: {},
      identity: '',
      path: this.$route.path
    }
  },
  created() {
    this.init()
  },
  methods: {
    init() {
      request.get("/main/loadIdentity").then((res) => {
        if (res.code !== "0") {
          ElMessage({message: '用户会话过期', type: 'error'});
          sessionStorage.clear()
          request.get("/main/signOut");
        }
        window.sessionStorage.setItem("identity", JSON.stringify(res.data));
        this.identity = res.data
      });
      request.get("/main/loadUserInfo").then((result) => {
        if (result.code !== "0") {
          ElMessage({message: '用户会话过期', type: 'error'});
          request.get("/main/signOut");
          sessionStorage.clear()
          this.$router.replace({path: "/login"});
        }
        window.sessionStorage.setItem("user", JSON.stringify(result.data));
        this.user = result.data
      });
    },
    judgeIdentity() {
      if (this.identity === 'stu') return 0;
      else if (this.identity === 'dormManager') return 1;
      else return 2;
    }
  },
}
</script>

<style scoped>
.sidebar {
  height: 100%;
  display: flex;
  flex-direction: column;
  position: relative;
  z-index: 2;
}

.sidebar::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, rgba(255,255,255,.18), rgba(255,255,255,.04));
  pointer-events: none;
}

/* —— brand —— */
.sidebar-brand {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 16px 14px 14px;
  border-bottom: 1px solid rgba(71,158,170,.16);
  margin-bottom: 4px;
  position: relative;
}

.brand-icon {
  width: 34px;
  height: 34px;
  border-radius: 10px;
  background: linear-gradient(135deg, var(--accent), var(--accent-blue) 58%, var(--accent-peach));
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 10px 24px rgba(35,183,164,.28);
}

.brand-icon img {
  width: 20px;
  height: 20px;
  filter: brightness(10);
}

.brand-text {
  display: flex;
  flex-direction: column;
}

.brand-name {
  color: #17323a;
  font-family: var(--font-display);
  font-weight: 700;
  font-size: 14px;
  letter-spacing: 0;
  line-height: 1.3;
}

.brand-sub {
  color: rgba(23,50,58,.56);
  font-size: 10px;
  letter-spacing: 0;
  text-transform: uppercase;
  font-weight: 500;
}

/* —— nav —— */
.sidebar-nav {
  flex: 1;
  padding: 6px 10px;
  overflow-y: auto;
}

.nav-label {
  padding: 16px 12px 5px;
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0;
  text-transform: uppercase;
  color: rgba(23,50,58,.45);
}

/* menu items */
.sidebar-nav :deep(.el-menu-item),
.sidebar-nav :deep(.el-sub-menu__title) {
  height: 34px;
  line-height: 34px;
  margin: 1px 0;
  border-radius: 8px;
  transition: all .2s var(--ease);
  font-size: 12px;
  color: rgba(23,50,58,.72) !important;
}

.sidebar-nav :deep(.el-menu-item:hover),
.sidebar-nav :deep(.el-sub-menu__title:hover) {
  background: rgba(35,183,164,.13) !important;
  color: #17323a !important;
}

.sidebar-nav :deep(.el-menu-item.is-active) {
  background: rgba(35,183,164,.24) !important;
  color: #0b756a !important;
  font-weight: 600;
  box-shadow: inset 0 0 0 1px rgba(35,183,164,.12), 0 8px 18px rgba(35,183,164,.14);
}

/* active indicator bar */
.sidebar-nav :deep(.el-menu-item.is-active)::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 18px;
  background: var(--accent);
  border-radius: 0 3px 3px 0;
}

/* sub-menu items */
.sidebar-nav :deep(.el-sub-menu .el-menu) {
  padding: 4px 0;
}

.sidebar-nav :deep(.el-sub-menu .el-menu-item) {
  height: 32px;
  line-height: 32px;
  padding-left: 40px !important;
  font-size: 12px;
  margin: 1px 4px;
}

.sidebar-nav :deep(.el-sub-menu .el-menu-item.is-active) {
  background: var(--sidebar-active) !important;
}
</style>
