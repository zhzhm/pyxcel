import axios from 'axios'
import hash from 'object-hash'
const instance = axios.create({
  baseURL: '/',
  timeout: 5000
})
instance.interceptors.request.use(config => {
  config.headers.sid = getSid()
  return config
})
export const listFiles = dir => {
  return instance.get('/rest/file/list' + (dir ? '/' + dir : ''))
}

export const deleteFile = filePath => {
  return instance.delete('/rest/file/delete/' + filePath)
}

export const executePython = (wsSessionId, pythonCode) => {
  return instance.post('/rest/python/execute', { pythonCode }, {
    headers: {
      'Content-Type': 'application/json',
      wsId: wsSessionId
    }
  })
}

export const getSid = () => {
  return location.href.substring(location.href.lastIndexOf('/') + 1)
}

export const sleep = async millisecond => new Promise(resolve => setTimeout(() => resolve(), millisecond))

export const shortRadomStr = () => {
  return hash(Math.random() * Number.MAX_SAFE_INTEGER, { algorithm: 'md5', encoding: 'base64' })
    .substring(0, 6)
    .replace(/\+|=/g, 'z')
}
