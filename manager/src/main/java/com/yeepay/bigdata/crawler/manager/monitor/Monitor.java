package com.yeepay.bigdata.crawler.manager.monitor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Logger;

/**
 * 类Monitor.java的实现描述：TODO 类实现描述
 *
 */
public class Monitor extends Thread {

	private static final Logger LOGGER = Logger.getLogger(Monitor.class);
	private static int DEFAULT_LISTENING_PORT = 8200;
	private List<Dumpable> list = new CopyOnWriteArrayList<Dumpable>();
	protected int _port = DEFAULT_LISTENING_PORT;
	private ServerSocket server_socket;

	public Monitor(int _port) {
		this._port = _port;
		super.setName("spider-monitor-thread");
		super.setDaemon(true);
	}

	public Monitor() {
		this(DEFAULT_LISTENING_PORT);
	}

	public void addMonitored(Dumpable dumpable) {
		list.add(dumpable);
	}

	@Override
	public void run() {

		try {
			server_socket = new ServerSocket(_port);
		} catch (IOException e) {
			LOGGER.error("can't create monitor server ..", e);
			return;
		}
		while (true) {
			Socket socket = null;
			try {
				socket = server_socket.accept();
			} catch (IOException e) {
				LOGGER.error("cannot accept socket", e);
				continue;
			}

			try {
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(socket.getInputStream()));
				PrintStream out = new PrintStream(socket.getOutputStream());
				String line;
				while ((line = reader.readLine()) != null) {
					if (line.startsWith("p")) {
						for (Dumpable m : list) {
							m.dump(out, "------------\n");
							out.flush();
						}
					}
				}
				out.close();
				reader.close();
			} catch (IOException e) {
				LOGGER.error(e.getMessage(), e);
			} finally {
				try {
					socket.close();
				} catch (IOException e) {
					LOGGER.error(e.getMessage(), e);
				}
			}

		}
	}
}
