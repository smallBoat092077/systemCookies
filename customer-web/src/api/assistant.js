import request from './request'

// AI 模式下后端要多轮调用大模型（实测 20s+），单独放宽超时，不能用全局的 15s
const AI_TIMEOUT = 60000

/** 与小助理对话一轮 */
export const sendChat = (data) => request.post('/assistant/chat', data, { timeout: AI_TIMEOUT })

/** 清空会话上下文 */
export const clearChatSession = (sessionId) =>
  request.delete('/assistant/session', { params: { sessionId } })
