import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getToken, setToken, removeToken, setUser, removeUser, getUser } from '@/utils/auth'
import request from '@/utils/request'

export const useUserStore = defineStore('user', () => {
  const token = ref(getToken())
  const user = ref(getUser())

  const isLoggedIn = computed(() => !!token.value)
  const username = computed(() => user.value?.username || '')

  async function login(loginForm) {
    const res = await request.post('/auth/login', loginForm)
    const data = res.data
    setToken(data.token)
    setUser(data.user)
    token.value = data.token
    user.value = data.user
    return data
  }

  async function register(registerForm) {
    return request.post('/auth/register', registerForm)
  }

  function logout() {
    removeToken()
    removeUser()
    token.value = null
    user.value = null
  }

  async function fetchProfile() {
    const res = await request.get('/user/profile')
    user.value = res.data
    setUser(res.data)
    return res.data
  }

  return { token, user, isLoggedIn, username, login, register, logout, fetchProfile }
})
