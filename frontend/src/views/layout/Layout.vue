<template>
  <el-container class="layout-container">
    <el-header class="header" height="60px">
      <div class="header-left">
        <h2 class="logo">TimeWeaver</h2>
      </div>
      <div class="header-right">
        <el-button @click="toggleDark" text circle size="large" :title="isDark ? '切换亮色模式' : '切换暗色模式'">
          <el-icon size="20">
            <template v-if="isDark"><svg viewBox="0 0 24 24" fill="currentColor"><path d="M12 7c-2.76 0-5 2.24-5 5s2.24 5 5 5 5-2.24 5-5-2.24-5-5-5zM2 13h2c.55 0 1-.45 1-1s-.45-1-1-1H2c-.55 0-1 .45-1 1s.45 1 1 1zm18 0h2c.55 0 1-.45 1-1s-.45-1-1-1h-2c-.55 0-1 .45-1 1s.45 1 1 1zM11 2v2c0 .55.45 1 1 1s1-.45 1-1V2c0-.55-.45-1-1-1s-1 .45-1 1zm0 18v2c0 .55.45 1 1 1s1-.45 1-1v-2c0-.55-.45-1-1-1s-1 .45-1 1zM5.99 4.58a.996.996 0 0 0-1.41 0 .996.996 0 0 0 0 1.41l1.06 1.06c.39.39 1.03.39 1.41 0s.39-1.03 0-1.41L5.99 4.58zm12.37 12.37a.996.996 0 0 0-1.41 0 .996.996 0 0 0 0 1.41l1.06 1.06c.39.39 1.03.39 1.41 0a.996.996 0 0 0 0-1.41l-1.06-1.06zm1.06-10.96a.996.996 0 0 0 0-1.41.996.996 0 0 0-1.41 0l-1.06 1.06c-.39.39-.39 1.03 0 1.41s1.03.39 1.41 0l1.06-1.06zM7.05 18.36a.996.996 0 0 0 0-1.41.996.996 0 0 0-1.41 0l-1.06 1.06c-.39.39-.39 1.03 0 1.41s1.03.39 1.41 0l1.06-1.06z"/></svg></template>
            <template v-else><svg viewBox="0 0 24 24" fill="currentColor"><path d="M9.37 5.51A7.35 7.35 0 0 0 9.1 7.5c0 4.08 3.32 7.4 7.4 7.4.68 0 1.35-.09 1.99-.27A7.014 7.014 0 0 1 12 19c-3.86 0-7-3.14-7-7 0-2.93 1.81-5.45 4.37-6.49zM12 3a9 9 0 1 0 9 9c0-.46-.04-.92-.1-1.36a5.389 5.389 0 0 1-4.4 2.26 5.403 5.403 0 0 1-3.14-9.8c-.44-.06-.9-.1-1.36-.1z"/></svg></template>
          </el-icon>
        </el-button>
        <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="notification-badge">
          <el-button @click="showNotifications = true" text circle size="large">
            <el-icon size="20"><Bell /></el-icon>
          </el-button>
        </el-badge>
        <el-dropdown trigger="click" @command="handleCommand">
          <span class="user-info">
            {{ userStore.user?.nickname || userStore.user?.username }}
            <el-icon><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">个人信息</el-dropdown-item>
              <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>
    <el-container>
      <el-aside width="200px" class="aside">
        <el-menu :default-active="activeMenu" router>
          <el-menu-item index="/dashboard">
            <el-icon><Odometer /></el-icon>
            <span>仪表盘</span>
          </el-menu-item>
          <el-menu-item index="/records">
            <el-icon><List /></el-icon>
            <span>时间记录</span>
          </el-menu-item>
          <el-menu-item index="/templates">
            <el-icon><CopyDocument /></el-icon>
            <span>时间模板</span>
          </el-menu-item>
          <el-menu-item index="/pomodoro">
            <el-icon><Timer /></el-icon>
            <span>番茄钟</span>
          </el-menu-item>
          <el-menu-item index="/stats">
            <el-icon><DataAnalysis /></el-icon>
            <span>数据分析</span>
          </el-menu-item>
          <el-menu-item index="/calendar">
            <el-icon><Calendar /></el-icon>
            <span>日历视图</span>
          </el-menu-item>
          <el-menu-item index="/goals">
            <el-icon><Flag /></el-icon>
            <span>目标管理</span>
          </el-menu-item>
          <el-menu-item index="/ai-sessions">
            <el-icon><Connection /></el-icon>
            <span>AI 会话</span>
          </el-menu-item>
          <el-menu-item index="/git">
            <el-icon><Share /></el-icon>
            <span>Git 集成</span>
          </el-menu-item>
          <el-menu-item index="/yearly">
            <el-icon><TrendCharts /></el-icon>
            <span>年度报告</span>
          </el-menu-item>
          <el-menu-item index="/reports">
            <el-icon><Document /></el-icon>
            <span>AI 周报</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>

    <!-- Notification Drawer -->
    <el-drawer v-model="showNotifications" title="通知" size="360px">
      <div v-if="insights.length === 0" class="empty-notifications">
        <el-empty description="暂无通知" :image-size="80" />
      </div>
      <div v-for="item in insights" :key="item.id" class="notification-item"
        :class="{ 'notification-unread': !item.isRead }"
        @click="markRead(item)">
        <div class="notification-title">{{ item.title }}</div>
        <div class="notification-content">{{ item.content }}</div>
        <div class="notification-time">{{ item.generatedAt }}</div>
      </div>
    </el-drawer>

    <!-- Keyboard Shortcuts Help -->
    <el-dialog v-model="showShortcuts" title="键盘快捷键" width="420px" :close-on-click-modal="true">
      <div class="shortcuts-list">
        <div v-for="item in shortcutsList" :key="item.key" class="shortcut-item">
          <el-tag size="small" style="width: 60px; text-align: center; font-weight: 600;">{{ item.key }}</el-tag>
          <span class="shortcut-desc">{{ item.desc }}</span>
        </div>
      </div>
    </el-dialog>
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/store/user'
import {
  Odometer, List, Timer, DataAnalysis, Flag, Connection, Document, Share,
  ArrowDown, Bell, CopyDocument, Calendar, TrendCharts,
} from '@element-plus/icons-vue'
import { ElNotification } from 'element-plus'
import request from '@/utils/request'
import { useShortcuts } from '@/composables/useShortcuts'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)
const showNotifications = ref(false)
const showShortcuts = ref(false)
const insights = ref([])
const isDark = ref(false)
let ws = null
let heartbeatTimer = null

const unreadCount = computed(() => insights.value.filter(i => !i.isRead).length)

function toggleDark() {
  if (window.__toggleDark) {
    window.__toggleDark()
    isDark.value = !isDark.value
  }
}

// Sync dark state on mount
onMounted(() => {
  isDark.value = localStorage.getItem('timeweaver-dark-mode') === 'true'
})

function handleCommand(command) {
  if (command === 'profile') {
    router.push('/profile')
  } else if (command === 'logout') {
    userStore.logout()
    router.push('/login')
  }
}

async function loadInsights() {
  try {
    const res = await request.get('/reports/insights')
    if (res.code === 200) insights.value = res.data
  } catch {}
}

async function markRead(item) {
  if (item.isRead) return
  try {
    await request.put(`/reports/insights/${item.id}/read`)
    item.isRead = 1
  } catch {}
}

function connectWebSocket() {
  const user = userStore.user
  if (!user) return
  const token = localStorage.getItem('token')
  const wsUrl = `${window.location.protocol === 'https:' ? 'wss:' : 'ws:'}//${window.location.host}/ws/notification?token=${token}`
  try {
    ws = new WebSocket(wsUrl)
    ws.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data)
        if (data.type === 'notification') {
          ElNotification({
            title: data.title,
            message: data.content,
            type: 'success',
            duration: 4000,
          })
          loadInsights()
        }
      } catch {}
    }
    ws.onclose = () => {
      // Reconnect after 5s
      setTimeout(connectWebSocket, 5000)
    }
    ws.onerror = () => {
      ws?.close()
    }
  } catch {}
}

onMounted(() => {
  loadInsights()
  // Delay WS connect to ensure user store is loaded
  setTimeout(connectWebSocket, 1000)
})

// 键盘快捷键（全局）
useShortcuts({
  '?': () => { showShortcuts.value = !showShortcuts.value },
  Escape: () => { showShortcuts.value = false },
}, { inputFilter: false })

const shortcutsList = [
  { key: '?', desc: '显示/隐藏快捷键帮助' },
  { key: 'S', desc: '开始/停止计时（仪表盘）' },
  { key: 'N', desc: '聚焦分类选择（仪表盘）' },
]

onUnmounted(() => {
  ws?.close()
  clearInterval(heartbeatTimer)
})
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  padding: 0 20px;
}

.logo {
  font-size: 20px;
  color: #409eff;
  margin: 0;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-info {
  cursor: pointer;
  color: #606266;
  display: flex;
  align-items: center;
  gap: 4px;
}

.aside {
  background: #fff;
  border-right: 1px solid #e4e7ed;
}

.main-content {
  background: #f5f7fa;
  padding: 20px;
  overflow-y: auto;
}

.notification-badge {
  margin-top: 4px;
}

.notification-item {
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background 0.2s;
}

.notification-item:hover {
  background: #f5f7fa;
}

.notification-unread {
  background: #ecf5ff;
}

.notification-unread:hover {
  background: #d9ecff;
}

.shortcuts-list { display: flex; flex-direction: column; gap: 12px; }
.shortcut-item { display: flex; align-items: center; gap: 12px; }
.shortcut-desc { font-size: 14px; color: #606266; }

.notification-title {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
}

.notification-content {
  font-size: 13px;
  color: #606266;
  line-height: 1.5;
}

.notification-time {
  font-size: 12px;
  color: #c0c4cc;
  margin-top: 4px;
}

.empty-notifications {
  padding: 40px 0;
}
</style>
