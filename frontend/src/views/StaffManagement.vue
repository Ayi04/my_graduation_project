<template>
  <div class="staff-management-container">
    <!-- 导航栏 -->
    <Navbar />

    <!-- 内容区域 -->
    <div class="content">
      <h1>工作人员管理</h1>
      
      <el-form :inline="true" :model="staffFilterForm">
        <el-form-item label="用户名">
          <el-input v-model="staffFilterForm.username" placeholder="输入用户名"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleStaffFilter">查询</el-button>
          <el-button @click="resetStaffFilter">重置</el-button>
        </el-form-item>
      </el-form>
      <el-button type="primary" @click="addStaff">添加工作人员</el-button>
      <el-table :data="paginatedStaffList" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80"></el-table-column>
        <el-table-column prop="username" label="用户名" width="120"></el-table-column>
        <el-table-column prop="email" label="邮箱"></el-table-column>
        <el-table-column prop="phone" label="电话" width="120"></el-table-column>
        <el-table-column prop="gender" label="性别" width="100"></el-table-column>
        <el-table-column prop="responsibleArea" label="负责区域"></el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="scope">
            <el-button type="primary" size="small" @click="editStaff(scope.row)">编辑</el-button>
            <el-button type="danger" size="small" @click="deleteStaff(scope.row)">删除</el-button>
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
          :total="staffList.length"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>
    
    <!-- 编辑工作人员对话框 -->
    <el-dialog
      v-model="editStaffDialogVisible"
      title="编辑工作人员"
      width="500px"
    >
      <el-form :model="editStaffForm" :rules="editStaffRules" ref="editStaffFormRef" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="editStaffForm.username" disabled></el-input>
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="editStaffForm.email"></el-input>
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="editStaffForm.phone"></el-input>
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-select v-model="editStaffForm.gender" placeholder="请选择性别">
            <el-option label="男" value="男"></el-option>
            <el-option label="女" value="女"></el-option>
            <el-option label="其他" value="其他"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="负责区域" prop="responsibleArea">
          <el-input v-model="editStaffForm.responsibleArea"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editStaffDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveStaffEdit">保存修改</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 添加工作人员对话框 -->
    <el-dialog
      v-model="addStaffDialogVisible"
      title="添加工作人员"
      width="500px"
    >
      <el-form :model="addStaffForm" :rules="addStaffRules" ref="addStaffFormRef" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="addStaffForm.username"></el-input>
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="addStaffForm.password" type="password"></el-input>
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="addStaffForm.email"></el-input>
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="addStaffForm.phone"></el-input>
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-select v-model="addStaffForm.gender" placeholder="请选择性别">
            <el-option label="男" value="男"></el-option>
            <el-option label="女" value="女"></el-option>
            <el-option label="其他" value="其他"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="负责区域" prop="responsibleArea">
          <el-input v-model="addStaffForm.responsibleArea"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="addStaffDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveStaffAdd">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useUserStore } from '../store/user'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import Navbar from '../components/Navbar.vue'

const userStore = useUserStore()

const staffFilterForm = reactive({
  username: ''
})

const staffList = ref([])

// 分页相关
const currentPage = ref(1)
const pageSize = ref(10)

// 编辑工作人员相关
const editStaffDialogVisible = ref(false)
const editStaffFormRef = ref(null)
const editStaffForm = reactive({
  id: '',
  username: '',
  email: '',
  phone: '',
  gender: '',
  responsibleArea: ''
})
const editStaffRules = {
  email: [{ required: true, message: '请输入邮箱', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入电话', trigger: 'blur' }],
  gender: [{ required: true, message: '请选择性别', trigger: 'blur' }],
  responsibleArea: [{ required: true, message: '请输入负责区域', trigger: 'blur' }]
}

// 添加工作人员相关
const addStaffDialogVisible = ref(false)
const addStaffFormRef = ref(null)
const addStaffForm = reactive({
  username: '',
  password: '',
  email: '',
  phone: '',
  gender: '',
  responsibleArea: ''
})
const addStaffRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  email: [{ required: true, message: '请输入邮箱', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入电话', trigger: 'blur' }],
  gender: [{ required: true, message: '请选择性别', trigger: 'blur' }],
  responsibleArea: [{ required: true, message: '请输入负责区域', trigger: 'blur' }]
}

const user = computed(() => userStore.user)
const userRole = computed(() => userStore.userRole)


const handleStaffFilter = async () => {
  try {
    // 调用后端API根据筛选条件获取工作人员
    const response = await axios.get('http://localhost:8080/api/user/logins/filter', {
      params: {
        username: staffFilterForm.username,
        role: 'STAFF'
      }
    })
    const staffLogins = response.data
    // 转换为前端需要的格式
    staffList.value = await Promise.all(staffLogins.map(async (login) => {
      // 获取对应的工作人员信息
      const staffResponse = await axios.get(`http://localhost:8080/api/user/staff/${login.id}`)
      if (staffResponse.data) {
        return {
          id: login.id,
          username: login.username,
          email: staffResponse.data.email,
          phone: staffResponse.data.phone,
          gender: staffResponse.data.gender,
          responsibleArea: staffResponse.data.responsibleArea
        }
      }
      return null
    }))
    // 过滤掉null值
    staffList.value = staffList.value.filter(item => item !== null)
    // 搜索后重置到第一页
    currentPage.value = 1
  } catch (error) {
    console.error('筛选工作人员失败', error)
    ElMessage.error('筛选工作人员失败，请稍后重试')
  }
}

// 分页后的工作人员列表
const paginatedStaffList = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return staffList.value.slice(start, end)
})

const resetStaffFilter = () => {
  staffFilterForm.username = ''
  loadStaff()
  // 重置到第一页
  currentPage.value = 1
}

// 分页方法
const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1 // 重置到第一页
}

const handleCurrentChange = (current) => {
  currentPage.value = current
}

const editStaff = async (staff) => {
  // 编辑工作人员
  editStaffForm.id = staff.id
  editStaffForm.username = staff.username
  editStaffForm.email = staff.email
  editStaffForm.phone = staff.phone
  editStaffForm.gender = staff.gender
  editStaffForm.responsibleArea = staff.responsibleArea
  editStaffDialogVisible.value = true
}

const saveStaffEdit = async () => {
  if (!editStaffFormRef.value) return
  await editStaffFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        // 更新工作人员信息
        const response = await axios.put('http://localhost:8080/api/user/updateInfo/staff', {
          gender: editStaffForm.gender,
          email: editStaffForm.email,
          phone: editStaffForm.phone,
          responsibleArea: editStaffForm.responsibleArea
        }, {
          params: {
            loginId: editStaffForm.id
          }
        })
        
        if (response.data) {
          ElMessage.success('更新成功')
          editStaffDialogVisible.value = false
          // 重新加载工作人员列表
          loadStaff()
        } else {
          ElMessage.error('更新失败')
        }
      } catch (error) {
        console.error('更新工作人员信息失败', error)
        ElMessage.error('更新失败，请稍后重试')
      }
    }
  })
}

const addStaff = () => {
  // 重置表单
  Object.assign(addStaffForm, {
    username: '',
    password: '',
    email: '',
    phone: '',
    gender: '',
    responsibleArea: ''
  })
  addStaffDialogVisible.value = true
}

const saveStaffAdd = async () => {
  if (!addStaffFormRef.value) return
  await addStaffFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        // 调用后端API添加工作人员
        const response = await axios.post('http://localhost:8080/api/user/register/staff', {
          username: addStaffForm.username,
          password: addStaffForm.password,
          name: addStaffForm.username, // 使用用户名作为默认姓名
          gender: addStaffForm.gender,
          phone: addStaffForm.phone,
          email: addStaffForm.email,
          responsibleArea: addStaffForm.responsibleArea
        })
        
        if (response.data) {
          ElMessage.success('添加成功')
          addStaffDialogVisible.value = false
          // 重新加载工作人员列表
          loadStaff()
        } else {
          ElMessage.error('添加失败')
        }
      } catch (error) {
        console.error('添加工作人员失败', error)
        ElMessage.error('添加失败，请稍后重试')
      }
    }
  })
}

const deleteStaff = async (staff) => {
  try {
    await ElMessageBox.confirm('确定要删除该工作人员吗？删除后将无法恢复。', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 调用后端API删除工作人员
    const response = await axios.delete(`http://localhost:8080/api/user/delete/${staff.id}`)
    if (response.data) {
      ElMessage.success('删除成功')
      // 重新加载工作人员列表
      loadStaff()
    } else {
      ElMessage.error('删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除工作人员失败', error)
      ElMessage.error('删除失败，请稍后重试')
    }
  }
}

const loadStaff = async () => {
  try {
    // 调用后端API获取角色为STAFF的登录用户
    const response = await axios.get('http://localhost:8080/api/user/logins/filter', {
      params: {
        role: 'STAFF'
      }
    })
    const staffLogins = response.data
    // 转换为前端需要的格式
    staffList.value = await Promise.all(staffLogins.map(async (login) => {
      // 获取对应的工作人员信息
      const staffResponse = await axios.get(`http://localhost:8080/api/user/staff/${login.id}`)
      if (staffResponse.data) {
        return {
          id: login.id,
          username: login.username,
          email: staffResponse.data.email,
          phone: staffResponse.data.phone,
          gender: staffResponse.data.gender,
          responsibleArea: staffResponse.data.responsibleArea
        }
      }
      return null
    }))
    // 过滤掉null值
    staffList.value = staffList.value.filter(item => item !== null)
  } catch (error) {
    console.error('加载工作人员列表失败', error)
    ElMessage.error('加载工作人员列表失败，请稍后重试')
  }
}

onMounted(() => {
  userStore.loadUserFromStorage()
  loadStaff()
})
</script>

<style scoped>
.staff-management-container {
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

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>