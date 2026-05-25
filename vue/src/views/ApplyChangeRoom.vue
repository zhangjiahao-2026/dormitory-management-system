<template>
  <div class="student-page">
    <el-breadcrumb separator-icon="ArrowRight" class="page-breadcrumb">
      <el-breadcrumb-item>申请调宿</el-breadcrumb-item>
    </el-breadcrumb>

    <el-card class="form-card">
      <div class="form-heading">
        <div>
          <p class="eyebrow">换宿申请</p>
          <h2>填写目标房间与床位</h2>
        </div>
        <el-tag effect="light">待提交</el-tag>
      </div>

      <el-form ref="form" :model="form" :rules="rules" label-width="112px" class="apply-form">
        <div class="form-grid">
          <el-form-item label="学号" prop="username">
            <el-input v-model="form.username" disabled/>
          </el-form-item>
          <el-form-item label="姓名" prop="name">
            <el-input v-model="form.name" disabled/>
          </el-form-item>
          <el-form-item label="当前房间号" prop="currentRoomId">
            <el-input v-model="form.currentRoomId" disabled/>
          </el-form-item>
          <el-form-item label="当前床位号" prop="currentBedId">
            <el-input v-model="form.currentBedId" disabled/>
          </el-form-item>
          <el-form-item label="目标房间号" prop="towardsRoomId">
            <el-input v-model.number="form.towardsRoomId" clearable placeholder="请输入目标房间号"/>
          </el-form-item>
          <el-form-item label="目标床位号" prop="towardsBedId">
            <el-input v-model.number="form.towardsBedId" clearable placeholder="请输入目标床位号"/>
          </el-form-item>
        </div>

        <el-form-item label="申请时间" prop="applyTime">
          <el-date-picker
              v-model="form.applyTime"
              clearable
              placeholder="请选择申请时间"
              type="datetime"
              value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>

        <div class="form-actions">
          <el-button @click="resetApplyFields">重置</el-button>
          <el-button type="primary" @click="save">提交申请</el-button>
        </div>
      </el-form>
    </el-card>
  </div>
</template>
<script src="@/assets/js/ApplyChangeRoom.js"></script>

<style scoped>
.student-page {
  padding: 20px;
}

.page-breadcrumb {
  margin-bottom: 16px;
}

.form-card {
  min-height: calc(100vh - 120px);
  border-radius: 8px;
}

.form-heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 24px;
  padding-bottom: 18px;
  border-bottom: 1px solid rgba(215, 236, 234, .86);
}

.eyebrow {
  margin: 0 0 6px;
  color: var(--text-secondary);
  font-size: 12px;
}

.form-heading h2 {
  margin: 0;
  color: var(--text-primary);
  font-size: 24px;
}

.apply-form {
  max-width: 820px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(240px, 1fr));
  gap: 4px 18px;
}

.apply-form :deep(.el-date-editor.el-input) {
  width: 100%;
  max-width: 360px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  max-width: 820px;
  padding-top: 8px;
}

@media (max-width: 760px) {
  .form-grid {
    grid-template-columns: 1fr;
  }

  .form-heading {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
