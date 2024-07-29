package dev.fizlrock.ears.domain.services;

import java.io.File;
import java.util.stream.Stream;

/**
 * FileStorage
 */
public interface FileStorage {

  /**
   * Создание нового файла с указанным идентификатором.
   * <p>
   * Так же создается экземпляр
   * {@link FileUploader}.
   * 
   * Может выбросить исключение если:
   * <ol>
   * <li>Если не достаточно места
   * <li>file_identifiers - null
   * <li>Идентификтор не уникален
   * 
   * </ol>
   * 
   * @param file_identifier
   * @return
   */
  void createAndOpenFile(String file_identifier, Long file_size);

  void closeFile(String file_identifier);

  void writeBytes(String file_identifier, byte[] bytes);


}
