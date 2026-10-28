'use client';

import { useCallback, useEffect, useState } from 'react';
import Link from 'next/link';
import { api, ApiError, assetUrl } from '@/lib/api';
import { Asset, Task, Template } from '@/lib/types';
import UploadSlot from './UploadSlot';

interface Slot {
  asset: Asset | null;
  uploading: boolean;
  error: string | null;
}

const EMPTY_SLOT: Slot = { asset: null, uploading: false, error: null };

export default function TemplateDetail({ id }: { id: string }) {
  const [template, setTemplate] = useState<Template | null>(null);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState<string | null>(null);

  const [slots, setSlots] = useState<Slot[]>([]);
  const [submitting, setSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState<string | null>(null);
  const [submittedTask, setSubmittedTask] = useState<Task | null>(null);

  useEffect(() => {
    let cancelled = false;
    api
      .getTemplate(id)
      .then((data) => {
        if (cancelled) return;
        setTemplate(data);
        setSlots(Array.from({ length: data.requiredImageCount }, () => ({ ...EMPTY_SLOT })));
        setLoading(false);
      })
      .catch((e) => {
        if (cancelled) return;
        if (e instanceof ApiError && e.status === 404) {
          setLoadError('模板不存在');
        } else {
          setLoadError('加载模板失败，请稍后重试');
        }
        setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, [id]);

  const validateFile = useCallback(
    (file: File): string | null => {
      if (!template) {
        return '模板信息缺失';
      }
      if (!template.acceptedMimeTypes.includes(file.type)) {
        return `图片类型不支持，仅支持 ${template.acceptedMimeTypes.join(' / ')}`;
      }
      if (file.size > template.maxFileSizeBytes) {
        return `图片超过大小限制（最大 ${formatBytes(template.maxFileSizeBytes)}）`;
      }
      return null;
    },
    [template],
  );

  const handleSelect = useCallback(
    async (index: number, file: File) => {
      if (!template) return;
      const clientError = validateFile(file);
      setSlots((prev) =>
        prev.map((s, i) => (i === index ? { ...s, error: clientError, uploading: false } : s)),
      );
      if (clientError) {
        return;
      }

      setSlots((prev) => prev.map((s, i) => (i === index ? { ...s, uploading: true, error: null } : s)));
      try {
        const asset = await api.uploadAsset(template.id, file);
        setSlots((prev) => prev.map((s, i) => (i === index ? { asset, uploading: false, error: null } : s)));
      } catch (e) {
        const message = e instanceof ApiError ? e.message : '上传失败，请重试';
        setSlots((prev) => prev.map((s, i) => (i === index ? { ...s, uploading: false, error: message } : s)));
      }
    },
    [template, validateFile],
  );

  const handleRemove = useCallback((index: number) => {
    setSlots((prev) => prev.map((s, i) => (i === index ? { ...EMPTY_SLOT } : s)));
  }, []);

  const handleSubmit = useCallback(async () => {
    if (!template || submitting) return;
    const missing = slots.some((s) => !s.asset);
    if (missing) {
      setSubmitError('请先完成所有素材上传');
      return;
    }
    setSubmitting(true);
    setSubmitError(null);
    try {
      const assetIds = slots.map((s) => s.asset!.id);
      const task = await api.submitTask(template.id, assetIds);
      setSubmittedTask(task);
      setSlots(Array.from({ length: template.requiredImageCount }, () => ({ ...EMPTY_SLOT })));
    } catch (e) {
      setSubmitError(e instanceof ApiError ? e.message : '提交失败，请重试');
    } finally {
      setSubmitting(false);
    }
  }, [template, slots, submitting]);

  if (loading) {
    return <div className="loading">模板加载中…</div>;
  }
  if (loadError) {
    return <div className="error">{loadError}</div>;
  }
  if (!template) {
    return <div className="empty">模板不存在</div>;
  }

  const allReady = slots.length > 0 && slots.every((s) => s.asset);

  return (
    <>
      <div className="page-title">
        <h1>{template.title}</h1>
        <p className="muted">{template.category}</p>
      </div>

      <div className="detail-grid">
        <div>
          <img className="detail-cover" src={assetUrl(template.coverUrl) ?? ''} alt={template.title} />
          <div className="requirements">
            <h3>素材要求</h3>
            <p>需要 {template.requiredImageCount} 张图片</p>
            <p>支持类型：{template.acceptedMimeTypes.join(' / ')}</p>
            <p>单张最大 {formatBytes(template.maxFileSizeBytes)}</p>
          </div>
        </div>

        <div>
          <p>{template.description}</p>

          <div className="upload-slots">
            {slots.map((slot, index) => (
              <UploadSlot
                key={index}
                index={index}
                asset={slot.asset}
                uploading={slot.uploading}
                error={slot.error}
                onSelect={(file) => void handleSelect(index, file)}
                onRemove={() => handleRemove(index)}
              />
            ))}
          </div>

          <div className="submit-bar">
            <button
              type="button"
              className="btn btn-primary"
              disabled={!allReady || submitting}
              onClick={() => void handleSubmit()}
            >
              {submitting ? '提交中…' : '提交制作'}
            </button>
            <Link className="btn btn-secondary" href="/history">
              查看历史记录
            </Link>
          </div>
          {submitError && <p className="submit-error">{submitError}</p>}

          {submittedTask && (
            <div className="accepted-panel">
              <h3>任务已受理</h3>
              <p>任务 ID：{submittedTask.id}</p>
              <p>
                正在后台处理，可到 <Link href="/history">历史记录</Link> 查看进度与结果。
              </p>
            </div>
          )}
        </div>
      </div>
    </>
  );
}

function formatBytes(bytes: number): string {
  if (bytes >= 1024 * 1024) {
    return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
  }
  if (bytes >= 1024) {
    return `${Math.round(bytes / 1024)} KB`;
  }
  return `${bytes} B`;
}