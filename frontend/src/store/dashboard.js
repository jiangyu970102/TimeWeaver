import { defineStore } from 'pinia'
import { ref } from 'vue'
import request from '@/utils/request'

export const useDashboardStore = defineStore('dashboard', () => {
  const overview = ref(null)

  async function fetchOverview() {
    const res = await request.get('/dashboard/overview')
    overview.value = res.data
    return res.data
  }

  return { overview, fetchOverview }
})
