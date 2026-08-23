import { Platform } from 'react-native'
import { requireNativeModule } from 'expo'

export type RootStatus = {
  supported: boolean
  available: boolean
  authorized: boolean
  uid: number | null
  source: 'su' | 'none'
  message: string
}

type NativeRootAccess = {
  status(): Promise<RootStatus>
  requestAuthorization(): Promise<RootStatus>
}

const native = Platform.OS === 'android'
  ? requireNativeModule<NativeRootAccess>('RootAccess')
  : null

export async function getRootStatus(): Promise<RootStatus> {
  if (native) return native.status()
  return {
    supported: false,
    available: false,
    authorized: false,
    uid: null,
    source: 'none',
    message: 'Расширенные права доступны только на Android.',
  }
}

/**
 * Проверяет уже существующий root-доступ через установленный su/менеджер root.
 * Функция не пытается получать root или обходить ограничения Android.
 */
export async function requestRootAuthorization(): Promise<RootStatus> {
  if (native) return native.requestAuthorization()
  return getRootStatus()
}
