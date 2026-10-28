import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import HistoryClient from './HistoryClient';
import { api } from '@/lib/api';
import type { Task } from '@/lib/types';

vi.mock('@/lib/api', () => ({
  api: {
    listTasks: vi.fn(),
    listTemplates: vi.fn(),
    getTask: vi.fn(),
  },
  assetUrl: (p: string | null | undefined) => (p ? p : ''),
}));

const mockedApi = vi.mocked(api);

function task(overrides: Partial<Task>): Task {
  return {
    id: 't1',
    userId: 'u1',
    templateId: 'demo-postcard',
    status: 'PROCESSING',
    currentStage: 'GENERATING',
    failStage: null,
    failReason: null,
    resultUrl: null,
    assetIds: '[]',
    createdAt: '2026-10-28T10:00:00',
    updatedAt: '2026-10-28T10:00:05',
    ...overrides,
  };
}

describe('HistoryClient 查询异常与任务失败区分', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockedApi.listTemplates.mockResolvedValue([]);
  });

  it('查询接口不可用时保留任务状态，而不是标记为失败', async () => {
    mockedApi.listTasks.mockResolvedValue([
      task({ id: 'a', status: 'PROCESSING', currentStage: 'GENERATING' }),
    ]);
    mockedApi.getTask.mockRejectedValue(new Error('network down'));

    render(<HistoryClient />);

    // 初次加载后恢复任务，并显示处理中
    expect(await screen.findByText('处理中')).toBeInTheDocument();

    // 等待一次轮询失败，出现查询异常提示与重试入口
    await waitFor(
      () => expect(screen.getByText(/查询暂时不可用/)).toBeInTheDocument(),
      { timeout: 5000 },
    );
    expect(screen.getByRole('button', { name: '重试查询' })).toBeInTheDocument();

    // 任务仍保持“处理中”，未被错误改写为失败
    expect(screen.getByText('处理中')).toBeInTheDocument();
    expect(screen.queryByText('失败')).not.toBeInTheDocument();
    expect(screen.queryByText(/失败原因/)).not.toBeInTheDocument();
  });
});