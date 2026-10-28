import { getUserId } from './user';
import type { Asset, Task, Template } from './types';

const BASE = process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:8080';

export class ApiError extends Error {
  status: number;

  constructor(message: string, status: number) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
  }
}

function userHeaders(): Record<string, string> {
  return { 'X-User-Id': getUserId() };
}

async function request<T>(path: string, init: RequestInit = {}): Promise<T> {
  let res: Response;
  try {
    res = await fetch(`${BASE}${path}`, init);
  } catch {
    throw new ApiError('无法连接服务器，请稍后重试', 0);
  }
  if (!res.ok) {
    let message = `请求失败（${res.status}）`;
    try {
      const body = await res.json();
      if (body && typeof body.message === 'string') {
        message = body.message;
      }
    } catch {
      // 忽略非 JSON 错误体
    }
    throw new ApiError(message, res.status);
  }
  return (await res.json()) as T;
}

/** 将后端相对路径（/uploads/...、/results/...、/demo/...）拼成可访问的绝对地址。 */
export function assetUrl(path: string | null | undefined): string | null {
  if (!path) {
    return null;
  }
  if (/^https?:\/\//.test(path)) {
    return path;
  }
  return `${BASE}${path}`;
}

export const api = {
  listTemplates(category?: string): Promise<Template[]> {
    const q = category ? `?category=${encodeURIComponent(category)}` : '';
    return request<Template[]>(`/api/templates${q}`, { headers: userHeaders() });
  },

  getTemplate(id: string): Promise<Template> {
    return request<Template>(`/api/templates/${encodeURIComponent(id)}`, { headers: userHeaders() });
  },

  uploadAsset(templateId: string, file: File): Promise<Asset> {
    const formData = new FormData();
    formData.append('file', file);
    // 注意：FormData 请求体不能手动设置 Content-Type，由浏览器补全 multipart 边界
    return request<Asset>(`/api/assets?templateId=${encodeURIComponent(templateId)}`, {
      method: 'POST',
      headers: userHeaders(),
      body: formData,
    });
  },

  submitTask(templateId: string, assetIds: string[]): Promise<Task> {
    return request<Task>('/api/tasks', {
      method: 'POST',
      headers: { ...userHeaders(), 'Content-Type': 'application/json' },
      body: JSON.stringify({ templateId, assetIds }),
    });
  },

  getTask(id: string, signal?: AbortSignal): Promise<Task> {
    return request<Task>(`/api/tasks/${encodeURIComponent(id)}`, { headers: userHeaders(), signal });
  },

  listTasks(): Promise<Task[]> {
    return request<Task[]>('/api/tasks', { headers: userHeaders() });
  },
};