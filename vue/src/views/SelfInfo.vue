<template>
  <div class="crud">
    <el-breadcrumb separator="/" class="crud-breadcrumb">
      <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item>个人信息</el-breadcrumb-item>
    </el-breadcrumb>

    <el-card class="crud-card">
      <div class="profile-hero">
        <!-- avatar -->
        <el-upload
            :on-success="uploadSuccess"
            :show-file-list="false"
            action="/api/files/upload"
            class="avatar-upload"
        >
          <div class="avatar-ring">
            <div class="avatar-inner">
              <img v-if="image" :src="'data:image;base64,' + image" class="avatar-img"/>
              <el-icon v-else :size="36" color="var(--accent)"><user-filled/></el-icon>
            </div>
            <div class="avatar-hover">
              <el-icon :size="18"><camera/></el-icon>
            </div>
          </div>
        </el-upload>
        <div class="profile-name">{{ name }}</div>
        <div class="profile-role">
          <span class="role-badge" :class="identityClass">{{ identityLabel }}</span>
        </div>
      </div>

      <div class="info-grid">
        <div class="info-item" v-for="item in infoList" :key="item.label">
          <div class="info-icon">
            <el-icon :size="16"><component :is="item.icon"/></el-icon>
          </div>
          <div class="info-body">
            <span class="info-label">{{ item.label }}</span>
            <span class="info-value">{{ item.value || '—' }}</span>
          </div>
        </div>
      </div>

      <div class="profile-actions">
        <el-button type="primary" @click="Edit" :icon="'edit'">编辑信息</el-button>
      </div>

      <!-- edit dialog -->
      <el-dialog
          v-model="dialogVisible"
          title="编辑个人信息"
          width="480px"
          @close="cancel"
      >
        <el-form ref="form" :model="form" :rules="rules" label-width="90px">
          <el-form-item label="账号" prop="username">
            <el-input v-model="form.username" disabled/>
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input v-model="form.password" :disabled="disabled" :show-password="showpassword">
              <template #suffix>
                <el-tooltip content="修改密码" placement="right">
                  <el-icon class="edit-pwd" @click="EditPass"><edit/></el-icon>
                </el-tooltip>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item :style="display" label="确认密码" prop="checkPass">
            <el-input v-model="form.checkPass" :show-password="showpassword"/>
          </el-form-item>
          <el-form-item label="姓名" prop="name">
            <el-input v-model="form.name"/>
          </el-form-item>
          <el-form-item label="性别" prop="gender">
            <el-radio v-model="form.gender" label="男">男</el-radio>
            <el-radio v-model="form.gender" label="女">女</el-radio>
          </el-form-item>
          <el-form-item label="年龄" prop="age">
            <el-input v-model.number="form.age"/>
          </el-form-item>
          <el-form-item label="手机号" prop="phoneNum">
            <el-input v-model.number="form.phoneNum"/>
          </el-form-item>
          <el-form-item label="邮箱" prop="email">
            <el-input v-model="form.email"/>
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="cancel">取 消</el-button>
          <el-button type="primary" @click="save">确 定</el-button>
        </template>
      </el-dialog>
    </el-card>
  </div>
</template>

<script src="@/assets/js/SelfInfo.js"></script>

<style scoped>
.crud {
  padding: 24px;
}

.crud-breadcrumb {
  margin-bottom: 16px;
}

.crud-card {
  min-height: calc(100vh - 128px);
}

/* —— hero —— */
.profile-hero {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40px 0 32px;
  border-bottom: 1px solid var(--border-light);
  margin-bottom: 32px;
}

.avatar-upload {
  display: flex;
  justify-content: center;
}

.avatar-ring {
  position: relative;
  width: 100px;
  height: 100px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--accent), #d4896e);
  padding: 3px;
  cursor: pointer;
  transition: transform .3s var(--ease), box-shadow .3s var(--ease);
}

.avatar-ring:hover {
  transform: scale(1.06);
  box-shadow: 0 8px 24px rgba(196,93,62,.3);
}

.avatar-inner {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background: var(--bg-card);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-hover {
  position: absolute;
  inset: 0;
  border-radius: 50%;
  background: rgba(30,26,23,.55);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  opacity: 0;
  transition: opacity .25s;
}

.avatar-ring:hover .avatar-hover {
  opacity: 1;
}

.profile-name {
  margin-top: 18px;
  font-family: var(--font-display);
  font-size: 22px;
  font-weight: 700;
  color: var(--text-title);
}

.profile-role {
  margin-top: 8px;
}

.role-badge {
  display: inline-block;
  padding: 3px 14px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: .5px;
}

.role-stu {
  background: rgba(196,93,62,.08);
  color: var(--accent);
}

.role-dormManager {
  background: rgba(45,125,111,.08);
  color: #2d7d6f;
}

.role-admin {
  background: rgba(107,91,149,.08);
  color: #6b5b95;
}

/* —— info grid —— */
.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 16px;
  max-width: 720px;
  margin: 0 auto 32px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px 18px;
  background: var(--bg-page);
  border-radius: var(--r-md);
  transition: box-shadow .25s var(--ease), transform .25s var(--ease);
}

.info-item:hover {
  box-shadow: var(--shadow-sm);
  transform: translateY(-1px);
}

.info-icon {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: linear-gradient(135deg, var(--accent), #d4896e);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.info-body {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.info-label {
  font-size: 11px;
  color: var(--text-muted);
  font-weight: 500;
  letter-spacing: .5px;
  text-transform: uppercase;
  margin-bottom: 2px;
}

.info-value {
  font-size: 14px;
  color: var(--text-title);
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* —— actions —— */
.profile-actions {
  display: flex;
  justify-content: center;
  padding-bottom: 8px;
}

.profile-actions .el-button {
  min-width: 160px;
  height: 42px;
  border-radius: var(--r-md);
  font-weight: 600;
  letter-spacing: 1px;
}

/* —— dialog —— */
.edit-pwd {
  cursor: pointer;
  color: var(--accent);
  transition: color .2s;
}

.edit-pwd:hover {
  color: var(--accent-hover);
}
</style>
