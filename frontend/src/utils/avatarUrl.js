import { alumniApi } from '../api/alumni'

const urlCache = new Map()

/**
 * 将 user.avatar（MinIO objectName 或已是 http(s) URL）转为可直接用于 img 的地址。
 */
export async function resolveAvatarUrl(avatar) {
  if (avatar == null) return ''
  const s = String(avatar).trim()
  if (!s) return ''
  if (/^https?:\/\//i.test(s)) return s
  if (urlCache.has(s)) return urlCache.get(s)
  try {
    const res = await alumniApi.getAvatarUrl(s)
    const url = typeof res.data === 'string' ? res.data : res.data?.url || ''
    urlCache.set(s, url)
    return url || ''
  } catch {
    return ''
  }
}

export function clearAvatarUrlCache(objectName) {
  if (objectName) urlCache.delete(String(objectName))
  else urlCache.clear()
}
