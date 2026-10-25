# 《云农智诊》全套软件工程图表（Mermaid）

> 使用说明：将每个 `mermaid` 代码块完整复制到 [https://mermaid.live](https://mermaid.live) 即可渲染。下列图均可直接粘贴进可行性分析、SA、OOA 与设计文档。

---

## 1. 面向对象（UML）类图（Class Diagram）

**文字解析**：本图描述《云农智诊》问题域的核心静态结构。`User` 通过继承特化为农户与农技员（管理员亦可特化），体现角色差异；用户与诊断记录、帖子为一对多关联，帖子与评论为一对多关联，与“一人多次识别、一帖多评”的业务事实一致。类上方法对应识别、问诊、发帖、审核等关键职责，便于映射到实现层实体与服务。

```mermaid
classDiagram
  direction TB

  class User {
    +String id
    +String username
    +String passwordHash
    +String role
    +String status
    +login()
    +logout()
    +updateProfile()
  }

  class Farmer {
    +detectDisease()
    +viewResult()
    +askAI()
    +createHelpPost()
  }

  class Expert {
    +publishGuide()
    +answerQuestion()
  }

  class Admin {
    +manageUsers()
    +reviewPosts()
    +maintainDiseaseLib()
  }

  class DetectionRecord {
    +String id
    +String crop
    +String disease
    +float confidence
    +String imageUrl
    +DateTime createdAt
    +save()
    +getContext()
  }

  class Post {
    +String id
    +String title
    +String content
    +String tag
    +String auditStatus
    +DateTime createdAt
    +submit()
    +approve()
    +reject()
  }

  class Comment {
    +String id
    +String content
    +Boolean isProfessional
    +DateTime createdAt
    +publish()
  }

  User <|-- Farmer : 继承
  User <|-- Expert : 继承
  User <|-- Admin : 继承
  User "1" --> "*" DetectionRecord : 产生识别记录
  User "1" --> "*" Post : 发布帖子
  Post "1" --> "*" Comment : 包含评论
  User "1" --> "*" Comment : 撰写评论
```

**如何查看此图**：打开 [Mermaid Live](https://mermaid.live)，粘贴上述代码；若中文注释显示异常，可去掉关联线上的中文标签仅保留多重度。

---

## 2. 用例图（Use Case Diagram）

**文字解析**：本图从外部参与者视角界定系统功能边界。普通农户聚焦拍照识别、结果查看、AI 问诊与发帖求助；农技员承担科普发布与答疑；管理员负责用户管理、帖子审核与病害库维护。三类用例集互不越权，支撑后续权限矩阵与接口鉴权设计。

```mermaid
flowchart TB
  subgraph Actors["参与者"]
    direction TB
    F(["普通农户"])
    E(["农技员"])
    A(["系统管理员"])
  end

  subgraph UC["云农智诊系统"]
    direction TB
    UC1((拍照识别))
    UC2((查看结果))
    UC3((AI问诊))
    UC4((发帖求助))
    UC5((发布科普))
    UC6((解答疑问))
    UC7((用户管理))
    UC8((帖子审核))
    UC9((病害库维护))
  end

  F --- UC1
  F --- UC2
  F --- UC3
  F --- UC4
  E --- UC5
  E --- UC6
  E --- UC2
  A --- UC7
  A --- UC8
  A --- UC9
```

**如何查看此图**：复制到 Mermaid Live 渲染。因 Mermaid 无原生 UML 椭圆用例语法，本图用圆形节点近似用例、圆角节点表示参与者，语义与标准用例图一致。

---

## 3. 核心业务时序图（Sequence Diagram）— 病虫害识别完整流程

**文字解析**：时序图刻画识别主路径上对象间的消息顺序：前端先将图片上传至图床并获得 URL，再以 URL 调用云托管 NFNet-F1，避免大文件直传与跨域问题；推理返回病害 Key 后写入数据库，最后由前端展示结果。该顺序与真实实现（图床 → 推理 → 落库 → 展示）完全一致。

```mermaid
sequenceDiagram
  autonumber
  actor User as 前端用户(Web端)
  participant Bed as 图床服务(ImgBB)
  participant AI as 云托管AI服务(NFNet-F1)
  participant DB as 数据库

  User->>Bed: 上传叶片图片(multipart)
  Bed-->>User: 返回公网图片URL
  User->>AI: 提交图片URL请求推理
  AI->>AI: 下载图像并NFNet-F1推理
  AI-->>User: 返回病害Key与置信度
  User->>DB: 写入诊断记录(作物/病害/置信度/URL/时间)
  DB-->>User: 写入成功(记录ID)
  User->>User: 页面展示识别结果
```

**如何查看此图**：粘贴至 Mermaid Live；`autonumber` 便于在报告中按序号引用消息。若课程要求写“GitHub 图床”，可将参与者 `ImgBB` 改为 `GitHub/图床`，消息语义不变。

---

## 4. 业务活动图（Activity Diagram）— 农户获取防治建议

**文字解析**：活动图描述农户获取防治建议的完整控制流，包含“上传是否成功”与“置信度是否 ≥ 80%”两个判定分支。失败或低置信度均回流至重新拍照/上传，高置信度则展示结果并进入大模型问诊，最终输出防治建议，体现质量门禁与业务闭环。

```mermaid
flowchart TD
  Start([开始:农户进入识别页]) --> Choose[选择或拍摄叶片图片]
  Choose --> Up{图片上传是否成功?}
  Up -->|否| Err1[提示网络或格式错误]
  Err1 --> Choose
  Up -->|是| URL[获得图床图片URL]
  URL --> Call[请求云托管NFNet-F1推理]
  Call --> Conf{模型置信度 ≥ 80%?}
  Conf -->|否| Err2[提示重新拍照或更换清晰叶片图]
  Err2 --> Choose
  Conf -->|是| Show[展示病害名称与置信度]
  Show --> Ask[进入AI问诊并提交问题]
  Ask --> LLM[大模型生成防治建议]
  LLM --> ShowAdv[页面展示防治建议]
  ShowAdv --> End([结束])
```

**如何查看此图**：在 Mermaid Live 中打开；菱形为决策节点，可用于报告“正常流 / 异常流”说明。

---

## 5. 结构化数据流图（DFD 0 层）— 系统全局数据流转

**文字解析**：0 层 DFD 将系统分解为身份验证、图像识别处理、问诊请求处理等加工，外部实体为用户与大模型 API（识别侧亦可对接 NFNet 服务）。数据存储包括用户表、识别记录表与帖子表；数据流体现“鉴权读用户表、识别写记录表、问诊读记录并调大模型”的全局流转关系。

```mermaid
flowchart LR
  U((用户))
  LLM((大模型API))

  P1["1.0 身份验证"]
  P2["2.0 图像识别处理"]
  P3["3.0 问诊请求处理"]

  D1[("D1 用户表")]
  D2[("D2 识别记录表")]
  D3[("D3 帖子表")]

  U -->|账号凭证| P1
  P1 <-->|查询/校验用户| D1
  P1 -->|登录态/角色| U

  U -->|图片URL与识别请求| P2
  P2 -->|写入诊断记录| D2
  P2 -->|病害与置信度| U

  U -->|问诊问题与记录ID| P3
  P3 -->|读取识别上下文| D2
  P3 -->|Prompt请求| LLM
  LLM -->|防治建议文本| P3
  P3 -->|问诊答复| U

  U -->|发帖内容| D3
```

**如何查看此图**：复制到 Mermaid Live；圆角/圆形表示外部实体，矩形表示加工，圆柱表示数据存储。若需在图中显式画出 NFNet，可在 `P2` 右侧增加外部实体并连接“图片URL / 病害Key”数据流。

---

## 附：顶层 DFD（可选，报告补充）

**文字解析**：顶层图将整系统视为黑盒，仅展示用户、管理员、AI 模型与系统的输入输出，适合放在结构化分析报告开篇。

```mermaid
flowchart LR
  U((用户))
  A((管理员))
  AI((AI模型))
  S["云农智诊系统"]

  U -->|图片/问诊/帖子| S
  S -->|结果/建议/内容| U
  A -->|审核与管理指令| S
  S -->|待办与统计| A
  S -->|URL或Prompt| AI
  AI -->|识别或生成结果| S
```

**如何查看此图**：同上，粘贴至 Mermaid Live 即可。
