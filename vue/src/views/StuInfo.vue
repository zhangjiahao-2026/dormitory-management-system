<template>
  <div class="crud">
    <el-breadcrumb separator="/" class="crud-breadcrumb">
      <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item>用户管理</el-breadcrumb-item>
      <el-breadcrumb-item>学生信息</el-breadcrumb-item>
    </el-breadcrumb>

    <el-card class="crud-card">
      <!-- toolbar -->
      <div class="crud-toolbar">
        <div class="crud-search">
          <el-input
              v-model="search"
              placeholder="搜索学生姓名..."
              prefix-icon="Search"
              clearable
              class="crud-input"
          />
          <el-button type="primary" @click="load" :icon="'Search'">搜索</el-button>
          <el-button @click="reset" :icon="'refresh-left'">重置</el-button>
        </div>
        <el-button type="primary" @click="add" :icon="'plus'">新增学生</el-button>
      </div>

      <!-- table -->
      <el-table
          v-loading="loading"
          :data="tableData"
          max-height="680"
          stripe
          class="crud-table"
      >
        <el-table-column type="index" label="#" width="56"/>
        <el-table-column prop="username" label="学号" sortable min-width="110"/>
        <el-table-column prop="name" label="姓名" min-width="90"/>
        <el-table-column
            prop="gender"
            label="性别"
            width="90"
            :filters="[{ text: '男', value: '男' }, { text: '女', value: '女' }]"
            :filter-method="filterTag"
        >
          <template #default="{ row }">
            <span class="gender-tag" :class="row.gender === '男' ? 'gender-m' : 'gender-f'">
              {{ row.gender }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="age" label="年龄" sortable width="90"/>
        <el-table-column prop="phoneNum" label="手机号" min-width="130"/>
        <el-table-column prop="email" label="邮箱" :show-overflow-tooltip="true" min-width="180"/>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="scope">
            <div class="crud-actions-vertical">
              <el-button type="primary" link size="small" @click="handleEdit(scope.row)">
                <el-icon :size="14"><edit/></el-icon> 编辑
              </el-button>
              <el-popconfirm title="确认删除该学生？" @confirm="handleDelete(scope.row.username)">
                <template #reference>
                  <el-button type="danger" link size="small">
                    <el-icon :size="14"><delete/></el-icon> 删除
                  </el-button>
                </template>
              </el-popconfirm>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- pagination -->
      <div class="crud-footer">
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

      <!-- dialog -->
      <el-dialog
          v-model="dialogVisible"
          :title="judgeAddOrEdit ? '编辑学生' : '新增学生'"
          width="480px"
          @close="cancel"
      >
        <el-form ref="form" :model="form" :rules="rules" label-width="90px">
          <el-form-item label="学号" prop="username">
            <el-input v-model="form.username" :disabled="judgeAddOrEdit"/>
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
          <el-form-item label="年龄" prop="age">
            <el-input v-model.number="form.age"/>
          </el-form-item>
          <el-form-item label="性别" prop="gender">
            <el-radio v-model="form.gender" label="男">男</el-radio>
            <el-radio v-model="form.gender" label="女">女</el-radio>
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

<script src="@/assets/js/StuInfo.js"></script>

<style scoped>
.crud {
  padding: 16px 20px 20px;
}

.crud-breadcrumb {
  margin-bottom: 12px;
}

.crud-card {
  min-height: 0;
  border-radius: 14px !important;
}

.crud-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--border-light);
}

.crud-search {
  display: flex;
  align-items: center;
  gap: 10px;
}

.crud-input {
  width: 300px;
}

.crud-table {
  border-radius: var(--r-md);
}

.crud-table :deep(.el-table__cell) {
  padding: 9px 0;
}

/* stripe row color */
.crud-table :deep(.el-table__row--striped td) {
  background: var(--bg-input) !important;
}

.crud-actions-vertical {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: center;
  gap: 8px;
  min-height: 32px;
}

.crud-actions-vertical :deep(.el-button) {
  width: 58px;
  min-height: 28px;
  justify-content: center;
  border-radius: 6px !important;
}

.crud-actions-vertical :deep(.el-button + .el-button) {
  margin-left: 0;
}

.gender-tag {
  display: inline-block;
  padding: 2px 10px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

.gender-m {
  background: rgba(196,93,62,.08);
  color: var(--accent);
}

.gender-f {
  background: rgba(107,91,149,.08);
  color: #6b5b95;
}

.crud-footer {
  display: flex;
  justify-content: flex-end;
  padding: 20px 0 4px;
}

.edit-pwd {
  cursor: pointer;
  color: var(--accent);
  transition: color .2s;
}

.edit-pwd:hover {
  color: var(--accent-hover);
}
</style>
