<template>
  <el-container class="app-shell">
    <el-header class="app-header">
      <div class="header-content">
        <div class="logo">
          <el-icon :size="28"><Sunny /></el-icon>
          <span class="title">{{ appTitle }}</span>
        </div>
        <el-button :icon="Setting" circle @click="openSettings" />
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
  </el-container>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { Sunny, Setting } from '@element-plus/icons-vue'
import CitySearch from './CitySearch.vue'
import FavoriteList from './FavoriteList.vue'
import SettingsDrawer from '../weather/SettingsDrawer.vue'

const appTitle = import.meta.env.VITE_APP_TITLE || 'Weather Visualizer'
const settingsVisible = ref(false)

const openSettings = () => {
  settingsVisible.value = true
}
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
