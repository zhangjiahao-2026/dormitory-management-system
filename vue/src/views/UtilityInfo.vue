<template>
  <div class="utility-page">
    <el-breadcrumb separator="/" class="utility-breadcrumb">
      <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item>宿舍管理</el-breadcrumb-item>
      <el-breadcrumb-item>水电管理</el-breadcrumb-item>
    </el-breadcrumb>

    <div class="utility-hero">
      <div>
        <div class="utility-kicker">Utility Monitor</div>
        <h2>宿舍水电管理</h2>
        <p>模拟采集宿舍水电数据，电量超过全局上限时生成告警。</p>
      </div>
      <div class="utility-actions">
        <el-button type="primary" :loading="simulating" @click="simulateCollect">
          <el-icon><cpu/></el-icon>
          模拟采集
        </el-button>
        <el-button v-if="isAdmin" @click="openConfig">
          <el-icon><setting/></el-icon>
          上限配置
        </el-button>
      </div>
    </div>

    <div class="utility-stats">
      <div class="utility-stat electric">
        <span>电量上限</span>
        <strong>{{ config.electricLimit || '--' }}</strong>
        <em>度</em>
      </div>
      <div class="utility-stat water">
        <span>水量提示上限</span>
        <strong>{{ config.waterLimit || '--' }}</strong>
        <em>吨</em>
      </div>
      <div class="utility-stat alert">
        <span>未处理告警</span>
        <strong>{{ alertTotal }}</strong>
        <em>条</em>
      </div>
    </div>

    <el-card class="utility-card">
      <div class="utility-toolbar">
        <div class="utility-search">
          <el-input
              v-model="search"
              placeholder="搜索宿舍号..."
              prefix-icon="Search"
              clearable
              class="utility-input"
          />
          <el-button type="primary" @click="load">
            <el-icon><search/></el-icon>
            搜索
          </el-button>
          <el-button @click="reset">
            <el-icon><refresh-left/></el-icon>
            重置
          </el-button>
        </div>
      </div>

      <el-table v-loading="loading" :data="tableData" stripe class="utility-table">
        <el-table-column label="#" type="index" width="56"/>
        <el-table-column label="宿舍号" prop="dormRoomId" sortable min-width="100"/>
        <el-table-column label="楼栋号" prop="dormBuildId" sortable min-width="100"/>
        <el-table-column label="电量(度)" min-width="110">
          <template #default="{ row }">
            <span :class="{ danger: row.electricOverLimit }">{{ formatValue(row.electricUsage) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="水量(吨)" min-width="110">
          <template #default="{ row }">
            <span :class="{ warn: row.waterOverLimit }">{{ formatValue(row.waterUsage) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" min-width="140">
          <template #default="{ row }">
            <el-tag v-if="row.electricOverLimit" type="danger">电量告警</el-tag>
            <el-tag v-else-if="row.waterOverLimit" type="warning">水量提示</el-tag>
            <el-tag v-else type="success">正常</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="采集来源" min-width="110">
          <template #default="{ row }">
            {{ row.collectSource === 'SCHEDULED' ? '自动采集' : row.collectSource === 'MANUAL' ? '手动模拟' : '--' }}
          </template>
        </el-table-column>
        <el-table-column label="采集时间" prop="collectTime" min-width="170"/>
      </el-table>

      <div class="utility-footer">
        <el-pagination
            v-model:currentPage="currentPage"
            :page-size="pageSize"
            :page-sizes="[10, 20]"
            :total="total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <el-card class="utility-card alert-card">
      <template #header>
        <div class="panel-title">
          <el-icon><warning/></el-icon>
          <span>未处理水电告警</span>
        </div>
      </template>
      <el-table v-loading="alertLoading" :data="alerts" stripe>
        <el-table-column label="宿舍号" prop="dormRoomId" min-width="90"/>
        <el-table-column label="楼栋号" prop="dormBuildId" min-width="90"/>
        <el-table-column label="类型" min-width="100">
          <template #default="{ row }">
            <el-tag :type="row.alertType === 'WATER' ? 'warning' : 'danger'">
              {{ alertTypeText(row.alertType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="触发值" prop="triggerValue" min-width="110"/>
        <el-table-column label="上限" prop="limitValue" min-width="90"/>
        <el-table-column label="告警时间" prop="createTime" min-width="170"/>
        <el-table-column label="状态" min-width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'HANDLED' ? 'success' : 'danger'">
              {{ row.status === 'HANDLED' ? '已处理' : '未处理' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="110">
          <template #default="{ row }">
            <el-button
                v-if="row.status !== 'HANDLED'"
                type="primary"
                size="small"
                @click="handleAlert(row.id)"
            >处理</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="utility-footer">
        <el-pagination
            v-model:currentPage="alertPage"
            :page-size="alertPageSize"
            :total="alertTotal"
            layout="total, prev, pager, next"
            @current-change="handleAlertPageChange"
        />
      </div>
    </el-card>

    <el-dialog v-model="configVisible" title="水电上限配置" width="420px">
      <el-form :model="configForm" label-width="130px">
        <el-form-item label="电量告警上限">
          <el-input v-model.number="configForm.electricLimit">
            <template #append>度</template>
          </el-input>
        </el-form-item>
        <el-form-item label="水量提示上限">
          <el-input v-model.number="configForm.waterLimit">
            <template #append>吨</template>
          </el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="configVisible = false">取消</el-button>
        <el-button type="primary" @click="saveConfig">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import request from "@/utils/request";
import {ElMessage} from "element-plus";

export default {
  name: "UtilityInfo",
  data() {
    return {
      loading: false,
      alertLoading: false,
      simulating: false,
      search: "",
      currentPage: 1,
      pageSize: 10,
      total: 0,
      tableData: [],
      alerts: [],
      alertPage: 1,
      alertPageSize: 10,
      alertTotal: 0,
      config: {},
      configForm: {
        electricLimit: 80,
        waterLimit: 20,
      },
      configVisible: false,
    };
  },
  created() {
    this.loadConfig();
    this.load();
    this.loadAlerts();
  },
  computed: {
    isAdmin() {
      return JSON.parse(sessionStorage.getItem("identity") || '""') === "admin";
    },
  },
  methods: {
    load() {
      this.loading = true;
      request.get("/utility/find", {
        params: {
          pageNum: this.currentPage,
          pageSize: this.pageSize,
          search: this.search,
        },
      }).then((res) => {
        if (res.code === "0") {
          this.tableData = res.data.records;
          this.total = res.data.total;
        }
      }).finally(() => {
        this.loading = false;
      });
    },
    loadAlerts() {
      this.alertLoading = true;
      request.get("/utility/alerts", {
        params: {
          pageNum: this.alertPage,
          pageSize: this.alertPageSize,
        },
      }).then((res) => {
        if (res.code === "0") {
          this.alerts = res.data.records;
          this.alertTotal = res.data.total;
        }
      }).finally(() => {
        this.alertLoading = false;
      });
    },
    loadConfig() {
      request.get("/utility/config").then((res) => {
        if (res.code === "0") {
          this.config = res.data;
          this.configForm = JSON.parse(JSON.stringify(res.data));
        }
      });
    },
    reset() {
      this.search = "";
      this.currentPage = 1;
      this.load();
    },
    simulateCollect() {
      this.simulating = true;
      request.post("/utility/simulate").then((res) => {
        if (res.code === "0") {
          ElMessage({message: `已模拟采集 ${res.data} 间宿舍`, type: "success"});
          this.load();
          this.loadAlerts();
        } else {
          ElMessage({message: res.msg, type: "error"});
        }
      }).finally(() => {
        this.simulating = false;
      });
    },
    openConfig() {
      if (!this.isAdmin) {
        ElMessage({message: "仅管理员可修改水电上限", type: "warning"});
        return;
      }
      this.configForm = JSON.parse(JSON.stringify(this.config));
      this.configVisible = true;
    },
    saveConfig() {
      if (!this.isAdmin) {
        ElMessage({message: "仅管理员可修改水电上限", type: "warning"});
        return;
      }
      request.put("/utility/config", this.configForm).then((res) => {
        if (res.code === "0") {
          ElMessage({message: "配置已保存", type: "success"});
          this.configVisible = false;
          this.loadConfig();
          this.load();
        } else {
          ElMessage({message: res.msg, type: "error"});
        }
      });
    },
    handleAlert(id) {
      request.put("/utility/alerts/" + id + "/handle").then((res) => {
        if (res.code === "0") {
          ElMessage({message: "告警已处理", type: "success"});
          this.loadAlerts();
        } else {
          ElMessage({message: res.msg, type: "error"});
        }
      });
    },
    handleSizeChange(pageSize) {
      this.pageSize = pageSize;
      this.load();
    },
    handleCurrentChange(pageNum) {
      this.currentPage = pageNum;
      this.load();
    },
    handleAlertPageChange(pageNum) {
      this.alertPage = pageNum;
      this.loadAlerts();
    },
    formatValue(value) {
      return value === null || value === undefined ? "--" : value;
    },
    alertTypeText(alertType) {
      return alertType === "WATER" ? "水量告警" : "电量告警";
    },
  },
};
</script>

<style scoped>
.utility-page {
  padding: 16px 20px 28px;
}

.utility-breadcrumb {
  margin-bottom: 12px;
}

.utility-hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  padding: 18px 22px;
  border-radius: 16px;
  background: linear-gradient(135deg, rgba(35,183,164,.18), rgba(90,167,255,.14), rgba(255,157,124,.12));
  border: 1px solid rgba(255,255,255,.72);
  box-shadow: var(--shadow-card);
  margin-bottom: 16px;
}

.utility-kicker {
  font-size: 11px;
  color: var(--accent);
  font-weight: 700;
  text-transform: uppercase;
}

.utility-hero h2 {
  margin: 2px 0 4px;
  color: var(--text-title);
  font-size: 22px;
}

.utility-hero p {
  color: var(--text-muted);
  font-size: 13px;
}

.utility-actions,
.utility-search {
  display: flex;
  align-items: center;
  gap: 10px;
}

.utility-stats {
  display: grid;
  grid-template-columns: repeat(3, minmax(160px, 1fr));
  gap: 14px;
  margin-bottom: 16px;
}

.utility-stat {
  padding: 16px 18px;
  border-radius: 14px;
  background: rgba(255,255,255,.9);
  box-shadow: var(--shadow-card);
}

.utility-stat span,
.utility-stat em {
  color: var(--text-muted);
  font-style: normal;
  font-size: 12px;
}

.utility-stat strong {
  display: inline-block;
  margin: 0 6px;
  font-size: 28px;
  color: var(--text-title);
}

.utility-card {
  margin-bottom: 16px;
}

.utility-toolbar {
  display: flex;
  justify-content: space-between;
  margin-bottom: 14px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--border-light);
}

.utility-input {
  width: 280px;
}

.utility-table :deep(.el-table__cell) {
  padding: 9px 0;
}

.utility-footer {
  display: flex;
  justify-content: flex-end;
  padding-top: 14px;
}

.panel-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 700;
}

.danger {
  color: #f0645d;
  font-weight: 700;
}

.warn {
  color: #d9901f;
  font-weight: 700;
}

@media (max-width: 900px) {
  .utility-hero,
  .utility-actions {
    align-items: stretch;
    flex-direction: column;
  }

  .utility-stats {
    grid-template-columns: 1fr;
  }
}
</style>
