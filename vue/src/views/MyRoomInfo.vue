<template>
  <div class="page-container">
    <el-breadcrumb separator-icon="ArrowRight" class="page-breadcrumb">
      <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item>我的宿舍</el-breadcrumb-item>
    </el-breadcrumb>
    <el-card class="page-card">
      <div class="room-wrapper">
        <div class="room-info">
          <el-descriptions :column="1" border title="房间信息" class="info-section">
            <el-descriptions-item>
              <template #label><el-icon><office-building/></el-icon> 楼宇号</template>
              {{ this.room.dormBuildId }}
            </el-descriptions-item>
            <el-descriptions-item>
              <template #label><el-icon><home-filled/></el-icon> 房间号</template>
              {{ this.room.dormRoomId }}
            </el-descriptions-item>
            <el-descriptions-item>
              <template #label><el-icon><tickets/></el-icon> 楼层</template>
              {{ this.room.floorNum }}
            </el-descriptions-item>
            <el-descriptions-item>
              <template #label><el-icon><user/></el-icon> 可住人数</template>
              {{ this.room.maxCapacity }}
            </el-descriptions-item>
            <el-descriptions-item>
              <template #label><el-icon><user-filled/></el-icon> 已住人数</template>
              {{ this.room.currentCapacity }}
            </el-descriptions-item>
          </el-descriptions>

          <el-descriptions :column="1" border title="床位信息" class="info-section">
            <el-descriptions-item v-for="(bed, index) in beds" :key="index">
              <template #label><el-icon><user/></el-icon> {{ bed.label }}</template>
              <el-tag
                  v-if="bed.value"
                  :type="name === bed.value ? 'primary' : 'info'"
                  effect="light"
              >{{ bed.value }}</el-tag>
              <span v-else class="empty-bed">空</span>
            </el-descriptions-item>
          </el-descriptions>
        </div>
      </div>
    </el-card>
  </div>
</template>
<script src="@/assets/js/MyRoomInfo.js"></script>

<style scoped>
.page-container {
  padding: 20px;
}

.page-breadcrumb {
  margin-bottom: 16px;
}

.page-card {
  min-height: calc(100vh - 120px);
}

.room-wrapper {
  display: flex;
  justify-content: center;
  padding: 20px 0;
}

.room-info {
  width: 560px;
}

.info-section {
  margin-bottom: 24px;
}

.info-section :deep(.el-descriptions__label) {
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 500;
}

.empty-bed {
  color: var(--text-secondary);
  font-size: 13px;
}
</style>
