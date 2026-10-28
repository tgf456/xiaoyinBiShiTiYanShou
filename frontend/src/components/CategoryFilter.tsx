export default function CategoryFilter({
  categories,
  active,
  onChange,
}: {
  categories: string[];
  active: string | null;
  onChange: (category: string | null) => void;
}) {
  return (
    <div className="category-list" role="group" aria-label="模板分类筛选">
      <button
        type="button"
        className={`category-btn ${active === null ? 'active' : ''}`}
        onClick={() => onChange(null)}
      >
        全部
      </button>
      {categories.map((c) => (
        <button
          key={c}
          type="button"
          className={`category-btn ${active === c ? 'active' : ''}`}
          onClick={() => onChange(c)}
        >
          {c}
        </button>
      ))}
    </div>
  );
}