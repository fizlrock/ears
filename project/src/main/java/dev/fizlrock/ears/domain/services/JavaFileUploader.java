package dev.fizlrock.ears.domain.services;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.atomic.AtomicLong;

import lombok.extern.slf4j.Slf4j;

/**
 * JavaFileUploader
 */
@Slf4j
public class JavaFileUploader implements FileUploader {

  final File file;
  final Long size_limit;
  Long writedBytes;
  AtomicLong notWritedChunks;
  AsynchronousFileChannel channel;

  CompletionHandler<Integer, ByteBuffer> handler;

  public JavaFileUploader(final File file, final Long size_limit) throws FileNotFoundException {
    this.size_limit = size_limit;
    this.file = file;
    writedBytes = 0l;
    notWritedChunks = new AtomicLong(0);
    handler = new Handler();

    RandomAccessFile nio_file = new RandomAccessFile(file, "rw");
    try {
      channel = AsynchronousFileChannel.open(file.toPath(), StandardOpenOption.WRITE);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

  }

  /**
   * Потоконебезопасный!
   */
  @Override
  public void writeBytes(byte[] bytes) {

    long new_size = writedBytes + bytes.length;
    if (size_limit < new_size) {
      // Тут закрыть поток
      // TODO
      throw new RuntimeException();
    }

    ByteBuffer buffer = ByteBuffer.wrap(bytes);
    notWritedChunks.incrementAndGet();
    channel.write(buffer, writedBytes, buffer, handler);

    writedBytes = new_size;

  }

  @Override
  public void close() {
    if (size_limit != writedBytes) {
      // Тут закрыть поток
      // TODO
      throw new RuntimeException("Ожидаются ещё данные");
    }

    throw new UnsupportedOperationException("Unimplemented method 'close'");
  }

  private class Handler implements CompletionHandler<Integer, ByteBuffer> {

    @Override
    public void completed(Integer bytes_count, ByteBuffer buffer) {
      log.debug("chunk write on disk. size: {}", bytes_count);
      var chunks = notWritedChunks.decrementAndGet();
      if (writedBytes == size_limit && chunks == 0)
        try {
          channel.close();
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
    }

    @Override
    public void failed(Throwable e, ByteBuffer buffer) {
      throw new RuntimeException(e);
    }

  }

}