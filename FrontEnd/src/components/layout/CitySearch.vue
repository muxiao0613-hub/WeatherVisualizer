<template>
  <div class="city-search">
    <el-input
      v-model="keyword"
      placeholder="搜索城市..."
      :prefix-icon="Search"
      clearable
      @input="handleSearch"
      @keyup.enter="handleSearch"
    />
    <div v-if="searchResults.length > 0" class="search-results">
      <div
        v-for="city in searchResults"
        :key="city.name"
        class="city-item"
        @click="selectCity(city)"
      >
        <div class="city-name">{{ city.name }}</div>
        <div class="city-country">{{ city.country }}</div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { cityApi } from '@/services/api'
import { useCityStore } from '@/stores/city'
import type { CityDTO } from '@/types'

const cityStore = useCityStore()
const keyword = ref('')
const searchResults = ref<CityDTO[]>([])

const handleSearch = async () => {
  if (!keyword.value.trim()) {
    searchResults.value = []
    return
  }

  try {
    searchResults.value = await cityApi.searchCities(keyword.value)
  } catch (error) {
    console.error('Search failed:', error)
  }
}

const selectCity = (city: CityDTO) => {
  cityStore.setCurrentCity(city)
  keyword.value = ''
  searchResults.value = []
}
</script>

<style scoped>
.city-search {
  margin-bottom: 20px;
}

.search-results {
  margin-top: 12px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  max-height: 300px;
  overflow-y: auto;
}

.city-item {
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background-color 0.2s;
}

.city-item:hover {
  background-color: #f5f7fa;
}

.city-item:last-child {
  border-bottom: none;
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
