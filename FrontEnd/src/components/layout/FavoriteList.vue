<template>
  <div class="favorite-list">
    <div class="section-title">
      <el-icon><Star /></el-icon>
      <span>收藏城市</span>
    </div>

    <el-skeleton v-if="loading" :loading="loading" animated>
      <template #template>
        <el-skeleton-item variant="text" style="width: 100%; margin-bottom: 12px;" />
        <el-skeleton-item variant="text" style="width: 100%; margin-bottom: 12px;" />
        <el-skeleton-item variant="text" style="width: 100%;" />
      </template>
    </el-skeleton>

    <div v-else-if="favorites.length > 0" class="favorites">
      <div
        v-for="city in favorites"
        :key="city.name"
        class="favorite-item"
        :class="{ active: isCurrentCity(city) }"
        @click="selectCity(city)"
      >
        <div class="city-info">
          <div class="city-name">{{ city.name }}</div>
          <div class="city-country">{{ city.country }}</div>
        </div>
        <el-button
          :icon="Delete"
          type="danger"
          size="small"
          circle
          text
          @click.stop="removeFavorite(city)"
        />
      </div>
    </div>

    <el-empty v-else description="暂无收藏城市" :image-size="80" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { Star, Delete } from '@element-plus/icons-vue'
import { favoriteApi } from '@/services/api'
import { useCityStore } from '@/stores/city'
import type { CityDTO } from '@/types'

const cityStore = useCityStore()
const favorites = ref<CityDTO[]>([])
const loading = ref(false)

const isCurrentCity = (city: CityDTO) => {
  return cityStore.currentCity.name === city.name
}

const loadFavorites = async () => {
  loading.value = true
  try {
    favorites.value = await favoriteApi.getAllFavorites()
  } catch (error) {
    console.error('Failed to load favorites:', error)
  } finally {
    loading.value = false
  }
}

const selectCity = (city: CityDTO) => {
  cityStore.setCurrentCity(city)
}

const removeFavorite = async (city: CityDTO) => {
  try {
    await favoriteApi.removeFavorite(city.name, city.country, city.lat, city.lon)
    favorites.value = favorites.value.filter(f => f.name !== city.name)
  } catch (error) {
    console.error('Failed to remove favorite:', error)
  }
}

onMounted(() => {
  loadFavorites()
})

defineExpose({
  loadFavorites
})
</script>

<style scoped>
.favorite-list {
  margin-top: 24px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 16px;
}

.favorites {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.favorite-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: white;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
}

.favorite-item:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transform: translateY(-1px);
}

.favorite-item.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.favorite-item.active .city-name,
.favorite-item.active .city-country {
  color: white;
}

.city-info {
  flex: 1;
}

.city-name {
  font-weight: 500;
  color: #303133;
}

.city-country {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
</style>
