<template>
  <div class="reservation-list-container">
    <!-- 导航栏 -->
    <Navbar />

    <!-- 内容区域 -->
    <div class="content">
      <h1>预约信息</h1>
      
      <!-- 筛选条件 -->
      <div class="filter">
        <el-form :inline="true" :model="filterForm">
          <el-form-item label="预约状态">
            <el-select v-model="filterForm.status" placeholder="选择状态" style="width: 120px;">
              <el-option label="全部" value=""></el-option>
              <el-option label="待签到" value="CONFIRMED"></el-option>
              <el-option label="已签到" value="COMPLETED"></el-option>
              <el-option label="已取消" value="CANCELLED"></el-option>
              <el-option label="已结束" value="ENDED"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="预约日期">
            <el-date-picker
              v-model="filterForm.date"
              type="date"
              placeholder="选择日期"
              style="width: 180px;"
            ></el-date-picker>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleFilter">查询</el-button>
            <el-button @click="resetFilter">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 预约列表 -->
      <el-table :data="paginatedReservations" style="width: 100%">
        <el-table-column prop="id" label="预约ID" min-width="80"></el-table-column>
        <el-table-column prop="seat.seatNumber" label="座位号" min-width="120">
          <template #default="scope">
            {{ scope.row.seat?.seatNumber }}
          </template>
        </el-table-column>
        <el-table-column prop="seat.area.name" label="区域" min-width="150">
          <template #default="scope">
            {{ scope.row.seat?.area?.name }}
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="开始时间" min-width="180">
          <template #default="scope">
            {{ formatDateTime(scope.row.startTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="endTime" :label="endTimeColumnLabel" min-width="180">
          <template #default="scope">
            <span v-if="scope.row.status === 'CONFIRMED'">
              {{ scope.row.countdown || '计算中...' }}
            </span>
            <span v-else>
              {{ formatDateTime(scope.row.endTime) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" min-width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">{{ getStatusText(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="280">
          <template #default="scope">
            <el-button 
              type="primary" 
              size="small" 
              @click="checkIn(scope.row)" 
              v-if="scope.row.status === 'CONFIRMED'"
            >
              签到
            </el-button>
            <el-button 
              type="warning" 
              size="small" 
              @click="endUse(scope.row)" 
              v-if="scope.row.status === 'COMPLETED'"
            >
              结束使用
            </el-button>
            <el-button 
              type="danger" 
              size="small" 
              @click="cancelReservation(scope.row)" 
              v-if="scope.row.status === 'CONFIRMED'"
            >
              取消预约
            </el-button>
            <el-button 
              type="danger" 
              size="small" 
              @click="deleteReservation(scope.row)" 
              v-if="(scope.row.status === 'CANCELLED' || scope.row.status === 'COMPLETED') && (userRole !== 'USER')"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="filteredReservations.length"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../store/user'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import Navbar from '../components/Navbar.vue'

const router = useRouter()
const userStore = useUserStore()
const reservations = ref([])
const countdownTimer = ref(null)
const endTimeColumnLabel = ref('结束时间')
const checkinTimeLimit = ref(15) // 默认15分钟

// 分页相关
const currentPage = ref(1)
const pageSize = ref(10)

const filterForm = reactive({
  status: '',
  date: ''
})

const user = computed(() => userStore.user)
const userRole = computed(() => userStore.userRole)

// 过滤后的预约记录
const filteredReservations = ref([])

// 分页后的预约记录
const paginatedReservations = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredReservations.value.slice(start, end)
})

const handleFilter = () => {
  // 执行筛选逻辑
  filteredReservations.value = reservations.value.filter(reservation => {
    // 状态匹配
    const statusMatch = !filterForm.status || reservation.status === filterForm.status
    
    // 日期匹配
    const dateMatch = !filterForm.date || (() => {
      const recordDate = new Date(reservation.startTime).toDateString()
      const searchDate = filterForm.date.toDateString()
      return recordDate === searchDate
    })()
    
    return statusMatch && dateMatch
  })
  currentPage.value = 1 // 重置到第一页
}

const resetFilter = () => {
  filterForm.status = ''
  filterForm.date = ''
  filteredReservations.value = [...reservations.value]
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

const getStatusText = (status) => {
  const statusMap = {
    CONFIRMED: '待签到',
    COMPLETED: '已签到',
    CANCELLED: '已取消',
    ENDED: '已结束'
  }
  return statusMap[status] || status
}

const getStatusType = (status) => {
  const typeMap = {
    CONFIRMED: 'info',
    COMPLETED: 'success',
    CANCELLED: 'warning',
    ENDED: 'success'
  }
  return typeMap[status] || 'info'
}

const formatDateTime = (date) => {
  if (!date) return ''
  const d = new Date(date)
  return `${d.getFullYear()}-${(d.getMonth() + 1).toString().padStart(2, '0')}-${d.getDate().toString().padStart(2, '0')} ${d.getHours().toString().padStart(2, '0')}:${d.getMinutes().toString().padStart(2, '0')}`
}

const loadCheckinTimeLimit = async () => {
  try {
    const response = await axios.get('http://localhost:8080/api/system/settings')
    if (response.data.checkinTimeLimit) {
      checkinTimeLimit.value = response.data.checkinTimeLimit
    }
  } catch (error) {
    console.error('加载签到时间限制失败', error)
    // 保持默认值15分钟
  }
}

const updateCountdowns = () => {
  const now = Date.now()
  let hasActiveCountdowns = false
  
  reservations.value.forEach(reservation => {
    if (reservation.status === 'CONFIRMED') {
      const startTime = reservation.startTime.getTime()
      const deadline = startTime + checkinTimeLimit.value * 60 * 1000 // 开始时间后checkinTimeLimit分钟
      const remaining = deadline - now
      
      if (remaining > 0) {
        // 计算剩余时间
        const minutes = Math.floor(remaining / 60000)
        const seconds = Math.floor((remaining % 60000) / 1000)
        reservation.countdown = `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`
        hasActiveCountdowns = true
      } else {
        // 倒计时结束，刷新预约列表
        loadReservations()
      }
    }
  })
  
  // 如果没有活跃的倒计时，停止定时器
  if (!hasActiveCountdowns && countdownTimer.value) {
    clearInterval(countdownTimer.value)
    countdownTimer.value = null
  }
}

const startCountdowns = async () => {
  // 先加载签到时间限制
  await loadCheckinTimeLimit()
  
  // 清理之前的定时器
  if (countdownTimer.value) {
    clearInterval(countdownTimer.value)
  }
  
  // 立即更新一次
  updateCountdowns()
  
  // 启动定时器，每秒更新一次
  countdownTimer.value = setInterval(updateCountdowns, 1000)
}

const clearCountdowns = () => {
  if (countdownTimer.value) {
    clearInterval(countdownTimer.value)
    countdownTimer.value = null
  }
}

const checkIn = async (reservation) => {
  if (!user.value || !user.value.id) {
    ElMessage.error('用户信息获取失败，请重新登录')
    return
  }
  
  try {
    // 调用后端API完成签到
    const response = await axios.post('http://localhost:8080/api/checkin', {
      loginId: user.value.id,
      reservationId: reservation.id
    })
    
    const checkin = response.data
    const checkinTime = new Date(checkin.checkinTime).toLocaleTimeString()
    
    ElMessage.success(`签到成功！签到时间：${checkinTime}`)
    // 刷新预约列表
    loadReservations()
  } catch (error) {
    console.error('签到失败', error)
    ElMessage.error('签到失败，请稍后重试')
  }
}

// 计算两个坐标点之间的距离（Haversine公式）
const calculateDistance = (lat1, lon1, lat2, lon2) => {
  const R = 6371e3; // 地球半径（米）
  const φ1 = lat1 * Math.PI / 180
  const φ2 = lat2 * Math.PI / 180
  const Δφ = (lat2 - lat1) * Math.PI / 180
  const Δλ = (lon2 - lon1) * Math.PI / 180

  const a = Math.sin(Δφ/2) * Math.sin(Δφ/2) +
          Math.cos(φ1) * Math.cos(φ2) *
          Math.sin(Δλ/2) * Math.sin(Δλ/2)
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a))

  return R * c
}

const endUse = async (reservation) => {
  if (!user.value || !user.value.id) {
    ElMessage.error('用户信息获取失败，请重新登录')
    return
  }
  
  // 检查预约状态，只有已签到的预约才能结束使用
  if (reservation.status === 'ENDED') {
    ElMessage.warning('已结束使用，请勿重复点击')
    return
  } else if (reservation.status !== 'COMPLETED') {
    ElMessage.warning('该预约已结束使用或状态不正确')
    return
  }
  
  try {
    // 调用后端API结束使用
    await axios.post('http://localhost:8080/api/seat/endUse', null, {
      params: {
        reservationId: reservation.id,
        loginId: user.value.id
      }
    })
    ElMessage.success('结束使用成功')
    // 刷新预约列表
    loadReservations()
  } catch (error) {
    console.error('结束使用失败', error)
    ElMessage.error('结束使用失败，请稍后重试')
  }
}

const cancelReservation = async (reservation) => {
  if (!user.value || !user.value.id) {
    ElMessage.error('用户信息获取失败，请重新登录')
    return
  }
  
  try {
    await ElMessageBox.confirm('确定要取消这条预约吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 调用后端API取消预约
    await axios.post('http://localhost:8080/api/seat/cancel', null, {
      params: {
        reservationId: reservation.id,
        loginId: user.value.id
      }
    })
    ElMessage.success('取消预约成功')
    // 刷新预约列表
    loadReservations()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消预约失败', error)
      ElMessage.error('取消预约失败，请稍后重试')
    }
  }
}

const deleteReservation = async (reservation) => {
  if (!user.value || !user.value.id) {
    ElMessage.error('用户信息获取失败，请重新登录')
    return
  }
  
  try {
    // 调用后端API删除预约
    await axios.delete('http://localhost:8080/api/seat/delete', {
      params: {
        reservationId: reservation.id,
        loginId: user.value.id
      }
    })
    ElMessage.success('删除预约成功')
    // 刷新预约列表
    loadReservations()
  } catch (error) {
    console.error('删除预约失败', error)
    ElMessage.error('删除预约失败，请稍后重试')
  }
}

const loadReservations = async () => {
  if (!user.value || !user.value.id) {
    ElMessage.error('用户信息获取失败，请重新登录')
    return
  }
  
  try {
    // 调用后端API获取预约记录
    const response = await axios.get(`http://localhost:8080/api/seat/reservations/login/${user.value.id}`)
    const reservationsData = response.data
    
    // 处理预约数据
    reservations.value = reservationsData.map(reservation => ({
      id: reservation.id,
      seat: {
        seatNumber: reservation.seat.seatNumber,
        area: {
          name: reservation.seat.area.name
        }
      },
      startTime: new Date(reservation.startTime),
      endTime: new Date(reservation.endTime),
      status: reservation.status
    }))
  } catch (error) {
    console.error('加载预约记录失败', error)
    // 如果API调用失败，使用模拟数据
    reservations.value = [
      {
        id: 1,
        seat: {
          seatNumber: '1-S1',
          area: {
            name: '静音区1'
          }
        },
        startTime: new Date(),
        endTime: new Date(Date.now() + 2 * 60 * 60 * 1000),
        status: 'CONFIRMED'
      },
      {
        id: 2,
        seat: {
          seatNumber: '1-B1',
          area: {
            name: '浏览区1'
          }
        },
        startTime: new Date(Date.now() - 2 * 60 * 60 * 1000),
        endTime: new Date(Date.now() + 1 * 60 * 60 * 1000),
        status: 'COMPLETED'
      },
      {
        id: 3,
        seat: {
          seatNumber: '2-N1',
          area: {
            name: '普通区2'
          }
        },
        startTime: new Date(Date.now() + 4 * 60 * 60 * 1000),
        endTime: new Date(Date.now() + 6 * 60 * 60 * 1000),
        status: 'CONFIRMED'
      }
    ]
    ElMessage.error('加载预约记录失败，使用模拟数据')
  } finally {
    // 初始化过滤后的预约记录
    filteredReservations.value = [...reservations.value]
    // 检查是否有未签到的预约
    const hasConfirmedReservations = reservations.value.some(reservation => reservation.status === 'CONFIRMED')
    // 更新列标签
    endTimeColumnLabel.value = hasConfirmedReservations ? '倒计时' : '结束时间'
    // 启动倒计时
    await startCountdowns()
  }
}

onMounted(() => {
  userStore.loadUserFromStorage()
  loadReservations()
})

onUnmounted(() => {
  // 清理倒计时定时器
  clearCountdowns()
})
</script>

<style scoped>
.reservation-list-container {
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
}

.filter {
  margin-bottom: 30px;
  padding: 20px;
  background: white;
  border-radius: 10px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.filter .el-form {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.filter .el-form-item {
  margin-bottom: 0;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>