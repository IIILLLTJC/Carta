import request from '@/utils/request'

export function uploadImage(file, scene = 'common') {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/api/common/upload/image', formData, {
    params: { scene }
  })
}
