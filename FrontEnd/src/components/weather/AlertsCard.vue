<template>
  <el-card class="alerts-card" shadow="hover">
    <template #header>
      <div class="card-header">
        <span class="card-title">天气预警</span>
        <el-badge :value="alerts.length" :hidden="alerts.length === 0" type="danger" />
      </div>
    </template>

    <el-skeleton v-if="loading" :loading="loading" animated>
      <template #template>
        <el-skeleton-item variant="text" style="width: 100%; margin-bottom: 12px;" />
        <el-skeleton-item variant="text" style="width: 100%; margin-bottom: 12px;" />
        <el-skeleton-item variant="text" style="width: 100%;" />
      </template>
    </el-skeleton>

    <div v-else-if="alerts.length > 0" class="alerts-list">
      <el-collapse v-model="activeAlerts">
        <el-collapse-item
          v-for="(alert, index) in alerts"
          :key="index"
          :name="index"
        >
          <template #title>
            <div class="alert-title">
              <el-tag :type="getAlertType(alert.level)" size="small">
                {{ getAlertLevelText(alert.level) }}
              </el-tag>
              <span class="alert-event">{{ alert.event }}</span>
            </div>
          </template>
          <div class="alert-content">
            <div class="alert-description">{{ alert.description }}</div>
            <div class="alert-time">
              <el-icon><Clock /></el-icon>
              <span>{{ formatTime(alert.start) }} - {{ formatTime(alert.end) }}</span>
            </div>
          </div>
        </el-collapse-item>
      </el-collapse>
    </div>

    <el-empty v-else description="暂无天气预警" :image-size="80" />
  </el-card>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { Clock } from '@element-plus/icons-vue'
import type { AlertDTO } from '@/types'
import dayjs from 'dayjs'

interface Props {
  alerts: AlertDTO[]
  loading: boolean
}

defineProps<Props>()

const activeAlerts = ref<number[]>([])

const getAlertType = (level: string) => {
  switch (level) {
    case '红色':
      return 'danger'
    case '橙色':
      return 'warning'
    case '黄色':
      return 'warning'
    default:
      return 'info'
  }
}

const getAlertLevelText = (level: string) => {
  return level || '预警'
}

const formatTime = (time: string) => {
  return dayjs(time).format('MM-DD HH:mm')
}
</script>

<style scoped>
.alerts-card {
  border-radius: 16px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.alerts-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.alert-title {
  display: flex;
  align-items: center;
  gap: 12px;
}

.alert-event {
  font-weight: 500;
  color: #303133;
}

.alert-content {
  padding: 8px 0;
}

.alert-description {
  color: #606266;
  line-height: 1.6;
  margin-bottom: 12px;
}

.alert-time {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #909399;
  font-size: 13px;
}

:deep(.el-collapse-item__header) {
  border-radius: 8px;
  margin-bottom: 8px;
}

:deep(.el-collapse-item__wrap) {
  border-radius: 8px;
}
</style>
