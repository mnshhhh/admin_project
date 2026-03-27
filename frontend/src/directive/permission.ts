import type { Directive } from 'vue'
import { useUserStore } from '@/store/user'

function checkPermission(el: HTMLElement, binding: any) {
  const { value } = binding
  if (!value || !Array.isArray(value) || value.length === 0) return

  const userStore = useUserStore()
  const hasPermission = value.some((perm: string) => userStore.hasPermission(perm))
  el.style.display = hasPermission ? '' : 'none'
}

export const permissionDirective: Directive = {
  mounted(el: HTMLElement, binding: any) {
    checkPermission(el, binding)
  },
  updated(el: HTMLElement, binding: any) {
    checkPermission(el, binding)
  }
}
