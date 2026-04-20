import request from './request'

export interface Activity {
  id: number
  name: string
  description: string
  startTime: string
  endTime: string
  status: number
  perLimit: number
}

export const activityApi = {
  getActivity(id: number) {
    return request.get<Activity>(`/api/activity/${id}`)
  },
  startActivity(id: number) {
    return request.post<Boolean>(`/api/activity/${id}/start`)
  },
  endActivity(id: number) {
    return request.post<Boolean>(`/api/activity/${id}/end`)
  }
}