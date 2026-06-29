import request from '@/utils/request'

// ── 仓库配置 ──

export function getRepos() {
  return request.get('/git/repos')
}

export function addRepo(repoPath) {
  return request.post('/git/repos', { repoPath })
}

export function removeRepo(id) {
  return request.delete(`/git/repos/${id}`)
}

export function updateAutoImport(id, autoImport) {
  return request.put(`/git/repos/${id}/auto-import`, { autoImport })
}

// ── 提交记录 ──

export function getGitCommits(params) {
  return request.get('/git/commits', { params })
}

export function scanCommits(params) {
  return request.post('/git/scan', null, { params })
}

export function importCommits(params) {
  return request.post('/git/import', null, { params })
}
