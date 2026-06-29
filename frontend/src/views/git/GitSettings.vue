<template>
  <div class="git-settings">
    <h3>Git 集成</h3>

    <!-- 仓库管理 -->
    <el-card shadow="hover" class="section-card">
      <template #header>
        <div class="card-header">
          <span>仓库管理</span>
          <el-button type="primary" size="small" @click="showAddDialog = true">
            + 添加仓库
          </el-button>
        </div>
      </template>

      <el-table :data="repos" v-loading="reposLoading" empty-text="暂无仓库配置" stripe>
        <el-table-column prop="repoName" label="仓库名称" width="180" />
        <el-table-column prop="repoPath" label="路径" min-width="300" show-overflow-tooltip />
        <el-table-column prop="autoImport" label="自动导入" width="120" align="center">
          <template #default="{ row }">
            <el-switch
              :model-value="row.autoImport"
              @change="(val) => toggleAutoImport(row, val)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center">
          <template #default="{ row }">
            <el-popconfirm title="确定删除此仓库？" @confirm="handleRemove(row)">
              <template #reference>
                <el-button type="danger" size="small" text>删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 扫描与导入 -->
    <el-card shadow="hover" class="section-card">
      <template #header>
        <div class="card-header">
          <span>扫描与导入</span>
          <div>
            <el-date-picker
              v-model="scanDate"
              type="date"
              value-format="YYYY-MM-DD"
              placeholder="选择日期"
              style="width: 150px; margin-right: 8px"
            />
            <el-button type="success" :loading="scanLoading" @click="handleScan">
              扫描提交
            </el-button>
            <el-button type="warning" :loading="importLoading" @click="handleImport">
              导入为时间记录
            </el-button>
          </div>
        </div>
      </template>

      <el-table :data="commits" v-loading="commitsLoading" empty-text="暂无提交记录" stripe>
        <el-table-column prop="repoPath" label="仓库" width="120" />
        <el-table-column prop="commitHash" label="Hash" width="100">
          <template #default="{ row }">
            <code style="font-size: 12px">{{ row.commitHash.substring(0, 8) }}</code>
          </template>
        </el-table-column>
        <el-table-column prop="authorName" label="作者" width="100" />
        <el-table-column prop="message" label="提交信息" min-width="300" show-overflow-tooltip />
        <el-table-column prop="committedAt" label="时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.committedAt) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.recordId" type="success" size="small">已导入</el-tag>
            <el-tag v-else type="info" size="small">未导入</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 添加仓库对话框 -->
    <el-dialog v-model="showAddDialog" title="添加仓库" width="500px">
      <el-form :model="addForm" label-width="80px">
        <el-form-item label="路径">
          <el-input v-model="addForm.repoPath" placeholder="输入本地仓库绝对路径，如 /Users/xxx/project" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" :loading="addLoading" @click="handleAdd">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getRepos, addRepo, removeRepo, updateAutoImport, getGitCommits, scanCommits, importCommits } from '@/api/git'

const repos = ref([])
const reposLoading = ref(false)
const commits = ref([])
const commitsLoading = ref(false)
const scanLoading = ref(false)
const importLoading = ref(false)
const addLoading = ref(false)
const showAddDialog = ref(false)
const scanDate = ref(new Date().toISOString().slice(0, 10))
const addForm = ref({ repoPath: '' })

function formatDateTime(dt) {
  if (!dt) return '—'
  return dt.replace('T', ' ').substring(0, 19)
}

async function loadRepos() {
  reposLoading.value = true
  try {
    const res = await getRepos()
    if (res.code === 200) repos.value = res.data || []
  } catch { ElMessage.error('加载仓库列表失败') }
  finally { reposLoading.value = false }
}

async function loadCommits() {
  commitsLoading.value = true
  try {
    const res = await getGitCommits({ date: scanDate.value })
    if (res.code === 200) commits.value = res.data || []
  } catch { /* ignore */ }
  finally { commitsLoading.value = false }
}

async function handleAdd() {
  if (!addForm.value.repoPath) {
    ElMessage.warning('请输入仓库路径')
    return
  }
  addLoading.value = true
  try {
    const res = await addRepo(addForm.value.repoPath)
    if (res.code === 200) {
      ElMessage.success('添加成功')
      showAddDialog.value = false
      addForm.value.repoPath = ''
      await loadRepos()
    }
  } catch { ElMessage.error('添加失败') }
  finally { addLoading.value = false }
}

async function handleRemove(row) {
  try {
    await removeRepo(row.id)
    ElMessage.success('已删除')
    await loadRepos()
  } catch { ElMessage.error('删除失败') }
}

async function toggleAutoImport(row, val) {
  try {
    await updateAutoImport(row.id, val)
    row.autoImport = val
    ElMessage.success(val ? '已开启自动导入' : '已关闭自动导入')
  } catch { ElMessage.error('操作失败') }
}

async function handleScan() {
  scanLoading.value = true
  try {
    const res = await scanCommits({ date: scanDate.value })
    if (res.code === 200) {
      ElMessage.success(`扫描完成，发现 ${res.data?.length || 0} 条新提交`)
      await loadCommits()
    }
  } catch { ElMessage.error('扫描失败，请检查仓库路径是否正确') }
  finally { scanLoading.value = false }
}

async function handleImport() {
  importLoading.value = true
  try {
    const res = await importCommits({ date: scanDate.value })
    if (res.code === 200) {
      ElMessage.success(`已导入 ${res.data?.length || 0} 条时间记录`)
      await loadCommits()
    }
  } catch { ElMessage.error('导入失败') }
  finally { importLoading.value = false }
}

onMounted(() => {
  loadRepos()
  loadCommits()
})
</script>

<style scoped>
.git-settings {
  max-width: 1200px;
}
.section-card {
  margin-bottom: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
