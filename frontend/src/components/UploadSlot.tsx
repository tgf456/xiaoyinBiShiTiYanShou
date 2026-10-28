'use client';

import { useRef } from 'react';
import { assetUrl } from '@/lib/api';
import { Asset } from '@/lib/types';

interface Props {
  index: number;
  asset: Asset | null;
  uploading: boolean;
  error: string | null;
  onSelect: (file: File) => void;
  onRemove: () => void;
}

export default function UploadSlot({ index, asset, uploading, error, onSelect, onRemove }: Props) {
  const inputRef = useRef<HTMLInputElement>(null);

  return (
    <div className={`upload-slot ${asset ? 'filled' : ''}`}>
      {asset ? (
        <div className="upload-preview">
          <img src={assetUrl(asset.url) ?? ''} alt={`素材 ${index + 1}`} />
          <div className="upload-actions">
            <button
              type="button"
              className="btn btn-secondary"
              aria-label={`替换第 ${index + 1} 张图片`}
              onClick={() => inputRef.current?.click()}
            >
              替换
            </button>
            <button
              type="button"
              className="btn btn-secondary"
              aria-label={`移除第 ${index + 1} 张图片`}
              onClick={onRemove}
            >
              移除
            </button>
          </div>
        </div>
      ) : (
        <button
          type="button"
          className="upload-placeholder"
          aria-label={`选择第 ${index + 1} 张图片`}
          onClick={() => inputRef.current?.click()}
          disabled={uploading}
        >
          {uploading ? '上传中…' : `选择图片 ${index + 1}`}
        </button>
      )}
      {error && <p className="upload-error">{error}</p>}
      <input
        ref={inputRef}
        type="file"
        accept="image/jpeg,image/png"
        hidden
        onChange={(e) => {
          const file = e.target.files?.[0];
          if (file) {
            onSelect(file);
          }
          e.target.value = '';
        }}
      />
    </div>
  );
}