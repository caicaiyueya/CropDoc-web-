/**
 * NFNet-F1 植物病害模型 88 类 disease_key → 中文名
 * 与 Hugging Face Arko007/nfnet-f1-plant-disease config.class_names 一一对应
 */
export const diseaseCnMap = {
  Apple__black_rot: '苹果黑腐病',
  Apple__healthy: '苹果（健康）',
  Apple__rust: '苹果锈病',
  Apple__scab: '苹果黑星病',
  Cassava__bacterial_blight: '木薯细菌性疫病',
  Cassava__brown_streak_disease: '木薯褐条病',
  Cassava__green_mottle: '木薯绿斑驳病',
  Cassava__healthy: '木薯（健康）',
  Cassava__mosaic_disease: '木薯花叶病',
  Cherry__healthy: '樱桃（健康）',
  Cherry__powdery_mildew: '樱桃白粉病',
  Chili__healthy: '辣椒（健康）',
  'Chili__leaf curl': '辣椒卷叶病',
  'Chili__leaf spot': '辣椒叶斑病',
  Chili__whitefly: '辣椒白粉虱危害',
  Chili__yellowish: '辣椒黄化症',
  Coffee__cercospora_leaf_spot: '咖啡尾孢叶斑病',
  Coffee__healthy: '咖啡（健康）',
  Coffee__red_spider_mite: '咖啡红蜘蛛危害',
  Coffee__rust: '咖啡锈病',
  Corn__common_rust: '玉米普通锈病',
  Corn__gray_leaf_spot: '玉米灰斑病',
  Corn__healthy: '玉米（健康）',
  Corn__northern_leaf_blight: '玉米大斑病',
  Cucumber__diseased: '黄瓜病害',
  Cucumber__healthy: '黄瓜（健康）',
  Gauva__diseased: '番石榴病害',
  Gauva__healthy: '番石榴（健康）',
  Grape__black_measles: '葡萄黑麻疹病',
  Grape__black_rot: '葡萄黑腐病',
  Grape__healthy: '葡萄（健康）',
  'Grape__leaf_blight_(isariopsis_leaf_spot)': '葡萄叶枯病（拟茎点霉叶斑）',
  Jamun__diseased: '枣病害',
  Jamun__healthy: '枣（健康）',
  Lemon__diseased: '柠檬病害',
  Lemon__healthy: '柠檬（健康）',
  Mango__diseased: '芒果病害',
  Mango__healthy: '芒果（健康）',
  Peach__bacterial_spot: '桃细菌性斑点病',
  Peach__healthy: '桃（健康）',
  Pepper_bell__bacterial_spot: '甜椒细菌性斑点病',
  Pepper_bell__healthy: '甜椒（健康）',
  Pomegranate__diseased: '石榴病害',
  Pomegranate__healthy: '石榴（健康）',
  Potato__early_blight: '马铃薯早疫病',
  Potato__healthy: '马铃薯（健康）',
  Potato__late_blight: '马铃薯晚疫病',
  Rice__brown_spot: '水稻褐斑病',
  Rice__healthy: '水稻（健康）',
  Rice__hispa: '水稻铁甲虫危害',
  Rice__leaf_blast: '水稻叶瘟',
  Rice__neck_blast: '水稻穗颈瘟',
  Soybean__bacterial_blight: '大豆细菌性疫病',
  Soybean__caterpillar: '大豆毛虫危害',
  Soybean__diabrotica_speciosa: '大豆叶甲危害',
  Soybean__downy_mildew: '大豆霜霉病',
  Soybean__healthy: '大豆（健康）',
  Soybean__mosaic_virus: '大豆花叶病毒病',
  Soybean__powdery_mildew: '大豆白粉病',
  Soybean__rust: '大豆锈病',
  Soybean__southern_blight: '大豆白绢病',
  Strawberry___leaf_scorch: '草莓叶焦病',
  Strawberry__healthy: '草莓（健康）',
  Sugarcane__bacterial_blight: '甘蔗细菌性疫病',
  Sugarcane__healthy: '甘蔗（健康）',
  Sugarcane__red_rot: '甘蔗赤腐病',
  Sugarcane__red_stripe: '甘蔗赤条病',
  Sugarcane__rust: '甘蔗锈病',
  Tea__algal_leaf: '茶藻藻斑病',
  Tea__anthracnose: '茶炭疽病',
  Tea__bird_eye_spot: '茶鸟眼斑病',
  Tea__brown_blight: '茶褐色叶枯病',
  Tea__healthy: '茶（健康）',
  Tea__red_leaf_spot: '茶红叶斑病',
  Tomato__bacterial_spot: '番茄细菌性斑点病',
  Tomato__early_blight: '番茄早疫病',
  Tomato__healthy: '番茄（健康）',
  Tomato__late_blight: '番茄晚疫病',
  Tomato__leaf_mold: '番茄叶霉病',
  Tomato__mosaic_virus: '番茄花叶病毒病',
  Tomato__septoria_leaf_spot: '番茄斑枯病',
  'Tomato__spider_mites_(two_spotted_spider_mite)': '番茄二斑叶螨危害',
  Tomato__target_spot: '番茄靶斑病',
  Tomato__yellow_leaf_curl_virus: '番茄黄化曲叶病毒病',
  Wheat__brown_rust: '小麦叶锈病',
  Wheat__healthy: '小麦（健康）',
  Wheat__septoria: '小麦壳针孢叶枯病',
  Wheat__yellow_rust: '小麦条锈病'
}

/** @param {string} key disease_key */
export function toDiseaseCn(key) {
  if (!key) return '未知病害'
  return diseaseCnMap[key] || key
}

/**
 * 简版发病病因（识别结果面板用，2～3 行）
 * key 可为 disease_key 或中文名
 */
const causeByKey = {
  Corn__common_rust:
    '由真菌玉米柄锈菌侵染引起；夏孢子随气流远距离传播，田间病残体与自生苗可成为初侵染源。',
  Corn__gray_leaf_spot:
    '主要由玉米尾孢菌引起；病菌随病残体越冬，分生孢子借风雨飞溅传播，连作田菌源积累重。',
  Corn__northern_leaf_blight:
    '由大斑突脐蠕孢菌侵染叶片；病残体带菌越冬，孢子借风雨传播，感病品种易流行。',
  Rice__leaf_blast:
    '稻瘟病菌侵染叶片所致；病谷、病稻草带菌，分生孢子随气流传播，偏氮密植加重。',
  Rice__neck_blast:
    '稻瘟病菌侵染穗颈；破口抽穗遇阴雨高湿时易侵染，氮肥过量、抗性弱品种发病重。',
  Rice__brown_spot:
    '水稻胡麻叶斑病菌侵染；土壤贫瘠、缺钾及种子带菌是重要诱因，孢子随风雨传播。',
  Tomato__early_blight:
    '由茄链格孢侵染；病残体与土壤带菌，分生孢子借风雨、农事操作传播，下部老叶先发。',
  Tomato__late_blight:
    '致病疫霉侵染；低温高湿、叶面结露时暴发，种薯/病残体可带菌，孢子囊随风雨扩散。',
  Tomato__leaf_mold:
    '番茄叶霉病菌侵染叶背；棚室高温高湿、通风不良时易发，病菌可附着棚膜与残体越冬。',
  Potato__early_blight:
    '茄链格孢侵染块茎与叶片；连作、残体带菌，高温干燥交替与风雨传播促发。',
  Potato__late_blight:
    '致病疫霉引起；带病种薯是主要初侵染源，冷凉高湿天气孢子囊大量产生并传播。',
  Apple__scab:
    '苹果黑星病菌侵染；病叶病果越冬，子囊孢子/分生孢子借风雨侵染幼嫩组织。',
  Apple__rust:
    '锈菌转主寄生；桧柏等转主寄主上产生担孢子，春季侵染苹果叶片与幼果。',
  Grape__black_rot:
    '葡萄黑腐病菌侵染；病果病蔓带菌越冬，分生孢子借雨水飞溅侵染花穗与果实。',
  Wheat__yellow_rust:
    '小麦条锈菌专性寄生；夏孢子随气流远距离传播，感病品种与早发区菌源是流行关键。',
  Soybean__rust:
    '大豆锈病菌侵染叶片；夏孢子借气流传播，田间湿度大、叶面结露时易侵入。'
}

const healthyCauseTip =
  '当前未见明显病原侵害迹象。黄化或斑点若出现，多与水肥不均、药害或早期侵染有关，建议持续观察。'

const defaultCauseTip =
  '多为病原真菌/细菌/病毒或虫害刺吸为害所致；田间病残体、带菌种子与连作是常见初侵染来源，可点「立即智能诊断」了解详情。'

/** @param {string} keyOrCn disease_key 或中文名 */
export function getCauseTips(keyOrCn) {
  if (!keyOrCn) return defaultCauseTip
  if (causeByKey[keyOrCn]) return causeByKey[keyOrCn]
  const cn = toDiseaseCn(keyOrCn)
  if (/健康/.test(cn) || /healthy/i.test(String(keyOrCn))) return healthyCauseTip
  const hit = Object.entries(diseaseCnMap).find(([, v]) => v === keyOrCn || v === cn)
  if (hit && causeByKey[hit[0]]) return causeByKey[hit[0]]
  return `「${cn}」常见由病原菌或虫害引起，病残体与连作田块易积累菌源，高湿郁闭环境利于扩展。`
}

/** 简版防治要点（问诊侧栏「防治措施」等） */
const adviceByKey = {
  Corn__common_rust:
    '清除田间病残体，选用抗病品种；发病初期喷施三唑类或代森锰锌类药剂，7～10 天一次，连喷 2～3 次；避免偏施氮肥，保持田间通风。',
  Corn__gray_leaf_spot:
    '合理密植、及时排水降湿；轮作倒茬减少菌源；病初选用苯醚甲环唑、吡唑醚菌酯等药剂轮换喷施。',
  Corn__northern_leaf_blight:
    '选用抗病品种，清除病残体；抽雄前后遇连阴雨及时用药，注意下部叶片保护。',
  Rice__leaf_blast:
    '浅水勤灌、避免偏氮；破口抽穗期重点用药，三环唑或稻瘟灵预防，发病后改用春雷霉素等治疗。',
  Rice__neck_blast:
    '破口前 5～7 天与齐穗期各防一次；控制氮肥，增强抗性，雨前雨后补喷。',
  Rice__brown_spot:
    '增施磷钾肥，改良土壤；种子消毒，病初喷施咪鲜胺或代森锰锌。',
  Tomato__early_blight:
    '及时摘除病叶，控制棚湿；代森锰锌、烯酰吗啉等轮换喷施，注意下部老叶清理。',
  Tomato__late_blight:
    '降温降湿，避免叶面结露；发现中心病株立即拔除，烯酰吗啉、霜脲氰等药剂保护。',
  Tomato__leaf_mold:
    '加强通风排湿，适当稀植；硫磺烟剂或咯菌腈类药剂防治。',
  Potato__early_blight:
    '轮作、清除病残；现蕾后保护性杀菌剂预防，发病后治疗剂交替使用。',
  Potato__late_blight:
    '选用抗病种薯，高垄栽培；雨季前预防用药，发现中心株立即处理。',
  Apple__scab:
    '清园落叶，萌芽前石硫合剂；生长季节保护性与治疗性杀菌剂交替。',
  Apple__rust:
    '远离桧柏等转主寄主；展叶后至幼果期喷三唑类药剂 2～3 次。',
  Grape__black_rot:
    '冬季清园，开花前后重点用药；及时摘除病果病穗。',
  Wheat__yellow_rust:
    '监测早发区，发病中心封锁；三唑酮、烯唑醇等药剂速治，控制扩散。',
  Soybean__rust:
    '合理密植降湿；初见病斑即喷三唑类或甲氧基丙烯酸酯类药剂。'
}

const healthyAdviceTip =
  '叶片状态良好。请继续保持合理水肥与通风，定期巡田，发现异常斑点及时拍照复检。'

const defaultAdviceTip =
  '及时清除病叶与残体，改善通风降湿；药剂防治请按当地农技指导轮换用药，严格遵循安全间隔期。'

/** @param {string} keyOrCn disease_key 或中文名 */
export function getPreventionTips(keyOrCn) {
  if (!keyOrCn) return defaultAdviceTip
  if (adviceByKey[keyOrCn]) return adviceByKey[keyOrCn]
  const cn = toDiseaseCn(keyOrCn)
  if (/健康/.test(cn) || /healthy/i.test(String(keyOrCn))) return healthyAdviceTip
  const hit = Object.entries(diseaseCnMap).find(([, v]) => v === keyOrCn || v === cn)
  if (hit && adviceByKey[hit[0]]) return adviceByKey[hit[0]]
  return `针对「${cn}」：${defaultAdviceTip}`
}

export default diseaseCnMap
