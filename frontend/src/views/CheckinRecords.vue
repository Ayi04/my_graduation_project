<template>
  <div class="checkin-records-container">
    <!-- 导航栏 -->
    <Navbar />

    <!-- 内容区域 -->
    <div class="content">
      <h1>签到记录</h1>
      
      <!-- 搜索和筛选 -->
      <div class="search-form">
        <el-form :inline="true" :model="searchForm">
          <el-form-item label="日期">
            <el-date-picker
              v-model="searchForm.date"
              type="date"
              placeholder="选择日期"
              style="width: 180px;"
            ></el-date-picker>
          </el-form-item>
          <el-form-item label="楼层">
            <el-input v-model="searchForm.floor" placeholder="输入楼层" style="width: 100px;"></el-input>
          </el-form-item>
          <el-form-item label="区域">
            <el-input v-model="searchForm.area" placeholder="区域名称" style="width: 150px;"></el-input>
          </el-form-item>
          <el-form-item label="座位号">
            <el-input v-model="searchForm.seat" placeholder="座位号" style="width: 100px;"></el-input>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="searchCheckinRecords">查询</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
      
      <!-- 签到记录列表 -->
      <el-table :data="paginatedCheckinRecords" style="width: 100%">
        <el-table-column prop="date" label="日期" min-width="120"></el-table-column>
        <el-table-column prop="time" label="签到时间" min-width="150"></el-table-column>
        <el-table-column prop="floor" label="楼层" min-width="80"></el-table-column>
        <el-table-column prop="area" label="区域" min-width="150"></el-table-column>
        <el-table-column prop="seat" label="座位号" min-width="100"></el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="filteredCheckinRecords.length"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../store/user'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import Navbar from '../components/Navbar.vue'

const router = useRouter()
const userStore = useUserStore()
const checkinRecords = ref([])
const filteredCheckinRecords = ref([])

// 分页相关
const currentPage = ref(1)
const pageSize = ref(10)

// 搜索表单
const searchForm = reactive({
  date: '',
  floor: '',
  area: '',
  seat: ''
})

const user = computed(() => userStore.user)
const userRole = computed(() => userStore.userRole)

// 分页后的签到记录
const paginatedCheckinRecords = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredCheckinRecords.value.slice(start, end)
})

// 搜索方法
const searchCheckinRecords = () => {
  let filtered = [...checkinRecords.value]
  
  // 按日期过滤
  if (searchForm.date) {
    // 使用本地时区处理搜索日期，与记录日期的处理方式保持一致
    const searchDate = searchForm.date
    const year = searchDate.getFullYear()
    const month = String(searchDate.getMonth() + 1).padStart(2, '0')
    const day = String(searchDate.getDate()).padStart(2, '0')
    const searchDateString = `${year}-${month}-${day}`
    filtered = filtered.filter(record => record.date === searchDateString)
  }
  
  // 按楼层过滤
  if (searchForm.floor) {
    filtered = filtered.filter(record => record.floor == searchForm.floor)
  }
  
  // 按区域过滤
  if (searchForm.area) {
    const areaName = searchForm.area.toLowerCase()
    filtered = filtered.filter(record => record.area.toLowerCase().includes(areaName))
  }
  
  // 按座位号过滤
  if (searchForm.seat) {
    const seatNumber = searchForm.seat.toLowerCase()
    filtered = filtered.filter(record => record.seat.toLowerCase().includes(seatNumber))
  }
  
  filteredCheckinRecords.value = filtered
  currentPage.value = 1 // 重置到第一页
}

// 重置搜索
const resetSearch = () => {
  searchForm.date = ''
  searchForm.floor = ''
  searchForm.area = ''
  searchForm.seat = ''
  filteredCheckinRecords.value = [...checkinRecords.value]
  currentPage.value = 1 // 重置到第一页
}

// 分页方法
const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1 // 重置到第一页
}

const handleCurrentChange = (current) => {
  currentPage.value = current
}

const loadCheckinRecords = async () => {
  if (!user.value || !user.value.id) {
    ElMessage.error('用户信息获取失败，请重新登录')
    return
  }
  
  try {
    // 调用后端API获取签到记录
    const response = await axios.get(`http://localhost:8080/api/checkin/login/${user.value.id}`)
    const checkins = response.data
    
    // 转换为前端需要的格式
    checkinRecords.value = checkins.map(checkin => {
      // 使用本地时区处理日期，避免时区偏移
      const checkinDate = new Date(checkin.checkinTime)
      const year = checkinDate.getFullYear()
      const month = String(checkinDate.getMonth() + 1).padStart(2, '0')
      const day = String(checkinDate.getDate()).padStart(2, '0')
      const dateString = `${year}-${month}-${day}`
      
      return {
        date: dateString,
        time: checkinDate.toLocaleTimeString(),
        floor: checkin.floor || '-',
        area: checkin.areaName || '-',
        seat: checkin.seatNumber || '-'
      }
    })
    
    // 初始化过滤后的签到记录
    filteredCheckinRecords.value = [...checkinRecords.value]
  } catch (error) {
    console.error('加载签到记录失败', error)
    ElMessage.error('加载签到记录失败，请稍后重试')
    
    // 如果API调用失败，使用模拟数据
    checkinRecords.value = [
      {
        date: '2026-03-15',
        time: '09:00',
        floor: '1',
        area: '静音区1',
        seat: '1-S1'
      },
      {
        date: '2026-03-14',
        time: '14:30',
        floor: '1',
        area: '浏览区1',
        seat: '1-B1'
      },
      {
        date: '2026-03-13',
        time: '10:00',
        floor: '2',
        area: '普通区2',
        seat: '2-N1'
      }
    ]
    
    // 初始化过滤后的签到记录
    filteredCheckinRecords.value = [...checkinRecords.value]
  }
}

onMounted(async () => {
  userStore.loadUserFromStorage()
  if (userRole.value === 'USER') {
    await loadCheckinRecords()
  }
})
</script>

<style scoped>
.checkin-records-container {
  min-height: 100vh;
  background: #f5f7fa;
}

.content {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
  width: 100%;
  box-sizing: border-box;
}

.content h1 {
  color: #333;
  margin-bottom: 20px;
  border-bottom: 1px solid #eaeaea;
  padding-bottom: 10px;
}

.search-form {
  margin-bottom: 30px;
  padding: 20px;
  background: white;
  border-radius: 10px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.search-form .el-form {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.search-form .el-form-item {
  margin-bottom: 0;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

</style>