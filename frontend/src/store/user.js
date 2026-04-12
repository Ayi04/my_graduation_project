import { defineStore } from 'pinia'
import axios from 'axios'

export const useUserStore = defineStore('user', {
  state: () => ({
    currentUser: null,
    isLoggedIn: false,
    loading: false,
    error: null
  }),
  getters: {
    user: (state) => state.currentUser,
    isAuthenticated: (state) => state.isLoggedIn,
    userRole: (state) => state.currentUser?.role
  },
  actions: {
    async login(username, password) {
      this.loading = true
      this.error = null
      try {
        console.log('发送登录请求:', { username, password })
        const response = await axios.post('http://localhost:8080/api/user/login', { username, password })
        console.log('登录请求成功:', response.data)
        const user = response.data
        this.currentUser = user
        this.isLoggedIn = true
        localStorage.setItem('user', JSON.stringify(user))
        return user
      } catch (error) {
        console.error('登录请求失败:', error)
        this.error = error.response?.data?.message || '登录失败'
        throw error
      } finally {
        this.loading = false
      }
    },
    async register(userData) {
      this.loading = true
      this.error = null
      try {
        console.log('发送注册请求:', userData)
        // 调用新的注册普通用户API
        const response = await axios.post('http://localhost:8080/api/user/register/user', {
          username: userData.username,
          password: userData.password,
          name: userData.username, // 使用用户名作为默认姓名
          gender: '未知', // 默认性别
          phone: userData.phone,
          email: userData.email
        })
        console.log('注册请求成功:', response.data)
        return response.data
      } catch (error) {
        console.error('注册请求失败:', error)
        this.error = error.response?.data?.message || '注册失败'
        throw error
      } finally {
        this.loading = false
      }
    },
    logout() {
      this.currentUser = null
      this.isLoggedIn = false
      localStorage.removeItem('user')
    },
    loadUserFromStorage() {
      const userStr = localStorage.getItem('user')
      if (userStr) {
        this.currentUser = JSON.parse(userStr)
        this.isLoggedIn = true
      }
    }
  }
})