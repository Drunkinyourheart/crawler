package com.yeepay.bigdata.crawler.manager.seed;

import com.yeepay.bigdata.crawler.manager.model.SeedInfo;
import com.yeepay.bigdata.crawler.manager.monitor.Monitor;
import com.yeepay.bigdata.crawler.manager.scheduler.SeedProcessEngine;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class SeedManager {

    private static final Logger logger = Logger.getLogger(SeedManager.class);

    private List<SeedInfo> seedInfos;

    private ScheduledExecutorService scheduledExecutorService;

    private SeedProcessEngine seedProcessEngine;

    private List<ScheduledFuture<?>> scheduledFutures = new ArrayList<ScheduledFuture<?>>();

    private Monitor monitor;

    public SeedManager(SeedProcessEngine seedProcessEngine) {
        this(seedProcessEngine, 10);
    }

    public SeedManager(SeedProcessEngine seedProcessEngine, int threadSize) {
        this.seedProcessEngine = seedProcessEngine;
        scheduledExecutorService = Executors.newScheduledThreadPool(threadSize);
        monitor = new Monitor();
        monitor.addMonitored(seedProcessEngine);
    }

    public void start() throws IOException {
        seedProcessEngine.start();
        monitor.start();

        /** ------------------------------------------------------------------------------------------------------- */

        scheduledExecutorService.scheduleAtFixedRate(new Runnable() {

            @Override
            public void run() {
                for (ScheduledFuture<?> scheduledFuture : scheduledFutures) {
                    scheduledFuture.cancel(true);
                }
                scheduledFutures.clear();

                Random random = new Random();
                // new seeds
                try {
                    seedInfos = listSeedInfos();
                } catch (ClassNotFoundException e1) {
                    logger.error(e1.getMessage(), e1);
                    stop();
                } catch (SQLException e1) {
                    logger.error(e1.getMessage(), e1);
                    stop();
                }
                for (final SeedInfo seedInfo : seedInfos) {
                    ScheduledFuture<?> future = scheduledExecutorService
                            .scheduleWithFixedDelay(new Runnable() {

                                                        @Override
                                                        public void run() {
                                                            try {
                                                                seedProcessEngine.submitSeed(seedInfo);
                                                            } catch (InterruptedException e) {
                                                                logger.error(e);
                                                            }
                                                        }
                                                    }, random.nextInt(300000), Integer.parseInt(seedInfo.getRate()),
                                    TimeUnit.MILLISECONDS);
                    scheduledFutures.add(future);
                }
            }
        }, 0, 5, TimeUnit.HOURS);
    }

    private List<SeedInfo> listSeedInfos() throws ClassNotFoundException,
            SQLException {
        return SeedQuery.listSeedInfosFromFile();
        // return SeedQuery.listSeedInfos();
    }

    public void stop() {
        scheduledExecutorService.shutdown();
        seedProcessEngine.stop();
    }

    public SeedProcessEngine getSeedProcessEngine() {
        return seedProcessEngine;
    }

    public void setSeedProcessEngine(SeedProcessEngine seedProcessEngine) {
        this.seedProcessEngine = seedProcessEngine;
    }

}
