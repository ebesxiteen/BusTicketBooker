package vn.zalopay.crypto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.nio.charset.StandardCharsets;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.Test;

class HMACUtilTest {

    private String expectedHmacSha256Hex(String key, String data) throws Exception {
        Mac mac = Mac.getInstance(HMACUtil.HMACSHA256);
        mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), HMACUtil.HMACSHA256));
        byte[] digest = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return HexStringUtil.byteArrayToHexString(digest);
    }

    @Test
    void hmacBase64EncodeMatchesStandardMacImplementation() throws Exception {
        String key = "secret-key";
        String payload = "sample-payload";

        String expectedHex = expectedHmacSha256Hex(key, payload);
        String expectedBase64 = java.util.Base64.getEncoder().encodeToString(
                HexStringUtil.hexStringToByteArray(expectedHex));

        String actual = HMACUtil.HMacBase64Encode(HMACUtil.HMACSHA256, key, payload);

        assertEquals(expectedBase64, actual);
    }

    @Test
    void hmacHexStringEncodeReturnsConsistentDigest() throws Exception {
        String key = "another-secret";
        String payload = "data";

        String expected = expectedHmacSha256Hex(key, payload);

        String actual = HMACUtil.HMacHexStringEncode(HMACUtil.HMACSHA256, key, payload);

        assertEquals(expected, actual);
    }

    @Test
    void hmacEncodingReturnsNullWhenAlgorithmIsUnsupported() {
        String base64 = HMACUtil.HMacBase64Encode("unknown", "key", "data");
        String hex = HMACUtil.HMacHexStringEncode("unknown", "key", "data");

        assertNull(base64);
        assertNull(hex);
    }
}
