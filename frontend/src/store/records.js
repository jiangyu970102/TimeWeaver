import { defineStore } from 'pinia'
import { ref } from 'vue'
import request from '@/utils/request'

export const useRecordStore = defineStore('records', () => {
  const currentRecord = ref(null)
  const todayRecords = ref([])

  async function startRecord(categoryId, description) {
    const res = await request.post('/records', { categoryId, description, source: 'manual' })
    currentRecord.value = res.data
    return res.data
  }

  async function stopRecord(id) {
    const res = await request.put(`/records/${id}/stop`)
    currentRecord.value = null
    await fetchTodayRecords()
    return res.data
  }

  async function fetchTodayRecords() {
    const res = await request.get('/records', { params: { date: new Date().toISOString().slice(0, 10) } })
    todayRecords.value = res.data?.records || []
    return res.data
  }

  async function deleteRecord(id) {
    await request.delete(`/records/${id}`)
    await fetchTodayRecords()
  }

  return { currentRecord, todayRecords, startRecord, stopRecord, fetchTodayRecords, deleteRecord }
})
