<template>
  <div class="home">
    <!-- stat cards -->
    <div class="stats-row">
      <div class="stat-card" v-for="(s, i) in stats" :key="i" :class="'stat-' + i">
        <div class="stat-ring" :style="{ background: s.gradient }">
          <el-icon :size="22"><component :is="s.icon"/></el-icon>
        </div>
        <div class="stat-body">
          <span class="stat-label">{{ s.label }}</span>
          <span class="stat-num">{{ s.value }}</span>
        </div>
        <div class="stat-live">
          <span class="live-dot"></span>
          实时
        </div>
      </div>
    </div>

    <!-- content grid -->
    <div class="content-grid">
      <!-- announcements -->
      <el-card class="panel panel-notice">
        <template #header>
          <div class="panel-title">
            <el-icon :size="16"><bell/></el-icon>
            <span>宿舍通告</span>
          </div>
        </template>
        <el-timeline class="notice-timeline">
          <el-timeline-item
              v-for="(a, idx) in activities.slice(0, 8)"
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

      <!-- chart -->
      <el-card class="panel panel-chart">
        <template #header>
          <div class="panel-title">
            <el-icon :size="16"><data-analysis/></el-icon>
            <span>学生分布</span>
          </div>
        </template>
        <home_echarts/>
      </el-card>

      <!-- right column -->
      <div class="panel-right">
        <weather/>
        <el-card class="panel panel-cal">
          <Calender/>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script src="@/assets/js/Home.js"></script>

<style scoped>
.home {
  padding: 24px;
}

/* —— stat cards —— */
.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  background: var(--bg-card);
  border-radius: var(--r-lg);
  padding: 22px 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: var(--shadow-card);
  transition: all .3s var(--ease);
  position: relative;
  overflow: hidden;
}

.stat-card:hover {
  box-shadow: var(--shadow-card-hover);
  transform: translateY(-3px);
}

.stat-ring {
  width: 48px;
  height: 48px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
}

.stat-0 .stat-ring { background: linear-gradient(135deg, #c45d3e, #e8a88a); }
.stat-1 .stat-ring { background: linear-gradient(135deg, #2d7d6f, #5ba8a0); }
.stat-2 .stat-ring { background: linear-gradient(135deg, #6b5b95, #a78bba); }
.stat-3 .stat-ring { background: linear-gradient(135deg, #c49b3e, #d4b87a); }

.stat-body {
  flex: 1;
  min-width: 0;
}

.stat-label {
  display: block;
  font-size: 12px;
  color: var(--text-muted);
  font-weight: 500;
  margin-bottom: 4px;
  letter-spacing: .5px;
}

.stat-num {
  display: block;
  font-family: var(--font-display);
  font-size: 28px;
  font-weight: 900;
  color: var(--text-title);
  line-height: 1.1;
}

.stat-live {
  position: absolute;
  top: 14px;
  right: 14px;
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 10px;
  color: var(--text-muted);
  font-weight: 600;
  letter-spacing: .5px;
}

.live-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #52c41a;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: .4; }
}

/* —— content grid —— */
.content-grid {
  display: grid;
  grid-template-columns: 260px 1fr 380px;
  gap: 20px;
  align-items: start;
}

.panel {
  border-radius: var(--r-lg) !important;
}

.panel-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-family: var(--font-display);
  font-size: 15px;
  font-weight: 700;
  color: var(--text-title);
}

/* notice */
.notice-timeline :deep(.el-timeline-item__timestamp) {
  font-size: 11px;
  color: var(--text-muted);
}

.notice-item {
  font-size: 13px;
  color: var(--text-body);
  line-height: 1.6;
}

/* chart */
.panel-chart {
  min-height: 480px;
}

/* right */
.panel-right {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.panel-cal {
  width: 100%;
}
</style>
