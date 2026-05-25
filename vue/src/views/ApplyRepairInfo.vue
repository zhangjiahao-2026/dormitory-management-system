<template>
  <div class="student-page">
    <el-breadcrumb separator-icon="ArrowRight" class="page-breadcrumb">
      <el-breadcrumb-item>报修申请</el-breadcrumb-item>
    </el-breadcrumb>

    <el-card class="form-card">
      <div class="form-heading">
        <div>
          <p class="eyebrow">维修报修</p>
          <h2>填写报修内容</h2>
        </div>
        <el-tag effect="light">待提交</el-tag>
      </div>

      <el-form ref="form" :model="form" :rules="rules" label-width="112px" class="apply-form">
        <div class="form-grid">
          <el-form-item label="楼宇号" prop="dormBuildId">
            <el-input v-model="form.dormBuildId" disabled/>
          </el-form-item>
          <el-form-item label="房间号" prop="dormRoomId">
            <el-input v-model="form.dormRoomId" disabled/>
          </el-form-item>
          <el-form-item label="申请人" prop="repairer">
            <el-input v-model="form.repairer" disabled/>
          </el-form-item>
          <el-form-item label="创建时间" prop="orderBuildTime">
            <el-date-picker
                v-model="form.orderBuildTime"
                clearable
                placeholder="请选择创建时间"
                type="datetime"
                value-format="YYYY-MM-DD HH:mm:ss"
            />
          </el-form-item>
        </div>

        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" clearable maxlength="40" placeholder="请输入报修标题"/>
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input
              v-model="form.content"
              :autosize="{ minRows: 5, maxRows: 10 }"
              clearable
              placeholder="请描述需要维修的问题"
              type="textarea"
          />
        </el-form-item>

        <div class="form-actions">
          <el-button @click="resetApplyFields">重置</el-button>
          <el-button type="primary" @click="save">提交报修</el-button>
        </div>
      </el-form>
    </el-card>
  </div>
</template>
<script src="@/assets/js/ApplyRepairInfo.js"></script>

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

.apply-form :deep(.el-date-editor.el-input),
.apply-form :deep(.el-textarea),
.apply-form :deep(.el-input) {
  width: 100%;
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
