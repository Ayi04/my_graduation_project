<template>
  <div class="seat-list-container">
    <!-- 导航栏 -->
    <Navbar />

    <!-- 内容区域 -->
    <div class="content">
      <h1>座位信息</h1>
      
      <!-- 筛选条件 -->
      <div class="filter">
        <el-form :inline="true" :model="filterForm">
          <el-form-item label="楼层">
            <el-input v-model.number="filterForm.floor" placeholder="输入楼层" clearable style="width: 120px;"></el-input>
          </el-form-item>
          <el-form-item label="区域类型">
            <el-select v-model="filterForm.areaType" placeholder="选择区域类型" clearable style="width: 150px;">
              <el-option label="静音区" value="SILENT"></el-option>
              <el-option label="浏览区" value="BROWSING"></el-option>
              <el-option label="普通区" value="NORMAL"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="座位状态">
            <el-select v-model="filterForm.status" placeholder="选择状态" clearable style="width: 120px;">
              <el-option label="可用" value="AVAILABLE"></el-option>
              <el-option label="已预约" value="RESERVED"></el-option>
              <el-option label="已占用" value="OCCUPIED"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleFilter">筛选</el-button>
            <el-button @click="resetFilter">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 区域列表 -->
      <div class="area-list">
        <el-card v-for="area in areas" :key="area.id" class="area-card" @click="navigateToArea(area.id)">
          <template #header>
            <div class="area-header">
              <h3>{{ area.name }} ({{ area.floor }}楼)</h3>
              <span class="area-info">
                总座位数: {{ area.seatCount }} | 可用座位数: {{ area.availableCount }}
              </span>
            </div>
          </template>
          <div class="area-detail">
            <p>区域类型: {{ getAreaTypeText(area.type) }}</p>
            <p>点击查看详细座位信息</p>
          </div>
        </el-card>
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
import { useRouter } from 'vue-router'
import { useUserStore } from '../store/user'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import Navbar from '../components/Navbar.vue'

const router = useRouter()
const userStore = useUserStore()
const areas = ref([])
const allAreas = ref([]) // 保存所有区域数据，用于筛选
const selectedSeat = ref(null)
const reserveDialogVisible = ref(false)

const filterForm = reactive({
  floor: '',
  areaType: '',
  status: ''
})

const reserveForm = reactive({
  date: new Date(),
  startTime: new Date(),
  endTime: new Date()
})

const reserveRules = {
  date: [{ required: true, message: '请选择预约日期', trigger: 'change' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }]
}

const user = computed(() => userStore.user)
const userRole = computed(() => userStore.userRole)

const getAreaTypeText = (type) => {
  const typeMap = {
    SILENT: '静音区',
    BROWSING: '浏览区',
    NORMAL: '普通区'
  }
  return typeMap[type] || type
}

const navigateToArea = (areaId) => {
  router.push(`/area/${areaId}`)
}

const handleFilter = () => {
  // 根据筛选条件过滤区域列表
  filterAreas()
}

const resetFilter = () => {
  filterForm.floor = ''
  filterForm.areaType = ''
  filterForm.status = ''
  filterAreas()
}

const filterAreas = () => {
  let filteredAreas = [...allAreas.value]
  
  // 按楼层筛选
  if (filterForm.floor) {
    filteredAreas = filteredAreas.filter(area => area.floor == filterForm.floor)
  }
  
  // 按区域类型筛选
  if (filterForm.areaType) {
    filteredAreas = filteredAreas.filter(area => area.type === filterForm.areaType)
  }
  
  // 按座位状态筛选
  if (filterForm.status) {
    // 这里需要获取每个区域的座位信息，然后检查是否有符合状态的座位
    // 由于后端API可能不支持按座位状态筛选，我们暂时只过滤可用座位数大于0的区域
    if (filterForm.status === 'AVAILABLE') {
      filteredAreas = filteredAreas.filter(area => area.availableCount > 0)
    }
  }
  
  areas.value = filteredAreas
}

const selectSeat = (seat) => {
  if (seat.status === 'AVAILABLE') {
    selectedSeat.value = seat
    reserveDialogVisible.value = true
  } else {
    ElMessage.info('该座位不可预约')
  }
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
    
    // 准备预约数据
    const reserveData = {
      loginId: user.value.id,
      seatId: selectedSeat.value.id,
      startTime: new Date(`${reserveForm.date.toISOString().split('T')[0]} ${reserveForm.startTime.toTimeString().split(' ')[0]}`),
      endTime: new Date(`${reserveForm.date.toISOString().split('T')[0]} ${reserveForm.endTime.toTimeString().split(' ')[0]}`)
    }
    
    // 调用后端API进行座位预约
    const reserveResponse = await axios.post('http://localhost:8080/api/seat/reserve', reserveData)
    if (reserveResponse.data) {
      ElMessage.success('预约成功')
      reserveDialogVisible.value = false
      // 刷新座位列表
      loadSeats()
    } else {
      ElMessage.error('预约失败，请稍后重试')
    }
  } catch (error) {
    console.error('预约失败', error)
    ElMessage.error('预约失败，请稍后重试')
  }
}

const loadSeats = async () => {
  try {
    // 调用后端API获取区域信息
    const response = await axios.get('http://localhost:8080/api/seat/areas')
    const areasData = response.data
    
    // 处理区域数据
    allAreas.value = areasData.map(area => ({
      id: area.id,
      name: area.name,
      floor: area.floor,
      type: area.type,
      seatCount: area.seatCount,
      availableCount: Math.min(area.availableCount, area.seatCount) // 确保可用座位数不超过总座位数
    }))
    
    // 应用筛选条件
    filterAreas()
  } catch (error) {
    console.error('加载座位失败', error)
    // 如果API调用失败，使用模拟数据
    allAreas.value = [
      {
        id: 1,
        name: '静音区1',
        floor: 1,
        type: 'SILENT',
        seatCount: 10,
        availableCount: 8
      },
      {
        id: 2,
        name: '浏览区1',
        floor: 1,
        type: 'BROWSING',
        seatCount: 30,
        availableCount: 25
      },
      {
        id: 3,
        name: '普通区1',
        floor: 1,
        type: 'NORMAL',
        seatCount: 60,
        availableCount: 45
      },
      {
        id: 4,
        name: '静音区2',
        floor: 2,
        type: 'SILENT',
        seatCount: 15,
        availableCount: 12
      },
      {
        id: 5,
        name: '浏览区2',
        floor: 2,
        type: 'BROWSING',
        seatCount: 40,
        availableCount: 32
      },
      {
        id: 6,
        name: '普通区2',
        floor: 2,
        type: 'NORMAL',
        seatCount: 80,
        availableCount: 60
      }
    ]
    
    // 应用筛选条件
    filterAreas()
  }
}

onMounted(() => {
  userStore.loadUserFromStorage()
  loadSeats()
})
</script>

<style scoped>
.seat-list-container {
  min-height: 100vh;
  background: #f5f7fa;
}

.content {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
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
}

.area-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(450px, 1fr));
  gap: 20px;
}

.area-card {
  transition: all 0.3s ease;
  cursor: pointer;
}

.area-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 16px 0 rgba(0, 0, 0, 0.15);
}

.area-detail {
  padding: 20px 0;
}

.area-detail p {
  margin-bottom: 10px;
  color: #666;
}

.area-detail p:last-child {
  color: #1890ff;
  font-weight: bold;
}

.area-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.area-header h3 {
  margin: 0;
  color: #333;
}

.area-info {
  font-size: 12px;
  color: #999;
}

.seats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(60px, 1fr));
  gap: 10px;
  margin-top: 20px;
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
</style>