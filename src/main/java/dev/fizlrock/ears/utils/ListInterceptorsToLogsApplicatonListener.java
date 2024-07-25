package dev.fizlrock.ears.utils;

import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.interceptor.GlobalServerInterceptorRegistry;

@Component
@Slf4j
public class ListInterceptorsToLogsApplicatonListener implements ApplicationListener<ContextRefreshedEvent> {

  @Autowired
  GlobalServerInterceptorRegistry registry;

  @Override
  public void onApplicationEvent(ContextRefreshedEvent event) {
    var report = registry.getServerInterceptors().stream()
    .map(Object::toString)
    .collect(Collectors.joining("\n"));
    log.info("finded interceptors:\n {}", report);
  }

}
