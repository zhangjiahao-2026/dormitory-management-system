<template>
  <div class="hdr">
    <div class="hdr-left">
      <Clock class="hdr-clock"/>
    </div>
    <div class="hdr-right">
      <el-dropdown trigger="click" @command="handleCommand">
        <div class="hdr-user">
          <div class="hdr-avatar">
            <el-icon :size="16"><user-filled/></el-icon>
          </div>
          <span class="hdr-name">个人中心</span>
          <el-icon class="hdr-arrow"><arrow-down /></el-icon>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="selfInfo">
              <el-icon :size="14"><user/></el-icon>
              <span style="margin-left:6px">个人信息</span>
            </el-dropdown-item>
            <el-dropdown-item command="logout" divided>
              <el-icon :size="14"><switch-button/></el-icon>
              <span style="margin-left:6px">退出登录</span>
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script>
import request from "@/utils/request";
import Clock from "@/components/Clock";
import {ElMessage} from "element-plus";

export default {
  name: "Header",
  components: { Clock },
  methods: {
    handleCommand(cmd) {
      if (cmd === 'selfInfo') {
        this.$router.push("/selfInfo");
      } else if (cmd === 'logout') {
        sessionStorage.clear();
        request.get("/main/signOut");
        ElMessage({ message: '已退出登录', type: 'success' });
        this.$router.replace({ path: '/login' });
      }
    }
  },
}
</script>

<style scoped>
.hdr {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  height: 100%;
}

.hdr-clock {
  font-size: 12px;
  color: var(--text-body);
  font-variant-numeric: tabular-nums;
  letter-spacing: 0;
  padding: 5px 12px;
  border-radius: 999px;
  background: rgba(255,255,255,.72);
  box-shadow: inset 0 0 0 1px rgba(35,183,164,.10);
}

.hdr-right {
  display: flex;
  align-items: center;
}

.hdr-user {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 13px 6px 8px;
  border-radius: 999px;
  cursor: pointer;
  transition: all .2s var(--ease);
  background: rgba(255,255,255,.68);
  box-shadow: inset 0 0 0 1px rgba(35,183,164,.10);
}

.hdr-user:hover {
  background: var(--accent-light);
  box-shadow: 0 8px 18px rgba(35,183,164,.12);
}

.hdr-avatar {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--accent), var(--accent-blue));
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 18px rgba(35,183,164,.22);
}

.hdr-name {
  font-size: 12px;
  color: var(--text-body);
  font-weight: 500;
}

.hdr-arrow {
  color: var(--text-muted);
  font-size: 12px;
  transition: transform .2s;
}

.hdr-user:hover .hdr-arrow {
  color: var(--accent);
}
</style>
