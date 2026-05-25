<template>
  <div class="home command-home">
    <section class="kpi-grid">
      <button
          v-for="(s, i) in stats"
          :key="s.label"
          class="kpi-card"
          :class="['tone-' + s.tone, { actionable: s.action }]"
          type="button"
          @click="triggerStat(s)"
      >
        <span class="kpi-icon">
          <el-icon :size="20"><component :is="s.icon"/></el-icon>
        </span>
        <span class="kpi-copy">
          <span class="kpi-label">{{ s.label }}</span>
          <strong>{{ s.value }}</strong>
        </span>
        <span v-if="i < 4" class="kpi-state">实时</span>
        <span v-else class="kpi-state alert">待处理</span>
      </button>
    </section>

    <section class="ops-grid">
      <el-card class="panel notice-panel">
        <template #header>
          <div class="panel-title">
            <span class="title-mark"></span>
            <span>宿舍通知</span>
          </div>
        </template>
        <el-timeline class="notice-timeline">
          <el-timeline-item
              v-for="(a, idx) in activities.slice(0, 7)"
              :key="idx"
              :timestamp="a.releaseTime"
              placement="top"
              color="var(--accent)"
              hollow
          >
            <div class="notice-item">{{ a.title }}</div>
          </el-timeline-item>
        </el-timeline>
      </el-card>

      <el-card class="panel chart-panel">
        <template #header>
          <div class="panel-title panel-title-row">
            <div>
              <span class="title-mark"></span>
              <span>学生分布</span>
            </div>
            <span class="panel-subtitle">按楼宇住宿情况汇总</span>
          </div>
        </template>
        <div class="chart-wrap">
          <home_echarts/>
        </div>
      </el-card>

      <aside class="right-rail">
        <el-card v-if="canViewAccess" class="panel alert-panel late-alert-panel" @click="goAccess">
          <div class="alert-head">
            <div>
              <span class="alert-label">晚归告警</span>
              <strong>{{ accessAlerts.unhandledCount || 0 }}</strong>
            </div>
            <el-icon :size="24"><clock/></el-icon>
          </div>
          <div v-if="accessAlerts.recentAlerts && accessAlerts.recentAlerts.length" class="alert-list">
            <div v-for="alert in accessAlerts.recentAlerts.slice(0, 3)" :key="alert.id" class="alert-item">
              <span>{{ alert.studentName || alert.studentUsername }} · {{ alert.dormBuildId }}号楼 {{ alert.dormRoomId }}</span>
              <em>{{ alert.returnTime }}</em>
            </div>
          </div>
          <div v-else class="alert-empty">暂无未处理晚归告警</div>
        </el-card>

        <el-card v-if="canViewUtility" class="panel alert-panel utility-alert-panel" @click="goUtility">
          <div class="alert-head">
            <div>
              <span class="alert-label">用电告警</span>
              <strong>{{ utilityAlerts.unhandledCount || 0 }}</strong>
            </div>
            <el-icon :size="24"><warning/></el-icon>
          </div>
          <div v-if="utilityAlerts.recentAlerts && utilityAlerts.recentAlerts.length" class="alert-list">
            <div v-for="alert in utilityAlerts.recentAlerts.slice(0, 3)" :key="alert.id" class="alert-item utility">
              <span>{{ alert.dormBuildId }}号楼 {{ alert.dormRoomId }} 宿舍</span>
              <em>{{ alert.triggerValue }} / {{ alert.limitValue }} 度</em>
            </div>
          </div>
          <div v-else class="alert-empty">暂无未处理用电告警</div>
        </el-card>

        <weather/>
        <el-card class="panel calendar-panel">
          <Calender/>
        </el-card>
      </aside>
    </section>
  </div>
</template>

<script src="@/assets/js/Home.js"></script>

<style scoped>
.command-home {
  min-height: 100%;
  padding: 18px 20px 24px;
  background: linear-gradient(180deg, rgba(244,252,255,.82), rgba(249,253,252,.52));
}

.kpi-grid {
  display: grid;
  grid-template-columns: repeat(6, minmax(122px, 1fr));
  gap: 12px;
  margin-bottom: 14px;
}

.kpi-card {
  min-height: 92px;
  border: 1px solid rgba(85,145,158,.16);
  border-radius: 8px;
  padding: 14px 12px;
  background: rgba(255,255,255,.92);
  box-shadow: 0 10px 24px rgba(38,94,105,.08);
  display: grid;
  grid-template-columns: 38px minmax(0, 1fr);
  grid-template-rows: 1fr auto;
  align-items: center;
  gap: 6px 10px;
  text-align: left;
  color: var(--text-title);
  transition: transform .18s var(--ease), box-shadow .18s var(--ease), border-color .18s var(--ease);
}

.kpi-card.actionable {
  cursor: pointer;
}

.kpi-card.actionable:hover {
  border-color: rgba(240,100,93,.34);
  box-shadow: 0 14px 28px rgba(240,100,93,.12);
  transform: translateY(-2px);
}

.kpi-icon {
  width: 38px;
  height: 38px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  grid-row: 1 / 3;
}

.tone-teal .kpi-icon { background: #23b7a4; }
.tone-blue .kpi-icon { background: #4e9df6; }
.tone-green .kpi-icon { background: #62b85f; }
.tone-cyan .kpi-icon { background: #37a9c7; }
.tone-danger .kpi-icon { background: #f0645d; }
.tone-warning .kpi-icon { background: #e7a33e; }

.kpi-copy {
  min-width: 0;
}

.kpi-label {
  display: block;
  color: var(--text-muted);
  font-size: 12px;
  font-weight: 600;
  line-height: 1.3;
}

.kpi-copy strong {
  display: block;
  color: var(--text-title);
  font-size: 26px;
  line-height: 1.1;
  margin-top: 4px;
}

.tone-danger .kpi-copy strong,
.tone-warning .kpi-copy strong {
  color: #f0645d;
}

.kpi-state {
  justify-self: start;
  padding: 2px 7px;
  border-radius: 6px;
  background: rgba(35,183,164,.10);
  color: #0b756a;
  font-size: 10px;
  font-weight: 700;
}

.kpi-state.alert {
  background: rgba(240,100,93,.10);
  color: #d84d47;
}

.ops-grid {
  display: grid;
  grid-template-columns: minmax(230px, 300px) minmax(420px, 1fr) minmax(280px, 340px);
  gap: 14px;
  align-items: start;
}

.panel {
  border-radius: 8px !important;
  border: 1px solid rgba(85,145,158,.14);
  box-shadow: 0 12px 28px rgba(38,94,105,.08);
  overflow: hidden;
}

.panel :deep(.el-card__header) {
  padding: 12px 14px;
  border-bottom: 1px solid rgba(85,145,158,.12);
}

.panel :deep(.el-card__body) {
  padding: 14px;
}

.panel-title,
.panel-title-row,
.panel-title-row > div {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--text-title);
  font-weight: 800;
}

.panel-title-row {
  justify-content: space-between;
}

.title-mark {
  width: 4px;
  height: 16px;
  border-radius: 4px;
  background: var(--accent);
}

.panel-subtitle {
  color: var(--text-muted);
  font-size: 12px;
  font-weight: 500;
}

.notice-panel {
  min-height: 520px;
}

.notice-timeline {
  padding-top: 2px;
}

.notice-timeline :deep(.el-timeline-item) {
  padding-bottom: 14px;
}

.notice-timeline :deep(.el-timeline-item__timestamp) {
  color: var(--text-muted);
  font-size: 11px;
}

.notice-item {
  padding: 7px 9px;
  border-radius: 6px;
  background: rgba(247,252,255,.9);
  color: var(--text-body);
  font-size: 13px;
  line-height: 1.5;
}

.chart-panel {
  min-height: 520px;
}

.chart-wrap {
  height: 455px;
  min-width: 0;
}

.right-rail {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.alert-panel {
  cursor: pointer;
}

.alert-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.alert-label {
  display: block;
  color: var(--text-muted);
  font-size: 12px;
  font-weight: 700;
}

.alert-head strong {
  display: block;
  color: #f0645d;
  font-size: 30px;
  line-height: 1.1;
  margin-top: 2px;
}

.alert-head .el-icon {
  color: #f0645d;
  padding: 9px;
  border-radius: 8px;
  background: rgba(240,100,93,.10);
}

.utility-alert-panel .alert-head .el-icon {
  color: #d88a1f;
  background: rgba(231,163,62,.12);
}

.alert-list {
  display: flex;
  flex-direction: column;
  gap: 7px;
  margin-top: 12px;
}

.alert-item {
  display: grid;
  gap: 3px;
  padding: 7px 9px;
  border-radius: 6px;
  background: rgba(255,246,244,.9);
  color: var(--text-body);
  font-size: 12px;
}

.alert-item.utility {
  background: rgba(255,249,237,.96);
}

.alert-item em {
  color: #d84d47;
  font-style: normal;
  font-weight: 700;
  white-space: nowrap;
}

.alert-empty {
  margin-top: 10px;
  color: var(--text-muted);
  font-size: 13px;
}

.calendar-panel {
  width: 100%;
}

@media (max-width: 1320px) {
  .kpi-grid {
    grid-template-columns: repeat(3, minmax(150px, 1fr));
  }

  .ops-grid {
    grid-template-columns: minmax(260px, 330px) minmax(420px, 1fr);
  }

  .right-rail {
    grid-column: 1 / -1;
    display: grid;
    grid-template-columns: repeat(2, minmax(260px, 1fr));
  }
}

@media (max-width: 900px) {
  .command-home {
    padding: 14px;
  }

  .kpi-grid,
  .ops-grid,
  .right-rail {
    grid-template-columns: 1fr;
  }

  .notice-panel,
  .chart-panel {
    min-height: auto;
  }

  .chart-wrap {
    height: 360px;
  }

  .panel-title-row {
    align-items: flex-start;
    flex-direction: column;
    gap: 4px;
  }
}
</style>
