import TemplateDetail from '@/components/TemplateDetail';

export default function TemplatePage({ params }: { params: { id: string } }) {
  return <TemplateDetail id={params.id} />;
}