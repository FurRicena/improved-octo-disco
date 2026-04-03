// api/upload.ts
import request from "@/utils/request.ts";

export function uploadFile(file: File) {
    const formData = new FormData()
    formData.append('file', file)

    return request<string>({
        url: '/upload',
        method: 'post',
        data: formData
    })
}