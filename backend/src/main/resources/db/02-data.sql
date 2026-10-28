-- 模板种子数据（幂等：忽略重复主键）
-- accepted_mime_types 使用逗号分隔，服务端返回时解析为数组

INSERT IGNORE INTO template (id, title, category, cover_url, description, required_image_count, accepted_mime_types, max_file_size_bytes) VALUES
('demo-postcard', '旅行明信片', '创意图片', '/demo/postcard-cover.svg', '上传图片，模拟生成一张明信片', 2, 'image/jpeg,image/png', 5242880),
('demo-collage', '拼贴画', '创意图片', '/demo/collage-cover.svg', '将多张图片拼接成一张创意拼贴画', 3, 'image/jpeg,image/png', 5242880),
('demo-magazine', '杂志封面', '海报设计', '/demo/magazine-cover.svg', '用一张主图制作杂志封面效果', 1, 'image/jpeg,image/png', 5242880),
('demo-banner', '横幅广告', '海报设计', '/demo/banner-cover.svg', '生成带主视觉的横幅广告图', 2, 'image/jpeg,image/png', 5242880),
('demo-product', '商品海报', '电商视觉', '/demo/product-cover.svg', '为商品生成简洁的促销海报', 3, 'image/jpeg,image/png', 5242880);