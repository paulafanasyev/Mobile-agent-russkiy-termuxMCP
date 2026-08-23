import {
  getRootStatus,
  requestRootAuthorization,
  type RootStatus,
} from '../../../modules/root-access'

export type { RootStatus }

export const rootAccess = {
  getStatus: getRootStatus,
  requestAuthorization: requestRootAuthorization,
}
