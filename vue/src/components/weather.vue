<template>
  <el-card class="weather-card">
    <div class="weather-wrap">
      <!-- top bar -->
      <div class="weather-head">
        <div class="weather-location">
          <el-icon :size="14"><location/></el-icon>
          <span>{{ cityName }}</span>
        </div>
        <span class="weather-state">{{ weatherText }}</span>
      </div>

      <!-- loading / error -->
      <div v-if="loading" class="weather-placeholder">
        <div class="shimmer-bar w60"></div>
        <div class="shimmer-bar w40"></div>
      </div>
      <div v-else-if="error" class="weather-error">
        <el-icon :size="18"><warning/></el-icon>
        {{ error }}
      </div>

      <!-- main content -->
      <div v-else class="weather-body">
        <div class="weather-temp-row">
          <span class="weather-icon">{{ weatherEmoji }}</span>
          <span class="weather-temp">{{ temperature }}<span class="weather-unit">°C</span></span>
        </div>
        <div class="weather-meta">
          <div class="meta-chip">
            <el-icon :size="12"><drop/></el-icon>
            {{ humidity }}%
          </div>
          <div class="meta-chip">
            <el-icon :size="12"><wind-power/></el-icon>
            {{ windSpeed }} km/h
          </div>
          <div class="meta-chip meta-time">
            {{ updateTime }}
          </div>
        </div>
      </div>
    </div>
  </el-card>
</template>

<script>
export default {
  name: "weather",
  data() {
    return {
      loading: true,
      error: "",
      cityName: "定位中...",
      weatherText: "--",
      weatherCode: -1,
      temperature: "--",
      humidity: "--",
      windSpeed: "--",
      updateTime: "--",
      lat: 31.2304,
      lon: 121.4737,
    };
  },
  computed: {
    weatherEmoji() {
      const code = this.weatherCode;
      if (code === 0) return "☀️";
      if (code <= 2) return "⛅";
      if (code === 3) return "☁️";
      if (code <= 48) return "🌫️";
      if (code <= 55) return "🌧️";
      if (code <= 65) return "🌧️";
      if (code <= 75) return "❄️";
      if (code <= 82) return "🌦️";
      if (code === 95) return "⛈️";
      return "🌤️";
    },
  },
  mounted() {
    this.loadWeather();
  },
  methods: {
    async loadWeather() {
      try {
        await this.resolveLocation();
        await this.fetchWeather();
      } catch (e) {
        this.error = "天气服务暂不可用";
      } finally {
        this.loading = false;
      }
    },
    resolveLocation() {
      return new Promise((resolve) => {
        if (!navigator.geolocation) {
          this.cityName = "上海";
          resolve();
          return;
        }
        navigator.geolocation.getCurrentPosition(
            (pos) => {
              this.lat = pos.coords.latitude;
              this.lon = pos.coords.longitude;
              this.cityName = "当前位置";
              resolve();
            },
            () => {
              this.cityName = "上海";
              resolve();
            },
            { timeout: 4000 }
        );
      });
    },
    async fetchWeather() {
      const url = `https://api.open-meteo.com/v1/forecast?latitude=${this.lat}&longitude=${this.lon}&current=temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m&timezone=Asia%2FShanghai`;
      const res = await fetch(url);
      if (!res.ok) throw new Error("fetch failed");
      const data = await res.json();
      const c = data.current || {};
      this.temperature = c.temperature_2m ?? "--";
      this.humidity = c.relative_humidity_2m ?? "--";
      this.windSpeed = c.wind_speed_10m ?? "--";
      this.weatherCode = c.weather_code ?? -1;
      this.weatherText = this.getWeatherText(c.weather_code);
      this.updateTime = (c.time || "").replace("T", " ").slice(11, 16) || "--";
    },
    getWeatherText(code) {
      const map = {
        0: "晴", 1: "晴间多云", 2: "多云", 3: "阴",
        45: "雾", 48: "冻雾",
        51: "小毛雨", 53: "毛雨", 55: "大毛雨",
        61: "小雨", 63: "中雨", 65: "大雨",
        71: "小雪", 73: "中雪", 75: "大雪",
        80: "阵雨", 81: "强阵雨", 82: "暴雨",
        95: "雷暴",
      };
      return map[code] || "未知";
    },
  },
};
</script>

<style scoped>
.weather-card {
  border-radius: 18px !important;
  overflow: hidden;
  position: relative;
  background:
      linear-gradient(135deg, rgba(35,183,164,.12), rgba(90,167,255,.10) 54%, rgba(255,157,124,.13)),
      rgba(255,255,255,.86) !important;
}

.weather-card::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgba(255,255,255,.18) 0 12%, transparent 12% 24%, rgba(255,255,255,.16) 24% 36%, transparent 36%) 0 0 / 28px 28px;
  pointer-events: none;
}

.weather-wrap {
  padding: 20px 20px 18px;
  position: relative;
}

/* —— head —— */
.weather-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.weather-location {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 13px;
  font-weight: 600;
  color: var(--text-title);
}

.weather-state {
  font-size: 12px;
  color: var(--text-muted);
  background: rgba(255,255,255,.70);
  padding: 2px 10px;
  border-radius: 12px;
}

/* —— body —— */
.weather-body {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.weather-temp-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.weather-icon {
  font-size: 32px;
  line-height: 1;
}

.weather-temp {
  font-family: var(--font-display);
  font-size: 40px;
  font-weight: 900;
  color: var(--text-title);
  line-height: 1;
  letter-spacing: 0;
}

.weather-unit {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-muted);
  margin-left: 2px;
  vertical-align: super;
}

/* —— meta chips —— */
.weather-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.meta-chip {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  font-size: 11px;
  font-weight: 500;
  color: var(--text-muted);
  background: rgba(255,255,255,.70);
  border-radius: 8px;
}

.meta-time {
  color: var(--accent);
  background: rgba(35,183,164,.10);
}

/* —— placeholder —— */
.weather-placeholder {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 16px 0;
}

.shimmer-bar {
  height: 14px;
  border-radius: 7px;
  background: linear-gradient(90deg, var(--bg-page) 25%, rgba(196,93,62,.06) 50%, var(--bg-page) 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}

.w60 { width: 60%; }
.w40 { width: 40%; }

@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

/* —— error —— */
.weather-error {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--accent);
  padding: 12px 0;
}
</style>
