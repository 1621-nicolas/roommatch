package com.roommatch.service;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class ImageUrlPolicyTest {
    final ImageUrlPolicy production = new ImageUrlPolicy(false);
    @Test void validatesSchemeAuthorityCredentialsAndSizeWithoutFetchingContent() {
        for (String url : new String[]{"https://example.test/room.jpg", "https://example.test/a.png?v=1", "HTTPS://example.test/a"})
            assertThat(production.validate(url)).isEqualTo(url);
        for (String url : new String[]{null, "", "data:image/png;base64,abcd", "javascript:alert(1)", "file:///etc/passwd", "//example.test/x", "/image.png",
                "http://example.test/x", "https://user:password@example.test/x", "https://example.test/x#secret", "https://example.test:99999/a",
                "https://example.test/a b", "https://example.test/a\nb", "https://example.test/" + "x".repeat(240)})
            assertThatThrownBy(() -> production.validate(url)).as("URL %s", url).isInstanceOf(IllegalArgumentException.class);
        assertThat(production.validate("https://example.test/á.png")).isEqualTo("https://example.test/%C3%A1.png");
        assertThat(new ImageUrlPolicy(true).validate("http://localhost:8080/test.jpg")).isEqualTo("http://localhost:8080/test.jpg");
    }
    @Test void galleryInsertPositionAndQuotaHaveDefinedBoundaries() {
        assertThat(ImageUrlPolicy.position(null, 0)).isEqualTo(1);
        assertThat(ImageUrlPolicy.position(null, 4)).isEqualTo(5);
        assertThat(ImageUrlPolicy.position(1, 4)).isEqualTo(1);
        assertThatThrownBy(() -> ImageUrlPolicy.position(0, 4)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ImageUrlPolicy.position(3, 1)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ImageUrlPolicy.position(null, 5)).isInstanceOf(com.roommatch.exception.ConflictException.class);
    }
}
