<template>
  <div :class="{ 'dark': isDark }">
    <router-view />
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'

const isDark = ref(false)

function toggleDark() {
  isDark.value = !isDark.value
  localStorage.setItem('timeweaver-dark-mode', isDark.value ? 'true' : 'false')
  document.documentElement.classList.toggle('dark', isDark.value)
}

function initTheme() {
  const saved = localStorage.getItem('timeweaver-dark-mode')
  if (saved === 'true') {
    isDark.value = true
    document.documentElement.classList.add('dark')
  }
}

// Expose toggle globally so Layout can call it
window.__toggleDark = toggleDark
window.__isDark = isDark

onMounted(initTheme)
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body, #app {
  height: 100%;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

/* ---- Dark Mode Variables ---- */
html.dark {
  --bg-primary: #1a1a2e;
  --bg-secondary: #16213e;
  --bg-card: #1e2746;
  --bg-main: #0f3460;
  --text-primary: #e0e0e0;
  --text-secondary: #a0a0b0;
  --text-muted: #6b7280;
  --border-color: #2a3a5e;
}

html.dark body {
  background: var(--bg-primary);
  color: var(--text-primary);
}

html.dark .el-card {
  background: var(--bg-card) !important;
  border-color: var(--border-color) !important;
}

html.dark .el-main {
  background: var(--bg-primary) !important;
}

html.dark .el-header {
  background: var(--bg-secondary) !important;
  border-bottom-color: var(--border-color) !important;
}

html.dark .el-aside {
  background: var(--bg-secondary) !important;
  border-right-color: var(--border-color) !important;
}

html.dark .el-menu {
  background: var(--bg-secondary) !important;
  border-right: none !important;
}

html.dark .el-menu-item {
  color: var(--text-secondary) !important;
}

html.dark .el-menu-item.is-active {
  background: rgba(64, 158, 255, 0.1) !important;
  color: #409eff !important;
}

html.dark .el-drawer {
  background: var(--bg-card) !important;
}

html.dark .notification-item {
  border-bottom-color: var(--border-color) !important;
}

html.dark .notification-title {
  color: var(--text-primary) !important;
}

html.dark .notification-content {
  color: var(--text-secondary) !important;
}

html.dark .notification-unread {
  background: rgba(64, 158, 255, 0.08) !important;
}

html.dark .notification-unread:hover {
  background: rgba(64, 158, 255, 0.15) !important;
}

html.dark .el-table {
  background: var(--bg-card) !important;
  color: var(--text-primary) !important;
}

html.dark .el-table__header th {
  background: var(--bg-secondary) !important;
  color: var(--text-secondary) !important;
}

html.dark .el-table__body tr {
  background: var(--bg-card) !important;
}

html.dark .el-table__body td {
  background: var(--bg-card) !important;
  color: var(--text-primary) !important;
}

html.dark .el-table--striped .el-table__body tr.el-table__row--striped td {
  background: var(--bg-secondary) !important;
}

html.dark .el-dialog {
  background: var(--bg-card) !important;
}

html.dark .el-dialog__title {
  color: var(--text-primary) !important;
}

html.dark .el-form-item__label {
  color: var(--text-secondary) !important;
}

html.dark .el-empty__description p {
  color: var(--text-muted) !important;
}

html.dark .el-divider {
  border-color: var(--border-color) !important;
}

html.dark .el-skeleton__item {
  background: var(--bg-secondary) !important;
}

html.dark input, html.dark .el-input__wrapper {
  background: var(--bg-secondary) !important;
}

html.dark .goal-name {
  color: var(--text-primary) !important;
}

html.dark .pomo-card {
  background: var(--bg-card) !important;
}

html.dark .pomo-time {
  color: var(--text-primary) !important;
}

html.dark .stat-chip {
  background: var(--bg-card) !important;
}

html.dark .stat-num {
  color: var(--text-primary) !important;
}

html.dark .summary-card {
  background: var(--bg-card) !important;
}

html.dark .task-input {
  color: var(--text-primary) !important;
}

html.dark .task-input::placeholder {
  color: var(--text-muted) !important;
}

html.dark .pomo-session {
  color: var(--text-muted) !important;
}

html.dark .period-label {
  color: var(--text-secondary) !important;
}

html.dark .profile-page .el-form-item__label {
  color: var(--text-secondary) !important;
}

html.dark .pomo-status {
  color: var(--text-muted) !important;
}

html.dark .setting-item select {
  background: var(--bg-secondary) !important;
  color: var(--text-primary) !important;
  border-color: var(--border-color) !important;
}

html.dark .setting-item {
  color: var(--text-muted) !important;
}

html.dark .user-info {
  color: var(--text-secondary) !important;
}

html.dark .el-progress__text {
  color: var(--text-secondary) !important;
}

html.dark .el-select-dropdown {
  background: var(--bg-card) !important;
}

html.dark .el-select-dropdown__item {
  color: var(--text-primary) !important;
}

html.dark .el-popper {
  background: var(--bg-card) !important;
  border-color: var(--border-color) !important;
}

html.dark .el-dropdown-menu {
  background: var(--bg-card) !important;
}

html.dark .el-dropdown-menu__item {
  color: var(--text-primary) !important;
}

html.dark .el-dropdown-menu__item:hover {
  background: rgba(64, 158, 255, 0.1) !important;
}

html.dark .el-date-picker {
  background: var(--bg-card) !important;
}

html.dark .el-picker-panel {
  background: var(--bg-card) !important;
}

html.dark .el-date-table td span {
  color: var(--text-primary) !important;
}

html.dark .el-date-table th {
  color: var(--text-secondary) !important;
}

html.dark .not-found-page {
  background: var(--bg-primary) !important;
}

html.dark .error-title {
  color: var(--text-primary) !important;
}

/* ── 仪表盘 ── */
html.dark .summary-value {
  color: var(--text-primary) !important;
}
html.dark .summary-label,
html.dark .chip-label,
html.dark .template-tip {
  color: var(--text-secondary) !important;
}
html.dark .timeline-item {
  border-bottom-color: var(--border-color) !important;
}
html.dark .timeline-time,
html.dark .timeline-duration {
  color: var(--text-muted) !important;
}
html.dark .timeline-desc {
  color: var(--text-secondary) !important;
}

/* ── 快捷模板按钮 ── */
html.dark .quick-templates-label {
  color: var(--text-muted) !important;
}

/* ── 统计图表 ── */
html.dark .stats-chart-title {
  color: var(--text-primary) !important;
}
html.dark .stats-card-header {
  color: var(--text-secondary) !important;
}

/* ── 提醒横幅 ── */
html.dark .el-alert--warning {
  background: rgba(230, 162, 60, 0.15) !important;
}

/* ── 标签 ── */
html.dark .el-tag.el-tag--plain {
  background: transparent !important;
  color: var(--text-primary) !important;
  border-color: var(--border-color) !important;
}

/* ── 分页 ── */
html.dark .el-pagination button,
html.dark .el-pagination .el-pager li {
  background: transparent !important;
  color: var(--text-secondary) !important;
}
html.dark .el-pagination .el-pager li.is-active {
  color: #409eff !important;
}

/* ── 进度条背景 ── */
html.dark .el-progress-bar__outer {
  background: var(--bg-secondary) !important;
}

/* ── 开关 ── */
html.dark .el-switch.is-checked .el-switch__core {
  border-color: #409eff !important;
  background: #409eff !important;
}

/* ── 统计卡片 ── */
html.dark .stat-chip {
  background: var(--bg-card) !important;
}
html.dark .stat-num {
  color: var(--text-primary) !important;
}

/* ── AI 会话页面 ── */
html.dark .chart-container {
  background: var(--bg-card) !important;
}

/* ── 目标管理 ── */
html.dark .goal-item {
  background: var(--bg-card) !important;
  border-color: var(--border-color) !important;
}
html.dark .goal-name {
  color: var(--text-primary) !important;
}
html.dark .goal-progress-text {
  color: var(--text-muted) !important;
}

/* ── 番茄钟 ── */
html.dark .pomo-card {
  background: var(--bg-card) !important;
}
html.dark .pomo-time {
  color: var(--text-primary) !important;
}
html.dark .pomo-status {
  color: var(--text-muted) !important;
}

/* ── 通知角标 ── */
html.dark .el-badge__content {
  border-color: var(--bg-secondary) !important;
}

/* ── ECharts 适配 ── */
html.dark .echarts-container {
  background: transparent !important;
}

</style>
