'use client';

import { useEffect, useState } from 'react';
import { api } from '@/lib/api';
import { useTaskTracker } from '@/hooks/useTaskTracker';
import { Template } from '@/lib/types';
import TaskCard from './TaskCard';

export default function HistoryClient() {
  const { tasks, queryError, initialLoading, loadInitial, refresh } = useTaskTracker([]);
  const [templates, setTemplates] = useState<Template[]>([]);

  useEffect(() => {
    void loadInitial();
  }, [loadInitial]);

  useEffect(() => {
    api.listTemplates().then(setTemplates).catch(() => {});
  }, []);

  const titleMap = new Map(templates.map((t) => [t.id, t.title]));

  return (
    <>
      <div className="page-title">
        <h1>历史记录</h1>
        <p className="muted">查看你的制作任务与结果</p>
      </div>

      {queryError && (
        <div className="retry-bar" role="alert">
          <span className="error-text">{queryError}</span>
          <button type="button" className="btn btn-secondary" onClick={() => void refresh()}>
            重试查询
          </button>
        </div>
      )}

      {initialLoading ? (
        <div className="loading">记录加载中…</div>
      ) : tasks.length === 0 ? (
        <div className="empty">还没有任务，去模板库开始制作吧</div>
      ) : (
        <div className="task-list">
          {tasks.map((t) => (
            <TaskCard key={t.id} task={t} title={titleMap.get(t.templateId) ?? t.templateId} />
          ))}
        </div>
      )}
    </>
  );
}