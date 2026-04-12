<template>
  <div class="user-management-container">
    <!-- 导航栏 -->
    <Navbar />

    <!-- 内容区域 -->
    <div class="content">
      <h1>用户管理</h1>
      
      <el-form :inline="true" :model="userFilterForm">
        <el-form-item label="用户名">
          <el-input v-model="userFilterForm.username" placeholder="输入用户名"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleUserFilter">查询</el-button>
          <el-button @click="resetUserFilter">重置</el-button>
        </el-form-item>
      </el-form>
      <el-table :data="paginatedUsers" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80"></el-table-column>
        <el-table-column prop="username" label="用户名" width="120"></el-table-column>
        <el-table-column prop="email" label="邮箱"></el-table-column>
        <el-table-column prop="phone" label="电话" width="120"></el-table-column>
        <el-table-column prop="gender" label="性别" width="100"></el-table-column>
        <el-table-column prop="creditScore" label="信用分" width="100"></el-table-column>
        <el-table-column label="禁止状态" width="120">
          <template #default="scope">
            <el-tag v-if="scope.row.isBanned" type="danger">已禁止</el-tag>
            <el-tag v-else type="success">正常</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="300">
          <template #default="scope">
            <el-button type="primary" size="small" @click="editUser(scope.row)">编辑</el-button>
            <el-button type="danger" size="small" @click="deleteUser(scope.row.id)">删除</el-button>
            <el-button v-if="!scope.row.isBanned" type="warning" size="small" @click="banUser(scope.row)">禁止</el-button>
            <el-button v-else type="success" size="small" @click="unbanUser(scope.row.id)">解除禁止</el-button>
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
          :total="users.length"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>
    
    <!-- 编辑用户对话框 -->
    <el-dialog
      v-model="editUserDialogVisible"
      title="编辑用户"
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
        <el-form-item label="信用分" prop="creditScore">
          <el-input v-model.number="editUserForm.creditScore"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editUserDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveUserEdit">保存修改</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 禁止用户对话框 -->
    <el-dialog
      v-model="banUserDialogVisible"
      title="禁止用户预约"
      width="400px"
    >
      <el-form :model="banUserForm" :rules="banUserRules" ref="banUserFormRef" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="banUserForm.username" disabled></el-input>
        </el-form-item>
        <el-form-item label="禁止天数" prop="banDays">
          <el-input v-model.number="banUserForm.banDays" placeholder="请输入禁止天数"></el-input>
        </el-form-item>
        <el-form-item label="禁止原因" prop="reason">
          <el-input v-model="banUserForm.reason" type="textarea" placeholder="请输入禁止原因"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="banUserDialogVisible = false">取消</el-button>
          <el-button type="danger" @click="saveBanUser">确定禁止</el-button>
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

const userFilterForm = reactive({
  username: ''
})

const users = ref([])

// 分页相关
const currentPage = ref(1)
const pageSize = ref(10)

// 编辑用户相关
const editUserDialogVisible = ref(false)
const editUserFormRef = ref(null)
const editUserForm = reactive({
  id: '',
  username: '',
  email: '',
  phone: '',
  gender: '',
  creditScore: 100
})
const editUserRules = {
  email: [{ required: true, message: '请输入邮箱', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入电话', trigger: 'blur' }],
  gender: [{ required: true, message: '请选择性别', trigger: 'blur' }]
}

// 禁止用户相关
const banUserDialogVisible = ref(false)
const banUserFormRef = ref(null)
const banUserForm = reactive({
  id: '',
  username: '',
  banDays: 3,
  reason: ''
})
const banUserRules = {
  banDays: [{ required: true, message: '请输入禁止天数', trigger: 'blur' }, { type: 'number', min: 1, message: '禁止天数至少为1天' }],
  reason: [{ required: true, message: '请输入禁止原因', trigger: 'blur' }]
}

const user = computed(() => userStore.user)
const userRole = computed(() => userStore.userRole)


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
          gender: userResponse.data.gender,
          creditScore: userResponse.data.creditScore,
          isBanned: userResponse.data.isBanned,
          banEndTime: userResponse.data.banEndTime
        }
      }
      return null
    }))
    // 过滤掉null值
    users.value = users.value.filter(item => item !== null)
    // 搜索后重置到第一页
    currentPage.value = 1
  } catch (error) {
    console.error('筛选用户失败', error)
    ElMessage.error('筛选用户失败，请稍后重试')
  }
}

// 分页后的用户列表
const paginatedUsers = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return users.value.slice(start, end)
})

const resetUserFilter = () => {
  userFilterForm.username = ''
  loadUsers()
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

const editUser = async (user) => {
  // 编辑用户
  editUserForm.id = user.id
  editUserForm.username = user.username
  editUserForm.email = user.email
  editUserForm.phone = user.phone
  editUserForm.gender = user.gender
  editUserForm.creditScore = user.creditScore || 100
  editUserDialogVisible.value = true
}

const saveUserEdit = async () => {
  if (!editUserFormRef.value) return
  await editUserFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        // 更新普通用户信息
        const response = await axios.put('http://localhost:8080/api/user/updateInfo/user', {
          gender: editUserForm.gender,
          email: editUserForm.email,
          phone: editUserForm.phone,
          creditScore: editUserForm.creditScore
        }, {
          params: {
            loginId: editUserForm.id
          }
        })
        
        if (response.data) {
          ElMessage.success('更新成功')
          editUserDialogVisible.value = false
          // 重新加载用户列表
          loadUsers()
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

const banUser = (user) => {
  // 禁止用户
  banUserForm.id = user.id
  banUserForm.username = user.username
  banUserForm.banDays = 3
  banUserDialogVisible.value = true
}

const saveBanUser = async () => {
  if (!banUserFormRef.value) return
  await banUserFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        // 调用后端API设置用户禁止状态
        const response = await axios.put('http://localhost:8080/api/user/setBanStatus', null, {
          params: {
            loginId: banUserForm.id,
            isBanned: true,
            banDays: banUserForm.banDays,
            reason: banUserForm.reason
          }
        })
        
        if (response.data) {
          ElMessage.success('禁止成功')
          banUserDialogVisible.value = false
          // 重新加载用户列表
          loadUsers()
        } else {
          ElMessage.error('禁止失败')
        }
      } catch (error) {
        console.error('禁止用户失败', error)
        ElMessage.error('禁止失败，请稍后重试')
      }
    }
  })
}

const unbanUser = async (userId) => {
  try {
    await ElMessageBox.confirm('确定要解除对该用户的禁止吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    
    // 调用后端API解除用户禁止状态
    const response = await axios.put('http://localhost:8080/api/user/setBanStatus', null, {
      params: {
        loginId: userId,
        isBanned: false,
        banDays: 0,
        reason: '管理员手动解除禁止'
      }
    })
    
    if (response.data) {
      ElMessage.success('解除禁止成功')
      // 重新加载用户列表
      loadUsers()
    } else {
      ElMessage.error('解除禁止失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('解除禁止用户失败', error)
      ElMessage.error('解除禁止失败，请稍后重试')
    }
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
          gender: userResponse.data.gender,
          creditScore: userResponse.data.creditScore,
          isBanned: userResponse.data.isBanned,
          banEndTime: userResponse.data.banEndTime
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

onMounted(() => {
  userStore.loadUserFromStorage()
  loadUsers()
})
</script>

<style scoped>
.user-management-container {
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