<template>
  <el-drawer
    v-model="visible"
    title="设置"
    size="400px"
    direction="rtl"
  >
    <el-form :model="form" label-width="100px" label-position="left">
      <el-divider content-position="left">默认设置</el-divider>

      <el-form-item label="默认城市">
        <el-input v-model="form.defaultCity" placeholder="请输入城市名称" />
      </el-form-item>

      <el-form-item label="温度单位">
        <el-select v-model="form.temperatureUnit" style="width: 100%">
          <el-option label="摄氏度 (°C)" value="C" />
          <el-option label="华氏度 (°F)" value="F" />
        </el-select>
      </el-form-item>

      <el-form-item label="风速单位">
        <el-select v-model="form.windSpeedUnit" style="width: 100%">
          <el-option label="米/秒 (m/s)" value="m/s" />
          <el-option label="千米/时 (km/h)" value="km/h" />
        </el-select>
      </el-form-item>

      <el-divider content-position="left">模块显示</el-divider>

      <el-form-item label="当前天气">
        <el-switch v-model="form.showCurrentCard" />
      </el-form-item>

      <el-form-item label="24小时趋势">
        <el-switch v-model="form.showLineChart" />
      </el-form-item>

      <el-form-item label="7天预报">
        <el-switch v-model="form.showBarChart" />
      </el-form-item>

      <el-form-item label="空气质量">
        <el-switch v-model="form.showGaugeCard" />
      </el-form-item>

      <el-form-item label="天气预警">
        <el-switch v-model="form.showAlertsCard" />
      </el-form-item>

      <el-form-item label="AI 助手">
        <el-switch v-model="form.showAiAssistant" />
      </el-form-item>
    </el-form>

    <template #footer>
      <div class="drawer-footer">
        <el-button @click="handleCancel">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">
          保存
        </el-button>
      </div>
    </template>
  </el-drawer>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { preferenceApi } from '@/services/api'
import { usePreferenceStore } from '@/stores/preference'
import type { PreferenceDTO } from '@/types'
import { ElMessage } from 'element-plus'

interface Props {
  modelValue: boolean
}

const props = defineProps<Props>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
}>()

const preferenceStore = usePreferenceStore()
const saving = ref(false)

const visible = ref(props.modelValue)

const form = ref<PreferenceDTO>({
  defaultCity: preferenceStore.preferences.defaultCity,
  temperatureUnit: preferenceStore.preferences.temperatureUnit,
  windSpeedUnit: preferenceStore.preferences.windSpeedUnit,
  showCurrentCard: preferenceStore.preferences.showCurrentCard,
  showLineChart: preferenceStore.preferences.showLineChart,
  showBarChart: preferenceStore.preferences.showBarChart,
  showGaugeCard: preferenceStore.preferences.showGaugeCard,
  showAlertsCard: preferenceStore.preferences.showAlertsCard,
  showAiAssistant: preferenceStore.preferences.showAiAssistant
})

watch(() => props.modelValue, (val) => {
  visible.value = val
  if (val) {
    form.value = { ...preferenceStore.preferences }
  }
})

watch(visible, (val) => {
  emit('update:modelValue', val)
})

const handleSave = async () => {
  saving.value = true

  try {
    const updated = await preferenceApi.updatePreferences(form.value)
    preferenceStore.setPreferences(updated)
    ElMessage.success('设置已保存')
    visible.value = false
  } catch (error) {
    ElMessage.error('保存失败，请稍后重试')
    console.error('Failed to save preferences:', error)
  } finally {
    saving.value = false
  }
}

const handleCancel = () => {
  visible.value = false
}
</script>

<style scoped>
.drawer-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}
</style>
