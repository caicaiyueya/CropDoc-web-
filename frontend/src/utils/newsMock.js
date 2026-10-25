/**
 * 农业资讯 Mock（接口为空时兜底展示）
 */

const COVER = '/images/hero-farm.jpg'

export const MOCK_NEWS = [
  {
    id: 'mock-news-1',
    title: '春季田间管理要点：控水控肥防病害',
    summary: '进入返青拔节期，需根据墒情合理灌溉，避免偏施氮肥，重点监测叶部病害早期症状。',
    content:
      '春季是作物生长与病虫害交错上升的关键阶段。建议浅水勤灌、增施磷钾肥，结合田间巡查及时发现病斑并取样识别。',
    cover: COVER,
    createTime: '2026-10-24 08:30:00'
  },
  {
    id: 'mock-news-2',
    title: '南方稻区注意稻瘟病早期监测',
    summary: '近期温湿度适宜稻瘟病菌萌发，破口抽穗前应做好预防用药与田间观察。',
    content:
      '稻瘟病流行与气候密切相关。农户可关注当地农技部门预警，选用抗病品种，并在关键生育期落实药剂保护。',
    cover: COVER,
    createTime: '2026-10-24 07:50:00'
  },
  {
    id: 'mock-news-3',
    title: '智能识别如何辅助基层农技服务',
    summary: '拍照识病可缩短初诊时间，结合问诊建议为农户提供可操作的防治路径。',
    content:
      '基层农技员可将 AI 识别结果作为初筛参考，再结合田间环境与用药史给出最终方案，提升服务效率与可追溯性。',
    cover: COVER,
    createTime: '2026-10-23 18:20:00'
  },
  {
    id: 'mock-news-4',
    title: '农技员认证通道已开放申请',
    summary: '普通农户可提交资质说明与证明材料，审核通过后可发布农技指导内容。',
    content:
      '认证农技员将获得专业病害资料查看与农技指导发帖权限，欢迎有经验的种植户与技术员申请。',
    cover: COVER,
    createTime: '2026-10-23 15:10:00'
  },
  {
    id: 'mock-news-5',
    title: '雨后田间病害应急处理指南',
    summary: '强降雨后及时排水降湿，清除病叶病果，必要时补喷保护性杀菌剂。',
    content:
      '雨后叶面长时间结露利于病原侵入。建议优先改善通风排水，再根据作物与病害类型选择药剂，注意安全间隔期。',
    cover: COVER,
    createTime: '2026-10-22 16:40:00'
  }
]

/**
 * @param {object[]} apiList
 * @returns {object[]}
 */
export function resolveNewsList(apiList) {
  const list = (apiList || []).map((n) => ({
    id: n.id,
    title: n.title,
    summary: n.summary || (n.content || '').slice(0, 72),
    content: n.content,
    cover: n.cover || COVER,
    createTime: n.createTime
  }))
  if (list.length) return list
  return MOCK_NEWS.map((n) => ({ ...n }))
}
