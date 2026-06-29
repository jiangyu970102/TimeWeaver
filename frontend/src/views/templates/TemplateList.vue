<template>
  <div class="template-list">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>时间记录模板</span>
          <el-button type="primary" size="small" @click="openCreate">
            + 新建模板
          </el-button>
        </div>
      </template>

      <el-table :data="templates" v-loading="loading" empty-text="暂无模板，点击右上角新建" stripe>
        <el-table-column prop="name" label="模板名称" width="180" />
        <el-table-column label="分类" width="160">
          <template #default="{ row }">
            <el-tag :color="getCategoryColor(row.categoryId)" size="small" effect="dark" style="color: #fff; border: none">
              {{ getCategoryName(row.categoryId) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="默认描述" min-width="300" show-overflow-tooltip />
        <el-table-column label="操作" width="160" align="center">
          <template #default="{ row }">
            <el-button size="small" text @click="openEdit(row)">编辑</el-button>
            <el-popconfirm title="确定删除此模板？" @confirm="handleDelete(row)">
              <template #reference>
                <el-button type="danger" size="small" text>删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新建/编辑弹窗 -->
    <el-dialog v-model="showDialog" :title="editingId ? '编辑模板' : '新建模板'" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="模板名称" prop="name">
          <el-input v-model="form.name" placeholder="如：晨会、编码、Code Review" maxlength="50" />
        </el-form-item>
        <el-form-item label="关联分类" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="选择分类" filterable style="width: 100%">
            <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id">
              <span :style="{ color: cat.color }">{{ cat.icon }} {{ cat.name }}</span>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="默认描述">
          <el-input v-model="form.description" placeholder="选填，开始计时后自动填入" maxlength="200" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getTemplates, createTemplate, updateTemplate, deleteTemplate } from '@/api/record-templates'
import { getCategories } from '@/api/categories'

const templates = ref([])
const categories = ref([])
const loading = ref(false)
const showDialog = ref(false)
const submitting = ref(false)
const editingId = ref(null)
const formRef = ref(null)

const form = reactive({
  name: '',
  categoryId: null,
  description: '',
})

const rules = {
  name: [{ required: true, message: '请输入模板名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
}

function getCategoryColor(categoryId) {
  const cat = categories.value.find(c => c.id === categoryId)
  return cat?.color || '#909399'
}

function getCategoryName(categoryId) {
  const cat = categories.value.find(c => c.id === categoryId)
  return cat?.name || '未知'
}

async function loadTemplates() {
  loading.value = true
  try {
    const res = await getTemplates()
    if (res.code === 200) templates.value = res.data || []
  } catch { ElMessage.error('加载模板失败') }
  finally { loading.value = false }
}

async function loadCategories() {
  try {
    const res = await getCategories()
    if (res.code === 200) categories.value = res.data || []
  } catch {}
}

function openCreate() {
  editingId.value = null
  form.name = ''
  form.categoryId = null
  form.description = ''
  showDialog.value = true
}

function openEdit(row) {
  editingId.value = row.id
  form.name = row.name
  form.categoryId = row.categoryId
  form.description = row.description || ''
  showDialog.value = true
}

async function submitForm() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    const data = { name: form.name, categoryId: form.categoryId, description: form.description || '' }
    if (editingId.value) {
      await updateTemplate(editingId.value, data)
      ElMessage.success('模板已更新')
    } else {
      await createTemplate(data)
      ElMessage.success('模板已创建')
    }
    showDialog.value = false
    await loadTemplates()
  } catch { ElMessage.error('操作失败') }
  finally { submitting.value = false }
}

async function handleDelete(row) {
  try {
    await deleteTemplate(row.id)
    ElMessage.success('模板已删除')
    await loadTemplates()
  } catch { ElMessage.error('删除失败') }
}

onMounted(() => {
  loadTemplates()
  loadCategories()
})
</script>

<style scoped>
.template-list {
  max-width: 900px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
