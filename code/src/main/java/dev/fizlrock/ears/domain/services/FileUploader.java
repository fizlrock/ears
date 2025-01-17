package dev.fizlrock.ears.domain.services;

/**
 * Функционал загрузки файлов
 */
public interface FileUploader {

  String getIdentifier();

  /**
   * Принимает данные и записывает в файл. Метод асинхронный.
   * <p>
   * Может выбросить исключение если:
   * <ol>
   * <li>Передан пустой массив
   * <li>Передано слишком много данных
   * <li>Ошибка при записи на диск
   * </ol>
   * 
   * @param bytes
   */
  void writeBytes(byte[] bytes);

  /**
   * Сигнализирует об окончании передачи данных.
   * Может выбросить исключение если:
   * <ol>
   * <li>Переданы не все данные
   * <li>Ошибка при записи на диск
   * </ol>
   * 
   */
  void close();

  void waitEndOfUploading() throws InterruptedException;

}
