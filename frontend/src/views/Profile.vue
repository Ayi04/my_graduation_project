<template>
  <div class="profile-container">
    <!-- 导航栏 -->
    <Navbar />

    <!-- 内容区域 -->
    <div class="content">
      <h1>个人中心</h1>
      
      <!-- 个人信息 -->
      <el-card class="info-card">
        <template #header>
          <div class="card-header">
            <h3>个人信息</h3>
          </div>
        </template>
        <div v-if="!showEditForm" class="info-content">
          <div class="avatar-container">
            <el-avatar :size="120" :src="userForm.avatar ? userForm.avatar : defaultAvatar" class="avatar">
              {{ userForm.username ? userForm.username.charAt(0) : 'U' }}
            </el-avatar>
            <div class="avatar-text">
              {{ userForm.username }}
            </div>
          </div>
          <el-descriptions :column="1" border class="info-descriptions">
            <el-descriptions-item label="性别">
              {{ userForm.gender }}
            </el-descriptions-item>
            <el-descriptions-item label="邮箱">
              {{ userForm.email }}
            </el-descriptions-item>
            <el-descriptions-item label="电话">
              {{ userForm.phone }}
            </el-descriptions-item>
            <el-descriptions-item v-if="userRole === 'USER'" label="信用分">
              {{ userForm.creditScore }}
            </el-descriptions-item>
            <el-descriptions-item v-if="userRole === 'STAFF'" label="负责区域">
              {{ userForm.responsibleArea }}
            </el-descriptions-item>
          </el-descriptions>
          <el-button type="primary" @click="handleEditClick" style="margin-top: 20px">修改信息</el-button>
        </div>
        <el-form v-else :model="userForm" :rules="userRules" ref="userFormRef" label-width="100px">
          <el-form-item label="头像">
            <div class="avatar-upload-container">
              <el-avatar :size="100" :src="userForm.avatar ? userForm.avatar : defaultAvatar" class="avatar">
                {{ userForm.username ? userForm.username.charAt(0) : 'U' }}
              </el-avatar>
              <el-upload
                class="avatar-uploader"
                :http-request="handleAvatarUpload"
                :show-file-list="false"
                :before-upload="beforeAvatarUpload"
              >
                <el-button size="small" type="primary">更换头像</el-button>
              </el-upload>
            </div>
          </el-form-item>
          <el-form-item label="用户名" prop="username">
            <el-input v-model="userForm.username" disabled></el-input>
          </el-form-item>
          <el-form-item label="邮箱" prop="email">
            <el-input v-model="userForm.email"></el-input>
          </el-form-item>
          <el-form-item label="电话" prop="phone">
            <el-input v-model="userForm.phone"></el-input>
          </el-form-item>
          <el-form-item label="性别" prop="gender">
            <el-select v-model="userForm.gender" placeholder="请选择性别">
              <el-option label="男" value="男"></el-option>
              <el-option label="女" value="女"></el-option>
              <el-option label="其他" value="其他"></el-option>
            </el-select>
          </el-form-item>

          <el-form-item v-if="userRole === 'USER' || userRole === 'user'" label="信用分" prop="creditScore">
            <el-input v-model="userForm.creditScore" disabled></el-input>
          </el-form-item>
          <el-form-item v-if="userRole === 'STAFF' || userRole === 'staff'" label="负责区域" prop="responsibleArea">
            <el-input v-model="userForm.responsibleArea" :disabled="!(userRole === 'ADMIN' || userRole === 'admin')"></el-input>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="updateUserInfo">保存修改</el-button>
            <el-button @click="showEditForm = false">取消</el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <!-- 操作按钮 -->
      <div class="action-buttons">
        <el-button type="primary" @click="showPasswordForm = !showPasswordForm">
          {{ showPasswordForm ? '取消修改' : '修改密码' }}
        </el-button>
        <el-button type="danger" @click="handleLogout">退出登录</el-button>
      </div>

      <!-- 修改密码表单 -->
      <el-card v-if="showPasswordForm" class="password-card">
        <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="100px">
          <el-form-item label="旧密码" prop="oldPassword">
            <el-input v-model="passwordForm.oldPassword" :type="oldPasswordVisible ? 'text' : 'password'">
              <template #suffix>
                <el-icon @click="oldPasswordVisible = !oldPasswordVisible">
                  <component :is="oldPasswordVisible ? 'View' : 'Hide'" />
                </el-icon>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item label="新密码" prop="newPassword">
            <el-input v-model="passwordForm.newPassword" :type="newPasswordVisible ? 'text' : 'password'">
              <template #suffix>
                <el-icon @click="newPasswordVisible = !newPasswordVisible">
                  <component :is="newPasswordVisible ? 'View' : 'Hide'" />
                </el-icon>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item label="确认新密码" prop="confirmPassword">
            <el-input v-model="passwordForm.confirmPassword" :type="confirmPasswordVisible ? 'text' : 'password'">
              <template #suffix>
                <el-icon @click="confirmPasswordVisible = !confirmPasswordVisible">
                  <component :is="confirmPasswordVisible ? 'View' : 'Hide'" />
                </el-icon>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="updatePassword">修改密码</el-button>
          </el-form-item>
        </el-form>
      </el-card>


    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../store/user'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { View, Hide } from '@element-plus/icons-vue'
import Navbar from '../components/Navbar.vue'

const router = useRouter()
const userStore = useUserStore()
const userFormRef = ref(null)
const passwordFormRef = ref(null)
const showPasswordForm = ref(false)
const showEditForm = ref(false)
const oldPasswordVisible = ref(false)
const newPasswordVisible = ref(false)
const confirmPasswordVisible = ref(false)

const userForm = reactive({
  username: '',
  email: '',
  phone: '',
  gender: '',
  role: '',
  creditScore: '',
  responsibleArea: '',
  avatar: ''
})

const defaultAvatar = '/avatars/default-avatar.jpg'


const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const userRules = {
  email: [{ required: true, message: '请输入邮箱', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入电话', trigger: 'blur' }]
}

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [{ required: true, message: '请输入新密码', trigger: 'blur' }],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}



const user = computed(() => userStore.user)
const userRole = computed(() => userStore.userRole)

const updateUserInfo = async () => {
  if (!userFormRef.value) return
  if (!user.value || !user.value.id) {
    ElMessage.error('用户信息获取失败，请重新登录')
    return
  }
  await userFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        await ElMessageBox.confirm('确定要保存修改吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'info'
        })
        
        // 根据用户角色调用对应的API
        let response
        if (userRole.value === 'USER' || userRole.value === 'user') {
          // 只发送User实体类中存在的字段
          const userData = {
            gender: userForm.gender,
            phone: userForm.phone,
            email: userForm.email
          }
          response = await axios.put('http://localhost:8080/api/user/updateInfo/user', userData, {
            params: {
              loginId: user.value.id
            }
          })
        } else if (userRole.value === 'STAFF' || userRole.value === 'staff') {
          // 只发送Staff实体类中存在的字段
          const staffData = {
            gender: userForm.gender,
            phone: userForm.phone,
            email: userForm.email,
            responsibleArea: userForm.responsibleArea
          }
          response = await axios.put('http://localhost:8080/api/user/updateInfo/staff', staffData, {
            params: {
              loginId: user.value.id
            }
          })
        } else if (userRole.value === 'ADMIN' || userRole.value === 'admin') {
          // 只发送Admin实体类中存在的字段
          const adminData = {
            gender: userForm.gender,
            phone: userForm.phone,
            email: userForm.email
          }
          response = await axios.put('http://localhost:8080/api/user/updateInfo/admin', adminData, {
            params: {
              loginId: user.value.id
            }
          })
        }
        
        console.log('用户信息更新请求成功:', response.data)
        if (response.data) {
          ElMessage.success('更新成功')
          // 重新加载用户信息
          await loadUserInfo()
          // 返回到只读展示模式
          showEditForm.value = false
        } else {
          ElMessage.error('更新失败')
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error('用户信息更新请求失败:', error)
          ElMessage.error('更新失败，请稍后重试')
        }
      }
    }
  })
}

const updatePassword = async () => {
  if (!passwordFormRef.value) return
  if (!user.value || !user.value.id) {
    ElMessage.error('用户信息获取失败，请重新登录')
    return
  }
  await passwordFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        console.log('发送密码修改请求:', {
          loginId: user.value.id,
          oldPassword: passwordForm.oldPassword,
          newPassword: passwordForm.newPassword
        })
        // 调用后端API修改密码
        const response = await axios.put('http://localhost:8080/api/user/updatePassword', null, {
          params: {
            loginId: user.value.id,
            oldPassword: passwordForm.oldPassword,
            newPassword: passwordForm.newPassword
          }
        })
        console.log('密码修改请求成功:', response.data)
        if (response.data) {
          ElMessage.success('密码修改成功')
          // 重置表单
          passwordForm.oldPassword = ''
          passwordForm.newPassword = ''
          passwordForm.confirmPassword = ''
          showPasswordForm.value = false
        } else {
          ElMessage.error('密码修改失败，旧密码错误')
        }
      } catch (error) {
        console.error('密码修改请求失败:', error)
        ElMessage.error('密码修改失败，请稍后重试')
      }
    }
  })
}

const handleAvatarSuccess = (response, file, fileList) => {
  console.log('头像上传响应:', response)
  if (response) {
    console.log('响应类型:', typeof response)
    console.log('响应JSON字符串:', JSON.stringify(response))
    if (response.avatar) {
      userForm.avatar = response.avatar
      console.log('更新头像路径:', response.avatar)
      ElMessage.success('头像上传成功')
    } else {
      console.error('服务器未返回头像路径:', response)
      ElMessage.error('头像上传失败: 服务器未返回头像路径')
    }
  } else {
    console.error('服务器无响应')
    ElMessage.error('头像上传失败: 服务器无响应')
  }
}

const handleAvatarError = (error, file, fileList) => {
  console.log('头像上传错误:', error)
  if (error.response) {
    console.log('错误响应数据:', error.response.data)
    console.log('错误状态:', error.response.status)
    console.log('错误头部:', error.response.headers)
    ElMessage.error(`头像上传失败: ${error.response.status} ${error.response.statusText}`)
  } else if (error.request) {
    console.log('错误请求:', error.request)
    ElMessage.error('头像上传失败: 服务器无响应')
  } else {
    console.log('错误信息:', error.message)
    ElMessage.error(`头像上传失败: ${error.message}`)
  }
}

const handleAvatarUpload = async (options) => {
  console.log('开始自定义上传请求:', options)
  console.log('用户信息:', user.value)
  console.log('用户ID:', user.value?.id)
  
  if (!user.value || !user.value.id) {
    ElMessage.error('用户信息获取失败，请重新登录')
    return
  }
  
  try {
    const formData = new FormData()
    formData.append('file', options.file)
    formData.append('loginId', user.value.id)
    
    console.log('FormData内容:', formData)
    console.log('文件类型:', options.file.type)
    console.log('文件大小:', options.file.size)
    
    const response = await axios.post('http://localhost:8080/api/user/uploadAvatar', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
    
    console.log('上传响应:', response)
    console.log('上传响应状态:', response.status)
    console.log('上传响应数据:', response.data)
    
    // 检查响应数据是否存在
    if (response.data) {
      console.log('响应数据存在')
      console.log('响应数据类型:', typeof response.data)
      console.log('响应数据JSON字符串:', JSON.stringify(response.data))
      
      if (response.data.avatar) {
        console.log('头像路径:', response.data.avatar)
        userForm.avatar = response.data.avatar
        // 强制更新DOM，确保头像能够正常显示
        await nextTick()
        ElMessage.success('头像上传成功')
        // 上传成功后直接使用返回的头像路径，不需要重新加载用户信息
      } else {
        console.error('服务器未返回头像路径:', response.data)
        ElMessage.error('头像上传失败: 服务器未返回头像路径')
      }
    } else {
      console.error('响应数据不存在')
      ElMessage.error('头像上传失败: 服务器返回空响应')
    }
  } catch (error) {
    console.error('上传错误:', error)
    if (error.response) {
      console.error('错误响应:', error.response)
      console.error('错误状态:', error.response.status)
      console.error('错误数据:', error.response.data)
      ElMessage.error(`头像上传失败: ${error.response.status} ${error.response.statusText}`)
    } else if (error.request) {
      console.error('错误请求:', error.request)
      ElMessage.error('头像上传失败: 服务器无响应')
    } else {
      console.error('错误信息:', error.message)
      ElMessage.error(`头像上传失败: ${error.message}`)
    }
  }
}

const beforeAvatarUpload = (file) => {
  console.log('上传前检查文件:', file)
  console.log('用户信息:', user.value)
  console.log('用户ID:', user.value?.id)
  console.log('用户ID类型:', typeof user.value?.id)
  
  // 检查用户信息是否存在
  if (!user.value || !user.value.id) {
    ElMessage.error('用户信息获取失败，请重新登录')
    return false
  }
  
  const isJpgOrPng = file.type === 'image/jpeg' || file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isJpgOrPng) {
    ElMessage.error('只能上传 JPG/PNG 图片！')
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB！')
  }
  console.log('上传前检查结果:', isJpgOrPng && isLt2M)
  return isJpgOrPng && isLt2M
}

const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}

const handleEditClick = () => {
  if (!user.value || !user.value.id) {
    ElMessage.error('用户信息获取失败，请重新登录')
    return
  }
  showEditForm.value = true
}

const loadUserInfo = async () => {
  if (user.value && user.value.id) {
    userForm.username = user.value.username || ''
    userForm.role = user.value.role || ''
    // 保留当前的头像路径，不重置为空
    
    // 根据用户角色获取详细信息
    try {
      let detailResponse
      if (userRole.value === 'USER' || userRole.value === 'user') {
        detailResponse = await axios.get(`http://localhost:8080/api/user/user/${user.value.id}`)
      } else if (userRole.value === 'STAFF' || userRole.value === 'staff') {
        detailResponse = await axios.get(`http://localhost:8080/api/user/staff/${user.value.id}`)
      } else if (userRole.value === 'ADMIN' || userRole.value === 'admin') {
        detailResponse = await axios.get(`http://localhost:8080/api/user/admin/${user.value.id}`)
      }
      
      if (detailResponse && detailResponse.data) {
        const userDetail = detailResponse.data
        userForm.email = userDetail.email || ''
        userForm.phone = userDetail.phone || ''
        userForm.gender = userDetail.gender || ''
        // 只有当后端返回了avatar字段时才更新，否则保留当前值
        if (userDetail.avatar) {
          userForm.avatar = userDetail.avatar
        }
        if (userDetail.creditScore) {
          userForm.creditScore = userDetail.creditScore
        }
        if (userDetail.responsibleArea) {
          userForm.responsibleArea = userDetail.responsibleArea
        }
      }
    } catch (error) {
      console.error('加载用户详细信息失败:', error)
      // 如果API调用失败，使用userStore中的基本信息
      userForm.email = user.value.email || ''
      userForm.phone = user.value.phone || ''
      if (user.value.creditScore) {
        userForm.creditScore = user.value.creditScore
      }
    }
  }
}



onMounted(async () => {
  userStore.loadUserFromStorage()
  await loadUserInfo()
})
</script>

<style scoped>
.profile-container {
  min-height: 100vh;
  background: #f5f7fa;
}

.content {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

.content h1 {
  color: #333;
  margin-bottom: 20px;
}

.info-card,
.password-card,
.checkin-card {
  margin-bottom: 30px;
  transition: all 0.3s ease;
}

.info-card:hover,
.password-card:hover,
.checkin-card:hover {
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

.action-buttons {
  margin: 20px 0;
  display: flex;
  gap: 10px;
}

/* 头像相关样式 */
.avatar-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 20px;
}

.avatar {
  margin-bottom: 10px;
}

.avatar-text {
  font-size: 18px;
  font-weight: bold;
  color: #333;
}

.info-content {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.info-descriptions {
  width: 100%;
  margin-top: 20px;
}

.avatar-upload-container {
  display: flex;
  align-items: center;
  gap: 20px;
}

.avatar-uploader {
  display: flex;
  align-items: center;
}
</style>