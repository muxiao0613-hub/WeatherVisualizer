<template>
  <el-container class="app-shell">
    <el-header class="app-header">
      <div class="header-content">
        <div class="logo">
          <el-icon :size="28"><Sunny /></el-icon>
          <span class="title">{{ appTitle }}</span>
        </div>
        <div class="header-actions">
          <el-button :icon="Location" @click="openCitySelector">选择城市</el-button>
          <el-button :icon="Setting" circle @click="openSettings" />
        </div>
      </div>
    </el-header>

    <el-container class="main-container">
      <el-aside width="280px" class="app-aside">
        <CitySearch />
        <FavoriteList />
      </el-aside>

      <el-main class="app-main">
        <slot />
      </el-main>
    </el-container>

    <SettingsDrawer v-model="settingsVisible" />
    
    <el-dialog v-model="citySelectorVisible" title="选择城市" width="600px">
      <el-autocomplete
        v-model="citySearchKeyword"
        :fetch-suggestions="searchCities"
        placeholder="请输入城市名称"
        :trigger-on-focus="false"
        @select="handleCitySelect"
        style="width: 100%"
      >
        <template #default="{ item }">
          <div class="city-option">
            <span class="city-name">{{ item.name }}</span>
            <span class="city-location">{{ item.state }}, {{ item.country }}</span>
          </div>
        </template>
      </el-autocomplete>
    </el-dialog>
  </el-container>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { Sunny, Setting, Location } from '@element-plus/icons-vue'
import CitySearch from './CitySearch.vue'
import FavoriteList from './FavoriteList.vue'
import SettingsDrawer from '../weather/SettingsDrawer.vue'
import { useCityStore } from '@/stores/city'
import { cityApi } from '@/services/api'
import { ElMessage } from 'element-plus'
import type { CityDTO } from '@/types'

const appTitle = import.meta.env.VITE_APP_TITLE || 'Weather Visualizer'
const settingsVisible = ref(false)
const citySelectorVisible = ref(false)
const citySearchKeyword = ref('')

const cityStore = useCityStore()

const openSettings = () => {
  settingsVisible.value = true
}

const openCitySelector = () => {
  citySelectorVisible.value = true
}

const searchCities = async (queryString: string, callback: (results: CityDTO[]) => void) => {
  if (!queryString || queryString.trim() === '') {
    callback([])
    return
  }

  try {
    const cities = await cityApi.searchCities(queryString)
    callback(cities || [])
  } catch (error) {
    console.error('搜索城市失败:', error)
    callback([])
  }
}

const handleCitySelect = async (city: CityDTO) => {
  console.log('选择城市:', city)
  cityStore.setCurrentCity(city)
  citySelectorVisible.value = false
  
  ElMessage.success(`已切换到 ${city.name}`)
}

const handleOpenCitySelector = () => {
  citySelectorVisible.value = true
}

onMounted(() => {
  window.addEventListener('openCitySelector', handleOpenCitySelector)
})

onBeforeUnmount(() => {
  window.removeEventListener('openCitySelector', handleOpenCitySelector)
})
</script>

<style scoped>
.app-shell {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.app-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 0 24px;
  display: flex;
  align-items: center;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.header-content {
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
}

.title {
  font-size: 20px;
  font-weight: 600;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.city-option {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.city-name {
  font-weight: 600;
  color: #303133;
}

.city-location {
  font-size: 12px;
  color: #909399;
}

.main-container {
  flex: 1;
  overflow: hidden;
}

.app-aside {
  background: #f5f7fa;
  border-right: 1px solid #e4e7ed;
  padding: 16px;
  overflow-y: auto;
}

.app-main {
  background: #f0f2f5;
  padding: 24px;
  overflow-y: auto;
}
</style>
