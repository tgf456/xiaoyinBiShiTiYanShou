import { describe, it, expect, vi } from 'vitest';
import { render, screen, within } from '@testing-library/react';
import TaskCard from './TaskCard';
import type { Task } from '@/lib/types';

vi.mock('@/lib/api', () => ({
  assetUrl: (p: string | null | undefined) => (p ? p : ''),
}));

function task(overrides: Partial<Task>): Task {
  return {
    id: 't1',
    userId: 'u1',
    templateId: 'demo-postcard',
    status: 'PROCESSING',
    currentStage: null,
    failStage: null,
    failReason: null,
    resultUrl: null,
    assetIds: '[]',
    createdAt: '2026-10-28T10:00:00',
    updatedAt: '2026-10-28T10:00:05',
    ...overrides,
  };
}

describe('TaskCard 多任务状态互不串扰', () => {
  it('每个任务展示各自的阶段 / 失败原因 / 结果，互不干扰', () => {
    render(
      <>
        <TaskCard
          task={task({ id: 'a', status: 'PROCESSING', currentStage: 'GENERATING' })}
          title="任务A"
        />
        <TaskCard
          task={task({ id: 'b', status: 'FAILED', failStage: 'GENERATING', failReason: '模拟生成失败' })}
          title="任务B"
        />
        <TaskCard
          task={task({ id: 'c', status: 'SUCCESS', resultUrl: '/results/x.jpg' })}
          title="任务C"
        />
      </>,
    );

    const cardA = screen.getByTestId('task-a');
    expect(within(cardA).getByText('处理中')).toBeInTheDocument();
    expect(within(cardA).getByText('当前阶段：模拟生成')).toBeInTheDocument();
    expect(within(cardA).queryByText(/失败原因/)).not.toBeInTheDocument();

    const cardB = screen.getByTestId('task-b');
    expect(within(cardB).getByText('失败')).toBeInTheDocument();
    expect(within(cardB).getByText(/模拟生成失败/)).toBeInTheDocument();
    expect(within(cardB).queryByText('处理中')).not.toBeInTheDocument();

    const cardC = screen.getByTestId('task-c');
    expect(within(cardC).getByText('成功')).toBeInTheDocument();
    expect(within(cardC).getByAltText('制作结果')).toBeInTheDocument();
    expect(within(cardC).queryByText(/失败原因/)).not.toBeInTheDocument();
  });
});