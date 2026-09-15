package com.roommatch.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.MockHttpServletRequest;
import static org.assertj.core.api.Assertions.*;

class ClientIpResolverTest {
    @Test void clientCannotChooseItsOwnQuotaKey() {
        var request = new MockHttpServletRequest(); request.setRemoteAddr("192.0.2.10");
        request.addHeader("X-Real-IP", "198.51.100.20"); request.addHeader("X-Forwarded-For", "203.0.113.30");
        assertThat(new ClientIpResolver("").resolve(request)).isEqualTo("192.0.2.10");
        assertThat(new ClientIpResolver("10.0.0.10").resolve(request)).isEqualTo("192.0.2.10");
    }
    @Test void trustedPeerMaySupplyOneLiteralAddress() {
        var resolver = new ClientIpResolver("10.0.0.10,2001:db8::1");
        var request = new MockHttpServletRequest(); request.setRemoteAddr("10.0.0.10"); request.addHeader("X-Real-IP", "192.0.2.1");
        assertThat(resolver.resolve(request)).isEqualTo("192.0.2.1");
        request.addHeader("X-Real-IP", "192.0.2.2");
        assertThat(resolver.resolve(request)).isEqualTo("10.0.0.10");
        request = new MockHttpServletRequest(); request.setRemoteAddr("2001:db8::1"); request.addHeader("X-Real-IP", "2001:db8::2");
        assertThat(resolver.resolve(request)).isEqualTo("2001:db8:0:0:0:0:0:2");
    }
    @ParameterizedTest @ValueSource(strings = {"example.test", "192.0.2.1,198.51.100.2", "127.1", "0x7f000001", "192.000.2.1", "256.1.1.1", "1.2.3.4:5000", "[::1]", "fe80::1%eth0", "2001:db8:::2", "::ffff:127.000.0.1", ""})
    void malformedProxyHeaderFallsBackToPeer(String header) {
        var request = new MockHttpServletRequest(); request.setRemoteAddr("10.0.0.10"); request.addHeader("X-Real-IP", header);
        assertThat(new ClientIpResolver("10.0.0.10").resolve(request)).isEqualTo("10.0.0.10");
    }
    @Test void configurationRejectsHostnamesWildcardsAndRanges() {
        for (String config : new String[]{"nginx", "*", "0.0.0.0/0", "10.0.0.0/24"})
            assertThatThrownBy(() -> new ClientIpResolver(config)).isInstanceOf(IllegalStateException.class);
    }
}
