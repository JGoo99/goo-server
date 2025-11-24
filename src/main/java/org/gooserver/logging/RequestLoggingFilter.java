package org.gooserver.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    private final LogFileService logFileService;

    public RequestLoggingFilter(LogFileService logFileService) {
        this.logFileService = logFileService;
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {

        long start = System.currentTimeMillis();
        int status;

        try {
            filterChain.doFilter(request, response);
            status = response.getStatus();
        } catch (Exception ex) {
            status = 500;
            logFileService.writeLine(buildLogLine(request, status, System.currentTimeMillis() - start, ex));
            throw ex;
        }

        long durationMs = System.currentTimeMillis() - start;
        logFileService.writeLine(buildLogLine(request, status, durationMs, null));
    }

    private String buildLogLine(HttpServletRequest request, int status, long durationMs, Exception ex) {
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String query = request.getQueryString();
        String fullPath = (query == null || query.isBlank()) ? uri : (uri + "?" + query);

        String clientIp = getClientIp(request);
        String userAgent = request.getHeader("User-Agent");
        if (userAgent == null) {
            userAgent = "-";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("method=").append(method)
            .append(" path=").append(fullPath)
            .append(" status=").append(status)
            .append(" durationMs=").append(durationMs)
            .append(" clientIp=").append(clientIp)
            .append(" userAgent=\"").append(userAgent).append("\"");

        if (ex != null) {
            sb.append(" error=").append(ex.getClass().getSimpleName());
        }

        return sb.toString();
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp;
        }
        return request.getRemoteAddr();
    }
}
