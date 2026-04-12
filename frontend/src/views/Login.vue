<template>
  <div class="login-container">
    <!-- 顶部标题栏 -->
    <div class="header">
      <h1>智能图书馆座位预约管理系统</h1>
    </div>
    
    <!-- 登录表单 -->
    <div class="login-form">
      <el-form :model="loginForm" :rules="loginRules" ref="loginFormRef" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="loginForm.username" placeholder="请输入用户名"></el-input>
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="loginForm.password" :type="passwordVisible ? 'text' : 'password'" placeholder="请输入密码">
            <template #suffix>
              <el-icon @click="passwordVisible = !passwordVisible">
                <component :is="passwordVisible ? 'View' : 'Hide'" />
              </el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleLogin" :loading="loading">登录</el-button>
          <el-button @click="handleRegister">注册</el-button>
        </el-form-item>
      </el-form>
    </div>
    
    <!-- 注册对话框 -->
    <el-dialog
      v-model="registerDialogVisible"
      title="用户注册"
      width="500px"
    >
      <el-form :model="registerForm" :rules="registerRules" ref="registerFormRef" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="registerForm.username" placeholder="请输入用户名"></el-input>
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="registerForm.password" type="password" placeholder="请输入密码"></el-input>
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="registerForm.email" placeholder="请输入邮箱"></el-input>
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="registerForm.phone" placeholder="请输入电话"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="registerDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmitRegister">注册</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../store/user'
import { ElMessage } from 'element-plus'
import { View, Hide } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const loginFormRef = ref(null)
const registerFormRef = ref(null)
const loading = ref(false)
const registerDialogVisible = ref(false)
const passwordVisible = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const registerForm = reactive({
  username: '',
  password: '',
  email: '',
  phone: ''
})

const loginRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const registerRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  email: [{ required: true, message: '请输入邮箱', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入电话', trigger: 'blur' }]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return
  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const user = await userStore.login(loginForm.username, loginForm.password)
        ElMessage.success('登录成功')
        // 根据用户角色跳转到不同首页
        if (user.role === 'ADMIN' || user.role === 'STAFF') {
          router.push('/home')
        } else {
          router.push('/home')
        }
      } catch (error) {
        ElMessage.error('登录失败，请检查用户名和密码')
      } finally {
        loading.value = false
      }
    }
  })
}

const handleRegister = () => {
  registerDialogVisible.value = true
}

const handleSubmitRegister = async () => {
  if (!registerFormRef.value) return
  await registerFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        // 确保注册为普通用户
        const userData = {
          ...registerForm,
          role: 'USER'
        }
        await userStore.register(userData)
        ElMessage.success('注册成功')
        registerDialogVisible.value = false
      } catch (error) {
        ElMessage.error('注册失败，请稍后重试')
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.login-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background-image: url('/images/login_beijingtu.jpg');
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  background-attachment: fixed;
  position: relative;
  overflow: hidden;
}

.login-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.4);
  z-index: 1;
  backdrop-filter: blur(0px);
}

.header {
  position: relative;
  z-index: 2;
  background: rgba(255, 255, 255, 0.4);
  padding: 20px;
  text-align: center;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(5px);
}

.header h1 {
  margin: 0;
  color: #000000;
  font-size: 24px;
  font-weight: bold;
  font-family: 'FZQianLiZhenYongLeDaDianTiS-GB', 'SimSun', 'STSong', '宋体', serif;
}

.login-form {
  position: relative;
  z-index: 2;
  width: 420px;
  padding: 50px;
  background: rgba(255, 255, 255, 0.7);
  border-radius: 20px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
  align-self: center;
  margin: auto;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.3);
  transition: all 0.3s ease;
}

.login-form:hover {
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.25);
  transform: translateY(-5px);
}

/* 表单元素样式 */
.el-form {
  width: 100%;
}

.el-form-item {
  margin-bottom: 24px;
}

.el-form-item__label {
  font-weight: 500;
  color: #333;
  font-size: 14px;
}

.el-input {
  border-radius: 20px;
  height: 44px;
  font-size: 14px;
}

.el-input__wrapper {
  border-radius: 20px;
}

/* 按钮样式 */
.el-button {
  border-radius: 20px;
  padding: 10px 24px;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.el-button--primary {
  background-color: #1890ff;
  border-color: #1890ff;
}

.el-button--primary:hover {
  background-color: #40a9ff;
  border-color: #40a9ff;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .login-form {
    width: 90%;
    max-width: 400px;
    padding: 30px;
  }
}
</style>
