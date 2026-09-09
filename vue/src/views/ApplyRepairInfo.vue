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

        <el-alert
            v-if="aiResult && aiResult.requires_human_review"
            :closable="false"
            class="ai-risk-alert"
            show-icon
            title="该申请需要宿管人工复核，AI 不会直接创建正式工单。"
            type="warning"
        />

        <el-card v-if="aiResult" class="ai-analysis-card" shadow="never">
          <template #header>
            <div class="analysis-header">
              <strong>AI 辅助分析</strong>
              <el-tag :type="urgencyTag(aiResult.urgency)">{{ urgencyName(aiResult.urgency) }}</el-tag>
            </div>
          </template>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="问题类别">{{ aiResult.category_name }}</el-descriptions-item>
            <el-descriptions-item label="置信度">{{ confidencePercent(aiResult.confidence) }}</el-descriptions-item>
            <el-descriptions-item label="建议部门">{{ aiResult.suggested_department_name || '待人工选择' }}</el-descriptions-item>
            <el-descriptions-item label="分析状态">{{ aiResult.status }}</el-descriptions-item>
          </el-descriptions>
          <div class="analysis-section">
            <h4>处理建议</h4>
            <ol v-if="aiResult.recommended_actions && aiResult.recommended_actions.length">
              <li v-for="action in aiResult.recommended_actions" :key="action">{{ action }}</li>
            </ol>
            <el-empty v-else :image-size="56" description="缺少可靠 SOP 依据，已转人工处理"/>
          </div>
          <div v-if="aiResult.review_reasons && aiResult.review_reasons.length" class="analysis-section">
            <h4>复核原因</h4>
            <el-tag v-for="reason in aiResult.review_reasons" :key="reason" class="reason-tag" type="danger">
              {{ reason }}
            </el-tag>
          </div>
          <div class="analysis-section">
            <h4>SOP 来源</h4>
            <el-table v-if="aiResult.sources && aiResult.sources.length" :data="aiResult.sources" size="small">
              <el-table-column label="文档" prop="document"/>
              <el-table-column label="章节" prop="section"/>
              <el-table-column label="相关度" width="100">
                <template #default="scope">{{ confidencePercent(scope.row.score) }}</template>
              </el-table-column>
            </el-table>
            <span v-else class="muted-text">没有检索到可引用的 SOP。</span>
          </div>
          <div class="analysis-feedback">
            <span>这次分析有帮助吗？</span>
            <el-button text type="success" @click="submitFeedback(aiResult.request_id, 'UP')">👍 有帮助</el-button>
            <el-button text type="danger" @click="openDownFeedback(aiResult.request_id)">👎 需改进</el-button>
          </div>
        </el-card>

        <div class="form-actions">
          <el-button @click="resetApplyFields">重置</el-button>
          <el-button :loading="aiLoading" type="primary" @click="analyzeAndSubmit">AI 分析并提交审核</el-button>
          <el-button :loading="manualLoading" type="success" plain @click="save">直接提交人工报修</el-button>
        </div>
      </el-form>
    </el-card>

    <el-card class="history-card">
      <template #header>
        <div class="analysis-header">
          <strong>我的 AI 报修申请</strong>
          <el-button text type="primary" @click="loadMine">刷新</el-button>
        </div>
      </template>
      <el-table :data="myRequests" empty-text="暂无 AI 报修申请">
        <el-table-column label="提交时间" prop="createdAt" width="170"/>
        <el-table-column label="标题" prop="title"/>
        <el-table-column label="类别" prop="category" width="150"/>
        <el-table-column label="紧急程度" prop="urgency" width="130"/>
        <el-table-column label="状态" width="120">
          <template #default="scope">
            <el-tag :type="requestStatusType(scope.row.status)">{{ requestStatusName(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="工单号" prop="repairId" width="100"/>
        <el-table-column label="审核备注" prop="operatorComment"/>
        <el-table-column label="反馈" width="150">
          <template #default="scope">
            <el-button text type="success" @click="submitFeedback(scope.row.requestId, 'UP')">赞</el-button>
            <el-button text type="danger" @click="openDownFeedback(scope.row.requestId)">踩</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="feedbackDialog" title="反馈 AI 分析问题" width="480px">
      <el-form label-width="90px">
        <el-form-item label="错误类型" required>
          <el-select v-model="feedbackForm.reason" style="width: 100%">
            <el-option v-for="item in feedbackReasons" :key="item.value" :label="item.label" :value="item.value"/>
          </el-select>
        </el-form-item>
        <el-form-item label="预期类别">
          <el-select v-model="feedbackForm.expected_category" clearable style="width: 100%">
            <el-option v-for="item in categoryOptions" :key="item.value" :label="item.label" :value="item.value"/>
          </el-select>
        </el-form-item>
        <el-form-item label="补充说明">
          <el-input v-model="feedbackForm.comment" :rows="3" maxlength="500" show-word-limit type="textarea"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="feedbackDialog = false">取消</el-button>
        <el-button type="primary" @click="submitDownFeedback">提交反馈</el-button>
      </template>
    </el-dialog>
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

.ai-risk-alert,
.ai-analysis-card,
.history-card {
  margin-top: 18px;
}

.analysis-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.analysis-section {
  margin-top: 18px;
}

.analysis-section h4 {
  margin: 0 0 10px;
}

.analysis-section ol {
  margin: 0;
  padding-left: 22px;
  line-height: 1.8;
}

.reason-tag {
  margin: 0 8px 8px 0;
}

.muted-text {
  color: var(--text-secondary);
}

.analysis-feedback {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px solid var(--border-color, #ebeef5);
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
