<template>
  <div class="statistics-container">
    <!-- 导航栏 -->
    <Navbar />

    <div class="content">
      <h1>数据统计</h1>
    
    <div class="stats-cards">
      <el-card class="stat-card">
        <template #header>
          <div class="card-header">
            <span>总用户数</span>
          </div>
        </template>
        <div class="stat-value">{{ totalUsers }}</div>
      </el-card>
      
      <el-card class="stat-card">
        <template #header>
          <div class="card-header">
            <span>总工作人员数</span>
          </div>
        </template>
        <div class="stat-value">{{ totalStaff }}</div>
      </el-card>
      
      <el-card class="stat-card">
        <template #header>
          <div class="card-header">
            <span>总座位数</span>
          </div>
        </template>
        <div class="stat-value">{{ totalSeats }}</div>
      </el-card>
      
      <el-card class="stat-card">
        <template #header>
          <div class="card-header">
            <span>今日预约数</span>
          </div>
        </template>
        <div class="stat-value">{{ todayReservations }}</div>
      </el-card>
    </div>
    
    <div class="charts-section">
      <el-card class="chart-card">
        <template #header>
          <div class="card-header">
            <span>座位使用情况</span>
          </div>
        </template>
        <div id="usageChart" class="chart"></div>
      </el-card>
      
      <el-card class="chart-card">
        <template #header>
          <div class="card-header">
            <span>反馈与投诉分布</span>
          </div>
        </template>
        <div id="feedbackChart" class="chart"></div>
      </el-card>
    </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useUserStore } from '../store/user'
import * as echarts from 'echarts'
import axios from 'axios'
import Navbar from '../components/Navbar.vue'

const userStore = useUserStore()

const totalUsers = ref(0)
const totalStaff = ref(0)
const totalSeats = ref(0)
const todayReservations = ref(0)
const usageChart = ref(null)
const feedbackChart = ref(null)

const user = computed(() => userStore.user)
const userRole = computed(() => userStore.userRole)


const loadStatistics = async () => {
  try {
    // 调用后端API获取统计数据
    const response = await axios.get('http://localhost:8080/api/statistics')
    const data = response.data
    
    totalUsers.value = data.totalUsers || 0
    totalStaff.value = data.totalStaff || 0
    totalSeats.value = data.totalSeats || 0
    todayReservations.value = data.todayReservations || 0
  } catch (error) {
    console.error('加载统计数据失败', error)
    // 使用模拟数据
    totalUsers.value = 1200
    totalStaff.value = 20
    totalSeats.value = 500
    todayReservations.value = 350
  }
}

const loadUsageStatus = async () => {
  try {
    // 调用后端API获取座位使用情况数据
    console.log('开始调用后端API获取座位使用情况数据')
    const response = await axios.get('http://localhost:8080/api/statistics/usage-status')
    console.log('后端API返回的数据:', response.data)
    return response.data
  } catch (error) {
    console.error('加载座位使用情况数据失败', error)
    // 使用基于实际区域结构的模拟数据
    console.log('使用前端模拟数据')
    return {
      areaNames: ['静音区1', '浏览区1', '普通区1', '静音区2', '浏览区2', '普通区2', '静音区3', '浏览区3', '普通区3'],
      usageCounts: [18, 15, 12, 20, 16, 10, 16, 14, 9],
      seatCounts: [20, 40, 40, 20, 40, 40, 20, 40, 40],
      floors: ['1', '2', '3'],
      floorAreaMap: {
        '1': [
          { areaName: '静音区1', usageCount: 18, seatCount: 20, floor: '1' },
          { areaName: '浏览区1', usageCount: 15, seatCount: 40, floor: '1' },
          { areaName: '普通区1', usageCount: 12, seatCount: 40, floor: '1' }
        ],
        '2': [
          { areaName: '静音区2', usageCount: 20, seatCount: 20, floor: '2' },
          { areaName: '浏览区2', usageCount: 16, seatCount: 40, floor: '2' },
          { areaName: '普通区2', usageCount: 10, seatCount: 40, floor: '2' }
        ],
        '3': [
          { areaName: '静音区3', usageCount: 16, seatCount: 20, floor: '3' },
          { areaName: '浏览区3', usageCount: 14, seatCount: 40, floor: '3' },
          { areaName: '普通区3', usageCount: 9, seatCount: 40, floor: '3' }
        ]
      }
    }
  }
}

const loadFeedbackDistribution = async () => {
  try {
    // 调用后端API获取反馈与投诉分布数据
    const response = await axios.get('http://localhost:8080/api/statistics/feedback-distribution')
    return response.data
  } catch (error) {
    console.error('加载反馈与投诉分布数据失败', error)
    // 使用模拟数据
    return {
      types: ['反馈', '投诉'],
      counts: [60, 30]
    }
  }
}

const initUsageChart = async () => {
  const chartDom = document.getElementById('usageChart')
  if (!chartDom) return
  
  usageChart.value = echarts.init(chartDom)
  
  // 获取座位使用情况数据
  const statusData = await loadUsageStatus()
  
  // 确保数据存在
  if (!statusData || !statusData.areaNames || !statusData.usageCounts) {
    console.error('座位使用情况数据不存在')
    return
  }
  
  console.log('准备显示的数据:', statusData)
  
  const option = {
    title: {
      text: '各区域今日使用总数对比',
      left: 'center'
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      },
      formatter: function(params) {
        const index = params[0].dataIndex
        return `区域名称: ${statusData.areaNames[index]}<br/>今日使用总数: ${statusData.usageCounts[index]}<br/>总座位数: ${statusData.seatCounts ? statusData.seatCounts[index] : 0}`
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '15%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: statusData.areaNames,
      axisLabel: {
        rotate: 45
      }
    },
    yAxis: {
      type: 'value',
      name: '今日使用总数'
    },
    series: [
      {
        name: '今日使用总数',
        type: 'bar',
        data: statusData.usageCounts,
        itemStyle: {
          color: '#409EFF'
        },
        barWidth: '60%',
        label: {
          show: true,
          position: 'top',
          formatter: '{c}'
        }
      }
    ]
  }
  
  usageChart.value.setOption(option)
}

const initFeedbackChart = async () => {
  const chartDom = document.getElementById('feedbackChart')
  if (!chartDom) return
  
  feedbackChart.value = echarts.init(chartDom)
  
  // 获取反馈与投诉分布数据
  const distributionData = await loadFeedbackDistribution()
  
  // 准备饼图数据
  const pieData = distributionData.types.map((type, index) => ({
    name: type,
    value: distributionData.counts[index]
  }))
  
  const option = {
    title: {
      text: '反馈与投诉分布',
      left: 'center'
    },
    tooltip: {
      trigger: 'item'
    },
    legend: {
      orient: 'vertical',
      left: 'left'
    },
    series: [
      {
        name: '类型',
        type: 'pie',
        radius: '50%',
        data: pieData,
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }
    ]
  }
  
  feedbackChart.value.setOption(option)
}

onMounted(async () => {
  userStore.loadUserFromStorage()
  loadStatistics()
  await initUsageChart()
  await initFeedbackChart()
  
  // 响应式调整
  window.addEventListener('resize', () => {
    usageChart.value?.resize()
    feedbackChart.value?.resize()
  })
})
</script>

<style scoped>
.statistics-container {
  min-height: 100vh;
  background: #f5f7fa;
}

.content {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
}

h1 {
  color: #333;
  margin-bottom: 30px;
  border-bottom: 1px solid #eaeaea;
  padding-bottom: 10px;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.stat-card {
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 16px 0 rgba(0, 0, 0, 0.15);
}

.card-header {
  font-weight: bold;
  color: #333;
}

.stat-value {
  font-size: 36px;
  font-weight: bold;
  color: #1890ff;
  text-align: center;
  margin-top: 20px;
}

.charts-section {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.chart-card {
  height: 400px;
}

.chart {
  width: 100%;
  height: 350px;
}

@media (max-width: 768px) {
  .charts-section {
    grid-template-columns: 1fr;
  }
}
</style>