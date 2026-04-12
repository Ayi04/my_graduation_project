<template>
  <div class="area-detail-container">
    <!-- 导航栏 -->
    <Navbar />

    <!-- 内容区域 -->
    <div class="content">
      <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
        <h1>{{ area?.name }} ({{ area?.floor }}楼)</h1>
        <el-button type="primary" @click="goBack">返回</el-button>
      </div>
      
      <div class="area-info">
        <p>区域类型: {{ getAreaTypeText(area?.type) }}</p>
        <p>总座位数: {{ area?.seatCount }} | 可用座位数: {{ area?.availableCount }}</p>
      </div>

      <!-- 座位网格 -->
      <div class="seats-grid">
        <div 
          v-for="seat in area?.seats" 
          :key="seat.id" 
          class="seat-item" 
          :class="{
            'available': seat.status === 'AVAILABLE',
            'reserved': seat.status === 'RESERVED',
            'occupied': seat.status === 'OCCUPIED'
          }"
          @click="selectSeat(seat)"
        >
          {{ seat.seatNumber }}
        </div>
      </div>

      <!-- 座位预约对话框 -->
      <el-dialog
        v-model="reserveDialogVisible"
        title="座位预约"
        width="500px"
      >
        <el-form :model="reserveForm" :rules="reserveRules" ref="reserveFormRef" label-width="80px">
          <el-form-item label="座位信息">
            <p>{{ selectedSeat?.area.name }} - {{ selectedSeat?.seatNumber }}</p>
          </el-form-item>
          <el-form-item label="预约类型" prop="reserveType">
            <el-radio-group v-model="reserveForm.reserveType">
              <el-radio label="current">立即预约</el-radio>
              <el-radio label="tomorrow" :disabled="!isTomorrowReserveAvailable" @click="handleTomorrowReserveClick">明日预约</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="预约日期" prop="date">
            <el-date-picker
              v-model="reserveForm.date"
              type="date"
              placeholder="选择日期"
              style="width: 100%;"
            ></el-date-picker>
          </el-form-item>
          <el-form-item label="开始时间" prop="startTime">
            <el-time-picker
              v-model="reserveForm.startTime"
              placeholder="选择开始时间"
              style="width: 100%;"
            ></el-time-picker>
          </el-form-item>
          <el-form-item label="结束时间" prop="endTime">
            <el-time-picker
              v-model="reserveForm.endTime"
              placeholder="选择结束时间"
              style="width: 100%;"
            ></el-time-picker>
          </el-form-item>
        </el-form>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="reserveDialogVisible = false">取消</el-button>
            <el-button type="primary" @click="handleReserve">确认预约</el-button>
          </span>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '../store/user'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import Navbar from '../components/Navbar.vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const area = ref(null)
const selectedSeat = ref(null)
const reserveDialogVisible = ref(false)

const reserveForm = reactive({
  reserveType: 'current',
  date: new Date(),
  startTime: new Date(),
  endTime: new Date()
})

const reserveRules = {
  reserveType: [{ required: true, message: '请选择预约类型', trigger: 'change' }],
  date: [{ required: true, message: '请选择预约日期', trigger: 'change' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }]
}

const user = computed(() => userStore.user)
const userRole = computed(() => userStore.userRole)

// 检查明日预约是否可用
const isTomorrowReserveAvailable = computed(() => {
  const now = new Date()
  const hour = now.getHours()
  return hour >= 21 && hour < 22
})



const getAreaTypeText = (type) => {
  const typeMap = {
    SILENT: '静音区',
    BROWSING: '浏览区',
    NORMAL: '普通区'
  }
  return typeMap[type] || type
}

const selectSeat = (seat) => {
  if (seat.status === 'AVAILABLE') {
    selectedSeat.value = seat
    reserveDialogVisible.value = true
  } else {
    ElMessage.info('该座位不可预约')
  }
}

const handleTomorrowReserveClick = () => {
  if (!isTomorrowReserveAvailable.value) {
    ElMessage.info('明日预约开放时间: 21:00-22:00')
  }
}

const goBack = () => {
  router.push('/seats')
}

const handleReserve = async () => {
  if (!selectedSeat.value) return
  if (!user.value || !user.value.id) {
    ElMessage.error('用户信息获取失败，请重新登录')
    return
  }
  
  try {
    // 检查用户是否可以预约
    const canReserveResponse = await axios.get(`http://localhost:8080/api/user/canReserve/${user.value.id}`)
    if (!canReserveResponse.data) {
      // 如果不能预约，获取用户信息查看是否被禁止
      const userResponse = await axios.get(`http://localhost:8080/api/user/user/${user.value.id}`)
      const userData = userResponse.data
      if (userData.isBanned && userData.banEndTime) {
        // 计算剩余禁止天数
        const banEndTime = new Date(userData.banEndTime)
        const currentTime = new Date()
        const diffTime = Math.abs(banEndTime - currentTime)
        const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))
        ElMessage.error(`您已被禁止预约 ${diffDays} 天`)
      } else {
        ElMessage.error('预约失败，可能是因为信用分不足')
      }
      return
    }
    
    // 检查图书馆是否处于关闭状态
    const systemSettingsResponse = await axios.get('http://localhost:8080/api/system/settings')
    const systemSettings = systemSettingsResponse.data
    if (systemSettings.isLibraryClosed) {
      // 检查当前时间是否在关闭时间范围内
      const now = new Date()
      const closeStartTime = new Date(systemSettings.closeStartTime)
      const closeEndTime = new Date(systemSettings.closeEndTime)
      if (now.getTime() >= closeStartTime.getTime() && now.getTime() <= closeEndTime.getTime()) {
        ElMessage.error('图书馆当前处于关闭状态，无法预约')
        return
      }
    }
    
    // 格式化时间为后端期望的格式
    const formatDate = (date) => {
      if (date instanceof Date) {
        return date.toISOString()
      }
      return date
    }
    
    const formatTime = (time) => {
      if (time instanceof Date) {
        const hours = time.getHours().toString().padStart(2, '0')
        const minutes = time.getMinutes().toString().padStart(2, '0')
        return `${hours}:${minutes}`
      }
      return time
    }
    
    const startTime = new Date(`${reserveForm.date.getFullYear()}-${(reserveForm.date.getMonth() + 1).toString().padStart(2, '0')}-${reserveForm.date.getDate().toString().padStart(2, '0')} ${formatTime(reserveForm.startTime)}`)
    const endTime = new Date(`${reserveForm.date.getFullYear()}-${(reserveForm.date.getMonth() + 1).toString().padStart(2, '0')}-${reserveForm.date.getDate().toString().padStart(2, '0')} ${formatTime(reserveForm.endTime)}`)
    
    // 调用后端API进行座位预约
    const response = await axios.post('http://localhost:8080/api/seat/reserve', {
      loginId: user.value.id,
      seatId: selectedSeat.value.id,
      startTime: startTime.toISOString(),
      endTime: endTime.toISOString()
    })
    
    const reservation = response.data
    if (reservation) {
      ElMessage.success('预约成功')
      reserveDialogVisible.value = false
      // 刷新座位列表
      loadAreaDetail()
    } else {
      ElMessage.error('预约失败，可能是因为预约时间不在开放时间范围内或座位已被预约')
    }
  } catch (error) {
    console.error('预约失败', error)
    ElMessage.error('预约失败，请稍后重试')
  }
}

const loadAreaDetail = async () => {
  const areaId = route.params.id
  try {
    // 调用后端API获取所有区域，然后找到对应ID的区域
    const areasResponse = await axios.get('http://localhost:8080/api/seat/areas')
    const areasData = areasResponse.data
    const areaData = areasData.find(a => a.id == areaId)
    
    // 调用后端API获取该区域的座位信息
    const seatsResponse = await axios.get(`http://localhost:8080/api/seat/seats/${areaId}`)
    const seatsData = seatsResponse.data
    
    // 处理数据
    const seats = seatsData.map(seat => ({
      id: seat.id,
      seatNumber: seat.seatNumber,
      status: seat.status,
      area: { name: areaData.name }
    }))
    
    // 计算实际可用座位数
    const actualAvailableCount = seats.filter(seat => seat.status === 'AVAILABLE').length
    
    area.value = {
      id: areaData.id,
      name: areaData.name,
      floor: areaData.floor,
      type: areaData.type,
      seatCount: areaData.seatCount,
      availableCount: actualAvailableCount,
      seats: seats
    }
  } catch (error) {
    console.error('加载区域详情失败', error)
    // 如果API调用失败，使用模拟数据
    const mockAreas = [
      {
        id: 1,
        name: '静音区1',
        floor: 1,
        type: 'SILENT',
        seatCount: 10,
        availableCount: 8,
        seats: [
          { id: 1, seatNumber: '1-S1', status: 'AVAILABLE', area: { name: '静音区1' } },
          { id: 2, seatNumber: '1-S2', status: 'RESERVED', area: { name: '静音区1' } },
          { id: 3, seatNumber: '1-S3', status: 'AVAILABLE', area: { name: '静音区1' } },
          { id: 4, seatNumber: '1-S4', status: 'AVAILABLE', area: { name: '静音区1' } },
          { id: 5, seatNumber: '1-S5', status: 'OCCUPIED', area: { name: '静音区1' } },
          { id: 6, seatNumber: '1-S6', status: 'AVAILABLE', area: { name: '静音区1' } },
          { id: 7, seatNumber: '1-S7', status: 'AVAILABLE', area: { name: '静音区1' } },
          { id: 8, seatNumber: '1-S8', status: 'AVAILABLE', area: { name: '静音区1' } },
          { id: 9, seatNumber: '1-S9', status: 'AVAILABLE', area: { name: '静音区1' } },
          { id: 10, seatNumber: '1-S10', status: 'AVAILABLE', area: { name: '静音区1' } }
        ]
      },
      {
        id: 2,
        name: '浏览区1',
        floor: 1,
        type: 'BROWSING',
        seatCount: 30,
        availableCount: 25,
        seats: [
          { id: 11, seatNumber: '1-B1', status: 'AVAILABLE', area: { name: '浏览区1' } },
          { id: 12, seatNumber: '1-B2', status: 'AVAILABLE', area: { name: '浏览区1' } },
          { id: 13, seatNumber: '1-B3', status: 'RESERVED', area: { name: '浏览区1' } },
          { id: 14, seatNumber: '1-B4', status: 'OCCUPIED', area: { name: '浏览区1' } },
          { id: 15, seatNumber: '1-B5', status: 'AVAILABLE', area: { name: '浏览区1' } },
          { id: 16, seatNumber: '1-B6', status: 'AVAILABLE', area: { name: '浏览区1' } },
          { id: 17, seatNumber: '1-B7', status: 'AVAILABLE', area: { name: '浏览区1' } },
          { id: 18, seatNumber: '1-B8', status: 'AVAILABLE', area: { name: '浏览区1' } },
          { id: 19, seatNumber: '1-B9', status: 'AVAILABLE', area: { name: '浏览区1' } },
          { id: 20, seatNumber: '1-B10', status: 'AVAILABLE', area: { name: '浏览区1' } }
        ]
      },
      {
        id: 3,
        name: '普通区1',
        floor: 1,
        type: 'NORMAL',
        seatCount: 60,
        availableCount: 45,
        seats: [
          { id: 31, seatNumber: '1-N1', status: 'AVAILABLE', area: { name: '普通区1' } },
          { id: 32, seatNumber: '1-N2', status: 'AVAILABLE', area: { name: '普通区1' } },
          { id: 33, seatNumber: '1-N3', status: 'RESERVED', area: { name: '普通区1' } },
          { id: 34, seatNumber: '1-N4', status: 'OCCUPIED', area: { name: '普通区1' } },
          { id: 35, seatNumber: '1-N5', status: 'AVAILABLE', area: { name: '普通区1' } },
          { id: 36, seatNumber: '1-N6', status: 'AVAILABLE', area: { name: '普通区1' } },
          { id: 37, seatNumber: '1-N7', status: 'AVAILABLE', area: { name: '普通区1' } },
          { id: 38, seatNumber: '1-N8', status: 'AVAILABLE', area: { name: '普通区1' } },
          { id: 39, seatNumber: '1-N9', status: 'AVAILABLE', area: { name: '普通区1' } },
          { id: 40, seatNumber: '1-N10', status: 'AVAILABLE', area: { name: '普通区1' } }
        ]
      },
      {
        id: 4,
        name: '静音区2',
        floor: 2,
        type: 'SILENT',
        seatCount: 15,
        availableCount: 12,
        seats: [
          { id: 61, seatNumber: '2-S1', status: 'AVAILABLE', area: { name: '静音区2' } },
          { id: 62, seatNumber: '2-S2', status: 'RESERVED', area: { name: '静音区2' } },
          { id: 63, seatNumber: '2-S3', status: 'AVAILABLE', area: { name: '静音区2' } },
          { id: 64, seatNumber: '2-S4', status: 'AVAILABLE', area: { name: '静音区2' } },
          { id: 65, seatNumber: '2-S5', status: 'OCCUPIED', area: { name: '静音区2' } }
        ]
      },
      {
        id: 5,
        name: '浏览区2',
        floor: 2,
        type: 'BROWSING',
        seatCount: 40,
        availableCount: 32,
        seats: [
          { id: 76, seatNumber: '2-B1', status: 'AVAILABLE', area: { name: '浏览区2' } },
          { id: 77, seatNumber: '2-B2', status: 'AVAILABLE', area: { name: '浏览区2' } },
          { id: 78, seatNumber: '2-B3', status: 'RESERVED', area: { name: '浏览区2' } },
          { id: 79, seatNumber: '2-B4', status: 'OCCUPIED', area: { name: '浏览区2' } },
          { id: 80, seatNumber: '2-B5', status: 'AVAILABLE', area: { name: '浏览区2' } }
        ]
      },
      {
        id: 6,
        name: '普通区2',
        floor: 2,
        type: 'NORMAL',
        seatCount: 80,
        availableCount: 60,
        seats: [
          { id: 116, seatNumber: '2-N1', status: 'AVAILABLE', area: { name: '普通区2' } },
          { id: 117, seatNumber: '2-N2', status: 'AVAILABLE', area: { name: '普通区2' } },
          { id: 118, seatNumber: '2-N3', status: 'RESERVED', area: { name: '普通区2' } },
          { id: 119, seatNumber: '2-N4', status: 'OCCUPIED', area: { name: '普通区2' } },
          { id: 120, seatNumber: '2-N5', status: 'AVAILABLE', area: { name: '普通区2' } }
        ]
      }
    ]
    
    area.value = mockAreas.find(a => a.id == areaId)
    ElMessage.error('加载区域详情失败，使用模拟数据')
  }
}

onMounted(() => {
  userStore.loadUserFromStorage()
  loadAreaDetail()
})
</script>

<style scoped>
.area-detail-container {
  min-height: 100vh;
  background: #f5f7fa;
}

.content {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.content h1 {
  color: #333;
  margin-bottom: 20px;
}

.area-info {
  margin-bottom: 30px;
  padding: 20px;
  background: white;
  border-radius: 10px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.area-info p {
  margin-bottom: 10px;
  color: #666;
}

.seats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(60px, 1fr));
  gap: 10px;
  padding: 20px;
  background: white;
  border-radius: 10px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.seat-item {
  width: 60px;
  height: 60px;
  display: flex;
  justify-content: center;
  align-items: center;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.seat-item:hover {
  transform: scale(1.1);
}

.seat-item.available {
  background: #67c23a;
  color: white;
}

.seat-item.reserved {
  background: #e6a23c;
  color: white;
}

.seat-item.occupied {
  background: #f56c6c;
  color: white;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
}

.disabled-tip {
  color: #999;
  font-size: 12px;
  margin-top: 10px;
}
</style>