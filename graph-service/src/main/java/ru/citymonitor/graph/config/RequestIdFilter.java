package ru.citymonitor.graph.config;
import jakarta.servlet.*;import jakarta.servlet.http.*;import org.slf4j.MDC;import org.springframework.stereotype.Component;import org.springframework.web.filter.OncePerRequestFilter;import java.io.IOException;import java.util.UUID;
@Component public class RequestIdFilter extends OncePerRequestFilter {
 protected void doFilterInternal(HttpServletRequest request,HttpServletResponse response,FilterChain chain)throws ServletException,IOException {String id=request.getHeader("X-Request-ID");if(id==null||id.isBlank()||id.length()>100)id=UUID.randomUUID().toString();MDC.put("requestId",id);response.setHeader("X-Request-ID",id);try{chain.doFilter(request,response);}finally{MDC.remove("requestId");}}
}
