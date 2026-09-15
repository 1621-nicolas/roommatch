package com.roommatch.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.roommatch.model.Usuario;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import java.time.Clock;
import java.util.List;
import static org.assertj.core.api.Assertions.*;

class AbuseProtectionFilterTest {
    private final AbuseProtectionFilter filter=new AbuseProtectionFilter(new RequestLimiter(Clock.systemUTC()),new SecurityErrorWriter(new ObjectMapper()),new ClientIpResolver(""));
    @AfterEach void clear() { SecurityContextHolder.clearContext(); }
    @Test void reportsUseActualEndpointAndReturnRetryAfter() throws Exception {
        var user=new Usuario(); user.setIdUsuario(1);
        SecurityContextHolder.getContext().setAuthentication(UsernamePasswordAuthenticationToken.authenticated(user,null,List.of()));
        for(int i=0;i<11;i++) {
            var req=new MockHttpServletRequest("POST","/api/reportes/usuarios/2");
            var res=new MockHttpServletResponse();
            filter.doFilter(req,res,(a,b)->{});
            assertThat(res.getStatus()).isEqualTo(i<10?200:429);
            if(i==10) assertThat(res.getHeader("Retry-After")).isNotBlank();
        }
    }
    @Test void forgedForwardedIpCannotResetRegistrationQuota() throws Exception {
        for(int i=0;i<6;i++) {
            var req=new MockHttpServletRequest("POST","/api/auth/register");
            req.addHeader("X-Forwarded-For","192.0.2."+i);
            req.addHeader("X-Real-IP","192.0.2."+i);
            var res=new MockHttpServletResponse();
            filter.doFilter(req,res,(a,b)->{});
            assertThat(res.getStatus()).isEqualTo(i<5?200:429);
        }
    }

    @Test void trustedProxySeparatesClientsButStillLimitsRepeatedRequests() throws Exception {
        var proxyFilter = new AbuseProtectionFilter(new RequestLimiter(Clock.systemUTC()), new SecurityErrorWriter(new ObjectMapper()), new ClientIpResolver("10.0.0.10"));
        for (int i = 0; i < 6; i++) {
            var req = new MockHttpServletRequest("POST", "/api/auth/register"); req.setRemoteAddr("10.0.0.10"); req.addHeader("X-Real-IP", "192.0.2." + i);
            var res = new MockHttpServletResponse(); proxyFilter.doFilter(req, res, (a,b) -> {});
            assertThat(res.getStatus()).isEqualTo(200);
        }
        for (int i = 0; i < 5; i++) {
            var req = new MockHttpServletRequest("POST", "/api/auth/register"); req.setRemoteAddr("10.0.0.10"); req.addHeader("X-Real-IP", "192.0.2.0");
            var res = new MockHttpServletResponse(); proxyFilter.doFilter(req, res, (a,b) -> {});
            assertThat(res.getStatus()).isEqualTo(i < 4 ? 200 : 429);
        }
    }
}
