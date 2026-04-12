<template>
  <div class="seat-management-container">
    <!-- 导航栏 -->
    <Navbar />

    <div class="content">
      <h1>座位管理</h1>
      
      <!-- 搜索和筛选 -->
      <el-card class="search-card">
        <el-form :inline="true" class="area-selector" style="display: flex; align-items: center; flex-wrap: nowrap;">
          <el-form-item label="输入楼层" style="margin-right: 10px;">
            <el-input v-model.number="selectedFloor" placeholder="请输入楼层" @change="loadAreasByFloor" style="width: 120px;"></el-input>
          </el-form-item>
          <el-form-item label="选择区域" style="margin-right: 10px;">
            <el-select v-model="selectedAreaId" placeholder="请选择区域" @change="loadSeats" style="width: 180px;">
              <el-option v-for="area in areas" :key="area.id" :label="area.name" :value="area.id"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="座位号搜索" style="margin-right: 10px;">
            <el-input v-model="seatNumberSearch" placeholder="请输入座位号" @keyup.enter="searchSeats" style="width: 180px;"></el-input>
          </el-form-item>
          <el-form-item style="margin-right: 10px;">
            <el-button type="primary" @click="searchSeats">查询</el-button>
          </el-form-item>
          <el-form-item style="margin-right: 10px;">
            <el-button type="primary" @click="resetFilters">重置</el-button>
          </el-form-item>
          <el-form-item style="margin-right: 10px;">
            <el-button type="primary" @click="addSeat">添加座位</el-button>
          </el-form-item>
          <el-form-item style="margin-right: 10px;">
            <el-button type="primary" @click="showAreaManagement">区域管理</el-button>
          </el-form-item>
        </el-form>
      </el-card>
      
      <!-- 座位列表 -->
      <el-card class="seats-card">
        <template #header>
          <div class="card-header">
            <h3>座位列表</h3>
          </div>
        </template>
        <el-table :data="paginatedSeats" style="width: 100%">
          <el-table-column prop="id" label="ID" min-width="80"></el-table-column>
          <el-table-column prop="seatNumber" label="座位号" min-width="120"></el-table-column>
          <el-table-column prop="status" label="状态" min-width="120">
            <template #default="scope">
              <el-select v-model="scope.row.status" @change="updateSeatStatus(scope.row)">
                <el-option label="可用" value="AVAILABLE"></el-option>
                <el-option label="已预约" value="RESERVED"></el-option>
                <el-option label="已占用" value="OCCUPIED"></el-option>
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="操作" min-width="150">
            <template #default="scope">
              <el-button type="primary" size="small" @click="editSeat(scope.row)">编辑</el-button>
              <el-button type="danger" size="small" @click="deleteSeat(scope.row.id)">删除</el-button>
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
            :total="seats.length"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </el-card>
    </div>
    
    <!-- 座位编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      title="座位管理"
      width="500px"
    >
      <el-form :model="seatForm" :rules="seatRules" ref="seatFormRef" label-width="100px">
        <el-form-item label="楼层" prop="floor">
          <el-input v-model.number="seatForm.floor" placeholder="请输入楼层" @change="loadAreasByFloorForAdd"></el-input>
        </el-form-item>
        <el-form-item label="区域" prop="areaId">
          <el-select v-model="seatForm.areaId" placeholder="请选择区域" @change="handleAreaChange">
            <el-option v-for="area in dialogAreas" :key="area.id" :label="area.name" :value="area.id"></el-option>
          </el-select>
        </el-form-item>
        <div style="margin-top: 10px; font-size: 12px; color: #909399;">
          提示：请先选择楼层，然后再选择区域
        </div>
        <el-form-item label="座位号" prop="seatNumber">
          <el-input v-model="seatForm.seatNumber" placeholder="请输入座位号"></el-input>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="seatForm.status" placeholder="请选择状态">
            <el-option label="可用" value="AVAILABLE"></el-option>
            <el-option label="已预约" value="RESERVED"></el-option>
            <el-option label="已占用" value="OCCUPIED"></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveSeat">保存</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 区域管理对话框 -->
    <el-dialog
      v-model="areaDialogVisible"
      title="区域管理"
      width="800px"
    >
      <!-- 楼层管理 -->
      <div class="floor-management">
        <h3 style="margin-bottom: 20px;">楼层管理</h3>
        <div style="display: flex; align-items: center; margin-bottom: 20px;">
          <el-input v-model="newFloor" placeholder="输入新楼层号" style="width: 150px; margin-right: 10px;"></el-input>
          <el-button type="primary" @click="addFloor">添加楼层</el-button>
        </div>
        <el-table :data="floors" style="width: 100%; margin-bottom: 30px;">
          <el-table-column prop="" label="楼层" min-width="100">
            <template #default="scope">
              {{ scope.row }}楼
            </template>
          </el-table-column>
          <el-table-column label="操作" min-width="150">
            <template #default="scope">
              <el-button type="danger" size="small" @click="deleteFloor(scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      
      <!-- 区域管理 -->
      <div class="area-management">
        <h3 style="margin-bottom: 20px;">区域管理</h3>
        <el-form :inline="true" style="margin-bottom: 20px;">
          <el-form-item label="输入楼层">
            <el-input v-model.number="areaFloorFilter" placeholder="请输入楼层" @change="loadAreasByFloorForManagement" style="width: 120px;"></el-input>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="addArea">添加区域</el-button>
          </el-form-item>
        </el-form>
        <el-table :data="managementAreas" style="width: 100%;">
          <el-table-column prop="id" label="ID" min-width="80"></el-table-column>
          <el-table-column prop="name" label="区域名称" min-width="150"></el-table-column>
          <el-table-column prop="type" label="区域类型" min-width="120">
            <template #default="scope">
              {{ areaTypeMap[scope.row.type] }}
            </template>
          </el-table-column>
          <el-table-column prop="floor" label="楼层" min-width="80"></el-table-column>
          <el-table-column prop="seatCount" label="座位数" min-width="80"></el-table-column>
          <el-table-column prop="availableCount" label="可用座位数" min-width="100"></el-table-column>
          <el-table-column label="操作" min-width="150">
            <template #default="scope">
              <el-button type="primary" size="small" @click="editArea(scope.row)">编辑</el-button>
              <el-button type="danger" size="small" @click="deleteArea(scope.row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>
    
    <!-- 区域编辑对话框 -->
    <el-dialog
      v-model="areaEditDialogVisible"
      :title="editingArea ? '编辑区域' : '添加区域'"
      width="500px"
    >
      <el-form :model="areaForm" :rules="areaRules" ref="areaFormRef" label-width="100px">
        <el-form-item label="楼层" prop="floor">
          <el-input v-model.number="areaForm.floor" placeholder="请输入楼层"></el-input>
        </el-form-item>
        <el-form-item label="区域名称" prop="name">
          <el-input v-model="areaForm.name" placeholder="请输入区域名称"></el-input>
        </el-form-item>
        <el-form-item label="区域类型" prop="type">
          <el-select v-model="areaForm.type" placeholder="请选择区域类型">
            <el-option label="静音区" value="SILENT"></el-option>
            <el-option label="浏览区" value="BROWSING"></el-option>
            <el-option label="普通区" value="NORMAL"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="开放时间" prop="openTime">
          <el-input v-model="areaForm.openTime" placeholder="例如：08:00-22:00"></el-input>
        </el-form-item>
        <el-form-item label="最低信用分" prop="minCreditScore">
          <el-input v-model.number="areaForm.minCreditScore" placeholder="请输入最低信用分"></el-input>
        </el-form-item>
        <el-form-item label="签到时间限制" prop="checkinTimeLimit">
          <el-input v-model.number="areaForm.checkinTimeLimit" placeholder="请输入签到时间限制（分钟）"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="areaEditDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveArea">保存</el-button>
        </span>
      </template>
    </el-dialog>
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
const dialogVisible = ref(false)
const seatFormRef = ref(null)
const selectedAreaId = ref('')
const selectedFloor = ref('')
const seatNumberSearch = ref('')

const floors = ref([1, 2, 3]) // 实际只有3层
const areas = ref([])
const dialogAreas = ref([]) // 用于对话框中的区域选择
const seats = ref([])
const allSeats = ref([]) // 用于存储所有座位，方便搜索

// 区域管理相关
const areaDialogVisible = ref(false)
const areaEditDialogVisible = ref(false)
const newFloor = ref('')
const areaFloorFilter = ref('')
const managementAreas = ref([])
const areaForm = ref({
  id: null,
  name: '',
  floor: '',
  type: 'NORMAL',
  openTime: '08:00-22:00',
  minCreditScore: 0,
  checkinTimeLimit: 15
})
const areaRules = {
  name: [{ required: true, message: '请输入区域名称', trigger: 'blur' }],
  floor: [{ required: true, message: '请输入楼层', trigger: 'blur' }],
  type: [{ required: true, message: '请选择区域类型', trigger: 'blur' }],
  minCreditScore: [{ required: true, message: '请输入最低信用分', trigger: 'blur' }],
  checkinTimeLimit: [{ required: true, message: '请输入签到时间限制', trigger: 'blur' }]
}
const areaFormRef = ref(null)
const editingArea = ref(null)
const areaTypeMap = {
  SILENT: '静音区',
  BROWSING: '浏览区',
  NORMAL: '普通区'
}

// 分页相关
const currentPage = ref(1)
const pageSize = ref(10)
const paginatedSeats = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return seats.value.slice(start, end)
})
const seatForm = ref({
  id: null,
  floor: '',
  areaId: null,
  seatNumber: '',
  status: 'AVAILABLE'
})

const seatRules = {
  floor: [{ required: true, message: '请输入楼层', trigger: 'blur' }],
  areaId: [{ required: true, message: '请选择区域', trigger: 'blur' }],
  seatNumber: [{ required: true, message: '请输入座位号', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'blur' }]
}

const user = computed(() => userStore.user)
const userRole = computed(() => userStore.userRole)



const loadAreas = async () => {
  try {
    const response = await axios.get('http://localhost:8080/api/seat/areas')
    areas.value = response.data
    // 不默认选择任何区域，让用户手动选择
    selectedAreaId.value = ''
    // 保持当前座位列表不变
  } catch (error) {
    console.error('加载区域失败', error)
    ElMessage.error('加载区域失败，请稍后重试')
  }
}

const loadAreasByFloor = async () => {
  if (!selectedFloor.value) {
    loadAreas()
    return
  }
  try {
    const response = await axios.get(`http://localhost:8080/api/seat/areas/${selectedFloor.value}`)
    areas.value = response.data
    // 不默认选择任何区域，让用户手动选择
    selectedAreaId.value = ''
    // 保持当前座位列表不变
  } catch (error) {
    console.error('加载区域失败', error)
    ElMessage.error('加载区域失败，请稍后重试')
  }
  return areas.value
}

const loadAreasByFloorForAdd = async () => {
  if (!seatForm.value.floor) {
    dialogAreas.value = []
    seatForm.value.areaId = null
    return
  }
  try {
    console.log('加载楼层:', seatForm.value.floor)
    const response = await axios.get(`http://localhost:8080/api/seat/areas/${seatForm.value.floor}`)
    console.log('加载的区域:', response.data)
    if (Array.isArray(response.data)) {
      dialogAreas.value = response.data
      console.log('dialogAreas数组长度:', dialogAreas.value.length)
    } else {
      dialogAreas.value = []
      console.log('响应数据不是数组:', response.data)
    }
    // 不默认选择任何区域，让用户手动选择
    seatForm.value.areaId = null
  } catch (error) {
    console.error('加载区域失败', error)
    ElMessage.error('加载区域失败，请稍后重试')
  }
}

const handleAreaChange = () => {
  console.log('选择的区域ID:', seatForm.value.areaId)
}

const loadSeats = async () => {
  if (!selectedAreaId.value) return
  // 只加载该区域的座位，不自动更新座位列表
  // 座位列表的更新由搜索按钮触发
  try {
    const response = await axios.get(`http://localhost:8080/api/seat/area/${selectedAreaId.value}`)
    allSeats.value = response.data
  } catch (error) {
    console.error('加载座位失败', error)
    ElMessage.error('加载座位失败，请稍后重试')
  }
}

const searchSeats = () => {
  console.log('搜索按钮点击，搜索关键词:', seatNumberSearch.value)
  console.log('allSeats数组长度:', allSeats.value.length)
  
  // 如果选择了楼层但没有选择区域，加载该楼层的所有座位
  if (selectedFloor.value && !selectedAreaId.value) {
    loadAreasByFloor().then(() => {
      // 加载该楼层所有区域的座位
      let allFloorSeats = []
      const loadAreaSeats = async () => {
        for (const area of areas.value) {
          try {
            const response = await axios.get(`http://localhost:8080/api/seat/area/${area.id}`)
            allFloorSeats = [...allFloorSeats, ...response.data]
          } catch (error) {
            console.error('加载区域座位失败', error)
          }
        }
        allSeats.value = allFloorSeats
        performSearch()
      }
      loadAreaSeats()
    })
  } else if (!selectedAreaId.value) {
    // 如果没有选择区域，加载所有座位
    loadAllSeats().then(() => {
      performSearch()
    })
  } else {
    // 如果选择了区域，使用已加载的区域座位
    performSearch()
  }
}

const performSearch = () => {
  if (!seatNumberSearch.value) {
    seats.value = allSeats.value
  } else {
    seats.value = allSeats.value.filter(seat => 
      seat.seatNumber.toLowerCase().includes(seatNumberSearch.value.toLowerCase())
    )
  }
  
  console.log('搜索后seats数组长度:', seats.value.length)
  // 搜索后重置到第一页
  currentPage.value = 1
}

const loadAllSeats = async () => {
  try {
    // 先加载所有区域
    const areasResponse = await axios.get('http://localhost:8080/api/seat/areas')
    const allAreas = areasResponse.data
    
    // 加载所有区域的座位
    let allSeatsData = []
    for (const area of allAreas) {
      const seatsResponse = await axios.get(`http://localhost:8080/api/seat/area/${area.id}`)
      allSeatsData = [...allSeatsData, ...seatsResponse.data]
    }
    
    allSeats.value = allSeatsData
    return allSeatsData
  } catch (error) {
    console.error('加载所有座位失败', error)
    ElMessage.error('加载所有座位失败，请稍后重试')
    return []
  }
}

const resetFilters = () => {
  // 重置所有筛选条件
  selectedFloor.value = ''
  selectedAreaId.value = ''
  seatNumberSearch.value = ''
  currentPage.value = 1
  
  // 重新加载所有区域
  loadAreas()
  // 重新加载所有座位
  loadAllSeats().then((allSeatsData) => {
    seats.value = allSeatsData
  })
}

// 分页相关方法
const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1 // 切换每页显示数量时重置到第一页
}

const handleCurrentChange = (current) => {
  currentPage.value = current
}

const addSeat = () => {
  seatForm.value = {
    id: null,
    floor: '',
    areaId: null,
    seatNumber: '',
    status: 'AVAILABLE'
  }
  dialogAreas.value = []
  dialogVisible.value = true
}

const editSeat = (seat) => {
  seatForm.value = { ...seat }
  dialogVisible.value = true
}

const saveSeat = async () => {
  if (!seatFormRef.value) return
  await seatFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        console.log('保存座位前的表单数据:', seatForm.value)
        let response
        if (seatForm.value.id) {
          // 编辑座位
          const editData = {
            id: seatForm.value.id,
            seatNumber: seatForm.value.seatNumber,
            status: seatForm.value.status,
            areaId: seatForm.value.areaId
          }
          console.log('编辑座位请求数据:', editData)
          response = await axios.put(`http://localhost:8080/api/seat/${seatForm.value.id}`, editData)
        } else {
          // 添加座位
          const addData = {
            seatNumber: seatForm.value.seatNumber,
            status: seatForm.value.status,
            areaId: seatForm.value.areaId
          }
          console.log('添加座位请求数据:', addData)
          response = await axios.post('http://localhost:8080/api/seat/', addData)
        }
        
        console.log('保存座位响应:', response.data)
        if (response.data) {
          ElMessage.success('保存成功')
          dialogVisible.value = false
          loadSeats()
        } else {
          ElMessage.error('保存失败')
        }
      } catch (error) {
        console.error('保存座位失败', error)
        ElMessage.error('保存失败，请稍后重试')
      }
    }
  })
}

const deleteSeat = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除此座位吗？删除后将无法恢复。', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await axios.delete(`http://localhost:8080/api/seat/${id}`)
    ElMessage.success('删除成功')
    loadSeats()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除座位失败', error)
      ElMessage.error('删除失败，请稍后重试')
    }
  }
}

const updateSeatStatus = async (seat) => {
  try {
    const response = await axios.put(`http://localhost:8080/api/seat/${seat.id}`, seat)
    if (response.data) {
      ElMessage.success('状态更新成功')
      // 如果座位状态从已预约改为可用，取消相应的预约
      if (seat.status === 'AVAILABLE') {
        // 这里可以调用取消预约的API
      }
    } else {
      ElMessage.error('状态更新失败')
    }
  } catch (error) {
    console.error('更新座位状态失败', error)
    ElMessage.error('状态更新失败，请稍后重试')
  }
}

// 区域管理相关方法
const showAreaManagement = () => {
  areaDialogVisible.value = true
  loadAreasByFloorForManagement()
}

// 添加楼层
const addFloor = () => {
  const floorNum = parseInt(newFloor.value)
  if (isNaN(floorNum) || floorNum <= 0) {
    ElMessage.error('请输入有效的楼层号')
    return
  }
  if (floors.value.includes(floorNum)) {
    ElMessage.error('该楼层已存在')
    return
  }
  floors.value.push(floorNum)
  floors.value.sort((a, b) => a - b)
  newFloor.value = ''
  ElMessage.success('楼层添加成功')
}

// 删除楼层
const deleteFloor = async (floor) => {
  // 检查该楼层是否有区域
  const hasAreas = areas.value.some(area => area.floor === floor)
  if (hasAreas) {
    ElMessage.error('该楼层还有区域，不能删除')
    return
  }
  try {
    await ElMessageBox.confirm('确定要删除此楼层吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    floors.value = floors.value.filter(f => f !== floor)
    ElMessage.success('楼层删除成功')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除楼层失败', error)
      ElMessage.error('删除失败，请稍后重试')
    }
  }
}

// 加载指定楼层的区域（用于区域管理）
const loadAreasByFloorForManagement = async () => {
  if (!areaFloorFilter.value) {
    loadAreas().then(() => {
      managementAreas.value = areas.value
    })
    return
  }
  try {
    const response = await axios.get(`http://localhost:8080/api/seat/areas/${areaFloorFilter.value}`)
    managementAreas.value = response.data
  } catch (error) {
    console.error('加载区域失败', error)
    ElMessage.error('加载区域失败，请稍后重试')
  }
}

// 添加区域
const addArea = () => {
  areaForm.value = {
    id: null,
    name: '',
    floor: areaFloorFilter.value || '',
    type: 'NORMAL',
    openTime: '08:00-22:00',
    minCreditScore: 0,
    checkinTimeLimit: 15
  }
  editingArea.value = null
  areaEditDialogVisible.value = true
}

// 编辑区域
const editArea = (area) => {
  areaForm.value = {
    id: area.id,
    name: area.name,
    floor: area.floor,
    type: area.type,
    openTime: area.openTime || '08:00-22:00',
    minCreditScore: area.minCreditScore || 0,
    checkinTimeLimit: area.checkinTimeLimit || 15
  }
  editingArea.value = area
  areaEditDialogVisible.value = true
}

// 保存区域
const saveArea = async () => {
  if (!areaFormRef.value) return
  await areaFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        let response
        if (areaForm.value.id) {
          // 编辑区域
          response = await axios.put(`http://localhost:8080/api/seat/area/${areaForm.value.id}`, areaForm.value)
        } else {
          // 添加区域
          response = await axios.post('http://localhost:8080/api/seat/area', areaForm.value)
        }
        if (response.data) {
          ElMessage.success(areaForm.value.id ? '区域更新成功' : '区域添加成功')
          areaEditDialogVisible.value = false
          loadAreasByFloorForManagement()
          loadAreas() // 重新加载区域列表
        } else {
          ElMessage.error('保存失败')
        }
      } catch (error) {
        console.error('保存区域失败', error)
        ElMessage.error('保存失败，请稍后重试')
      }
    }
  })
}

// 删除区域
const deleteArea = async (areaId) => {
  try {
    await ElMessageBox.confirm('确定要删除此区域吗？删除后区域内的所有座位也将被删除。', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const response = await axios.delete(`http://localhost:8080/api/seat/area/${areaId}`)
    if (response.data) {
      ElMessage.success('区域删除成功')
      loadAreasByFloorForManagement()
      loadAreas() // 重新加载区域列表
    } else {
      ElMessage.error('删除失败，可能有未完成的预约')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除区域失败', error)
      ElMessage.error('删除失败，请稍后重试')
    }
  }
}

onMounted(() => {
  userStore.loadUserFromStorage()
  loadAreas()
  // 页面加载时默认加载所有座位
  loadAllSeats().then((allSeatsData) => {
    seats.value = allSeatsData
  })
})
</script>

<style scoped>
.seat-management-container {
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

.area-selector {
  margin-bottom: 0;
}

.seats-card {
  transition: all 0.3s ease;
}

.seats-card:hover {
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

.dialog-footer {
  text-align: right;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>