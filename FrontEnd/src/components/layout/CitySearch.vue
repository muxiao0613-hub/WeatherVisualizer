<template>
  <div class="city-search">
    <el-input
      v-model="keyword"
      placeholder="搜索城市（支持中文和拼音）..."
      :prefix-icon="Search"
      clearable
      @input="handleSearch"
      @keyup.enter="handleSearch"
    />
    <div v-if="searchResults.length > 0" class="search-results">
      <div
        v-for="city in searchResults"
        :key="city.name + city.state + city.country"
        class="city-item"
        @click="selectCity(city)"
      >
        <div class="city-name">{{ city.name }}</div>
        <div class="city-location">
          <span v-if="city.state" class="state">{{ city.state }}</span>
          <span class="country">{{ city.country }}</span>
        </div>
      </div>
    </div>
    <div v-else-if="keyword && keyword.trim() && searchResults.length === 0" class="search-tips">
      <div class="tip-item">💡 搜索提示：</div>
      <div class="tip-item">• 尝试使用城市全名，如"北京"、"上海"</div>
      <div class="tip-item">• 支持拼音搜索，如"beijing"、"shanghai"</div>
      <div class="tip-item">• 常见城市：北京、上海、广州、深圳、杭州、南京</div>
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
let searchTimeout: number | null = null

const handleSearch = () => {
  console.log('handleSearch called, keyword:', keyword.value)
  
  if (searchTimeout) {
    clearTimeout(searchTimeout)
  }

  if (!keyword.value.trim()) {
    console.log('Keyword is empty, clearing results')
    searchResults.value = []
    return
  }

  console.log('Setting timeout for search...')
  searchTimeout = setTimeout(async () => {
    try {
      console.log('Searching cities with keyword:', keyword.value)
      const results = await cityApi.searchCities(keyword.value)
      console.log('Search results received:', results)
      console.log('Results length:', results?.length || 0)
      searchResults.value = results || []
    } catch (error) {
      console.error('Search failed:', error)
      searchResults.value = []
    }
  }, 300)
}

const selectCity = (city: CityDTO) => {
  console.log('=== City selected ===')
  console.log('Selected city:', city)
  console.log('City name:', city.name)
  console.log('City lat:', city.lat)
  console.log('City lon:', city.lon)
  console.log('Current city before update:', cityStore.currentCity)
  console.log('Calling cityStore.setCurrentCity...')
  
  cityStore.setCurrentCity(city)
  
  console.log('City store updated, current city is now:', cityStore.currentCity)
  console.log('Clearing search results...')
  
  keyword.value = ''
  searchResults.value = []
  
  console.log('=== City selection complete ===')
  console.log('城市已更新，Dashboard的watch监听器将自动触发天气数据加载')
}
</script>

<style scoped>
.city-search {
  margin-bottom: 20px;
  position: relative;
  z-index: 1000;
}

.search-results {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  margin-top: 8px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  max-height: 300px;
  overflow-y: auto;
  z-index: 1001;
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
  font-size: 14px;
}

.city-location {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
  display: flex;
  gap: 4px;
}

.city-location .state {
  color: #606266;
}

.city-location .country {
  color: #909399;
}

.search-tips {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  margin-top: 8px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  padding: 16px;
  z-index: 1001;
}

.tip-item {
  padding: 6px 0;
  font-size: 13px;
  color: #606266;
  line-height: 1.6;
}

.tip-item:first-child {
  font-weight: 600;
  color: #409eff;
  margin-bottom: 8px;
}
</style>
