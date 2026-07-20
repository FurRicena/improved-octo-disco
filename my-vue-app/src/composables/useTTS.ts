import { ref } from 'vue'

export function useTTS() {
const isSpeaking = ref(false)

/**
* 播放语音
* @param text 要朗读的文本
* @param onStart 开始播放时的回调
* @param onEnd 播放结束时的回调
* @returns Promise，在播放完成或出错时 resolve/reject
*/
const speak =
    (text: string, onStart?: () => void, onEnd?: () => void): Promise<void> => {
        return new Promise((resolve, reject) => {
            if (!window.speechSynthesis) {
                console.warn('当前浏览器不支持语音合成')
                reject(new Error('TTS not supported'))
                return
            }
            // 取消任何正在播放的语音，避免重叠
            window.speechSynthesis.cancel()
            const utterance = new SpeechSynthesisUtterance(text)
            utterance.lang = 'zh-CN'
            utterance.rate = 1.0
            utterance.pitch = 1.0
            utterance.onstart = () => {
                isSpeaking.value = true
                onStart?.()
            }
            utterance.onend = () => {
                isSpeaking.value = false
                onEnd?.()
                resolve()
            }
            utterance.onerror = (err) => {
                isSpeaking.value = false
                console.error('语音播放出错', err)
                reject(err)
            }
            window.speechSynthesis.speak(utterance)
        })
    }

/**
* 停止当前播放
*/
const stop = () => {
if (window.speechSynthesis) {
window.speechSynthesis.cancel()
isSpeaking.value = false
}
}

return { speak, stop, isSpeaking }
}