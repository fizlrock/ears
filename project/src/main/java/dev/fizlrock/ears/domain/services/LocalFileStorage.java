package dev.fizlrock.ears.domain.services;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * LocalFileStorage
 */
@Slf4j
@Component
public class LocalFileStorage implements FileStorage {

  @Autowired
  LocalFileStorage instance;

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
  private Map<String, FileUploader> uploaders = new ConcurrentHashMap<>();

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
  public void createAndOpenFile(String file_identifier, Long size) {
    log.debug("Создание файла {} с ограничением по размеру {}", file_identifier, size);

    if (size > fileSizeLimit) {
      throw new IllegalArgumentException("Слишком большой файл");
    }

    if (files_map.containsKey(file_identifier))
      throw new IllegalArgumentException("Идентификатор не уникальный");

    boolean haveVolume = false;

    if (storageSize.get() + size < storageSizeLimit) {
      var updated_size = storageSize.addAndGet(size);
      if (updated_size < storageSizeLimit)
        haveVolume = true;
      // TODO
      // Тут всё ещё может произойти ошибка
    }

    if (!haveVolume)
      throw new RuntimeException("Недостаточно места(ограничение)");

    log.debug("Места хватает. Текущий размер хранилища: {}", storageSize.get());

    var new_file = new File(rootFolder, file_identifier);
    try {
      new_file.createNewFile();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }

    log.debug("Файл {} успешно создан", file_identifier);
    files_map.put(file_identifier, new_file);

    FileUploader uploader;

    try {
      uploader = new JavaFileUploader(new_file, size, file_identifier);
    } catch (FileNotFoundException e) {
      throw new UncheckedIOException(e);
    }
    log.debug("Загрузчик для файла {} успешно открыт", file_identifier);
    uploaders.put(file_identifier, uploader);
  }

  @Override
  public void closeFile(String file_identifier) {
    FileUploader uploader = uploaders.remove(file_identifier);
    if (uploader == null)
      throw new IllegalStateException("Загрузчик с переданным идентификатором не найден");

    try {
      uploader.close();
    } catch (Exception e) {
      log.error("Ошибка закрытия загрузчика {}. Удаления файла...", file_identifier);
      File f = files_map.remove(file_identifier);
      if (f == null)
        throw new NullPointerException("File == null???");
      if (f.delete())
        log.debug("Файл {} успешно удален", file_identifier);
      else
        log.warn("Ошибка удаление файла: {}", file_identifier);

      throw e;
    }
  }

  @Override
  public void writeBytes(String file_identifier, byte[] bytes) {

    FileUploader uploader = uploaders.get(file_identifier);
    if (uploader == null)
      throw new IllegalArgumentException("Загрузчик с переданным идентификатором не найден");

    try {
      uploader.writeBytes(bytes);
    } catch (Exception e) {
      instance.closeFile(file_identifier);
      throw e;
    }

  }

}
