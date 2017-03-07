package com.test;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.protobuf.ByteString;
import com.test.grpc.*;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

public class HelloWorldClient {

	private static final int SERVER_PORT = Integer.parseInt(System.getenv("SERVER_PORT"));
	private static final String SERVER_ADDRESS = System.getenv("SERVER_ADDR");
	private static final String USER_NAME = System.getenv("RPC_USER");

	private static final int REPETITIONS = Integer.parseInt(System.getenv("REPETITIONS"));
	private static final int SLEEP_TIME_MS = 1000;

	private static final Logger logger = Logger.getLogger(HelloWorldClient.class.getName());

	private final ManagedChannel channel;
	private final GreeterGrpc.GreeterBlockingStub blockingStub;

	/**
	 * Construct client connecting to HelloWorld server at {@code host:port}.
	 */
	public HelloWorldClient(String host, int port) {
		this(ManagedChannelBuilder.forAddress(host, port)
				// Channels are secure by default (via SSL/TLS). For the example
				// we disable TLS to avoid
				// needing certificates.
				.usePlaintext(true));
	}

	/**
	 * Construct client for accessing RouteGuide server using the existing
	 * channel.
	 */
	HelloWorldClient(ManagedChannelBuilder<?> channelBuilder) {
		channel = channelBuilder.build();
		blockingStub = GreeterGrpc.newBlockingStub(channel);
	}

	public void shutdown() throws InterruptedException {
		channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
	}

	/** Say hello to server. */
	public void greet(String name) {
		logger.info("Will try to greet " + name + " ...");
		final ByteString bs = ByteString.copyFrom(new byte[] { 0, 1, 0 });
		final HelloRequest request = HelloRequest.newBuilder().setName(name).setMsg(bs).addTags("t1").addTags("t2").build();
		HelloReply response;
		try {
			response = blockingStub.sayHello(request);
		} catch (final StatusRuntimeException e) {
			logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
			return;
		}
		logger.info("Greeting: " + response.getMessage());
	}

	/**
	 * Greet server. If provided, the first element of {@code args} is the name
	 * to use in the greeting.
	 */
	public static void main(String[] args) throws Exception {
		final HelloWorldClient client = new HelloWorldClient(SERVER_ADDRESS, SERVER_PORT);
		try {
			for (int i = 0; i < REPETITIONS; i++) {
				final String user = USER_NAME + "_" + i;
				client.greet(user);
				Thread.sleep(SLEEP_TIME_MS);
			}
		} finally {
			client.shutdown();
		}
	}
}
