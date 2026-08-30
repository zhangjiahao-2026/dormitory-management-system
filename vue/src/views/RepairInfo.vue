<template>
  <div>
    <el-breadcrumb separator-icon="ArrowRight" style="margin: 16px">
      <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item>信息管理</el-breadcrumb-item>
      <el-breadcrumb-item>报修信息</el-breadcrumb-item>
    </el-breadcrumb>
    <el-card class="ai-review-card">
      <template #header>
        <div class="ai-panel-header">
          <div>
            <strong>AI 报修待审核</strong>
            <span class="ai-panel-tip">AI 仅提供辅助分析，确认后才创建正式工单</span>
          </div>
          <el-button :loading="aiLoading" type="primary" plain @click="refreshAiPanel">刷新</el-button>
        </div>
      </template>
      <el-row :gutter="12" class="metrics-row">
        <el-col :span="4"><el-statistic title="分析请求" :value="metrics.totalRequests || 0"/></el-col>
        <el-col :span="4"><el-statistic title="成功率" :value="metricPercent(metrics.successRate)" suffix="%"/></el-col>
        <el-col :span="4"><el-statistic title="平均延迟" :value="metrics.averageLatencyMs || 0" suffix="ms"/></el-col>
        <el-col :span="4"><el-statistic title="结构化输出" :value="metricPercent(metrics.structuredOutputRate)" suffix="%"/></el-col>
        <el-col :span="4"><el-statistic title="人工复核" :value="metricPercent(metrics.humanReviewRate)" suffix="%"/></el-col>
        <el-col :span="4"><el-statistic title="正向反馈" :value="metricPercent(metrics.positiveFeedbackRate)" suffix="%"/></el-col>
      </el-row>
      <div class="ai-search-row">
        <el-input v-model="aiSearch" clearable placeholder="搜索标题或问题描述" @keyup.enter="loadAiPending"/>
        <el-button icon="Search" type="primary" @click="loadAiPending">查询</el-button>
      </div>
      <el-table v-loading="aiLoading" :data="aiPending" empty-text="暂无待审核 AI 申请">
        <el-table-column label="提交时间" prop="createdAt" width="165"/>
        <el-table-column label="楼栋" prop="dormBuildId" width="80"/>
        <el-table-column label="房间" prop="dormRoomId" width="80"/>
        <el-table-column label="申请人" prop="applicantName" width="100"/>
        <el-table-column label="标题" prop="title"/>
        <el-table-column label="AI 类别" prop="category" width="145"/>
        <el-table-column label="紧急程度" prop="urgency" width="110"/>
        <el-table-column label="置信度" width="90">
          <template #default="scope">{{ confidencePercent(scope.row.confidence) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="110">
          <template #default="scope">
            <el-button size="small" type="primary" @click="openAiReview(scope.row)">审核</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
          v-if="aiTotal > aiPageSize"
          v-model:currentPage="aiPageNum"
          :page-size="aiPageSize"
          :total="aiTotal"
          layout="total, prev, pager, next"
          @current-change="loadAiPending"
      />
    </el-card>
    <el-card style="margin: 15px; min-height: calc(100vh - 111px)">
      <div>
        <!--    功能区-->
        <div style="margin: 10px 0">
          <!--    搜索区-->
          <div style="margin: 10px 0">
            <el-input v-model="search" clearable placeholder="请输入标题" prefix-icon="Search" style="width: 20%"/>
            <el-button icon="Search" style="margin-left: 5px" type="primary" @click="load"></el-button>
            <el-button icon="refresh-left" style="margin-left: 10px" type="default" @click="reset"></el-button>
            <div style="float: right">
              <el-tooltip content="添加" placement="top">
                <el-button icon="plus" style="width: 50px" type="primary" @click="add"
                ></el-button>
              </el-tooltip>
            </div>
          </div>
        </div>
        <!--    表格-->
        <el-table v-loading="loading" :data="tableData" border max-height="705" style="width: 100%">
          <el-table-column label="#" type="index"/>
          <el-table-column :show-overflow-tooltip="true" label="标题" prop="title"/>
          <el-table-column label="宿舍号" prop="dormBuildId" sortable width="150px"/>
          <el-table-column label="房间号" prop="dormRoomId" sortable width="150px"/>
          <el-table-column label="申请人" prop="repairer" width="150px"/>
          <el-table-column
              :filter-method="filterTag"
              :filters="[
              { text: '完成', value: '完成' },
              { text: '未完成', value: '未完成' },
            ]"
              filter-placement="bottom-end"
              label="订单状态"
              prop="state"
              sortable
          >
            <template #default="scope">
              <el-tag
                  :type="scope.row.state === '完成' ? 'success' : 'info'"
                  disable-transitions
              >{{ scope.row.state }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="订单创建时间" prop="orderBuildTime" sortable/>
          <el-table-column label="订单完成时间" prop="orderFinishTime" sortable/>
          <!--      操作栏-->
          <el-table-column label="操作" width="190px">
            <template #default="scope">
              <el-button icon="more-filled" type="default" @click="showDetail(scope.row)"></el-button>
              <el-button icon="Edit" type="primary" @click="handleEdit(scope.row)"></el-button>
              <el-popconfirm title="确认删除？" @confirm="handleDelete(scope.row.id)">
                <template #reference>
                  <el-button icon="Delete" type="danger"></el-button>
                </template>
              </el-popconfirm>
            </template>
          </el-table-column>
        </el-table>
        <!--分页-->
        <div style="margin: 10px 0">
          <el-pagination
              v-model:currentPage="currentPage"
              :page-size="pageSize"
              :page-sizes="[10, 20]"
              :total="total"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
          >
          </el-pagination>
        </div>
        <!--      弹窗-->
        <div>
          <el-dialog v-model="dialogVisible" class="repair-edit-dialog" title="工单信息" width="720px" @close="cancel">
            <el-form ref="form" :model="form" :rules="rules" class="repair-edit-form" label-width="110px">
              <el-form-item label="标题" prop="title">
                <el-input v-model="form.title" clearable></el-input>
              </el-form-item>
              <el-form-item label="楼宇号" prop="dormBuildId">
                <el-input v-model="form.dormBuildId" clearable></el-input>
              </el-form-item>
              <el-form-item label="房间号" prop="dormRoomId">
                <el-input v-model="form.dormRoomId" clearable></el-input>
              </el-form-item>
              <el-form-item label="申请人" prop="repairer">
                <el-input v-model="form.repairer" clearable></el-input>
              </el-form-item>
              <el-form-item label="内容" prop="content">
                <el-input
                    v-model="form.content"
                    :autosize="{ minRows: 4, maxRows: 10 }"
                    autosize
                    clearable
                    type="textarea"
                ></el-input>
              </el-form-item>
              <el-form-item :style="finishTime" label="订单状态" prop="state">
                <el-radio v-model="form.state" label="完成">完成</el-radio>
                <el-radio v-model="form.state" label="未完成">未完成</el-radio>
              </el-form-item>
              <el-form-item label="订单创建时间" prop="orderBuildTime">
                <el-date-picker
                    v-model="form.orderBuildTime"
                    :disabled="buildTimeDisabled"
                    clearable
                    placeholder="选择时间"
                    type="datetime"
                    value-format="YYYY-MM-DD HH:mm:ss"
                ></el-date-picker>
              </el-form-item>
              <el-form-item :style="finishTime" label="订单完成时间" prop="orderFinishTime">
                <el-date-picker
                    v-model="form.orderFinishTime"
                    clearable
                    placeholder="选择时间"
                    type="datetime"
                    value-format="YYYY-MM-DD HH:mm:ss"
                ></el-date-picker>
              </el-form-item>
            </el-form>
            <template #footer>
              <span class="dialog-footer">
                <el-button @click="cancel">取 消</el-button>
                <el-button type="primary" @click="save">确 定</el-button>
              </span>
            </template>
          </el-dialog>
          <!--   内容详情弹窗-->
          <el-dialog v-model="detailDialog" class="repair-detail-dialog" title="工单详情" width="640px">
            <el-card>
              <div v-html="detail.content"></div>
            </el-card>
            <template #footer>
              <span class="dialog-footer">
                <el-button type="primary" @click="closeDetails">确 定</el-button>
              </span>
            </template>
          </el-dialog>
          <el-dialog v-model="aiDialog" title="AI 报修人工审核" width="720px">
            <template v-if="aiDetail.requestId">
              <el-alert
                  v-if="parseJson(aiDetail.reviewReasons).length"
                  :closable="false"
                  show-icon
                  title="该分析存在需要人工确认的风险"
                  type="warning"
              />
              <el-descriptions :column="2" border class="ai-detail-block">
                <el-descriptions-item label="宿舍">{{ aiDetail.dormBuildId }} 栋 {{ aiDetail.dormRoomId }}</el-descriptions-item>
                <el-descriptions-item label="申请人">{{ aiDetail.applicantName }}</el-descriptions-item>
                <el-descriptions-item label="标题">{{ aiDetail.title }}</el-descriptions-item>
                <el-descriptions-item label="置信度">{{ confidencePercent(aiDetail.confidence) }}</el-descriptions-item>
                <el-descriptions-item :span="2" label="问题描述">{{ aiDetail.content }}</el-descriptions-item>
              </el-descriptions>
              <div class="ai-detail-block">
                <strong>处理建议</strong>
                <ol><li v-for="item in parseJson(aiDetail.recommendedActions)" :key="item">{{ item }}</li></ol>
              </div>
              <div class="ai-detail-block">
                <strong>SOP 来源</strong>
                <el-table :data="parseJson(aiDetail.sources)" size="small">
                  <el-table-column label="文档" prop="document"/>
                  <el-table-column label="章节" prop="section"/>
                  <el-table-column label="相关度" width="90">
                    <template #default="scope">{{ confidencePercent(scope.row.score) }}</template>
                  </el-table-column>
                </el-table>
              </div>
              <el-form :model="aiDecision" label-width="100px">
                <el-form-item label="确认类别">
                  <el-select v-model="aiDecision.category" style="width: 100%">
                    <el-option v-for="item in categoryOptions" :key="item.value" :label="item.label" :value="item.value"/>
                  </el-select>
                </el-form-item>
                <el-form-item label="紧急程度">
                  <el-select v-model="aiDecision.urgency" style="width: 100%">
                    <el-option v-for="item in urgencyOptions" :key="item.value" :label="item.label" :value="item.value"/>
                  </el-select>
                </el-form-item>
                <el-form-item label="处理部门">
                  <el-select v-model="aiDecision.department" style="width: 100%">
                    <el-option v-for="item in departmentOptions" :key="item.value" :label="item.label" :value="item.value"/>
                  </el-select>
                </el-form-item>
                <el-form-item label="审核备注">
                  <el-input v-model="aiDecision.operatorComment" :rows="3" maxlength="500" show-word-limit type="textarea"/>
                </el-form-item>
              </el-form>
            </template>
            <template #footer>
              <el-button type="danger" plain @click="rejectAi">拒绝申请</el-button>
              <el-button @click="aiDialog = false">取消</el-button>
              <el-button :loading="aiSubmitting" type="primary" @click="confirmAi">确认创建工单</el-button>
            </template>
          </el-dialog>
        </div>
      </div>
    </el-card>
  </div>
</template>
<script src="@/assets/js/RepairInfo.js"></script>

<style scoped>
.ai-review-card {
  margin: 15px;
}

.ai-panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.ai-panel-tip {
  margin-left: 12px;
  color: var(--text-secondary);
  font-size: 12px;
}

.ai-search-row {
  display: flex;
  gap: 8px;
  width: 420px;
  margin-bottom: 12px;
}

.metrics-row {
  margin-bottom: 18px;
}

.ai-detail-block {
  margin-top: 16px;
}

.ai-detail-block ol {
  line-height: 1.8;
}

:deep(.repair-edit-dialog),
:deep(.repair-detail-dialog) {
  max-width: calc(100vw - 32px);
}

.repair-edit-form :deep(.el-input),
.repair-edit-form :deep(.el-textarea),
.repair-edit-form :deep(.el-date-editor) {
  width: 100%;
}
</style>
