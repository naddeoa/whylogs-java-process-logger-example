package org.example;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import ai.whylabs.service.model.AsyncLogResponse;
import ai.whylabs.service.model.LogAsyncRequest;
import com.whylogs.core.DatasetProfile;

public class Logger implements AutoCloseable {
    private final ConcurrentLinkedQueue<LogData> messageQueue;
    private Map<Long, DatasetProfile> results;
    private final Thread processingThread;
    private final ScheduledExecutorService executorService;
    private final WhyLabsClientManager whylabs;

    public Logger() {
        messageQueue = new ConcurrentLinkedQueue<>();
        results = new HashMap<>();
        processingThread = new Thread(this::processLogMessages);
        processingThread.start();
        executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(this::uploadData, 0, 15, TimeUnit.MINUTES);
        whylabs = new WhyLabsClientManager();
    }

    public void uploadData() {
        Map<Long, DatasetProfile> retryProfiles = new HashMap<>();

        for (Map.Entry<Long, DatasetProfile> entry : results.entrySet()) {
            DatasetProfile profile = entry.getValue();
            try {
                AsyncLogResponse response = whylabs.logApi.logAsync("org-0", "model-47", new LogAsyncRequest()
                        .datasetTimestamp(profile.getDataTimestamp().toEpochMilli())
                        .segmentTags(new ArrayList<>()));
                this.uploadToUrl(response.getUploadUrl(), profile);
                System.out.println("Uploaded dataset profile for timestamp " + entry.getKey());
            } catch (Throwable e) {
                System.err.println("Error uploading dataset profile for timestamp " + entry.getKey() + ": " + e.getMessage());
                e.printStackTrace();
                retryProfiles.put(entry.getKey(), entry.getValue());
            }
        }

        results = retryProfiles;
    }

    private void uploadToUrl(String url, DatasetProfile profile) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/octet-stream");
        connection.setRequestMethod("PUT");

        try (OutputStream out = connection.getOutputStream()) {
            profile.toProtobuf().build().writeDelimitedTo(out);
        }

        if (connection.getResponseCode() != 200) {
            throw new RuntimeException("Error uploading profile: " + connection.getResponseCode() + " " + connection.getResponseMessage());
        }
    }

    public void log(LogData logData) {
        messageQueue.offer(logData);
    }

    private void processLogMessages() {
        while (!Thread.currentThread().isInterrupted()) {
            LogData logData = messageQueue.poll();
            if (logData != null) {
                profile(logData.data, logData.timestamp);
            } else {
                Thread.yield();
            }
        }
    }

    private void profile(List<Map<String, Object>> messages, Long timestamp) {
        // Get the existing DatasetProfile for the given timestamp, or create a new one if it doesn't exist
        DatasetProfile datasetProfile = results.computeIfAbsent(timestamp, k -> {
            System.out.println("Created profile with timestamp " + timestamp);
            return new DatasetProfile("session id", Instant.now(), Instant.ofEpochMilli(timestamp), new HashMap<>(), new HashMap<>());
        });
        for (Map<String, Object> message : messages) {
            // Update the datasetProfile based on the message data
            datasetProfile.track(message);
        }

    }

    public Map<Long, DatasetProfile> getResults() {
        return new HashMap<>(results);
    }

    public static class LogData {
        final List<Map<String, Object>> data;
        final Long timestamp;

        public LogData(List<Map<String, Object>> data, Long timestamp) {
            this.data = data;
            this.timestamp = timestamp;
        }
    }

    @Override
    public void close() {
        while (!messageQueue.isEmpty()) {
            Thread.yield();
        }

        processingThread.interrupt();
        executorService.shutdown();


        int size = results.size();
        // Upload pending profiles before shutting down

        this.uploadData();
//        while(!results.isEmpty()){
//            this.uploadData();
//        }

        System.out.println("uploaded " + size + " pending profiles");

        try {
            processingThread.join();
        } catch (InterruptedException e) {
            System.err.println("Interrupted while waiting for processing thread to shut down: " + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }
}

