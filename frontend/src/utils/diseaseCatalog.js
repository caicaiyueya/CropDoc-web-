/**
 * 病害库演示数据与字段归一化
 * 预留后端字段：image_url / description / causes / conditions / prevention
 */

const COVER = '/images/hero-farm.jpg'
const POS = ['18% 40%', '72% 35%', '45% 70%', '30% 20%', '60% 55%', '25% 65%']

/** 演示用完整病害资料（接口缺字段时合并） */
export const MOCK_DISEASES = [
  {
    id: 'mock-1',
    name: '稻瘟病',
    level: 'BASIC',
    image_url: COVER,
    coverPos: POS[0],
    description:
      '稻瘟病是水稻上危害最重的真菌性病害之一，可发生在叶片、节、穗颈等部位，流行年份可造成大幅度减产。',
    causes:
      '由稻瘟病菌（Magnaporthe oryzae）侵染引起。病谷、病稻草是重要初侵染源，分生孢子随气流传播，在叶片表面有水膜时侵入。',
    conditions:
      '适温（24～28℃）配合高湿、连阴雨、露水重的天气最易流行；偏施氮肥、密植郁闭、感病品种会显著加重发生。',
    prevention:
      '选用抗病品种；浅水勤灌，避免长期深水与偏氮；破口抽穗期用三环唑、稻瘟灵等预防，发病后改用春雷霉素等治疗剂，注意轮换用药。'
  },
  {
    id: 'mock-2',
    name: '玉米普通锈病',
    level: 'BASIC',
    image_url: COVER,
    coverPos: POS[1],
    description:
      '玉米普通锈病以叶部橙黄色夏孢子堆为典型症状，严重时叶片早枯，影响灌浆与产量。',
    causes:
      '由玉米柄锈菌侵染引起。夏孢子随气流远距离传播，田间病残体与自生苗可提供菌源。',
    conditions:
      '凉爽高湿、昼夜温差大、叶面结露时间长时扩展快；感病品种、过密种植、偏施氮肥易加重。',
    prevention:
      '清除病残体，选用抗病品种；发病初期喷施三唑类或代森锰锌类药剂，7～10 天一次，连喷 2～3 次；保持通风，控制氮肥。'
  },
  {
    id: 'mock-3',
    name: '小麦赤霉病',
    level: 'PRO',
    image_url: COVER,
    coverPos: POS[2],
    description:
      '小麦赤霉病主要危害穗部，形成粉红霉层与枯白穗，籽粒皱缩，还可能产生真菌毒素，影响食品安全。',
    causes:
      '主要由禾谷镰孢等镰刀菌侵染。病残体与土壤带菌，孢子借风雨飞溅到花器侵入。',
    conditions:
      '扬花期遇连阴雨、高温高湿最易大发生；感病品种、氮肥过量、田间郁闭会提高风险。',
    prevention:
      '扬花初期及时预防用药（氰烯菌酯、戊唑醇等）；雨前雨后补喷；收获后清除病残，合理轮作与控氮。'
  },
  {
    id: 'mock-4',
    name: '番茄晚疫病',
    level: 'BASIC',
    image_url: COVER,
    coverPos: POS[3],
    description:
      '番茄晚疫病可在短期内造成叶片、茎秆与果实大面积坏死，棚室与露地均可暴发。',
    causes:
      '由致病疫霉（Phytophthora infestans）侵染。病残体与种薯（相关寄主）可带菌，孢子囊随风雨扩散。',
    conditions:
      '冷凉高湿、叶面结露、通风不良时极易流行；连作田与密植棚室风险更高。',
    prevention:
      '降温排湿，避免叶面长时间结露；发现中心病株立即清除；烯酰吗啉、霜脲氰等药剂保护与治疗交替使用。'
  },
  {
    id: 'mock-5',
    name: '苹果黑星病',
    level: 'PRO',
    image_url: COVER,
    coverPos: POS[4],
    description:
      '苹果黑星病危害叶片与果实，形成橄榄色至黑色病斑，导致早期落叶与果实商品性下降。',
    causes:
      '由苹果黑星病菌侵染。病叶病果越冬，子囊孢子与分生孢子借风雨侵染幼嫩组织。',
    conditions:
      '春季多雨、湿润凉爽时初侵染高峰明显；树冠郁闭、清园不彻底会加重全年发生。',
    prevention:
      '秋冬彻底清园；萌芽前石硫合剂；生长季节保护性与治疗性杀菌剂交替，重点抓好幼果期防治。'
  },
  {
    id: 'mock-6',
    name: '大豆锈病',
    level: 'BASIC',
    image_url: COVER,
    coverPos: POS[5],
    description:
      '大豆锈病在叶背形成褐色夏孢子堆，严重时叶片早落，鼓粒期受害影响百粒重。',
    causes:
      '由大豆锈病菌侵染叶片。夏孢子借气流传播，田间高湿利于侵入。',
    conditions:
      '温暖高湿、叶面结露、密植郁闭时扩展迅速；连作与感病品种发病更重。',
    prevention:
      '合理密植降湿；初见病斑即喷三唑类或甲氧基丙烯酸酯类药剂；收获后清除残体并轮作。'
  }
]

/**
 * 将后端 Disease 归一化为详情展示结构
 * @param {object} raw
 * @param {number} [index]
 */
export function normalizeDisease(raw, index = 0) {
  if (!raw) return null
  const name = raw.name || raw.title || '未知病害'
  const mock = MOCK_DISEASES.find((m) => m.name === name) || MOCK_DISEASES[index % MOCK_DISEASES.length]
  return {
    id: raw.id ?? mock.id,
    name,
    level: raw.level || mock.level || 'BASIC',
    image_url: raw.image_url || raw.imageUrl || raw.cover || mock.image_url || COVER,
    coverPos: raw.coverPos || mock.coverPos || POS[index % POS.length],
    description: raw.description || mock.description || '暂无描述',
    causes: raw.causes || raw.cause || mock.causes || `${name}常见由病原菌或虫害引起，田间残体与连作是重要初侵染源。`,
    conditions:
      raw.conditions ||
      raw.condition ||
      mock.conditions ||
      '高温高湿、通风不良、偏施氮肥时更易流行，连作密植田块风险更高。',
    prevention:
      raw.prevention ||
      raw.preventionAdvice ||
      mock.prevention ||
      '及时清除病残体，改善通风降湿；按当地农技指导轮换用药，遵循安全间隔期。',
    // 列表摘要
    summary: (raw.description || mock.description || '').slice(0, 56)
  }
}

/**
 * 列表不足时用 Mock 补齐，保证演示效果
 * @param {object[]} apiList
 * @param {number} minCount
 */
export function fillDiseaseList(apiList, minCount = 6) {
  const mapped = (apiList || []).map((d, i) => normalizeDisease(d, i))
  if (mapped.length >= minCount) return mapped
  const names = new Set(mapped.map((d) => d.name))
  const pad = MOCK_DISEASES.filter((m) => !names.has(m.name)).map((m, i) =>
    normalizeDisease(m, mapped.length + i)
  )
  return [...mapped, ...pad].slice(0, Math.max(minCount, mapped.length))
}

/**
 * 标准病害参考图：优先 API 字段，其次按中文名匹配病害库，最后占位图
 * @param {{ name?: string, api?: object }} opts
 * @returns {string}
 */
export function resolveStandardDiseaseImage(opts = {}) {
  const api = opts.api || {}
  const fromApi =
    api.standard_image ||
    api.standardImage ||
    api.reference_image ||
    api.referenceImage ||
    api.disease_image ||
    api.diseaseImage
  if (fromApi && typeof fromApi === 'string') return fromApi

  const name = (opts.name || '').toString().trim()
  if (name) {
    const hit = MOCK_DISEASES.find(
      (m) => m.name === name || name.includes(m.name) || m.name.includes(name.replace(/（健康）$/, ''))
    )
    if (hit?.image_url) return hit.image_url
  }
  return COVER
}
