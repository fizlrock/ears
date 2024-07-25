package dev.fizlrock.ears.services;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class LocalAudioStorage {

  @Value("${dev.fizlrock.ears.services.LocalAudioStorage.audioFolder:audio_storage}")
  private String pathToRootFolder;

  private File rootFolder;

  @PostConstruct
  void init() {
    rootFolder = new File(pathToRootFolder);
    if (!rootFolder.exists()) {
      log.warn("Папка не найдена, создание");
      rootFolder.mkdir();
    }
    log.debug("Использование папки: {}", rootFolder);
  }

  private Map<String, OutputStream> aliveOutputStreams = new HashMap<>();

  public void openAudio(String uuid) {
    var file = new File(rootFolder, uuid);

    try {
      file.createNewFile();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    OutputStream stream;

    try {
      stream = new FileOutputStream(file);
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);
    }
    aliveOutputStreams.put(uuid, stream);
  }

  public void writeBytes(String uuid, byte[] bytes) {
    var stream = aliveOutputStreams.get(uuid);
    try {
      stream.write(bytes);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void closeAudio(String uuid) {
    try {
      aliveOutputStreams.get(uuid).close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

  }

}
