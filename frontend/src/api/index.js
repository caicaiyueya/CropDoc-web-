/**
 * API 模块划分：
 * - auth.js           注册登录改密
 * - certification.js  认证申请与审核
 * - detection.js      上传/识别/诊断
 * - post.js           社区帖子评论收藏举报
 * - content.js        资讯病害作物个人中心
 * - admin.js          管理端其余接口
 * - request.js        Axios 统一封装
 */
export { default as request } from './request'
export * from './auth'
export * from './certification'
export * from './detection'
export * from './post'
export * from './content'
export * from './admin'
