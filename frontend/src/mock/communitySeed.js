/**
 * 农友交流演示种子（与 sql/04_community_seed.sql、DataInitializer 对齐）
 * 密码统一：Demo@123
 */

export const SEED_USERS = [
  { id: 101, username: 'farmer_wang', nickname: '种田老王', role: 'FARMER', region: '湖南长沙' },
  { id: 102, username: 'farmer_zhang', nickname: '小麦张', role: 'FARMER', region: '河南周口' },
  { id: 103, username: 'farmer_li', nickname: '玉米李', role: 'FARMER', region: '吉林长春' },
  { id: 104, username: 'farmer_chen', nickname: '翠花种植', role: 'FARMER', region: '四川眉山' },
  { id: 105, username: 'farmer_liu', nickname: '田间小陈', role: 'FARMER', region: '江苏盐城' },
  { id: 106, username: 'expert_li', nickname: '李农技', role: 'EXPERT', region: '湖南农技中心' },
  { id: 107, username: 'expert_zhao', nickname: '赵专家', role: 'EXPERT', region: '省植保站' }
]

/** 帖子封面（与 DataInitializer images 字段一致：category||url） */
const COVER_RICE_BLAST = '/images/community/rice-blast.png'
const COVER_CORN_RUST = '/images/community/corn-rust.png'
const COVER_WHEAT_FUSARIUM = '/images/community/wheat-fusarium.png'
const COVER_TOMATO_BLIGHT = '/images/community/tomato-late-blight.png'

export const SEED_POSTS = [
  {
    id: 1001,
    title: '春季稻瘟病早期识别与防治要点（专家科普）',
    content:
      '稻瘟病是水稻上最重要的真菌病害之一。叶片出现梭形灰绿色病斑时要提高警惕，破口抽穗前后遇连阴雨更易流行。\n建议选用抗病品种、控制氮肥，破口前预防用药，药剂轮换。',
    authorId: 106,
    authorName: '李农技',
    authorRole: 'EXPERT',
    tags: ['官方', '专家', '经验'],
    tag: 'GUIDE',
    pinned: true,
    viewCount: 328,
    createTime: '2026-03-18 09:20:00',
    images: [`experience||${COVER_RICE_BLAST}`]
  },
  {
    id: 1002,
    title: '玉米叶子发黄长锈斑，求助各位农友！',
    content:
      '自家两亩玉米下部叶片出现橙黄色粉状物，露水重。请问是否普通锈病？求用药建议。',
    authorId: 103,
    authorName: '玉米李',
    authorRole: 'FARMER',
    tags: ['求助'],
    tag: 'NORMAL',
    pinned: false,
    viewCount: 156,
    createTime: '2026-03-20 14:05:00',
    images: [`help||${COVER_CORN_RUST}`]
  },
  {
    id: 1003,
    title: '小麦扬花期防赤霉病，我的三年实操经验',
    content:
      '扬花初期用药、雨前雨后盯紧、药剂轮换、控氮通风。欢迎补充各地差异做法。',
    authorId: 102,
    authorName: '小麦张',
    authorRole: 'FARMER',
    tags: ['经验'],
    tag: 'NORMAL',
    pinned: false,
    viewCount: 210,
    createTime: '2026-03-22 08:40:00',
    images: [`experience||${COVER_WHEAT_FUSARIUM}`]
  },
  {
    id: 1004,
    title: '棚室番茄晚疫病：降湿比打药更重要',
    content: '先通风排湿，再配合药剂保护；发现中心病株立即清除。',
    authorId: 107,
    authorName: '赵专家',
    authorRole: 'EXPERT',
    tags: ['官方', '专家', '经验'],
    tag: 'GUIDE',
    pinned: false,
    viewCount: 275,
    createTime: '2026-03-24 16:30:00',
    images: [`experience||${COVER_TOMATO_BLIGHT}`]
  }
]

export const SEED_COMMENTS = [
  {
    id: 2001,
    postId: 1001,
    authorId: 101,
    authorName: '种田老王',
    authorRole: 'FARMER',
    content: '写得很清楚，正好我们村最近准备统一预防用药，收藏了！',
    isProfessional: false,
    isQuality: false,
    createTime: '2026-03-18 11:02:00'
  },
  {
    id: 2002,
    postId: 1001,
    authorId: 107,
    authorName: '赵专家',
    authorRole: 'EXPERT',
    content: '补充一点：感病品种田块破口抽穗期建议连续保护两次，间隔 5～7 天。',
    isProfessional: true,
    isQuality: true,
    createTime: '2026-03-18 15:40:00'
  },
  {
    id: 2003,
    postId: 1002,
    authorId: 106,
    authorName: '李农技',
    authorRole: 'EXPERT',
    content: '从描述看高度疑似普通锈病。初期可用三唑类药剂，7～10 天一次，连喷 2～3 次。',
    isProfessional: true,
    isQuality: true,
    createTime: '2026-03-20 16:18:00'
  },
  {
    id: 2004,
    postId: 1002,
    authorId: 104,
    authorName: '翠花种植',
    authorRole: 'FARMER',
    content: '我们这边也这样，打了两遍好多了，记得别偏施氮肥。',
    isProfessional: false,
    isQuality: false,
    createTime: '2026-03-20 18:05:00'
  },
  {
    id: 2005,
    postId: 1003,
    authorId: 101,
    authorName: '种田老王',
    authorRole: 'FARMER',
    content: '学到了，往年扬花遇雨就慌，今年按这个节奏试试看。',
    isProfessional: false,
    isQuality: false,
    createTime: '2026-03-22 10:12:00'
  },
  {
    id: 2006,
    postId: 1004,
    authorId: 104,
    authorName: '翠花种植',
    authorRole: 'FARMER',
    content: '降湿这点太关键了，去年就是闷棚打药效果差。',
    isProfessional: false,
    isQuality: false,
    createTime: '2026-03-24 19:10:00'
  }
]

const userById = Object.fromEntries(SEED_USERS.map((u) => [u.id, u]))
const userByUsername = Object.fromEntries(SEED_USERS.map((u) => [u.username, u]))

export function findSeedUser(idOrName) {
  if (idOrName == null) return null
  return userById[idOrName] || userByUsername[idOrName] || null
}

export function getSeedPost(id) {
  return SEED_POSTS.find((p) => String(p.id) === String(id)) || null
}

export function getSeedComments(postId) {
  return SEED_COMMENTS.filter((c) => String(c.postId) === String(postId)).sort((a, b) =>
    String(b.createTime).localeCompare(String(a.createTime))
  )
}

/** 侧栏「活跃农友」展示用 */
export const SEED_ACTIVE_USERS = [
  { name: '李农技', role: '认证农技员' },
  { name: '赵专家', role: '认证农技员' },
  { name: '翠花种植', role: '活跃农户' },
  { name: '田间小陈', role: '经验达人' }
]
