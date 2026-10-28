const STORAGE_KEY = 'material-maker-user-id';

function generateId(): string {
  if (typeof crypto !== 'undefined' && 'randomUUID' in crypto) {
    return crypto.randomUUID();
  }
  return 'uid-' + Math.random().toString(36).slice(2) + Date.now().toString(36);
}

/** 轻量身份：首次生成 id 存 localStorage，后续所有请求经 X-User-Id 请求头传递。 */
export function getUserId(): string {
  if (typeof window === 'undefined') {
    return '';
  }
  let id = window.localStorage.getItem(STORAGE_KEY);
  if (!id) {
    id = generateId();
    window.localStorage.setItem(STORAGE_KEY, id);
  }
  return id;
}