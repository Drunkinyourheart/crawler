package com.yeepay.bigdata.crawler.crawl.monitor;

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Monitor extends Thread implements LifeCycle {

    private static final Logger LOGGER = Logger.getLogger(Monitor.class);
    private static int DEFAULT_LISTENING_PORT = 8100;
    private List<Dumpable> list = new CopyOnWriteArrayList<Dumpable>();
    protected int _port = DEFAULT_LISTENING_PORT;
    private ServerSocket server_socket;

    public Monitor() {
    }

    public Monitor(int port_) {
        _port = port_;
    }

    public void addMonitored(Dumpable m_) {
        list.add(m_);
    }

    public void run() {
        try {
            server_socket = new ServerSocket(_port);
        } catch (IOException e) {
            LOGGER.error("cannot create server socket on port " + _port, e);
            return;
        }
        while (true) {
            Socket socket;
            try {
                socket = server_socket.accept();
            } catch (IOException e) {
                LOGGER.error("cannot accept socket", e);
                return;
            }
            try {
                String line;
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintStream out = new PrintStream(socket.getOutputStream());
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("p")) {
                        for (Dumpable m : list) {
                            m.dump(out, "------------\n");
                        }
                    }

                }
                socket.close();
            } catch (Throwable e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    public void destroy() {
        if (server_socket != null) {
            try {
                server_socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
