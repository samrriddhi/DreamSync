import axios from 'axios'

export const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080',
  headers: { 'Content-Type': 'application/json' },
})

api.interceptors.request.use(config => {
  const token = localStorage.getItem('dreamsync_access_token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

export type ApiProject = { id:number; name:string; description?:string; status?:string }
export type ApiTask = {
  id:number; title:string; description?:string; status:string; priority:string;
  dueDate?:string; project?:{id:number;name?:string}; createdBy?:{id?:number;name?:string;email?:string}; assignee?:{id?:number;name?:string;email?:string}
}
export type TaskPayload = { title:string; description:string; status:string; priority:string; dueDate?:string; project:{id:number}; assignee?:{id:number}|null }
export type ProjectMember = { userId:number; name:string; email:string; role:string }
export type DashboardStats = { projectName:string; totalTasks:number; completedTasks:number; inProgressTasks:number; todoTasks:number; highPriorityTasks:number; completionPercentage:number }

export async function register(name:string,email:string,password:string) { return (await api.post('/auth/register',{name,email,password})).data }

export async function login(email:string,password:string) {
  const {data}=await api.post('/auth/login',{email,password})
  localStorage.setItem('dreamsync_access_token',data.accessToken)
  localStorage.setItem('dreamsync_refresh_token',data.refreshToken)
}
export function logout() {
  localStorage.removeItem('dreamsync_access_token')
  localStorage.removeItem('dreamsync_refresh_token')
}
export async function getProjects() { return (await api.get<ApiProject[]>('/projects')).data }
export async function getTasks() { return (await api.get<ApiTask[]>('/tasks')).data }
export async function createTask(payload:TaskPayload) { return (await api.post<ApiTask>('/tasks',payload)).data }
export async function updateTask(id:number,payload:TaskPayload) { return (await api.put<ApiTask>(`/tasks/${id}`,payload)).data }

export async function getProjectDashboard(projectId:number) { return (await api.get<DashboardStats>(`/dashboard/project/${projectId}`)).data }

export async function deleteTask(id:number) { return api.delete(`/tasks/${id}`) }

export async function getProjectMembers(projectId:number) { return (await api.get<ProjectMember[]>(`/projects/${projectId}/members`)).data }

export type Comment = { id:number; message:string; userName:string; createdAt:string }
export type ActivityItem = { userName:string; action:string; description:string; createdAt:string }
export type NotificationItem = { id:number; message:string; read:boolean; createdAt:string }
export async function getTaskComments(taskId:number) { return (await api.get<Comment[]>(`/tasks/${taskId}/comments`)).data }
export async function addTaskComment(taskId:number,message:string) { return (await api.post<Comment>(`/tasks/${taskId}/comments`,{message})).data }
export async function deleteTaskComment(commentId:number) { return api.delete(`/tasks/comments/${commentId}`) }
export async function getMyActivity() { return (await api.get<ActivityItem[]>('/activity/me')).data }
export async function getNotifications() { return (await api.get<NotificationItem[]>('/notifications')).data }
export async function markNotificationRead(id:number) { return api.put(`/notifications/${id}/read`) }
export async function inviteProjectMember(projectId:number,email:string,role:'DEVELOPER'|'TESTER') { return api.post(`/projects/${projectId}/members`,{email,role}) }

export type CurrentUser = { id:number; name:string; email:string; role:string }
export async function getCurrentUser() { return (await api.get<CurrentUser>('/auth/me')).data }

export async function updateProjectMemberRole(projectId:number,userId:number,role:'DEVELOPER'|'TESTER') { return api.put(`/projects/${projectId}/members/${userId}`,{role}) }
export async function removeProjectMember(projectId:number,userId:number) { return api.delete(`/projects/${projectId}/members/${userId}`) }
