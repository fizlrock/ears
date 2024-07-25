package dev.fizlrock.ears.controllers;

import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCall.Listener;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;

/**
 * FirstInterceptor
 */
@Slf4j
@GrpcGlobalServerInterceptor
public class FirstInterceptor implements ServerInterceptor {

  @Override
  public <ReqT, RespT> Listener<ReqT> interceptCall(
      ServerCall<ReqT, RespT> call,
      Metadata headers,
      ServerCallHandler<ReqT, RespT> next) {

    log.info("header received from client:" + headers);
    log.info("call received from client:" + call.getAuthority());
    log.info("call received from client:" + call.getAttributes());
    log.info("call received from client:" + call.getMethodDescriptor());
    log.info("call received from client:" + call.getSecurityLevel());



    return next.startCall(call, headers);

  }

}
