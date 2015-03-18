package com.yeepay.bigdata.crawler.schedule.thrift.client;

import org.apache.commons.pool.BasePoolableObjectFactory;
import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

/**
 *  管理 TFramedTransport 连接
 *
 */
public class ThriftTransportPool<T extends TTransport> {

	private GenericObjectPool internalPool;

	public ThriftTransportPool(final GenericObjectPool.Config poolConfig,
			PoolableObjectFactory factory) {
		this.internalPool = new GenericObjectPool(factory, poolConfig);
	}

	public ThriftTransportPool(final GenericObjectPool.Config config,
			String host, int port) {
		this.internalPool = new GenericObjectPool(new TFramedTransportFactory(
				host, port), config);
	}

	@SuppressWarnings("unchecked")
	public T getResource() {
		try {
			return (T) internalPool.borrowObject();
		} catch (Exception e) {
			throw new RuntimeException(
					"Could not get a resource from the pool", e);
		}
	}

	public void returnResourceObject(final T resource) {
		try {
			internalPool.returnObject(resource);
		} catch (Exception e) {
			throw new RuntimeException(
					"Could not return the resource to the pool", e);
		}
	}

	public void returnBrokenResource(final T resource) {
		returnBrokenResourceObject(resource);
	}

	public void returnResource(final T resource) {
		returnResourceObject(resource);
	}

	protected void returnBrokenResourceObject(final T resource) {
		try {
			internalPool.invalidateObject(resource);
		} catch (Exception e) {
			throw new RuntimeException(
					"Could not return the resource to the pool", e);
		}
	}

	public void destroy() {
		try {
			internalPool.close();
		} catch (Exception e) {
			throw new RuntimeException("Could not destroy the pool", e);
		}
	}

	public static class TFramedTransportFactory extends
			BasePoolableObjectFactory {

		private String host;

		private int port;

		public TFramedTransportFactory(String host, int port) {
			this.host = host;
			this.port = port;
		}

		@Override
		public Object makeObject() throws Exception {
			TFramedTransport transport = new TFramedTransport(new TSocket(host,
					port));
			transport.open();
			return transport;
		}

		@Override
		public void destroyObject(Object obj) throws Exception {
			if (obj instanceof TFramedTransport) {
				TFramedTransport transport = (TFramedTransport) obj;
				try {
					if (transport.isOpen()) {
						try {
							transport.close();
						} catch (Exception e) {
						}
					}
				} catch (Exception e) {
				}
			}
		}

		@Override
		public boolean validateObject(Object obj) {
			if (obj instanceof TFramedTransport) {
				TFramedTransport transport = (TFramedTransport) obj;
				try {
					return transport.isOpen();
				} catch (Exception e) {
					return false;
				}
			}
			return false;

		}

	}

	public static class TSocketFactory extends BasePoolableObjectFactory {

		private String host;
		private int port;

		public TSocketFactory(String host, int port) {
			super();
			this.host = host;
			this.port = port;
		}

		@Override
		public Object makeObject() throws Exception {
			TSocket transport = new TSocket(host, port);
			transport.open();
			return transport;
		}

		@Override
		public void destroyObject(Object obj) throws Exception {
			if (obj instanceof TSocket) {
				TSocket transport = (TSocket) obj;
				try {
					if (transport.isOpen()) {
						transport.close();
					}
				} catch (Exception e) {
				}
			}
		}

		@Override
		public boolean validateObject(Object obj) {
			if (obj instanceof TSocket) {
				TSocket transport = (TSocket) obj;
				try {
					return transport.isOpen();
				} catch (Exception e) {
					return false;
				}
			}
			return false;
		}

	}
}
