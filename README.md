# Raise Packet Limit（raisepacketlimit）

一个 NeoForge 1.21.1 模组：解除 Minecraft 原版网络包 2 MiB 大小硬上限，并整合 Packet Fixer 的 NBT、自定义 Payload、区块数据、字符串与超时兼容功能。默认使用 **64 MiB** 的受限配置。

适用 NeoForge **21.1.243**（`net.neoforged:neoforge` 版本范围 `[21.1.243,)`）。

## 解决什么问题

服务端向客户端发送大型数据包（典型场景：AE2 应用能源2 无线样板/合成终端，含数千样板）时，
包体积超过 2 MiB，玩家被踢出，报错形如：

```
您已被踢出：Internal Exception: io.netty.handler.codec.EncoderException:
Failed to encode packet 'clientbound/minecraft:container_set_content'
```

## 根因（已对 21.1.247 实际字节码核实）

1.21.1 的传输管线对单包大小有四道硬编码闸门（全部位于 `net.minecraft.network`）：

| 闸门 | 原版硬编码 | 本模组改为 |
|---|---|---|
| `Varint21LengthFieldPrepender.encode`（出站帧长） | 21 位 varint，帧长 ≤ 2097151 | 5 字节 varint（覆盖 2³⁵-1 字节） |
| `Varint21FrameDecoder`（入站帧长） | 21 位 varint，3 字节缓冲 | 5 字节 varint |
| `CompressionEncoder.encode`（压缩前输入大小） | 8388608（8 MiB） | `maxPacketSize`（默认 67108864） |
| `CompressionDecoder.decode`（声明解压大小） | 8388608（8 MiB） | `maxPacketSize`（默认 67108864） |

> 注意：PacketFixer 3.3.1 无效的根本原因——它没有任何针对上述帧长度/压缩大小类的 Mixin，
> 并且它只管自定义 payload 与 NBT 等，管不到原版包的这四道闸。
>
> 另外：**1.21.1 的 `PacketEncoder` / `PacketDecoder` 本身并没有 2097152 检查**
> （那是 1.20.x 的行为，1.21 重构网络层后检查移到了帧长度与压缩层）。
> 真正踢人的是 `Varint21LengthFieldPrepender` 抛出的
> `EncoderException("Packet too large: size X is over 8")`
> （该异常被 `IdDispatchCodec` 包装成 "Failed to encode packet ..."）。
> NeoForge 的 `GenericPacketSplitter` 只拆分 mod 自定义 payload，原版包（如
> `container_set_content`）从不拆分，因此必须放宽原版限制本身。

## 安装

**服务端和客户端都必须安装本模组**，且所有网络大小配置两边要一致。此模组已整合 Packet Fixer，**不要再安装 Packet Fixer**，以避免重复 Mixin 注入：

1. 把 `raisepacketlimit-1.1.0.jar` 放入服务端的 `mods/` 目录。
2. 把同一个 jar 放入客户端的 `mods/` 目录（启动器需使用 NeoForge 21.1.243 及以上）。
3. 启动两端，确认配置生成：`config/raisepacketlimit.toml`。
4. 修改配置后**必须重启**两端才生效。
5. 兼容 Fabric/Paper 等其他加载器的同类修复可并存；NeoForge 环境中不应再同时安装 Packet Fixer。

## 配置

文件：`config/raisepacketlimit.toml`。默认值是安全上限，不采用 Packet Fixer 的无限制默认行为；过高设置会增加恶意客户端导致内存耗尽的风险。

```toml
[general]
# 单个网络包的最大字节数（压缩前，双向）。默认 67108864 = 64 MiB。
# 范围：2097152（2 MiB，原版值）~ 536870912（512 MiB）。
maxPacketSize = 67108864

# Packet Fixer 兼容项：默认 64 MiB。
maxNbtBytes = 67108864
maxCustomPayloadBytes = 67108864
maxChunkDataBytes = 67108864

# 默认维持原版 32767；只有确认相关模组需要时再提高。
maxStringLength = 32767

# 大型注册表或数据包传输时的连接/心跳超时，单位秒。
connectionTimeoutSeconds = 120

# 危险：将 NBT 实例配额替换为 maxNbtBytes，默认关闭。
forceUnlimitedNbt = false
```

## 验证命令（可选）

任意玩家（无需管理员权限，只作用于自己）执行：`/testbigpacket` 或 `/testbigpacket 16`（发送约 16 MiB 的测试包）

- 命令向执行者发送一个**原版** `container_set_content` 数据包（其内容为若干携带大 NBT 的物品，
  总大小约等于请求的 MiB 数），发送后立即恢复真实背包显示。
- 原版包不会被 GenericPacketSplitter 拆分，因此这是对 2 MiB 闸门的真实测试：
  - 安装本模组：包正常到达，玩家不被踢。
  - 未安装/配置未生效：玩家被踢出，报错与 AE2 终端场景一致。

## 错误现象与解决对照表

| 现象 | 原因 | 解决 |
|---|---|---|
| 打开 AE2 无线样板/合成终端被踢：`EncoderException: Failed to encode packet 'clientbound/minecraft:container_set_content'` | 包 > 2 MiB，21 位帧长 varint 放不下 | 两端都装本模组并重启 |
| 客户端收到大包被踢：`DecoderException` / `CorruptedFrameException: length wider than 21-bit` | 客户端未装本模组（或配置小于服务端） | 客户端安装本模组，配置与服务端一致 |
| 服务端报 `Packet too big (is X, should be less than 8388608)` | `CompressionEncoder` 8 MiB 输入上限 | 已由本模组解除（受 `maxPacketSize` 控制） |
| 两端都装了仍被踢 | 配置不一致 / 未重启 / 装了但被其他代理（如 Velocity）拦截 | 对齐两端配置、重启；Velocity 的 `max-packet-size` 也调到 ≥ 67108864 |
| 与 PacketFixer 同装 | 重复修改相同网络类 | 删除 PacketFixer，仅保留本模组；本模组已包含其 NeoForge 1.21.x 功能 |

## 构建

需要 JDK 21。命令行执行：

```bat
gradlew build
```

产物：`build/libs/raisepacketlimit-1.1.0.jar`

## 技术实现

- 18 个 Mixin（sponge-mixin，NeoForge 内置），单个 jar 同时适用于客户端和服务端：
  - `CompressionEncoderMixin`、`CompressionDecoderMixin`：`@ModifyConstant` 替换 `8388608` 字面量为配置值。
  - `Varint21LengthFieldPrependerMixin`、`Varint21FrameDecoderMixin`：`@ModifyConstant` 替换 `3` 字面量为 `5`。
- 常量均为编译期内联字面量，故按方法逐个 `@ModifyConstant` 修改，不依赖字段引用。
- 配置通过 `ModConfigSpec`（`mods.toml` 注册，`config/raisepacketlimit.toml`），
  Mixin 读取静态持有类 `PacketSizeLimits` 的值（默认即 64 MiB，配置加载后同步）。
- `mixin 配置 defaultRequire = 0`：个别环境目标缺失时仅告警，不崩溃。
- 四个核心帧/压缩 Mixin 设置 `priority = 1100`，确保其配置的帧大小与压缩上限保持一致。
- **无 refmap**：NeoForge 1.21 开发与运行环境均为 Mojang 官方映射（mojmap），
  mixin 目标名处处一致，refmap 无意义（见 NeoForge 官方 1.21 迁移说明），
  因此构建不启用 mixin 注解处理器，jar 内不包含 refmap——这是正常且推荐的做法。
- 另整合 Packet Fixer 的 14 个 NeoForge 1.21.x Mixin：自定义 Payload、NBT 读取、区块包、字符串、连接读超时、登录/心跳超时与 VarInt/VarLong 守卫。VarInt 和 VarLong 保持协议规定的 5/10 字节宽度，不能安全地“无限扩大”。
- Packet Fixer 的 MIT 版权和许可声明将随 JAR 一起分发，源文件位于 [`src/main/resources/THIRD_PARTY_NOTICES.md`](src/main/resources/THIRD_PARTY_NOTICES.md)。

## 许可

MIT License
