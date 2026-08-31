package org.apache.tika.grpc.v2;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 */
@io.grpc.stub.annotations.GrpcGenerated
public final class TikaV2Grpc {

  private TikaV2Grpc() {}

  public static final java.lang.String SERVICE_NAME = "org.apache.tika.grpc.v2.TikaV2";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<org.apache.tika.grpc.v2.FetchAndParseRequest,
      org.apache.tika.grpc.v2.FetchAndParseReply> getFetchAndParseMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "FetchAndParse",
      requestType = org.apache.tika.grpc.v2.FetchAndParseRequest.class,
      responseType = org.apache.tika.grpc.v2.FetchAndParseReply.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<org.apache.tika.grpc.v2.FetchAndParseRequest,
      org.apache.tika.grpc.v2.FetchAndParseReply> getFetchAndParseMethod() {
    io.grpc.MethodDescriptor<org.apache.tika.grpc.v2.FetchAndParseRequest, org.apache.tika.grpc.v2.FetchAndParseReply> getFetchAndParseMethod;
    if ((getFetchAndParseMethod = TikaV2Grpc.getFetchAndParseMethod) == null) {
      synchronized (TikaV2Grpc.class) {
        if ((getFetchAndParseMethod = TikaV2Grpc.getFetchAndParseMethod) == null) {
          TikaV2Grpc.getFetchAndParseMethod = getFetchAndParseMethod =
              io.grpc.MethodDescriptor.<org.apache.tika.grpc.v2.FetchAndParseRequest, org.apache.tika.grpc.v2.FetchAndParseReply>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "FetchAndParse"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.apache.tika.grpc.v2.FetchAndParseRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.apache.tika.grpc.v2.FetchAndParseReply.getDefaultInstance()))
              .setSchemaDescriptor(new TikaV2MethodDescriptorSupplier("FetchAndParse"))
              .build();
        }
      }
    }
    return getFetchAndParseMethod;
  }

  private static volatile io.grpc.MethodDescriptor<org.apache.tika.grpc.v2.FetchAndParseRequest,
      org.apache.tika.grpc.v2.FetchAndParseReply> getFetchAndParseServerSideStreamingMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "FetchAndParseServerSideStreaming",
      requestType = org.apache.tika.grpc.v2.FetchAndParseRequest.class,
      responseType = org.apache.tika.grpc.v2.FetchAndParseReply.class,
      methodType = io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
  public static io.grpc.MethodDescriptor<org.apache.tika.grpc.v2.FetchAndParseRequest,
      org.apache.tika.grpc.v2.FetchAndParseReply> getFetchAndParseServerSideStreamingMethod() {
    io.grpc.MethodDescriptor<org.apache.tika.grpc.v2.FetchAndParseRequest, org.apache.tika.grpc.v2.FetchAndParseReply> getFetchAndParseServerSideStreamingMethod;
    if ((getFetchAndParseServerSideStreamingMethod = TikaV2Grpc.getFetchAndParseServerSideStreamingMethod) == null) {
      synchronized (TikaV2Grpc.class) {
        if ((getFetchAndParseServerSideStreamingMethod = TikaV2Grpc.getFetchAndParseServerSideStreamingMethod) == null) {
          TikaV2Grpc.getFetchAndParseServerSideStreamingMethod = getFetchAndParseServerSideStreamingMethod =
              io.grpc.MethodDescriptor.<org.apache.tika.grpc.v2.FetchAndParseRequest, org.apache.tika.grpc.v2.FetchAndParseReply>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "FetchAndParseServerSideStreaming"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.apache.tika.grpc.v2.FetchAndParseRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.apache.tika.grpc.v2.FetchAndParseReply.getDefaultInstance()))
              .setSchemaDescriptor(new TikaV2MethodDescriptorSupplier("FetchAndParseServerSideStreaming"))
              .build();
        }
      }
    }
    return getFetchAndParseServerSideStreamingMethod;
  }

  private static volatile io.grpc.MethodDescriptor<org.apache.tika.grpc.v2.FetchAndParseRequest,
      org.apache.tika.grpc.v2.FetchAndParseReply> getFetchAndParseBiDirectionalStreamingMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "FetchAndParseBiDirectionalStreaming",
      requestType = org.apache.tika.grpc.v2.FetchAndParseRequest.class,
      responseType = org.apache.tika.grpc.v2.FetchAndParseReply.class,
      methodType = io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
  public static io.grpc.MethodDescriptor<org.apache.tika.grpc.v2.FetchAndParseRequest,
      org.apache.tika.grpc.v2.FetchAndParseReply> getFetchAndParseBiDirectionalStreamingMethod() {
    io.grpc.MethodDescriptor<org.apache.tika.grpc.v2.FetchAndParseRequest, org.apache.tika.grpc.v2.FetchAndParseReply> getFetchAndParseBiDirectionalStreamingMethod;
    if ((getFetchAndParseBiDirectionalStreamingMethod = TikaV2Grpc.getFetchAndParseBiDirectionalStreamingMethod) == null) {
      synchronized (TikaV2Grpc.class) {
        if ((getFetchAndParseBiDirectionalStreamingMethod = TikaV2Grpc.getFetchAndParseBiDirectionalStreamingMethod) == null) {
          TikaV2Grpc.getFetchAndParseBiDirectionalStreamingMethod = getFetchAndParseBiDirectionalStreamingMethod =
              io.grpc.MethodDescriptor.<org.apache.tika.grpc.v2.FetchAndParseRequest, org.apache.tika.grpc.v2.FetchAndParseReply>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "FetchAndParseBiDirectionalStreaming"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.apache.tika.grpc.v2.FetchAndParseRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.apache.tika.grpc.v2.FetchAndParseReply.getDefaultInstance()))
              .setSchemaDescriptor(new TikaV2MethodDescriptorSupplier("FetchAndParseBiDirectionalStreaming"))
              .build();
        }
      }
    }
    return getFetchAndParseBiDirectionalStreamingMethod;
  }

  private static volatile io.grpc.MethodDescriptor<org.apache.tika.grpc.v2.ParseBytesRequest,
      org.apache.tika.grpc.v2.ParseBytesReply> getParseBytesMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ParseBytes",
      requestType = org.apache.tika.grpc.v2.ParseBytesRequest.class,
      responseType = org.apache.tika.grpc.v2.ParseBytesReply.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<org.apache.tika.grpc.v2.ParseBytesRequest,
      org.apache.tika.grpc.v2.ParseBytesReply> getParseBytesMethod() {
    io.grpc.MethodDescriptor<org.apache.tika.grpc.v2.ParseBytesRequest, org.apache.tika.grpc.v2.ParseBytesReply> getParseBytesMethod;
    if ((getParseBytesMethod = TikaV2Grpc.getParseBytesMethod) == null) {
      synchronized (TikaV2Grpc.class) {
        if ((getParseBytesMethod = TikaV2Grpc.getParseBytesMethod) == null) {
          TikaV2Grpc.getParseBytesMethod = getParseBytesMethod =
              io.grpc.MethodDescriptor.<org.apache.tika.grpc.v2.ParseBytesRequest, org.apache.tika.grpc.v2.ParseBytesReply>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ParseBytes"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.apache.tika.grpc.v2.ParseBytesRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.apache.tika.grpc.v2.ParseBytesReply.getDefaultInstance()))
              .setSchemaDescriptor(new TikaV2MethodDescriptorSupplier("ParseBytes"))
              .build();
        }
      }
    }
    return getParseBytesMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static TikaV2Stub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<TikaV2Stub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<TikaV2Stub>() {
        @java.lang.Override
        public TikaV2Stub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new TikaV2Stub(channel, callOptions);
        }
      };
    return TikaV2Stub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports all types of calls on the service
   */
  public static TikaV2BlockingV2Stub newBlockingV2Stub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<TikaV2BlockingV2Stub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<TikaV2BlockingV2Stub>() {
        @java.lang.Override
        public TikaV2BlockingV2Stub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new TikaV2BlockingV2Stub(channel, callOptions);
        }
      };
    return TikaV2BlockingV2Stub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static TikaV2BlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<TikaV2BlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<TikaV2BlockingStub>() {
        @java.lang.Override
        public TikaV2BlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new TikaV2BlockingStub(channel, callOptions);
        }
      };
    return TikaV2BlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static TikaV2FutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<TikaV2FutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<TikaV2FutureStub>() {
        @java.lang.Override
        public TikaV2FutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new TikaV2FutureStub(channel, callOptions);
        }
      };
    return TikaV2FutureStub.newStub(factory, channel);
  }

  /**
   */
  public interface AsyncService {

    /**
     * <pre>
     * Fetch via a previously saved fetcher and return a typed Document.
     * </pre>
     */
    default void fetchAndParse(org.apache.tika.grpc.v2.FetchAndParseRequest request,
        io.grpc.stub.StreamObserver<org.apache.tika.grpc.v2.FetchAndParseReply> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getFetchAndParseMethod(), responseObserver);
    }

    /**
     */
    default void fetchAndParseServerSideStreaming(org.apache.tika.grpc.v2.FetchAndParseRequest request,
        io.grpc.stub.StreamObserver<org.apache.tika.grpc.v2.FetchAndParseReply> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getFetchAndParseServerSideStreamingMethod(), responseObserver);
    }

    /**
     */
    default io.grpc.stub.StreamObserver<org.apache.tika.grpc.v2.FetchAndParseRequest> fetchAndParseBiDirectionalStreaming(
        io.grpc.stub.StreamObserver<org.apache.tika.grpc.v2.FetchAndParseReply> responseObserver) {
      return io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall(getFetchAndParseBiDirectionalStreamingMethod(), responseObserver);
    }

    /**
     * <pre>
     * Parse-only entrypoint (TIKA-4795 PoC): parse the exact bytes the caller already
     * holds. No fetcher registration. Reply is the same Document contract.
     * </pre>
     */
    default void parseBytes(org.apache.tika.grpc.v2.ParseBytesRequest request,
        io.grpc.stub.StreamObserver<org.apache.tika.grpc.v2.ParseBytesReply> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getParseBytesMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service TikaV2.
   */
  public static abstract class TikaV2ImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return TikaV2Grpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service TikaV2.
   */
  public static final class TikaV2Stub
      extends io.grpc.stub.AbstractAsyncStub<TikaV2Stub> {
    private TikaV2Stub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected TikaV2Stub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new TikaV2Stub(channel, callOptions);
    }

    /**
     * <pre>
     * Fetch via a previously saved fetcher and return a typed Document.
     * </pre>
     */
    public void fetchAndParse(org.apache.tika.grpc.v2.FetchAndParseRequest request,
        io.grpc.stub.StreamObserver<org.apache.tika.grpc.v2.FetchAndParseReply> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getFetchAndParseMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void fetchAndParseServerSideStreaming(org.apache.tika.grpc.v2.FetchAndParseRequest request,
        io.grpc.stub.StreamObserver<org.apache.tika.grpc.v2.FetchAndParseReply> responseObserver) {
      io.grpc.stub.ClientCalls.asyncServerStreamingCall(
          getChannel().newCall(getFetchAndParseServerSideStreamingMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<org.apache.tika.grpc.v2.FetchAndParseRequest> fetchAndParseBiDirectionalStreaming(
        io.grpc.stub.StreamObserver<org.apache.tika.grpc.v2.FetchAndParseReply> responseObserver) {
      return io.grpc.stub.ClientCalls.asyncBidiStreamingCall(
          getChannel().newCall(getFetchAndParseBiDirectionalStreamingMethod(), getCallOptions()), responseObserver);
    }

    /**
     * <pre>
     * Parse-only entrypoint (TIKA-4795 PoC): parse the exact bytes the caller already
     * holds. No fetcher registration. Reply is the same Document contract.
     * </pre>
     */
    public void parseBytes(org.apache.tika.grpc.v2.ParseBytesRequest request,
        io.grpc.stub.StreamObserver<org.apache.tika.grpc.v2.ParseBytesReply> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getParseBytesMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service TikaV2.
   */
  public static final class TikaV2BlockingV2Stub
      extends io.grpc.stub.AbstractBlockingStub<TikaV2BlockingV2Stub> {
    private TikaV2BlockingV2Stub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected TikaV2BlockingV2Stub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new TikaV2BlockingV2Stub(channel, callOptions);
    }

    /**
     * <pre>
     * Fetch via a previously saved fetcher and return a typed Document.
     * </pre>
     */
    public org.apache.tika.grpc.v2.FetchAndParseReply fetchAndParse(org.apache.tika.grpc.v2.FetchAndParseRequest request) throws io.grpc.StatusException {
      return io.grpc.stub.ClientCalls.blockingV2UnaryCall(
          getChannel(), getFetchAndParseMethod(), getCallOptions(), request);
    }

    /**
     */
    @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/10918")
    public io.grpc.stub.BlockingClientCall<?, org.apache.tika.grpc.v2.FetchAndParseReply>
        fetchAndParseServerSideStreaming(org.apache.tika.grpc.v2.FetchAndParseRequest request) {
      return io.grpc.stub.ClientCalls.blockingV2ServerStreamingCall(
          getChannel(), getFetchAndParseServerSideStreamingMethod(), getCallOptions(), request);
    }

    /**
     */
    @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/10918")
    public io.grpc.stub.BlockingClientCall<org.apache.tika.grpc.v2.FetchAndParseRequest, org.apache.tika.grpc.v2.FetchAndParseReply>
        fetchAndParseBiDirectionalStreaming() {
      return io.grpc.stub.ClientCalls.blockingBidiStreamingCall(
          getChannel(), getFetchAndParseBiDirectionalStreamingMethod(), getCallOptions());
    }

    /**
     * <pre>
     * Parse-only entrypoint (TIKA-4795 PoC): parse the exact bytes the caller already
     * holds. No fetcher registration. Reply is the same Document contract.
     * </pre>
     */
    public org.apache.tika.grpc.v2.ParseBytesReply parseBytes(org.apache.tika.grpc.v2.ParseBytesRequest request) throws io.grpc.StatusException {
      return io.grpc.stub.ClientCalls.blockingV2UnaryCall(
          getChannel(), getParseBytesMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do limited synchronous rpc calls to service TikaV2.
   */
  public static final class TikaV2BlockingStub
      extends io.grpc.stub.AbstractBlockingStub<TikaV2BlockingStub> {
    private TikaV2BlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected TikaV2BlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new TikaV2BlockingStub(channel, callOptions);
    }

    /**
     * <pre>
     * Fetch via a previously saved fetcher and return a typed Document.
     * </pre>
     */
    public org.apache.tika.grpc.v2.FetchAndParseReply fetchAndParse(org.apache.tika.grpc.v2.FetchAndParseRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getFetchAndParseMethod(), getCallOptions(), request);
    }

    /**
     */
    public java.util.Iterator<org.apache.tika.grpc.v2.FetchAndParseReply> fetchAndParseServerSideStreaming(
        org.apache.tika.grpc.v2.FetchAndParseRequest request) {
      return io.grpc.stub.ClientCalls.blockingServerStreamingCall(
          getChannel(), getFetchAndParseServerSideStreamingMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Parse-only entrypoint (TIKA-4795 PoC): parse the exact bytes the caller already
     * holds. No fetcher registration. Reply is the same Document contract.
     * </pre>
     */
    public org.apache.tika.grpc.v2.ParseBytesReply parseBytes(org.apache.tika.grpc.v2.ParseBytesRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getParseBytesMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service TikaV2.
   */
  public static final class TikaV2FutureStub
      extends io.grpc.stub.AbstractFutureStub<TikaV2FutureStub> {
    private TikaV2FutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected TikaV2FutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new TikaV2FutureStub(channel, callOptions);
    }

    /**
     * <pre>
     * Fetch via a previously saved fetcher and return a typed Document.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<org.apache.tika.grpc.v2.FetchAndParseReply> fetchAndParse(
        org.apache.tika.grpc.v2.FetchAndParseRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getFetchAndParseMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Parse-only entrypoint (TIKA-4795 PoC): parse the exact bytes the caller already
     * holds. No fetcher registration. Reply is the same Document contract.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<org.apache.tika.grpc.v2.ParseBytesReply> parseBytes(
        org.apache.tika.grpc.v2.ParseBytesRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getParseBytesMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_FETCH_AND_PARSE = 0;
  private static final int METHODID_FETCH_AND_PARSE_SERVER_SIDE_STREAMING = 1;
  private static final int METHODID_PARSE_BYTES = 2;
  private static final int METHODID_FETCH_AND_PARSE_BI_DIRECTIONAL_STREAMING = 3;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final AsyncService serviceImpl;
    private final int methodId;

    MethodHandlers(AsyncService serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_FETCH_AND_PARSE:
          serviceImpl.fetchAndParse((org.apache.tika.grpc.v2.FetchAndParseRequest) request,
              (io.grpc.stub.StreamObserver<org.apache.tika.grpc.v2.FetchAndParseReply>) responseObserver);
          break;
        case METHODID_FETCH_AND_PARSE_SERVER_SIDE_STREAMING:
          serviceImpl.fetchAndParseServerSideStreaming((org.apache.tika.grpc.v2.FetchAndParseRequest) request,
              (io.grpc.stub.StreamObserver<org.apache.tika.grpc.v2.FetchAndParseReply>) responseObserver);
          break;
        case METHODID_PARSE_BYTES:
          serviceImpl.parseBytes((org.apache.tika.grpc.v2.ParseBytesRequest) request,
              (io.grpc.stub.StreamObserver<org.apache.tika.grpc.v2.ParseBytesReply>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_FETCH_AND_PARSE_BI_DIRECTIONAL_STREAMING:
          return (io.grpc.stub.StreamObserver<Req>) serviceImpl.fetchAndParseBiDirectionalStreaming(
              (io.grpc.stub.StreamObserver<org.apache.tika.grpc.v2.FetchAndParseReply>) responseObserver);
        default:
          throw new AssertionError();
      }
    }
  }

  public static final io.grpc.ServerServiceDefinition bindService(AsyncService service) {
    return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
        .addMethod(
          getFetchAndParseMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              org.apache.tika.grpc.v2.FetchAndParseRequest,
              org.apache.tika.grpc.v2.FetchAndParseReply>(
                service, METHODID_FETCH_AND_PARSE)))
        .addMethod(
          getFetchAndParseServerSideStreamingMethod(),
          io.grpc.stub.ServerCalls.asyncServerStreamingCall(
            new MethodHandlers<
              org.apache.tika.grpc.v2.FetchAndParseRequest,
              org.apache.tika.grpc.v2.FetchAndParseReply>(
                service, METHODID_FETCH_AND_PARSE_SERVER_SIDE_STREAMING)))
        .addMethod(
          getFetchAndParseBiDirectionalStreamingMethod(),
          io.grpc.stub.ServerCalls.asyncBidiStreamingCall(
            new MethodHandlers<
              org.apache.tika.grpc.v2.FetchAndParseRequest,
              org.apache.tika.grpc.v2.FetchAndParseReply>(
                service, METHODID_FETCH_AND_PARSE_BI_DIRECTIONAL_STREAMING)))
        .addMethod(
          getParseBytesMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              org.apache.tika.grpc.v2.ParseBytesRequest,
              org.apache.tika.grpc.v2.ParseBytesReply>(
                service, METHODID_PARSE_BYTES)))
        .build();
  }

  private static abstract class TikaV2BaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    TikaV2BaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return org.apache.tika.grpc.v2.TikaV2Proto.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("TikaV2");
    }
  }

  private static final class TikaV2FileDescriptorSupplier
      extends TikaV2BaseDescriptorSupplier {
    TikaV2FileDescriptorSupplier() {}
  }

  private static final class TikaV2MethodDescriptorSupplier
      extends TikaV2BaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final java.lang.String methodName;

    TikaV2MethodDescriptorSupplier(java.lang.String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (TikaV2Grpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new TikaV2FileDescriptorSupplier())
              .addMethod(getFetchAndParseMethod())
              .addMethod(getFetchAndParseServerSideStreamingMethod())
              .addMethod(getFetchAndParseBiDirectionalStreamingMethod())
              .addMethod(getParseBytesMethod())
              .build();
        }
      }
    }
    return result;
  }
}
