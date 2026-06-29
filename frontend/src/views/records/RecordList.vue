<template>
  <div class="record-list-page">
    <!-- Date Navigation -->
    <el-card class="mb-20">
      <div class="date-nav">
        <el-button @click="changeDate(-1)" text>
          <el-icon><ArrowLeft /></el-icon>
        </el-button>
        <el-date-picker
          v-model="selectedDate"
          type="date"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
          @change="loadRecords"
          size="large"
          style="width: 160px"
        />
        <el-button @click="changeDate(1)" text>
          <el-icon><ArrowRight /></el-icon>
        </el-button>
        <el-button @click="selectedDate = today; loadRecords()" size="small" class="ml-4">今天</el-button>
        <span class="daily-total">总时长：{{ formatDuration(dailyTotal) }}</span>
      </div>
    </el-card>

    <!-- Record Table -->
    <el-card>
      <template #header>
        <div class="card-header">
          <span>{{ selectedDate }} 记录</span>
          <el-button type="primary" size="small" @click="showAddDialog = true">
            + 手动添加
          </el-button>
          <el-button size="small" @click="exportCSV">
            导出 CSV
          </el-button>
        </div>
      </template>

      <el-table :data="records" stripe style="width: 100%" v-loading="loading">
        <el-table-column label="时间" width="160">
          <template #default="{ row }">
            {{ formatTime(row.startTime) }} - {{ formatTime(row.endTime) }}
          </template>
        </el-table-column>
        <el-table-column label="分类" width="120">
          <template #default="{ row }">
            <el-tag :color="getCategoryColor(row.categoryId)" size="small" effect="dark" style="color:#fff; border:none">
              {{ getCategoryName(row.categoryId) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="描述" min-width="200">
          <template #default="{ row }">
            {{ row.description || '—' }}
          </template>
        </el-table-column>
        <el-table-column label="时长" width="80" align="right">
          <template #default="{ row }">
            {{ formatDuration(row.durationMin) }}
          </template>
        </el-table-column>
        <el-table-column label="来源" width="80">
          <template #default="{ row }">
            <el-tag :type="row.source === 'pomodoro' ? 'warning' : 'info'" size="small">
              {{ row.source === 'pomodoro' ? '番茄钟' : '手动' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" size="small" @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && records.length === 0" description="当天暂无记录" :image-size="80" />
    </el-card>

    <!-- Add Record Dialog -->
    <el-dialog v-model="showAddDialog" title="手动添加记录" width="420px">
      <el-form :model="form" label-width="60px">
        <el-form-item label="分类">
          <el-select v-model="form.categoryId" placeholder="选择分类" style="width: 100%" filterable>
            <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" placeholder="描述" maxlength="200" />
        </el-form-item>
        <el-form-item label="开始">
          <el-date-picker v-model="form.startTime" type="datetime" placeholder="开始时间" format="YYYY-MM-DD HH:mm" style="width: 100%" />
        </el-form-item>
        <el-form-item label="结束">
          <el-date-picker v-model="form.endTime" type="datetime" placeholder="结束时间" format="YYYY-MM-DD HH:mm" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleAdd">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, ArrowRight } from '@element-plus/icons-vue'
import { formatTime, formatDuration } from '@/utils/format'
import { getRecords, createRecord, deleteRecord } from '@/api/records'
import { getCategories } from '@/api/categories'

const today = new Date().toISOString().slice(0, 10)
const selectedDate = ref(today)
const records = ref([])
const categories = ref([])
const loading = ref(false)
const showAddDialog = ref(false)
const saving = ref(false)

const form = ref({
  categoryId: null,
  description: '',
  startTime: null,
  endTime: null,
})

const dailyTotal = computed(() =>
  records.value.reduce((sum, r) => sum + (r.durationMin || 0), 0))

function getCategoryColor(id) {
  return categories.value.find(c => c.id === id)?.color || '#909399'
}
function getCategoryName(id) {
  return categories.value.find(c => c.id === id)?.name || '未知'
}

function changeDate(delta) {
  const d = new Date(selectedDate.value)
  d.setDate(d.getDate() + delta)
  selectedDate.value = d.toISOString().slice(0, 10)
  loadRecords()
}

async function loadRecords() {
  loading.value = true
  try {
    const res = await getRecords({ date: selectedDate.value })
    records.value = res.data || []
  } catch {}
  finally { loading.value = false }
}

async function loadCategories() {
  try {
    const res = await getCategories()
    if (res.code === 200) categories.value = res.data || []
  } catch {}
}

async function handleAdd() {
  if (!form.value.categoryId) { ElMessage.warning('请选择分类'); return }
  if (!form.value.startTime || !form.value.endTime) { ElMessage.warning('请选择开始和结束时间'); return }
  saving.value = true
  try {
    await createRecord({
      ...form.value,
      source: 'manual',
      startTime: form.value.startTime.toISOString ? form.value.startTime.toISOString() : form.value.startTime,
      endTime: form.value.endTime.toISOString ? form.value.endTime.toISOString() : form.value.endTime,
    })
    ElMessage.success('添加成功')
    showAddDialog.value = false
    form.value = { categoryId: null, description: '', startTime: null, endTime: null }
    await loadRecords()
  } catch {}
  finally { saving.value = false }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确定删除这条记录？', '确认')
    await deleteRecord(row.id)
    ElMessage.success('已删除')
    await loadRecords()
  } catch {}
}

function exportCSV() {
  if (!records.value.length) {
    ElMessage.warning('没有数据可导出')
    return
  }

  const headers = ['日期', '开始时间', '结束时间', '分类', '描述', '时长(分钟)', '来源']
  const rows = records.value.map(r => [
    selectedDate.value,
    formatTime(r.startTime),
    formatTime(r.endTime),
    getCategoryName(r.categoryId),
    r.description || '',
    r.durationMin || 0,
    r.source === 'pomodoro' ? '番茄钟' : '手动',
  ])

  const csv = [headers.join(','), ...rows.map(row => row.map(cell => `"${cell}"`).join(','))].join('\n')
  const blob = new Blob(['\uFEFF' + csv], { type: 'text/csv;charset=utf-8;' })
  const a = document.createElement('a')
  a.href = URL.createObjectURL(blob)
  a.download = `时间记录_${selectedDate.value}.csv`
  a.click()
  URL.revokeObjectURL(a.href)
  ElMessage.success('已导出')
}

onMounted(() => {
  loadCategories()
  loadRecords()
})
</script>

<style scoped>
.mb-20 { margin-bottom: 20px; }
.ml-4 { margin-left: 8px; }
.date-nav {
  display: flex;
  align-items: center;
  gap: 8px;
}
.daily-total {
  margin-left: auto;
  font-size: 14px;
  color: #909399;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
