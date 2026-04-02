import axios from 'axios'

export function getTheme() {
  return axios.get('/api/settings/theme')
}

export function saveTheme(theme) {
  return axios.put('/api/settings/theme', { theme })
}
