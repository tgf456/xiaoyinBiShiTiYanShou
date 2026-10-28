/** @type {import('next').NextConfig} */
const nextConfig = {
  // lint 在 CI 中单独执行，避免与 build 耦合
  eslint: { ignoreDuringBuilds: true },
  reactStrictMode: true,
  // 独立输出，便于 Docker 多阶段构建以最小体积运行
  output: 'standalone',
};

export default nextConfig;