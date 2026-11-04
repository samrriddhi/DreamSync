import axios, { InternalAxiosRequestConfig } from 'axios'

export const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080',
  headers: { 'Content-Type': 'application/json' },
})

api.interceptors.request.use(config => {
  const token = localStorage.getItem('dreamsync_access_token')
  const publicAuthRoutes = ['/auth/register', '/auth/login', '/auth/refresh', '/auth/logout']
  if (token && !publicAuthRoutes.includes(config.url ?? '')) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

type RetriableRequest = InternalAxiosRequestConfig & { _retry?: boolean }
let refreshRequest: Promise<string> | null = null

api.interceptors.response.use(response => response, async error => {
  const original = error.config as RetriableRequest | undefined
  const route = original?.url ?? ''
  const isPublicAuthRoute = ['/auth/register', '/auth/login', '/auth/refresh', '/auth/logout'].includes(route)

  if (error.response?.status !== 401 || !original || original._retry || isPublicAuthRoute) {
    return Promise.reject(error)
  }

  const refreshToken = localStorage.getItem('dreamsync_refresh_token')
  if (!refreshToken) {
    localStorage.removeItem('dreamsync_access_token')
    window.dispatchEvent(new Event('dreamsync:session-expired'))
    return Promise.reject(error)
  }

  original._retry = true
  if (!refreshRequest) {
    refreshRequest = axios.post(`${api.defaults.baseURL}/auth/refresh`, { refreshToken })
      .then(response => {
        const accessToken = response.data.accessToken as string
        const nextRefreshToken = response.data.refreshToken as string
        localStorage.setItem('dreamsync_access_token', accessToken)
        localStorage.setItem('dreamsync_refresh_token', nextRefreshToken)
        return accessToken
      })
      .catch(refreshError => {
        localStorage.removeItem('dreamsync_access_token')
        localStorage.removeItem('dreamsync_refresh_token')
        window.dispatchEvent(new Event('dreamsync:session-expired'))
        throw refreshError
      })
      .finally(() => { refreshRequest = null })
  }

  try {
    const accessToken = await refreshRequest
    original.headers.Authorization = `Bearer ${accessToken}`
    return api(original)
  } catch {
    return Promise.reject(error)
  }
})

export type ApiProject = { id:number; name:string; description?:string; status?:string }
export type ApiOrganization = { id:number; name:string; description?:string }
export type ApiTask = {
  id:number; title:string; description?:string; status:string; priority:string; estimatedHours?:number;
  dueDate?:string; project?:{id:number;name?:string}; createdBy?:{id?:number;name?:string;email?:string}; assignee?:{id?:number;name?:string;email?:string}
}
export type TaskPayload = { title:string; description:string; status:string; priority:string; estimatedHours?:number; dueDate?:string|null; project:{id:number}; assignee?:{id:number}|null }
export type TaskDependency = { id:number; taskId:number; taskTitle:string; dependsOnTaskId:number; dependsOnTaskTitle:string }
export async function getProjectDependencies(projectId:number) { return (await api.get<TaskDependency[]>(`/dependencies/project/${projectId}`)).data }
export async function createTaskDependency(taskId:number,dependsOnTaskId:number) { return (await api.post<TaskDependency>('/dependencies',{task:{id:taskId},dependsOnTask:{id:dependsOnTaskId}})).data }
export async function deleteTaskDependency(dependencyId:number) { return api.delete(`/dependencies/${dependencyId}`) }
export type ProjectMember = { userId:number; name:string; email:string; role:string }
export type DashboardStats = { projectName:string; totalTasks:number; completedTasks:number; inProgressTasks:number; todoTasks:number; highPriorityTasks:number; completionPercentage:number }
export type TimelineTask = { id:number; title:string; dueDate?:string; status:string; estimatedHours?:number }
export type ProjectTimeline = { projectName:string; tasks:TimelineTask[] }
export type CriticalPath = { projectName:string; criticalPath:string[]; totalEstimatedHours:number }
export type RiskAnalysis = { projectName:string; riskLevel:string; overdueTasks:number; blockedTasks:number; highPriorityTasks:number; completionPercentage:number }
export type BlockedTask = { id:number; title:string; blockedBy:string }
export type ImpactTask = { id:number; title:string; status:string; priority:string }
export type ImpactAnalysis = { delayedTask:string; affectedTaskCount:number; affectedTasks:ImpactTask[] }
export async function getTaskImpact(taskId:number) { return (await api.get<ImpactAnalysis>(`/analysis/impact/${taskId}`)).data }

export async function getProjectTimeline(projectId:number) { return (await api.get<ProjectTimeline>(`/timeline/${projectId}`)).data }
export async function getCriticalPath(projectId:number) { return (await api.get<CriticalPath>(`/analysis/critical-path/${projectId}`)).data }
export async function getRiskAnalysis(projectId:number) { return (await api.get<RiskAnalysis>(`/risk-analysis/${projectId}`)).data }
export async function getBlockedTasks(projectId:number) { return (await api.get<BlockedTask[]>(`/blocked-tasks/${projectId}`)).data }


export async function register(name:string,email:string,password:string) { return (await api.post('/auth/register',{name,email,password})).data }

export async function login(email:string,password:string) {
  const {data}=await api.post('/auth/login',{email,password})
  localStorage.setItem('dreamsync_access_token',data.accessToken)
  localStorage.setItem('dreamsync_refresh_token',data.refreshToken)
}
export function logout() {
  const refreshToken = localStorage.getItem('dreamsync_refresh_token')
  localStorage.removeItem('dreamsync_access_token')
  localStorage.removeItem('dreamsync_refresh_token')
  if (refreshToken) {
    void api.post('/auth/logout', { refreshToken }).catch(() => undefined)
  }
}
export async function getOrganizations() { return (await api.get<ApiOrganization[]>('/organizations')).data }
export async function createOrganization(name:string,description:string) { return (await api.post<ApiOrganization>('/organizations',{name,description})).data }
export async function createProject(name:string,description:string,organizationId:number) { return (await api.post<ApiProject>('/projects',{name,description,organization:{id:organizationId}})).data }
export async function updateProject(id:number,name:string,description:string,status:string) { return (await api.put<ApiProject>(`/projects/${id}`,{name,description,status})).data }
export async function deleteProject(id:number) { return api.delete(`/projects/${id}`) }
export async function getProjects() { return (await api.get<ApiProject[]>('/projects')).data }
export async function getTasks() { return (await api.get<ApiTask[]>('/tasks')).data }
export async function createTask(payload:TaskPayload) { return (await api.post<ApiTask>('/tasks',payload)).data }
export async function updateTask(id:number,payload:TaskPayload) { return (await api.put<ApiTask>(`/tasks/${id}`,payload)).data }

export async function getProjectDashboard(projectId:number) { return (await api.get<DashboardStats>(`/dashboard/project/${projectId}`)).data }

export async function deleteTask(id:number) { return api.delete(`/tasks/${id}`) }

export async function getProjectMembers(projectId:number) { return (await api.get<ProjectMember[]>(`/projects/${projectId}/members`)).data }

export type Attachment = { id:number; fileName:string; fileType?:string; uploadedAt:string; canDelete:boolean }
export async function getTaskAttachments(taskId:number) { return (await api.get<Attachment[]>(`/tasks/${taskId}/attachments`)).data }
export async function uploadTaskAttachment(taskId:number,file:File) {
  const form = new FormData()
  form.append('file',file)
  return (await api.post<Attachment>(`/tasks/${taskId}/attachments`,form,{headers:{'Content-Type':'multipart/form-data'}})).data
}
export async function downloadTaskAttachment(attachmentId:number) {
  return (await api.get<Blob>(`/attachments/${attachmentId}/download`,{responseType:'blob'})).data
}
export async function deleteTaskAttachment(attachmentId:number) { return api.delete(`/attachments/${attachmentId}`) }

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

export type ChatMessage = { id:number; message:string; senderName:string; sentAt:string }
export async function getProjectChat(projectId:number) { return (await api.get<ChatMessage[]>(`/projects/${projectId}/chat`)).data }
export async function sendProjectChatMessage(projectId:number,message:string) { return (await api.post<ChatMessage>(`/projects/${projectId}/chat`,{message})).data }

export function connectProjectChat(projectId:number,onMessage:(message:ChatMessage)=>void) {
  let socket:WebSocket|null = null
  let closed = false
  let reconnectTimer:number|undefined
  const connect=()=>{
    const token=localStorage.getItem('dreamsync_access_token')
    if(closed||!token)return
    const baseUrl=api.defaults.baseURL||window.location.origin
    socket=new WebSocket(baseUrl.replace(/^http/,'ws').replace(/\/$/,'')+'/ws-native')
    socket.onopen=()=>socket?.send(`CONNECT\naccept-version:1.2\nhost:localhost\nAuthorization:Bearer ${token}\n\n\0`)
    socket.onmessage=event=>{
      for(const frame of String(event.data).split('\0').filter(value=>value.trim())){
        const separator=frame.indexOf('\n\n')
        if(separator<0)continue
        const headers=frame.slice(0,separator)
        if(headers.startsWith('CONNECTED')){
          socket?.send(`SUBSCRIBE\nid:project-chat-${projectId}\ndestination:/topic/projects/${projectId}/chat\nack:auto\n\n\0`)
        }else if(headers.startsWith('MESSAGE')){
          try{const item=JSON.parse(frame.slice(separator+2)) as ChatMessage;if(item.id)onMessage(item)}catch{/* Ignore malformed broker messages. */}
        }else if(headers.startsWith('ERROR')){closed=true;socket?.close()}
      }
    }
    socket.onclose=()=>{if(!closed)reconnectTimer=window.setTimeout(connect,3000)}
    socket.onerror=()=>socket?.close()
  }
  connect()
  return ()=>{closed=true;if(reconnectTimer!==undefined)window.clearTimeout(reconnectTimer);socket?.close()}
}

export type RealtimeNotification = { sender:string; message:string }

export function connectRealtimeNotifications(onNotification:(notification:RealtimeNotification)=>void) {
  let socket:WebSocket|null = null
  let reconnectTimer:number|undefined
  let closed = false

  const connect = () => {
    const token = localStorage.getItem('dreamsync_access_token')
    if (closed || !token) return

    const baseUrl = api.defaults.baseURL || window.location.origin
    const websocketUrl = baseUrl.replace(/^http/, 'ws').replace(/\/$/, '') + '/ws-native'
    socket = new WebSocket(websocketUrl)

    socket.onopen = () => {
      socket?.send(`CONNECT\naccept-version:1.2\nhost:localhost\nAuthorization:Bearer ${token}\n\n\0`)
    }

    socket.onmessage = event => {
      const frames = String(event.data).split('\0').filter(frame => frame.trim().length > 0)
      for (const frame of frames) {
        const separator = frame.indexOf('\n\n')
        if (separator < 0) continue
        const headers = frame.slice(0, separator)
        const body = frame.slice(separator + 2)

        if (headers.startsWith('CONNECTED')) {
          socket?.send('SUBSCRIBE\nid:dreamsync-notifications\ndestination:/user/queue/notifications\nack:auto\n\n\0')
        } else if (headers.startsWith('MESSAGE')) {
          try {
            const notification = JSON.parse(body) as RealtimeNotification
            if (notification.message) onNotification(notification)
          } catch {
            // Ignore malformed or unrelated broker messages.
          }
        } else if (headers.startsWith('ERROR')) {
          closed = true
          socket?.close()
        }
      }
    }

    socket.onclose = () => {
      if (!closed) reconnectTimer = window.setTimeout(connect, 3000)
    }
    socket.onerror = () => socket?.close()
  }

  connect()
  return () => {
    closed = true
    if (reconnectTimer !== undefined) window.clearTimeout(reconnectTimer)
    socket?.close()
  }
}
