<template>
  <div class="login-wrap">
    <!-- decorative left panel -->
    <div class="login-art">
      <div class="art-pattern"></div>
      <div class="art-content">
        <div class="art-icon">
          <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
            <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/>
            <polyline points="9 22 9 12 15 12 15 22"/>
          </svg>
        </div>
        <h1>高校宿舍<br/>管理系统</h1>
        <p>高效管理 · 贴心服务</p>
      </div>
      <div class="art-footer">
        Dormitory Management System v1.0
      </div>
    </div>

    <!-- right form panel -->
    <div class="login-form-side">
      <div class="login-card">
        <div class="login-header">
          <h2>欢迎回来</h2>
          <p>请登录您的账号以继续</p>
        </div>

        <el-form ref="form" :model="form" :rules="rules" size="large" class="form-fields">
          <el-form-item prop="username">
            <el-input v-model="form.username" placeholder="用户名" clearable>
              <template #prefix>
                <el-icon><user/></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item prop="password">
            <el-input v-model="form.password" placeholder="密码" show-password>
              <template #prefix>
                <el-icon><lock/></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item prop="identity">
            <div class="role-select">
              <div
                  class="role-chip"
                  :class="{ active: form.identity === 'stu' }"
                  @click="form.identity = 'stu'"
              >
                <el-icon :size="16"><user/></el-icon>
                学生
              </div>
              <div
                  class="role-chip"
                  :class="{ active: form.identity === 'dormManager' }"
                  @click="form.identity = 'dormManager'"
              >
                <el-icon :size="16"><avatar/></el-icon>
                宿管
              </div>
              <div
                  class="role-chip"
                  :class="{ active: form.identity === 'admin' }"
                  @click="form.identity = 'admin'"
              >
                <el-icon :size="16"><setting/></el-icon>
                管理员
              </div>
            </div>
          </el-form-item>

          <el-form-item>
            <el-button
                :disabled="!disabled"
                type="primary"
                class="login-btn"
                @click="login"
            >登 录</el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script src="@/assets/js/Login.js"></script>

<style scoped>
.login-wrap {
  position: fixed;
  inset: 0;
  display: flex;
}

/* —— left decorative panel —— */
.login-art {
  width: 44%;
  background: var(--sidebar-bg);
  position: relative;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  overflow: hidden;
}

.art-pattern {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(circle at 20% 80%, rgba(196,93,62,.12) 0%, transparent 50%),
    radial-gradient(circle at 80% 20%, rgba(232,168,138,.08) 0%, transparent 50%);
}

.art-pattern::after {
  content: '';
  position: absolute;
  inset: 0;
  background: url("data:image/svg+xml,%3Csvg viewBox='0 0 256 256' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='n'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='.75' numOctaves='4' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23n)' opacity='.04'/%3E%3C/svg%3E");
  pointer-events: none;
}

.art-content {
  position: relative;
  z-index: 1;
  text-align: center;
  padding: 40px;
}

.art-icon {
  width: 64px;
  height: 64px;
  border-radius: 16px;
  background: linear-gradient(135deg, var(--accent), #d4896e);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 32px;
  box-shadow: 0 8px 24px rgba(196,93,62,.35);
}

.art-content h1 {
  font-family: var(--font-display);
  font-size: 38px;
  font-weight: 900;
  color: rgba(255,255,255,.92);
  line-height: 1.3;
  margin-bottom: 16px;
  letter-spacing: 2px;
}

.art-content p {
  font-size: 15px;
  color: rgba(255,255,255,.4);
  letter-spacing: 4px;
}

.art-footer {
  position: absolute;
  bottom: 24px;
  color: rgba(255,255,255,.2);
  font-size: 11px;
  letter-spacing: 1px;
}

/* —— right form panel —— */
.login-form-side {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-page);
  padding: 40px;
}

.login-card {
  width: 100%;
  max-width: 400px;
}

.login-header {
  margin-bottom: 36px;
}

.login-header h2 {
  font-family: var(--font-display);
  font-size: 28px;
  font-weight: 700;
  color: var(--text-title);
  margin-bottom: 8px;
}

.login-header p {
  color: var(--text-muted);
  font-size: 14px;
}

/* inputs */
.form-fields .el-input :deep(.el-input__wrapper) {
  padding: 10px 16px;
  border-radius: var(--r-md);
  background: var(--bg-card);
  box-shadow: var(--shadow-xs), 0 0 0 1px var(--border) inset;
}

.form-fields .el-input :deep(.el-input__wrapper.is-focus) {
  box-shadow: var(--shadow-sm), 0 0 0 2px var(--accent) inset;
}

.form-fields .el-input :deep(.el-input__inner) {
  font-size: 14px;
}

.form-fields .el-input :deep(.el-input__prefix) {
  align-items: center;
}

.form-fields .el-input :deep(.el-input__prefix .el-icon) {
  display: flex;
  align-items: center;
  justify-content: center;
}

/* role selector */
.role-select {
  display: flex;
  gap: 10px;
  width: 100%;
}

.role-chip {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 10px 0;
  border: 1.5px solid var(--border);
  border-radius: var(--r-md);
  cursor: pointer;
  font-size: 13px;
  font-weight: 500;
  color: var(--text-muted);
  background: var(--bg-card);
  transition: all .2s var(--ease);
}

.role-chip:hover {
  border-color: var(--accent);
  color: var(--accent);
}

.role-chip.active {
  background: var(--accent);
  border-color: var(--accent);
  color: #fff;
  box-shadow: 0 4px 12px rgba(196,93,62,.3);
}

/* login button */
.login-btn {
  width: 100%;
  height: 48px;
  border-radius: var(--r-md) !important;
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 3px;
}
</style>
