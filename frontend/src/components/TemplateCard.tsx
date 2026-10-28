import Link from 'next/link';
import { assetUrl } from '@/lib/api';
import { Template } from '@/lib/types';

export default function TemplateCard({ template }: { template: Template }) {
  return (
    <Link href={`/templates/${template.id}`} className="card">
      <img className="card-cover" src={assetUrl(template.coverUrl) ?? ''} alt={template.title} />
      <div className="card-body">
        <h2 className="card-title">{template.title}</h2>
        <p className="card-meta">{template.category} · 需 {template.requiredImageCount} 张图片</p>
      </div>
    </Link>
  );
}