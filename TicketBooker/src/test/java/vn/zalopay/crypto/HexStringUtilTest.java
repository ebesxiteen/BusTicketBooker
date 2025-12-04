package vn.zalopay.crypto;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class HexStringUtilTest {

    @Test
    void byteArrayToHexStringConvertsEachByteToLowercaseHex() {
        byte[] input = new byte[] {0x00, 0x0f, (byte) 0xff};

        String hex = HexStringUtil.byteArrayToHexString(input);

        assertEquals("000fff", hex);
    }

    @Test
    void hexStringToByteArrayHandlesMixedCaseInput() {
        String hex = "0A1b2C";

        byte[] result = HexStringUtil.hexStringToByteArray(hex);

        assertArrayEquals(new byte[] {0x0a, 0x1b, 0x2c}, result);
    }
}
