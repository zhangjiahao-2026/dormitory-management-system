<template>
  <div class="student-page">
    <el-breadcrumb separator-icon="ArrowRight" class="page-breadcrumb">
      <el-breadcrumb-item>我的宿舍</el-breadcrumb-item>
    </el-breadcrumb>

    <el-card class="page-card">
      <el-empty v-if="!hasRoom" description="暂无宿舍信息"/>

      <div v-else class="room-dashboard">
        <section class="room-hero">
          <div>
            <p class="eyebrow">当前宿舍</p>
            <h2>{{ room.dormBuildId }} 号楼 {{ room.dormRoomId }} 室</h2>
            <p class="hero-subtitle">{{ room.floorNum }} 层 · {{ occupancyText }}</p>
          </div>
          <el-tag type="success" effect="light" size="large">已入住</el-tag>
        </section>

        <section class="summary-grid">
          <div class="summary-item">
            <el-icon><office-building/></el-icon>
            <span>楼宇号</span>
            <strong>{{ room.dormBuildId }}</strong>
          </div>
          <div class="summary-item">
            <el-icon><home-filled/></el-icon>
            <span>房间号</span>
            <strong>{{ room.dormRoomId }}</strong>
          </div>
          <div class="summary-item">
            <el-icon><tickets/></el-icon>
            <span>楼层</span>
            <strong>{{ room.floorNum }}</strong>
          </div>
          <div class="summary-item">
            <el-icon><user-filled/></el-icon>
            <span>入住人数</span>
            <strong>{{ room.currentCapacity }}/{{ room.maxCapacity }}</strong>
          </div>
          <div class="summary-item utility-item electric">
            <el-icon><tickets/></el-icon>
            <span>本月用电量</span>
            <strong>{{ monthlyElectricUsage }} kWh</strong>
          </div>
          <div class="summary-item utility-item water">
            <el-icon><tickets/></el-icon>
            <span>本月用水量</span>
            <strong>{{ monthlyWaterUsage }} 吨</strong>
          </div>
        </section>

        <section class="beds-section">
          <div class="section-heading">
            <h3>床位信息</h3>
          </div>

          <div class="bed-grid">
            <article
                v-for="bed in beds"
                :key="bed.key"
                class="bed-card"
                :class="{ 'is-mine': bed.isMine, 'is-empty': !bed.value }"
            >
              <div class="bed-icon">
                <el-icon><user/></el-icon>
              </div>
              <div class="bed-content">
                <span class="bed-label">{{ bed.label }}</span>
                <strong>{{ bed.displayName || '空床位' }}</strong>
                <small>{{ bed.isMine ? '我的床位' : (bed.value ? '已入住' : '可入住') }}</small>
              </div>
            </article>
          </div>
        </section>
      </div>
    </el-card>
  </div>
</template>
<script src="@/assets/js/MyRoomInfo.js"></script>

<style scoped>
.student-page {
  padding: 20px;
}

.page-breadcrumb {
  margin-bottom: 16px;
}

.page-card {
  min-height: calc(100vh - 120px);
  border-radius: 8px;
}

.room-dashboard {
  display: flex;
  flex-direction: column;
  gap: 22px;
}

.room-hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 24px;
  border-radius: 8px;
  background: linear-gradient(135deg, rgba(35, 183, 164, .14), rgba(64, 158, 255, .12));
  border: 1px solid rgba(35, 183, 164, .18);
}

.eyebrow {
  margin: 0 0 6px;
  font-size: 12px;
  color: var(--text-secondary);
}

.room-hero h2 {
  margin: 0;
  font-size: 28px;
  line-height: 1.25;
  color: var(--text-primary);
}

.hero-subtitle {
  margin: 8px 0 0;
  color: var(--text-secondary);
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(120px, 1fr));
  gap: 14px;
}

.summary-item {
  display: grid;
  grid-template-columns: 34px 1fr;
  gap: 2px 10px;
  align-items: center;
  padding: 16px;
  border: 1px solid rgba(215, 236, 234, .9);
  border-radius: 8px;
  background: rgba(255, 255, 255, .74);
}

.summary-item .el-icon {
  grid-row: span 2;
  width: 34px;
  height: 34px;
  border-radius: 8px;
  background: rgba(35, 183, 164, .12);
  color: #0b756a;
}

.summary-item span {
  color: var(--text-secondary);
  font-size: 12px;
}

.summary-item strong {
  color: var(--text-primary);
  font-size: 18px;
}

.summary-item.utility-item {
  border-color: rgba(64, 158, 255, .18);
  background: rgba(245, 251, 255, .82);
}

.summary-item.utility-item .el-icon {
  background: rgba(64, 158, 255, .12);
  color: #3178c6;
}

.summary-item.water {
  border-color: rgba(35, 183, 164, .2);
  background: rgba(242, 253, 250, .82);
}

.summary-item.water .el-icon {
  background: rgba(35, 183, 164, .14);
  color: #0b756a;
}

.beds-section {
  padding-top: 4px;
}

.section-heading {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
}

.section-heading h3 {
  margin: 0;
  font-size: 18px;
  color: var(--text-primary);
}

.section-heading span {
  font-size: 12px;
  color: var(--text-secondary);
}

.bed-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(128px, 1fr));
  gap: 14px;
}

.bed-card {
  display: flex;
  gap: 12px;
  min-height: 108px;
  padding: 16px;
  border: 1px solid rgba(215, 236, 234, .9);
  border-radius: 8px;
  background: rgba(255, 255, 255, .82);
}

.bed-card.is-mine {
  border-color: rgba(35, 183, 164, .45);
  background: rgba(35, 183, 164, .13);
  box-shadow: 0 12px 26px rgba(35, 183, 164, .12);
}

.bed-card.is-empty {
  background: rgba(255, 255, 255, .56);
}

.bed-icon {
  width: 34px;
  height: 34px;
  border-radius: 8px;
  background: rgba(64, 158, 255, .1);
  color: #3178c6;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.bed-card.is-mine .bed-icon {
  background: rgba(35, 183, 164, .2);
  color: #0b756a;
}

.bed-card.is-empty .bed-icon {
  background: rgba(144, 147, 153, .12);
  color: #909399;
}

.bed-content {
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-width: 0;
}

.bed-label,
.bed-content small {
  color: var(--text-secondary);
  font-size: 12px;
}

.bed-content strong {
  color: var(--text-primary);
  font-size: 16px;
  overflow-wrap: anywhere;
}

@media (max-width: 980px) {
  .summary-grid,
  .bed-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 640px) {
  .room-hero {
    align-items: flex-start;
    flex-direction: column;
  }

  .summary-grid,
  .bed-grid {
    grid-template-columns: 1fr;
  }
}
</style>
