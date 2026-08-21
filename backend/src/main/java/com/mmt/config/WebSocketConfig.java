package com.mmt.config;
import org.springframework.context.annotation.Configuration; import org.springframework.web.socket.config.annotation.*; import org.springframework.web.socket.handler.TextWebSocketHandler; import org.springframework.web.socket.WebSocketSession; import org.springframework.web.socket.TextMessage; import org.springframework.web.socket.CloseStatus; import org.springframework.web.socket.WebSocketHandler; import java.util.Set; import java.util.concurrent.CopyOnWriteArraySet;
@Configuration @EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
  public static final Set<WebSocketSession> SESSIONS=new CopyOnWriteArraySet<>();
  @Override public void registerWebSocketHandlers(WebSocketHandlerRegistry r){ r.addHandler(new LiveUpdateHandler(),"/ws/updates").setAllowedOriginPatterns("*"); }
  static class LiveUpdateHandler extends TextWebSocketHandler { @Override public void afterConnectionEstablished(WebSocketSession s){ SESSIONS.add(s); } @Override public void afterConnectionClosed(WebSocketSession s,CloseStatus c){ SESSIONS.remove(s); } }
}
