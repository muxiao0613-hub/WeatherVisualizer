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
        <div class="city-search-container">
          <el-input 
            v-model="cityKeyword" 
            placeholder="搜索城市..." 
            clearable
            @input="handleCitySearch"
            @keyup.enter="handleCitySearch"
          />
          <div v-if="citySearchResults.length > 0" class="city-search-results">
            <div
              v-for="city in citySearchResults"
              :key="city.name"
              class="city-result-item"
              @click="selectDefaultCity(city)"
            >
              <div class="city-name">{{ city.name }}</div>
              <div class="city-location">{{ city.country }} {{ city.state }}</div>
            </div>
          </div>
        </div>
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
import { preferenceApi, cityApi } from '@/services/api'
import { usePreferenceStore } from '@/stores/preference'
import type { PreferenceDTO, CityDTO } from '@/types'
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

const cityKeyword = ref('')
const citySearchResults = ref<CityDTO[]>([])

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
    cityKeyword.value = form.value.defaultCity || ''
  }
})

watch(visible, (val) => {
  emit('update:modelValue', val)
  if (!val) {
    citySearchResults.value = []
  }
})

const handleCitySearch = async () => {
  if (!cityKeyword.value.trim()) {
    citySearchResults.value = []
    return
  }

  try {
    console.log('搜索城市:', cityKeyword.value)
    const results = await cityApi.searchCities(cityKeyword.value)
    console.log('搜索结果:', results)
    citySearchResults.value = results
  } catch (error) {
    console.error('搜索失败:', error)
    citySearchResults.value = []
  }
}

const selectDefaultCity = (city: CityDTO) => {
  console.log('选择默认城市:', city)
  form.value.defaultCity = city.name
  cityKeyword.value = city.name
  citySearchResults.value = []
}

const handleSave = async () => {
  saving.value = true
  console.log('正在保存设置:', form.value)

  try {
    const updated = await preferenceApi.updatePreferences(form.value)
    console.log('API返回的设置:', updated)
    preferenceStore.setPreferences(updated)
    console.log('Store中的设置已更新:', preferenceStore.preferences)
    ElMessage.success('设置已保存')
    visible.value = false
    
    // 如果默认城市发生变化，触发页面刷新以加载新城市的天气数据
    window.location.reload()
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

.city-search-container {
  position: relative;
  width: 100%;
}

.city-search-results {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background: white;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  max-height: 300px;
  overflow-y: auto;
  z-index: 1000;
  margin-top: 4px;
}

.city-result-item {
  padding: 12px 16px;
  cursor: pointer;
  transition: background-color 0.2s;
  border-bottom: 1px solid #f0f0f0;
}

.city-result-item:last-child {
  border-bottom: none;
}

.city-result-item:hover {
  background-color: #f5f7fa;
}

.city-name {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
}

.city-location {
  font-size: 12px;
  color: #909399;
}
</style>
