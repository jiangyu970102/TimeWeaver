import dayjs from 'dayjs'

export function formatDateTime(date) {
  return date ? dayjs(date).format('YYYY-MM-DD HH:mm:ss') : ''
}

export function formatDate(date) {
  return date ? dayjs(date).format('YYYY-MM-DD') : ''
}

export function formatTime(date) {
  return date ? dayjs(date).format('HH:mm') : ''
}

export function formatDuration(minutes) {
  if (!minutes && minutes !== 0) return ''
  const h = Math.floor(minutes / 60)
  const m = minutes % 60
  if (h > 0) {
    return m > 0 ? `${h}h${m}min` : `${h}h`
  }
  return `${m}min`
}

export function getWeekNumber(date) {
  return dayjs(date).isoWeek()
}

export function getToday() {
  return dayjs().format('YYYY-MM-DD')
}

export function getWeekRange(date) {
  const d = dayjs(date)
  const start = d.startOf('week').format('YYYY-MM-DD')
  const end = d.endOf('week').format('YYYY-MM-DD')
  return { start, end }
}
