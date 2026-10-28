'use client';

import { useEffect, useMemo, useState } from 'react';
import { api } from '@/lib/api';
import { Template } from '@/lib/types';
import TemplateCard from './TemplateCard';
import CategoryFilter from './CategoryFilter';

export default function TemplateList() {
  const [templates, setTemplates] = useState<Template[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [category, setCategory] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;
    api
      .listTemplates()
      .then((data) => {
        if (!cancelled) {
          setTemplates(data);
          setLoading(false);
        }
      })
      .catch(() => {
        if (!cancelled) {
          setError('加载模板失败，请稍后重试');
          setLoading(false);
        }
      });
    return () => {
      cancelled = true;
    };
  }, []);

  const categories = useMemo(() => Array.from(new Set(templates.map((t) => t.category))), [templates]);
  const filtered = category ? templates.filter((t) => t.category === category) : templates;

  if (loading) {
    return <div className="loading">模板加载中…</div>;
  }
  if (error) {
    return <div className="error">{error}</div>;
  }
  if (templates.length === 0) {
    return <div className="empty">暂无模板可用</div>;
  }

  return (
    <>
      <div className="page-title">
        <h1>模板库</h1>
        <p className="muted">选择一个模板开始制作</p>
      </div>
      <CategoryFilter categories={categories} active={category} onChange={setCategory} />
      {filtered.length === 0 ? (
        <div className="empty">该分类下暂无模板</div>
      ) : (
        <div className="grid">
          {filtered.map((t) => (
            <TemplateCard key={t.id} template={t} />
          ))}
        </div>
      )}
    </>
  );
}