package dev.scx.websocket.x;

/// WebSocket 客户端
///
/// 注意: ScxWebSocketClient 不继承 ScxHttpClient.
///
/// WebSocket 客户端在实现协议时确实需要通过 HTTP/1.1 发起 Upgrade 握手,
/// 但它对外表达的不是一个通用 HTTP 客户端, 而是一个专门用于建立 WebSocket 连接的客户端入口.
///
/// 换句话说, WebSocketClient 与 HttpClient 的关系不是 is-a, 而是 uses-a / has-a:
///
/// - ScxHttpClient 用于发起普通 HTTP 请求;
/// - ScxWebSocketClient 用于创建 WebSocket 握手请求, 并最终升级为 WebSocket 连接;
/// - WebSocket 握手请求和握手响应本身可以视为特殊的 HTTP 请求 / 响应,
///   因此 ScxWebSocketClientHandshakeRequest、ScxWebSocketServerHandshakeResponse
///   这类协议阶段对象可以继承对应的 HTTP 请求 / 响应接口;
/// - 但 ScxWebSocketClient 本身是客户端入口对象, 不是通用 HTTP 客户端.
///
/// 如果 ScxWebSocketClient 继承 ScxHttpClient,
/// 它就会暴露 request() 普通 HTTP 客户端入口,
/// 从而让使用者误以为 WebSocketClient 可以或应该被当作通用 HttpClient 使用.
/// 这会模糊 API 职责边界.
///
/// 因此, ScxWebSocketClient 只暴露 handshakeRequest(),
/// 内部实现可以组合 ScxHttpClient 完成实际的 HTTP Upgrade 握手.
///
/// @author scx567888
public interface ScxWebSocketClient {

    ScxWebSocketClientHandshakeRequest handshakeRequest();

}
