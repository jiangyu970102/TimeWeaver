import { onMounted, onUnmounted } from 'vue'

/**
 * 注册全局键盘快捷键
 * @param {Object} shortcuts - { key: handler } 映射，key 为小写字母
 * @param {Object} options
 * @param {boolean} options.inputFilter - 是否在输入框聚焦时禁用（默认 true）
 */
export function useShortcuts(shortcuts, options = {}) {
  const { inputFilter = true } = options

  function isInputFocused() {
    const tag = document.activeElement?.tagName?.toLowerCase()
    return tag === 'input' || tag === 'textarea' || tag === 'select'
      || document.activeElement?.getAttribute('contenteditable') === 'true'
  }

  function handler(e) {
    // 忽略组合键
    if (e.ctrlKey || e.metaKey || e.altKey) return

    const key = e.key.toLowerCase()
    if (shortcuts[key]) {
      if (inputFilter && isInputFocused()) return
      e.preventDefault()
      shortcuts[key](e)
    }
  }

  onMounted(() => {
    window.addEventListener('keydown', handler)
  })

  onUnmounted(() => {
    window.removeEventListener('keydown', handler)
  })
}
