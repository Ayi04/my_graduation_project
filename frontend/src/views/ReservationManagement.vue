<template>
  <div class="reservation-management-container">
    <!-- 导航栏 -->
    <Navbar />

    <div class="content">
      <h1>预约管理</h1>
      
      <!-- 搜索和筛选 -->
      <el-card class="search-card">
        <el-form :inline="true" class="search-form" style="display: flex; align-items: center; flex-wrap: nowrap;">
          <el-form-item label="用户名" style="margin-right: 10px;">
            <el-input v-model="searchUsername" placeholder="请输入用户名" style="width: 150px;"></el-input>
          </el-form-item>
          <el-form-item label="座位号" style="margin-right: 10px;">
            <el-input v-model="searchSeatNumber" placeholder="请输入座位号" style="width: 150px;"></el-input>
          </el-form-item>
          <el-form-item label="状态" style="margin-right: 10px;">
            <el-select v-model="searchStatus" placeholder="请选择状态" style="width: 120px;">
              <el-option label="全部" value=""></el-option>
              <el-option label="待签到" value="CONFIRMED"></el-option>
              <el-option label="已签到" value="COMPLETED"></el-option>
              <el-option label="已取消" value="CANCELLED"></el-option>
              <el-option label="已结束" value="ENDED"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item style="margin-right: 10px;">
            <el-button type="primary" @click="searchReservations">查询</el-button>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>
      </el-card>
      
      <!-- 预约列表 -->
      <el-card class="reservations-card">
        <template #header>
          <div class="card-header">
            <h3>预约列表</h3>
          </div>
        </template>
        <el-table :data="paginatedReservations" style="width: 100%">
          <el-table-column prop="id" label="ID" width="80"></el-table-column>
          <el-table-column prop="login.username" label="用户" width="120"></el-table-column>
          <el-table-column prop="seat.seatNumber" label="座位号" width="100"></el-table-column>
          <el-table-column prop="seat.area.name" label="区域" width="150"></el-table-column>
          <el-table-column prop="reservationTime" label="预约时间" width="180">
            <template #default="scope">
              {{ formatDateTime(scope.row.reservationTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="startTime" label="开始时间" width="180">
            <template #default="scope">
              {{ formatDateTime(scope.row.startTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="endTime" label="结束时间" width="180">
            <template #default="scope">
              {{ formatDateTime(scope.row.endTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="scope">
              <el-select v-model="scope.row.status" @change="updateReservationStatus(scope.row)">
                <el-option label="待签到" value="CONFIRMED"></el-option>
                <el-option label="已签到" value="COMPLETED"></el-option>
                <el-option label="已取消" value="CANCELLED"></el-option>
                <el-option label="已结束" value="ENDED"></el-option>
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120">
            <template #default="scope">
              <el-button type="danger" size="small" @click="deleteReservation(scope.row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <!-- 分页组件 -->
        <div class="pagination-container">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="reservations.length"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../store/user'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import Navbar from '../components/Navbar.vue'

const router = useRouter()
const userStore = useUserStore()

// 搜索和筛选
const searchUsername = ref('')
const searchSeatNumber = ref('')
const searchStatus = ref('')

// 预约列表
const reservations = ref([])
const allReservations = ref([])

// 分页相关
const currentPage = ref(1)
const pageSize = ref(10)
const paginatedReservations = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return reservations.value.slice(start, end)
})

const user = computed(() => userStore.user)
const userRole = computed(() => userStore.userRole)



const loadReservations = async () => {
  try {
    const response = await axios.get('http://localhost:8080/api/seat/reservations')
    allReservations.value = response.data
    reservations.value = allReservations.value
  } catch (error) {
    console.error('加载预约记录失败', error)
    ElMessage.error('加载预约记录失败，请稍后重试')
  }
}

const searchReservations = () => {
  let filteredReservations = allReservations.value
  
  if (searchUsername.value) {
    filteredReservations = filteredReservations.filter(reservation => 
      reservation.login && reservation.login.username.toLowerCase().includes(searchUsername.value.toLowerCase())
    )
  }
  
  if (searchSeatNumber.value) {
    filteredReservations = filteredReservations.filter(reservation => 
      reservation.seat && reservation.seat.seatNumber.toLowerCase().includes(searchSeatNumber.value.toLowerCase())
    )
  }
  
  if (searchStatus.value) {
    filteredReservations = filteredReservations.filter(reservation => 
      reservation.status === searchStatus.value
    )
  }
  
  reservations.value = filteredReservations
  currentPage.value = 1
}

const resetSearch = () => {
  searchUsername.value = ''
  searchSeatNumber.value = ''
  searchStatus.value = ''
  reservations.value = allReservations.value
  currentPage.value = 1
}

const updateReservationStatus = async (reservation) => {
  try {
    if (!reservation || !reservation.id) {
      ElMessage.error('预约信息获取失败')
      return
    }
    
    const response = await axios.put(`http://localhost:8080/api/seat/reservation/${reservation.id}`, {
      status: reservation.status
    })
    
    if (response.data) {
      ElMessage.success('状态更新成功')
      
      // 如果状态改为已签到，添加签到记录
      if (reservation.status === 'COMPLETED') {
        await addCheckinRecord(reservation)
      }
    } else {
      ElMessage.error('状态更新失败')
    }
  } catch (error) {
    console.error('更新预约状态失败', error)
    ElMessage.error('状态更新失败，请稍后重试')
  }
}

const addCheckinRecord = async (reservation) => {
  try {
    await axios.post('http://localhost:8080/api/checkin', {
      loginId: reservation.login.id,
      reservationId: reservation.id
    })
    console.log('签到记录添加成功')
  } catch (error) {
    console.error('添加签到记录失败', error)
    ElMessage.warning('状态更新成功，但签到记录添加失败')
  }
}

const deleteReservation = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除此预约记录吗？删除后将无法恢复。', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    if (!user.value || !user.value.id) {
      ElMessage.error('用户信息获取失败，请重新登录')
      return
    }
    
    await axios.delete(`http://localhost:8080/api/seat/delete`, {
      params: {
        reservationId: id,
        loginId: user.value.id
      }
    })
    
    ElMessage.success('删除成功')
    loadReservations()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除预约记录失败', error)
      ElMessage.error('删除失败，请稍后重试')
    }
  }
}

const formatDateTime = (date) => {
  if (!date) return ''
  const d = new Date(date)
  return `${d.getFullYear()}-${(d.getMonth() + 1).toString().padStart(2, '0')}-${d.getDate().toString().padStart(2, '0')} ${d.getHours().toString().padStart(2, '0')}:${d.getMinutes().toString().padStart(2, '0')}`
}

// 分页相关方法
const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
}

const handleCurrentChange = (current) => {
  currentPage.value = current
}

onMounted(() => {
  userStore.loadUserFromStorage()
  loadReservations()
})
</script>

<style scoped>
.reservation-management-container {
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
  border-bottom: 1px solid #eaeaea;
  padding-bottom: 10px;
}

.search-card {
  margin-bottom: 20px;
  transition: all 0.3s ease;
  padding: 5px 10px;
}

.search-card :deep(.el-form) {
  margin-bottom: 0;
}

.search-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 16px 0 rgba(0, 0, 0, 0.15);
}

.search-form {
  margin-bottom: 0;
}

.reservations-card {
  transition: all 0.3s ease;
}

.reservations-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 16px 0 rgba(0, 0, 0, 0.15);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h3 {
  margin: 0;
  color: #333;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>