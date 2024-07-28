package dev.fizlrock.ears.domain.services;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAccumulator;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

/**
 * LocalFileStorage
 */
@Slf4j
@Component
public class LocalFileStorage implements FileStorage {

  @Value("${dev.fizlrock.LocalFileStorage.folderPath:./users_data}")
  private String rootFolderPath;

  private File rootFolder;

  @Value("${dev.fizlrock.LocalFileStorage.oneFileSizeLimit:5000}")
  private Long fileSizeLimit;

  /**
   * Лимит суммарного размера хранимых файлов
   */
  @Value("${dev.fizlrock.LocalFileStorage.storageSizeLimit:50000}")
  private Long storageSizeLimit;

  private AtomicLong storageSize = new AtomicLong();

  private Map<String, File> files_map = new ConcurrentHashMap<>();

  @PostConstruct
  void init() {
    log.debug("Для хранения файлов используется папка: {}", rootFolderPath);
    rootFolder = new File(rootFolderPath);

    if (rootFolder.mkdir())
      log.debug("Папка создана");
    else
      log.debug("Папка найдена");

    log.debug("Поиск файлов...");
    var files = rootFolder.listFiles();

    log.debug("Найдено {} файлов", files.length);
    Stream.of(files).forEach(f -> {
      files_map.put(f.getName(), f);
      storageSize.addAndGet(f.length());
    });

    log.debug("Суммарный размер хранилища: {} байт",
        storageSize.get());
  }

  @Override
  public FileUploader getFileUploader(String file_identifier, Long size) {

    if (size > fileSizeLimit) {
      // TODO file size check;
      throw new RuntimeException();
    }

    boolean haveVolume = false;

    if (storageSize.get() + size < storageSizeLimit) {
      var updated_size = storageSize.addAndGet(size);
      if (updated_size < storageSizeLimit)
        haveVolume = true;
      // Тут всё ещё может произойти ошибка
    }

    // TODO
    if (!haveVolume)
      throw new RuntimeException();

    var new_file = new File(rootFolder, file_identifier);
    try {
      new_file.createNewFile();
    } catch (IOException e) {
      // TODO
      throw new RuntimeException();
    }

    files_map.put(file_identifier, new_file);

    return new JavaFileUploader(new_file, size);
  }

  @Override
  public Stream<File> getAllFiles() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'getAllFiles'");
  }

}
