<template>
  <div class="admin-panel-container">
    <!-- 导航栏 -->
    <Navbar />

    <!-- 内容区域 -->
    <div class="content">
      <h1>管理员面板</h1>
      
      <!-- 功能选项卡 -->
      <el-tabs v-model="activeTab">
        <el-tab-pane label="用户管理" name="users">
          <el-form :inline="true" :model="userFilterForm">
            <el-form-item label="用户名">
              <el-input v-model="userFilterForm.username" placeholder="输入用户名"></el-input>
            </el-form-item>
            <el-form-item label="角色">
              <el-select v-model="userFilterForm.role" placeholder="选择角色">
                <el-option label="全部" value=""></el-option>
                <el-option label="普通用户" value="USER"></el-option>
                <el-option label="工作人员" value="STAFF"></el-option>
                <el-option label="管理员" value="ADMIN"></el-option>
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleUserFilter">筛选</el-button>
              <el-button @click="resetUserFilter">重置</el-button>
            </el-form-item>
          </el-form>
          <el-table :data="users" style="width: 100%">
            <el-table-column prop="id" label="ID" width="80"></el-table-column>
            <el-table-column prop="username" label="用户名" width="120"></el-table-column>
            <el-table-column prop="email" label="邮箱"></el-table-column>
            <el-table-column prop="phone" label="电话" width="120"></el-table-column>
            <el-table-column prop="role" label="角色" width="100">
              <template #default="scope">
                <el-tag :type="getRoleType(scope.row.role)">{{ getRoleText(scope.row.role) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="creditScore" label="信用分" width="100">
              <template #default="scope">
                <span v-if="scope.row.role === 'USER'">{{ scope.row.creditScore || '-' }}</span>
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150">
              <template #default="scope">
                <el-button type="primary" size="small" @click="editUser(scope.row)">编辑</el-button>
                <el-button type="danger" size="small" @click="deleteUser(scope.row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="工作人员管理" name="staff">
          <el-form :inline="true" :model="staffFilterForm">
            <el-form-item label="用户名">
              <el-input v-model="staffFilterForm.username" placeholder="输入用户名"></el-input>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleStaffFilter">筛选</el-button>
              <el-button @click="resetStaffFilter">重置</el-button>
            </el-form-item>
          </el-form>
          <el-button type="primary" @click="addStaff">添加工作人员</el-button>
          <el-table :data="staffList" style="width: 100%">
            <el-table-column prop="id" label="ID" width="80"></el-table-column>
            <el-table-column prop="username" label="用户名" width="120"></el-table-column>
            <el-table-column prop="email" label="邮箱"></el-table-column>
            <el-table-column prop="phone" label="电话" width="120"></el-table-column>
            <el-table-column label="操作" width="150">
              <template #default="scope">
                <el-button type="primary" size="small" @click="editStaff(scope.row)">编辑</el-button>
                <el-button type="danger" size="small" @click="deleteStaff(scope.row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="数据统计" name="statistics">
          <div class="statistics-container">
            <el-card class="stat-card">
              <template #header>
                <div class="card-header">
                  <h3>用户统计</h3>
                </div>
              </template>
              <div class="stat-item">
                <span class="stat-label">总用户数：</span>
                <span class="stat-value">{{ userStats.total }}</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">普通用户：</span>
                <span class="stat-value">{{ userStats.user }}</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">工作人员：</span>
                <span class="stat-value">{{ userStats.staff }}</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">管理员：</span>
                <span class="stat-value">{{ userStats.admin }}</span>
              </div>
            </el-card>
            <el-card class="stat-card">
              <template #header>
                <div class="card-header">
                  <h3>预约统计</h3>
                </div>
              </template>
              <div class="stat-item">
                <span class="stat-label">总预约数：</span>
                <span class="stat-value">{{ reservationStats.total }}</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">已完成：</span>
                <span class="stat-value">{{ reservationStats.completed }}</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">已取消：</span>
                <span class="stat-value">{{ reservationStats.cancelled }}</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">失败：</span>
                <span class="stat-value">{{ reservationStats.failed }}</span>
              </div>
            </el-card>
          </div>
        </el-tab-pane>
        <el-tab-pane label="系统管理" name="system">
          <el-form :model="systemForm" :rules="systemRules" ref="systemFormRef" label-width="100px">
            <el-form-item label="公告" prop="notice">
              <el-input v-model="systemForm.notice" type="textarea" :rows="4"></el-input>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="updateSystemSettings">保存设置</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </div>
    
    <!-- 编辑用户对话框 -->
    <el-dialog
      v-model="editUserDialogVisible"
      :title="editUserDialogTitle"
      width="500px"
    >
      <el-form :model="editUserForm" :rules="editUserRules" ref="editUserFormRef" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="editUserForm.username" disabled></el-input>
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="editUserForm.email"></el-input>
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="editUserForm.phone"></el-input>
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-select v-model="editUserForm.gender" placeholder="请选择性别">
            <el-option label="男" value="男"></el-option>
            <el-option label="女" value="女"></el-option>
            <el-option label="其他" value="其他"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item v-if="editUserForm.role === 'USER'" label="信用分" prop="creditScore">
          <el-input v-model.number="editUserForm.creditScore"></el-input>
        </el-form-item>
        <el-form-item v-if="editUserForm.role === 'STAFF'" label="负责区域" prop="responsibleArea">
          <el-input v-model="editUserForm.responsibleArea"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editUserDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveUserEdit">保存修改</el-button>
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
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '../store/user'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import Navbar from '../components/Navbar.vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const activeTab = ref('users')

const userFilterForm = reactive({
  username: '',
  role: ''
})

const staffFilterForm = reactive({
  username: ''
})

const systemForm = reactive({
  notice: '欢迎使用智能图书馆座位预约管理系统'
})

const systemRules = {
  notice: [{ required: true, message: '请输入公告内容', trigger: 'blur' }]
}

const users = ref([])
const staffList = ref([])

const userStats = reactive({
  total: 12,
  user: 10,
  staff: 1,
  admin: 1
})

const reservationStats = reactive({
  total: 50,
  completed: 45,
  cancelled: 3,
  failed: 2
})

// 编辑用户相关
const editUserDialogVisible = ref(false)
const editUserDialogTitle = ref('编辑用户')
const editUserForm = reactive({
  id: '',
  username: '',
  email: '',
  phone: '',
  gender: '',
  role: '',
  creditScore: 100,
  responsibleArea: ''
})
const editUserRules = {
  email: [{ required: true, message: '请输入邮箱', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入电话', trigger: 'blur' }],
  gender: [{ required: true, message: '请选择性别', trigger: 'blur' }]
}
const editUserFormRef = ref(null)

// 添加工作人员相关
const addStaffDialogVisible = ref(false)
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
const addStaffFormRef = ref(null)

const user = computed(() => userStore.user)
const userRole = computed(() => userStore.userRole)

const getRoleText = (role) => {
  const roleMap = {
    USER: '普通用户',
    STAFF: '工作人员',
    ADMIN: '管理员'
  }
  return roleMap[role] || role
}

const getRoleType = (role) => {
  const typeMap = {
    USER: 'info',
    STAFF: 'warning',
    ADMIN: 'danger'
  }
  return typeMap[role] || 'info'
}

const handleUserFilter = async () => {
  try {
    // 调用后端API根据筛选条件获取普通用户
    const response = await axios.get('http://localhost:8080/api/user/logins/filter', {
      params: {
        username: userFilterForm.username,
        role: 'USER'
      }
    })
    const userLogins = response.data
    // 转换为前端需要的格式
    users.value = await Promise.all(userLogins.map(async (login) => {
      // 获取对应的普通用户信息
      const userResponse = await axios.get(`http://localhost:8080/api/user/user/${login.id}`)
      if (userResponse.data) {
        return {
          id: login.id,
          username: login.username,
          email: userResponse.data.email,
          phone: userResponse.data.phone,
          role: 'USER',
          creditScore: userResponse.data.creditScore
        }
      }
      return null
    }))
    // 过滤掉null值
    users.value = users.value.filter(item => item !== null)
  } catch (error) {
    console.error('筛选用户失败', error)
    ElMessage.error('筛选用户失败，请稍后重试')
  }
}

const resetUserFilter = () => {
  userFilterForm.username = ''
  userFilterForm.role = ''
  loadUsers()
}

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
          phone: staffResponse.data.phone
        }
      }
      return null
    }))
    // 过滤掉null值
    staffList.value = staffList.value.filter(item => item !== null)
  } catch (error) {
    console.error('筛选工作人员失败', error)
    ElMessage.error('筛选工作人员失败，请稍后重试')
  }
}

const resetStaffFilter = () => {
  staffFilterForm.username = ''
  loadStaff()
}

const editUser = async (user) => {
  // 编辑用户
  editUserDialogTitle.value = '编辑普通用户'
  editUserForm.id = user.id
  editUserForm.username = user.username
  editUserForm.email = user.email
  editUserForm.phone = user.phone
  editUserForm.role = 'USER'
  editUserForm.creditScore = user.creditScore || 100
  
  // 获取用户的性别信息
  try {
    const userResponse = await axios.get(`http://localhost:8080/api/user/user/${user.id}`)
    if (userResponse.data) {
      editUserForm.gender = userResponse.data.gender || ''
    }
  } catch (error) {
    console.error('获取用户信息失败', error)
  }
  
  editUserDialogVisible.value = true
}

const editStaff = async (staff) => {
  // 编辑工作人员
  editUserDialogTitle.value = '编辑工作人员'
  editUserForm.id = staff.id
  editUserForm.username = staff.username
  editUserForm.email = staff.email
  editUserForm.phone = staff.phone
  editUserForm.role = 'STAFF'
  
  // 获取工作人员的详细信息
  try {
    const staffResponse = await axios.get(`http://localhost:8080/api/user/staff/${staff.id}`)
    if (staffResponse.data) {
      editUserForm.gender = staffResponse.data.gender || ''
      editUserForm.responsibleArea = staffResponse.data.responsibleArea || ''
    }
  } catch (error) {
    console.error('获取工作人员信息失败', error)
  }
  
  editUserDialogVisible.value = true
}

const saveUserEdit = async () => {
  if (!editUserFormRef.value) return
  await editUserFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        let response
        if (editUserForm.role === 'USER') {
          // 更新普通用户信息
          response = await axios.put('http://localhost:8080/api/user/updateInfo/user', {
            gender: editUserForm.gender,
            email: editUserForm.email,
            phone: editUserForm.phone,
            creditScore: editUserForm.creditScore
          }, {
            params: {
              loginId: editUserForm.id
            }
          })
        } else if (editUserForm.role === 'STAFF') {
          // 更新工作人员信息
          response = await axios.put('http://localhost:8080/api/user/updateInfo/staff', {
            gender: editUserForm.gender,
            email: editUserForm.email,
            phone: editUserForm.phone,
            responsibleArea: editUserForm.responsibleArea
          }, {
            params: {
              loginId: editUserForm.id
            }
          })
        }
        
        if (response.data) {
          ElMessage.success('更新成功')
          editUserDialogVisible.value = false
          // 重新加载数据
          if (editUserForm.role === 'USER') {
            loadUsers()
          } else if (editUserForm.role === 'STAFF') {
            loadStaff()
          }
        } else {
          ElMessage.error('更新失败')
        }
      } catch (error) {
        console.error('更新用户信息失败', error)
        ElMessage.error('更新失败，请稍后重试')
      }
    }
  })
}

const deleteUser = async (userId) => {
  try {
    await ElMessageBox.confirm('确定要删除该用户吗？删除后将无法恢复。', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 调用后端API删除用户
    const response = await axios.delete(`http://localhost:8080/api/user/delete/${userId}`)
    if (response.data) {
      ElMessage.success('删除成功')
      // 重新加载用户列表
      loadUsers()
    } else {
      ElMessage.error('删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除用户失败', error)
      ElMessage.error('删除失败，请稍后重试')
    }
  }
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

const updateSystemSettings = async () => {
  try {
    // 这里应该调用后端API更新系统设置
    // 暂时模拟更新成功
    ElMessage.success('设置更新成功')
  } catch (error) {
    ElMessage.error('设置更新失败，请稍后重试')
  }
}

const loadUsers = async () => {
  try {
    // 调用后端API获取角色为USER的登录用户
    const response = await axios.get('http://localhost:8080/api/user/logins/filter', {
      params: {
        role: 'USER'
      }
    })
    const userLogins = response.data
    // 转换为前端需要的格式
    users.value = await Promise.all(userLogins.map(async (login) => {
      // 获取对应的普通用户信息
      const userResponse = await axios.get(`http://localhost:8080/api/user/user/${login.id}`)
      if (userResponse.data) {
        return {
          id: login.id,
          username: login.username,
          email: userResponse.data.email,
          phone: userResponse.data.phone,
          role: 'USER',
          creditScore: userResponse.data.creditScore
        }
      }
      return null
    }))
    // 过滤掉null值
    users.value = users.value.filter(item => item !== null)
  } catch (error) {
    console.error('加载用户列表失败', error)
    ElMessage.error('加载用户列表失败，请稍后重试')
  }
}

const loadStaff = async () => {
  try {
    // 调用后端API获取所有登录用户，然后筛选出工作人员
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
          phone: staffResponse.data.phone
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
  // 根据URL参数设置默认选项卡
  const type = route.query.type
  if (type === 'staff') {
    activeTab.value = 'staff'
  } else if (type === 'user') {
    activeTab.value = 'users'
  }
  loadUsers()
  loadStaff()
})
</script>

<style scoped>
.admin-panel-container {
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

.statistics-container {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.stat-card {
  transition: all 0.3s ease;
}

.stat-card:hover {
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

.stat-item {
  margin-bottom: 10px;
  display: flex;
  justify-content: space-between;
}

.stat-label {
  font-weight: bold;
  color: #666;
}

.stat-value {
  font-size: 18px;
  color: #1890ff;
  font-weight: bold;
}
</style>