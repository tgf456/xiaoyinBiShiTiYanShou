import type { Metadata } from 'next';
import Link from 'next/link';
import './globals.css';

export const metadata: Metadata = {
  title: '素材创作台',
  description: '浏览模板、上传图片并提交制作任务',
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="zh-CN">
      <body>
        <header className="navbar">
          <Link href="/" className="brand">素材创作台</Link>
          <nav className="nav-links">
            <Link href="/">模板库</Link>
            <Link href="/history">历史记录</Link>
          </nav>
        </header>
        <main className="container">{children}</main>
      </body>
    </html>
  );
}