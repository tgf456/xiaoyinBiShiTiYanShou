export type TaskStatus = 'WAITING' | 'PROCESSING' | 'SUCCESS' | 'FAILED';
export type TaskStage = 'VALIDATING' | 'GENERATING' | 'FINALIZING';

export interface Template {
  id: string;
  title: string;
  category: string;
  coverUrl: string;
  description: string;
  requiredImageCount: number;
  acceptedMimeTypes: string[];
  maxFileSizeBytes: number;
}

export interface Asset {
  id: string;
  url: string;
  originalName: string;
  sizeBytes: number;
}

export interface Task {
  id: string;
  userId: string;
  templateId: string;
  status: TaskStatus;
  currentStage: TaskStage | null;
  failStage: TaskStage | null;
  failReason: string | null;
  resultUrl: string | null;
  assetIds: string | null;
  createdAt: string;
  updatedAt: string;
}

export function isUnfinished(task: Task): boolean {
  return task.status === 'WAITING' || task.status === 'PROCESSING';
}

export const STATUS_LABEL: Record<TaskStatus, string> = {
  WAITING: '等待处理',
  PROCESSING: '处理中',
  SUCCESS: '成功',
  FAILED: '失败',
};

export const STAGE_LABEL: Record<TaskStage, string> = {
  VALIDATING: '素材检查',
  GENERATING: '模拟生成',
  FINALIZING: '结果整理',
};