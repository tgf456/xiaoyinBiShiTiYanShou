'use client';

import { useCallback, useEffect, useRef, useState } from 'react';
import { api } from '@/lib/api';
import { isUnfinished, Task } from '@/lib/types';

const DEFAULT_INTERVAL_MS = 2000;

interface Options {
  intervalMs?: number;
}

function mergeById(prev: Task[], updates: Task[]): Task[] {
  const map = new Map<string, Task>();
  for (const t of prev) {
    map.set(t.id, t);
  }
  for (const u of updates) {
    map.set(u.id, u);
  }
  return Array.from(map.values());
}

/**
 * 追踪个人任务：仅对未完成任务轮询；当没有待处理任务时自动停止；
 * 组件卸载时清理 interval 并中止进行中的查询请求。
 * 查询接口异常与“任务执行失败”严格区分：异常仅设置 queryError，不修改任务状态。
 */
export function useTaskTracker(initialTasks: Task[] | (() => Task[]) = [], options: Options = {}) {
  const intervalMs = options.intervalMs ?? DEFAULT_INTERVAL_MS;

  const [tasks, setTasks] = useState<Task[]>(initialTasks);
  const [queryError, setQueryError] = useState<string | null>(null);
  const [initialLoading, setInitialLoading] = useState(false);

  const unfinishedIds = tasks.filter(isUnfinished).map((t) => t.id);
  const hasUnfinished = unfinishedIds.length > 0;
  const unfinishedRef = useRef<string[]>(unfinishedIds);
  unfinishedRef.current = unfinishedIds;

  const pollOnce = useCallback(async (ids: string[], signal?: AbortSignal) => {
    try {
      const results = await Promise.all(ids.map((id) => api.getTask(id, signal)));
      setTasks((prev) => mergeById(prev, results));
      setQueryError(null);
    } catch {
      if (signal?.aborted) {
        return;
      }
      setQueryError('任务状态查询暂时不可用，后台任务仍在处理中，可稍后重试');
    }
  }, []);

  useEffect(() => {
    if (!hasUnfinished) {
      return;
    }
    const controller = new AbortController();
    const interval = setInterval(() => {
      void pollOnce(unfinishedRef.current, controller.signal);
    }, intervalMs);
    return () => {
      clearInterval(interval);
      controller.abort();
    };
  }, [hasUnfinished, intervalMs, pollOnce]);

  const refresh = useCallback(async () => {
    try {
      const data = await api.listTasks();
      setTasks(data);
      setQueryError(null);
      return data;
    } catch {
      setQueryError('任务状态查询暂时不可用，后台任务仍在处理中，可稍后重试');
      return null;
    }
  }, []);

  const loadInitial = useCallback(async () => {
    setInitialLoading(true);
    try {
      const data = await api.listTasks();
      setTasks(data);
      setQueryError(null);
    } catch {
      setQueryError('任务状态查询暂时不可用，后台任务仍在处理中，可稍后重试');
    } finally {
      setInitialLoading(false);
    }
  }, []);

  return { tasks, queryError, initialLoading, refresh, loadInitial };
}