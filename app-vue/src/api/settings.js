import request from './user'

export function getTheme() {
  return request.get('/settings/theme')
}

export function saveTheme(theme) {
  return request.put('/settings/theme', { theme })
}
