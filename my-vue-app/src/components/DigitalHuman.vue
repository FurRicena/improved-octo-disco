<template>
  <div class="digital-human" :class="state" @click="emitRepeat">
    <Vue3Lottie
        ref="lottieRef"
        :animationData="currentAnimation"
        :loop="true"
        :autoplay="true"
        :height="100"
        :width="100"
    />
    <transition name="bubble">
      <div v-if="state === 'speaking' && currentMessage" class="speech-bubble">
        {{ currentMessage }}
      </div>
    </transition>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import {Vue3Lottie} from 'vue3-lottie'
// 导入动画 JSON 文件（如果 TypeScript 报错，需要添加声明，见下方说明）
import idleAnimation from '@/assets/animations/chef-idle.json'
import listeningAnimation from '@/assets/animations/chef-listening.json'
import speakingAnimation from '@/assets/animations/chef-speaking.json'

type HumanState = 'idle' | 'listening' | 'speaking'

const state = ref<HumanState>('idle')
const currentMessage = ref('')
const lastMessage = ref('')

const currentAnimation = computed(() => {
  switch (state.value) {
    case 'listening': return listeningAnimation
    case 'speaking':  return speakingAnimation
    default:          return idleAnimation
  }
})

// 暴露方法供父组件调用
const startListening = () => {
  state.value = 'listening'
  currentMessage.value = ''
}

const startSpeaking = (message: string) => {
  lastMessage.value = message
  currentMessage.value = message
  state.value = 'speaking'
}

const stopSpeaking = () => {
  if (state.value === 'speaking') {
    state.value = 'idle'
    currentMessage.value = ''
  }
}

const emit = defineEmits<{
  (e: 'repeat', message: string): void
}>()
const emitRepeat = () => {
  if (lastMessage.value) {
    emit('repeat', lastMessage.value)
  }
}

defineExpose({
  startListening,
  startSpeaking,
  stopSpeaking
})
</script>

<style scoped>
.digital-human {
  position: relative;
  display: inline-flex;
  cursor: pointer;
  justify-content: center;
  align-items: center;
}
.speech-bubble {
  position: absolute;
  left: 100%;           /* 紧贴数字人右侧 */
  top: 50%;             /* 垂直居中 */
  transform: translateY(-50%);  /* 垂直方向偏移自身高度的一半，实现居中 */
  margin-left: 12px;    /* 与数字人的间距 */
  background: white;
  border-radius: 20px;
  padding: 8px 16px;
  font-size: 14px;
  white-space: normal;
  word-break: break-word;
  box-shadow: 0 2px 8px rgba(0,0,0,0.15);
  border: 1px solid #ffd966;
  color: #333;
  max-width: 220px;
  min-width: 100px;     /* 避免太窄 */
  z-index: 100;
}

/* 调整小箭头：指向左侧的数字人 */
.speech-bubble::after {
  content: '';
  position: absolute;
  left: -8px;           /* 箭头在气泡左侧外部 */
  top: 50%;
  transform: translateY(-50%);
  border-width: 8px 8px 8px 0;
  border-style: solid;
  border-color: transparent white transparent transparent; /* 箭头指向左 */
  /* 之前是底部箭头，现在改到左侧 */
}
.bubble-enter-active, .bubble-leave-active {
  transition: all 0.2s ease;
}
.bubble-enter-from, .bubble-leave-to {
  opacity: 0;
  transform: translateX(-50%) translateY(10px);
}
</style>