package org.vividus.util.qrCode;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.vividus.util.ResourceUtils;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class QRCodeReaderTest {

    private static final Path QR_CODE_PATH = loadResource("qrCode.png");
    private static final Path IMAGE_PATH = loadResource("blackSquare168x168.png");
    private static final Path QR_CODE_INVALID_PATH = Path.of("qrCode2.png");
    private static final String EXPECTED_VALUE = "https://github.com/vividus-framework/vividus";

    private QRCodeReader qrCodeReader;

    @BeforeEach
    void beforeEach()
    {
        qrCodeReader = new QRCodeReader();
    }

    @Test
    void shouldReadQRCode() throws IOException
    {
        String actualQRCodeValue = qrCodeReader.readQRCode(QR_CODE_PATH);
        assertEquals(EXPECTED_VALUE, actualQRCodeValue);
    }

    @Test
    void shouldReturnEmptyStringInCaseOfQRCodeAbsents() throws IOException
    {
        String actualQRCodeValue = qrCodeReader.readQRCode(IMAGE_PATH);
        assertEquals("", actualQRCodeValue);
    }

    @Test
    void shouldThrowIOExceptionInCaseOfFileAbsents()
    {
        assertThrows(IOException.class, () ->
                qrCodeReader.readQRCode(QR_CODE_INVALID_PATH));
    }

    private static Path loadResource(String filePath) {
        return ResourceUtils.loadFile(QRCodeReader.class, filePath).toPath();
    }
}
