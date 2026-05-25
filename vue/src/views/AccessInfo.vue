<template>
  <div class="access-page">
    <el-breadcrumb separator="/" class="access-breadcrumb">
      <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item>出入管理</el-breadcrumb-item>
    </el-breadcrumb>

    <div class="access-hero">
      <div>
        <div class="access-kicker">Access Control</div>
        <h2>学生返寝与大件物品登记</h2>
        <p>统一记录学生返寝情况，超过 23:00 自动生成晚归告警。</p>
      </div>
      <div class="access-summary">
        <span>未处理晚归</span>
        <strong>{{ alertTotal }}</strong>
      </div>
    </div>

    <el-tabs v-model="activeTab" class="access-tabs" @tab-change="handleTabChange">
      <el-tab-pane label="返寝登记" name="returns">
        <el-card class="access-card">
          <div class="access-toolbar">
            <div class="access-search">
              <el-input v-model="returnSearch" clearable placeholder="搜索学号 / 姓名 / 宿舍号" prefix-icon="Search"/>
              <el-button type="primary" @click="loadReturns">
                <el-icon><search/></el-icon>
                搜索
              </el-button>
              <el-button @click="resetReturns">
                <el-icon><refresh-left/></el-icon>
                重置
              </el-button>
            </div>
            <el-button type="primary" @click="openReturnDialog()">
              <el-icon><plus/></el-icon>
              新增返寝
            </el-button>
          </div>

          <el-table v-loading="returnLoading" :data="returnRecords" stripe>
            <el-table-column label="#" type="index" width="56"/>
            <el-table-column label="学号" prop="studentUsername" min-width="110"/>
            <el-table-column label="姓名" prop="studentName" min-width="90"/>
            <el-table-column label="楼宇" prop="dormBuildId" min-width="80"/>
            <el-table-column label="宿舍" prop="dormRoomId" min-width="90"/>
            <el-table-column label="返寝时间" prop="returnTime" min-width="170"/>
            <el-table-column label="状态" min-width="100">
              <template #default="{ row }">
                <el-tag :type="row.late === 1 ? 'danger' : 'success'">
                  {{ row.late === 1 ? '晚归' : '正常' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="备注" prop="remark" min-width="150"/>
            <el-table-column label="操作" width="130" fixed="right">
              <template #default="{ row }">
                <el-button icon="Edit" type="primary" @click="openReturnDialog(row)"/>
                <el-popconfirm title="确认删除该返寝记录？" @confirm="deleteReturn(row.id)">
                  <template #reference>
                    <el-button icon="Delete" type="danger"/>
                  </template>
                </el-popconfirm>
              </template>
            </el-table-column>
          </el-table>

          <div class="access-footer">
            <el-pagination
                v-model:currentPage="returnPage"
                :page-size="returnPageSize"
                :page-sizes="[10, 20]"
                :total="returnTotal"
                layout="total, sizes, prev, pager, next, jumper"
                @size-change="handleReturnSizeChange"
                @current-change="handleReturnPageChange"
            />
          </div>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="大件物品登记" name="items">
        <el-card class="access-card">
          <div class="access-toolbar">
            <div class="access-search">
              <el-input v-model="itemSearch" clearable placeholder="搜索学号 / 姓名 / 物品 / 宿舍号" prefix-icon="Search"/>
              <el-button type="primary" @click="loadItems">
                <el-icon><search/></el-icon>
                搜索
              </el-button>
              <el-button @click="resetItems">
                <el-icon><refresh-left/></el-icon>
                重置
              </el-button>
            </div>
            <el-button type="primary" @click="openItemDialog()">
              <el-icon><plus/></el-icon>
              新增登记
            </el-button>
          </div>

          <el-table v-loading="itemLoading" :data="itemRecords" stripe>
            <el-table-column label="#" type="index" width="56"/>
            <el-table-column label="学号" prop="studentUsername" min-width="110"/>
            <el-table-column label="姓名" prop="studentName" min-width="90"/>
            <el-table-column label="宿舍" min-width="120">
              <template #default="{ row }">{{ row.dormBuildId }} 号楼 {{ row.dormRoomId }}</template>
            </el-table-column>
            <el-table-column label="物品" prop="itemName" min-width="140"/>
            <el-table-column label="方向" min-width="90">
              <template #default="{ row }">
                <el-tag :type="row.direction === 'OUT' ? 'warning' : 'success'">
                  {{ row.direction === 'OUT' ? '带出' : '带入' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="登记时间" prop="registerTime" min-width="170"/>
            <el-table-column label="备注" prop="remark" min-width="150"/>
            <el-table-column label="操作" width="130" fixed="right">
              <template #default="{ row }">
                <el-button icon="Edit" type="primary" @click="openItemDialog(row)"/>
                <el-popconfirm title="确认删除该登记？" @confirm="deleteItem(row.id)">
                  <template #reference>
                    <el-button icon="Delete" type="danger"/>
                  </template>
                </el-popconfirm>
              </template>
            </el-table-column>
          </el-table>

          <div class="access-footer">
            <el-pagination
                v-model:currentPage="itemPage"
                :page-size="itemPageSize"
                :page-sizes="[10, 20]"
                :total="itemTotal"
                layout="total, sizes, prev, pager, next, jumper"
                @size-change="handleItemSizeChange"
                @current-change="handleItemPageChange"
            />
          </div>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="晚归告警" name="alerts">
        <el-card class="access-card">
          <el-table v-loading="alertLoading" :data="lateAlerts" stripe>
            <el-table-column label="学号" prop="studentUsername" min-width="110"/>
            <el-table-column label="姓名" prop="studentName" min-width="90"/>
            <el-table-column label="宿舍" min-width="120">
              <template #default="{ row }">{{ row.dormBuildId }} 号楼 {{ row.dormRoomId }}</template>
            </el-table-column>
            <el-table-column label="返寝时间" prop="returnTime" min-width="170"/>
            <el-table-column label="告警时间" prop="createTime" min-width="170"/>
            <el-table-column label="状态" min-width="100">
              <template #default="{ row }">
                <el-tag type="danger">{{ row.status === 'HANDLED' ? '已处理' : '未处理' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="110" fixed="right">
              <template #default="{ row }">
                <el-button size="small" type="primary" @click="handleLateAlert(row.id)">处理</el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="access-footer">
            <el-pagination
                v-model:currentPage="alertPage"
                :page-size="alertPageSize"
                :total="alertTotal"
                layout="total, prev, pager, next"
                @current-change="handleAlertPageChange"
            />
          </div>
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="returnDialogVisible" :title="returnForm.id ? '修改返寝记录' : '新增返寝记录'" width="460px" @close="resetReturnForm">
      <el-form ref="returnFormRef" :model="returnForm" :rules="returnRules" label-width="96px">
        <el-form-item label="学生学号" prop="studentUsername">
          <el-input v-model="returnForm.studentUsername" clearable/>
        </el-form-item>
        <el-form-item label="返寝时间" prop="returnTime">
          <el-date-picker
              v-model="returnForm.returnTime"
              clearable
              placeholder="选择返寝时间"
              type="datetime"
              value-format="YYYY-MM-DD HH:mm:ss"
              style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="returnForm.remark" type="textarea" :autosize="{ minRows: 3, maxRows: 6 }"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="returnDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveReturn">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="itemDialogVisible" :title="itemForm.id ? '修改大件登记' : '新增大件登记'" width="500px" @close="resetItemForm">
      <el-form ref="itemFormRef" :model="itemForm" :rules="itemRules" label-width="96px">
        <el-form-item label="学生学号" prop="studentUsername">
          <el-input v-model="itemForm.studentUsername" clearable/>
        </el-form-item>
        <el-form-item label="物品名称" prop="itemName">
          <el-input v-model="itemForm.itemName" clearable/>
        </el-form-item>
        <el-form-item label="进出方向" prop="direction">
          <el-radio-group v-model="itemForm.direction">
            <el-radio label="IN">带入</el-radio>
            <el-radio label="OUT">带出</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="登记时间" prop="registerTime">
          <el-date-picker
              v-model="itemForm.registerTime"
              clearable
              placeholder="选择登记时间"
              type="datetime"
              value-format="YYYY-MM-DD HH:mm:ss"
              style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="itemForm.remark" type="textarea" :autosize="{ minRows: 3, maxRows: 6 }"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="itemDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveItem">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import request from "@/utils/request";
import {ElMessage} from "element-plus";

export default {
  name: "AccessInfo",
  data() {
    return {
      activeTab: "returns",
      returnLoading: false,
      itemLoading: false,
      alertLoading: false,
      returnSearch: "",
      itemSearch: "",
      returnPage: 1,
      returnPageSize: 10,
      returnTotal: 0,
      itemPage: 1,
      itemPageSize: 10,
      itemTotal: 0,
      alertPage: 1,
      alertPageSize: 10,
      alertTotal: 0,
      returnRecords: [],
      itemRecords: [],
      lateAlerts: [],
      returnDialogVisible: false,
      itemDialogVisible: false,
      returnForm: {},
      itemForm: {direction: "IN"},
      returnRules: {
        studentUsername: [{required: true, message: "请输入学生学号", trigger: "blur"}],
        returnTime: [{required: true, message: "请选择返寝时间", trigger: "change"}],
      },
      itemRules: {
        studentUsername: [{required: true, message: "请输入学生学号", trigger: "blur"}],
        itemName: [{required: true, message: "请输入物品名称", trigger: "blur"}],
        direction: [{required: true, message: "请选择进出方向", trigger: "change"}],
        registerTime: [{required: true, message: "请选择登记时间", trigger: "change"}],
      },
    };
  },
  created() {
    this.loadReturns();
    this.loadItems();
    this.loadAlerts();
  },
  methods: {
    handleTabChange(tab) {
      const tabName = typeof tab === "string" ? tab : tab && tab.props ? tab.props.name : "";
      if (tabName === "items") {
        this.loadItems();
      }
      if (tabName === "alerts") {
        this.loadAlerts();
      }
    },
    loadReturns() {
      this.returnLoading = true;
      request.get("/access/returns", {
        params: {
          pageNum: this.returnPage,
          pageSize: this.returnPageSize,
          search: this.returnSearch,
        },
      }).then((res) => {
        if (res.code === "0") {
          this.returnRecords = res.data.records;
          this.returnTotal = res.data.total;
        }
      }).finally(() => {
        this.returnLoading = false;
      });
    },
    loadItems() {
      this.itemLoading = true;
      request.get("/access/items", {
        params: {
          pageNum: this.itemPage,
          pageSize: this.itemPageSize,
          search: this.itemSearch,
        },
      }).then((res) => {
        if (res.code === "0") {
          this.itemRecords = res.data.records;
          this.itemTotal = res.data.total;
        }
      }).finally(() => {
        this.itemLoading = false;
      });
    },
    loadAlerts() {
      this.alertLoading = true;
      request.get("/access/lateAlerts", {
        params: {
          pageNum: this.alertPage,
          pageSize: this.alertPageSize,
        },
      }).then((res) => {
        if (res.code === "0") {
          this.lateAlerts = res.data.records;
          this.alertTotal = res.data.total;
        }
      }).finally(() => {
        this.alertLoading = false;
      });
    },
    resetReturns() {
      this.returnSearch = "";
      this.returnPage = 1;
      this.loadReturns();
    },
    resetItems() {
      this.itemSearch = "";
      this.itemPage = 1;
      this.loadItems();
    },
    openReturnDialog(row) {
      this.returnForm = row ? JSON.parse(JSON.stringify(row)) : {};
      this.returnDialogVisible = true;
    },
    openItemDialog(row) {
      this.itemForm = row ? JSON.parse(JSON.stringify(row)) : {direction: "IN"};
      this.itemDialogVisible = true;
    },
    saveReturn() {
      this.$refs.returnFormRef.validate((valid) => {
        if (!valid) return;
        const action = this.returnForm.id
            ? request.put("/access/returns/" + this.returnForm.id, this.returnForm)
            : request.post("/access/returns", this.returnForm);
        action.then((res) => {
          if (res.code === "0") {
            ElMessage({message: "返寝记录已保存", type: "success"});
            this.returnDialogVisible = false;
            this.loadReturns();
            this.loadAlerts();
          } else {
            ElMessage({message: res.msg, type: "error"});
          }
        });
      });
    },
    saveItem() {
      this.$refs.itemFormRef.validate((valid) => {
        if (!valid) return;
        const action = this.itemForm.id
            ? request.put("/access/items/" + this.itemForm.id, this.itemForm)
            : request.post("/access/items", this.itemForm);
        action.then((res) => {
          if (res.code === "0") {
            ElMessage({message: "大件物品登记已保存", type: "success"});
            this.itemDialogVisible = false;
            this.loadItems();
          } else {
            ElMessage({message: res.msg, type: "error"});
          }
        });
      });
    },
    deleteReturn(id) {
      request.delete("/access/returns/" + id).then((res) => {
        if (res.code === "0") {
          ElMessage({message: "返寝记录已删除", type: "success"});
          this.loadReturns();
          this.loadAlerts();
        } else {
          ElMessage({message: res.msg, type: "error"});
        }
      });
    },
    deleteItem(id) {
      request.delete("/access/items/" + id).then((res) => {
        if (res.code === "0") {
          ElMessage({message: "大件登记已删除", type: "success"});
          this.loadItems();
        } else {
          ElMessage({message: res.msg, type: "error"});
        }
      });
    },
    handleLateAlert(id) {
      request.put("/access/lateAlerts/" + id + "/handle").then((res) => {
        if (res.code === "0") {
          ElMessage({message: "晚归告警已处理", type: "success"});
          this.loadAlerts();
        } else {
          ElMessage({message: res.msg, type: "error"});
        }
      });
    },
    resetReturnForm() {
      if (this.$refs.returnFormRef) {
        this.$refs.returnFormRef.resetFields();
      }
      this.returnForm = {};
    },
    resetItemForm() {
      if (this.$refs.itemFormRef) {
        this.$refs.itemFormRef.resetFields();
      }
      this.itemForm = {direction: "IN"};
    },
    handleReturnSizeChange(pageSize) {
      this.returnPageSize = pageSize;
      this.loadReturns();
    },
    handleReturnPageChange(pageNum) {
      this.returnPage = pageNum;
      this.loadReturns();
    },
    handleItemSizeChange(pageSize) {
      this.itemPageSize = pageSize;
      this.loadItems();
    },
    handleItemPageChange(pageNum) {
      this.itemPage = pageNum;
      this.loadItems();
    },
    handleAlertPageChange(pageNum) {
      this.alertPage = pageNum;
      this.loadAlerts();
    },
  },
};
</script>

<style scoped>
.access-page {
  padding: 16px 20px 28px;
}

.access-breadcrumb {
  margin-bottom: 12px;
}

.access-hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  padding: 18px 22px;
  border-radius: 16px;
  background: linear-gradient(135deg, rgba(35,183,164,.18), rgba(90,167,255,.14), rgba(255,157,124,.12));
  border: 1px solid rgba(255,255,255,.72);
  box-shadow: var(--shadow-card);
  margin-bottom: 14px;
}

.access-kicker {
  color: var(--accent);
  font-size: 11px;
  font-weight: 700;
  text-transform: uppercase;
}

.access-hero h2 {
  margin: 2px 0 4px;
  color: var(--text-title);
  font-size: 22px;
}

.access-hero p,
.access-summary span {
  color: var(--text-muted);
  font-size: 13px;
}

.access-summary {
  min-width: 132px;
  padding: 12px 16px;
  border-radius: 12px;
  background: rgba(255,255,255,.78);
  text-align: center;
}

.access-summary strong {
  display: block;
  color: #f0645d;
  font-size: 32px;
  line-height: 1.1;
}

.access-card {
  margin-top: 10px;
}

.access-toolbar {
  display: flex;
  justify-content: space-between;
  gap: 14px;
  margin-bottom: 14px;
}

.access-search {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.access-search .el-input {
  width: 280px;
}

.access-footer {
  display: flex;
  justify-content: flex-end;
  padding-top: 14px;
}

@media (max-width: 900px) {
  .access-hero,
  .access-toolbar {
    align-items: stretch;
    flex-direction: column;
  }

  .access-search .el-input {
    width: 100%;
  }
}
</style>
