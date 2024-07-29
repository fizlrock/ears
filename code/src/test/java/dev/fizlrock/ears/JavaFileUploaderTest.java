package dev.fizlrock.ears;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.FileInputStream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import dev.fizlrock.ears.domain.services.FileUploader;
import dev.fizlrock.ears.domain.services.JavaFileUploader;

@DisplayName("JUnit 5 Example")
public class JavaFileUploaderTest {

  @Test
  void successCreateFileUploaderAndUploadData() throws Exception {

    File f = File.createTempFile("javatests", ".tmp");
    long sizeLimit = 40l;
    String identifier = f.getName();

    FileUploader uploader = new JavaFileUploader(f, sizeLimit, identifier);

    assertEquals(identifier, uploader.getIdentifier());

    byte[] data = new byte[10];
    for (int i = 0; i < 4; i++)
      uploader.writeBytes(data);
    uploader.close();

    uploader.waitEndOfUploading();

    var fis = new FileInputStream(f);
    var readed = fis.readAllBytes();

    for (int i = 0; i < 40; i++)
      assertEquals(data[0], readed[i]);

  }

  @Test
  void writeToMuchData() throws Exception {

    File f = File.createTempFile("javatests", ".tmp");
    long sizeLimit = 40l;
    String identifier = f.getName();

    FileUploader uploader = new JavaFileUploader(f, sizeLimit, identifier);

    assertEquals(identifier, uploader.getIdentifier());

    byte[] datap1 = new byte[38];
    byte[] datap2 = new byte[3];

    uploader.writeBytes(datap1);

    assertThrows(IllegalArgumentException.class, () -> uploader.writeBytes(datap2));
  }

}
