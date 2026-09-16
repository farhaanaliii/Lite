package com.farhanali.lite;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class DownloadService extends Service {
    public static final String ACTION_START = "download.START";
    public static final String ACTION_CANCEL = "download.CANCEL";
    public static final String EXTRA_URL = "url";
    public static final String EXTRA_FILE_NAME = "file_name";
    public static final String EXTRA_IS_UPDATE = "is_update";
    public static final String EXTRA_SHA256 = "sha256";

    private static final String CHANNEL_ID = "download_channel";
    private static final int NOTIF_ID = 2001;

    public interface ProgressListener {
        void on_progress(int percent);
        void on_complete();
        void on_failed();
    }

    private static final Handler main_handler = new Handler(Looper.getMainLooper());
    private static ProgressListener progress_listener;

    public static void set_progress_listener(ProgressListener listener) {
        progress_listener = listener;
    }

    public static void cancel(Context context) {
        Intent intent = new Intent(context, DownloadService.class);
        intent.setAction(ACTION_CANCEL);
        context.startService(intent);
    }

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private Future<?> download_future;
    private NotificationManager notif_manager;

    public static void start_download(Context context, String url, String file_name, boolean is_update, String sha256) {
        Intent intent = new Intent(context, DownloadService.class);
        intent.setAction(ACTION_START);
        intent.putExtra(EXTRA_URL, url);
        intent.putExtra(EXTRA_FILE_NAME, file_name);
        intent.putExtra(EXTRA_IS_UPDATE, is_update);
        intent.putExtra(EXTRA_SHA256, sha256);
        ContextCompat.startForegroundService(context, intent);
    }

    public static void start_download(Context context, String url, String file_name, boolean is_update) {
        start_download(context, url, file_name, is_update, null);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        notif_manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID, getString(R.string.download_channel_name), NotificationManager.IMPORTANCE_LOW);
            channel.setSound(null, null);
            notif_manager.createNotificationChannel(channel);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null || ACTION_CANCEL.equals(intent.getAction())) {
            cancel_download();
            stop_self();
            return START_NOT_STICKY;
        }

        start_in_foreground();

        String url = intent.getStringExtra(EXTRA_URL);
        String file_name = intent.getStringExtra(EXTRA_FILE_NAME);
        boolean is_update = intent.getBooleanExtra(EXTRA_IS_UPDATE, false);
        String sha256 = intent.getStringExtra(EXTRA_SHA256);

        download_future = executor.submit(() -> run_download(url, file_name, is_update, sha256));
        return START_NOT_STICKY;
    }

    private void run_download(String url_str, String file_name, boolean is_update, String expected_sha256) {
        expected_sha256 = normalize_sha256(expected_sha256);
        if (is_update && (expected_sha256 == null || expected_sha256.isEmpty())) {
            on_download_failed();
            stop_self();
            return;
        }
        File dir = is_update ? getCacheDir() : getExternalFilesDir(null);
        File target_file = new File(dir, file_name);

        if (target_file.exists()) {
            if (expected_sha256 != null && !expected_sha256.isEmpty()) {
                if (expected_sha256.equalsIgnoreCase(calculate_sha256(target_file))) {
                    notify_complete();
                    on_download_complete(target_file, is_update);
                    stop_self();
                    return;
                }
                delete_file(target_file);
            }
        }

        HttpURLConnection conn = null;

        try {
            conn = (HttpURLConnection) new URL(url_str).openConnection();
            conn.setInstanceFollowRedirects(true);
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(30000);
            conn.connect();

            int code = conn.getResponseCode();
            while (code == HttpURLConnection.HTTP_MOVED_PERM || code == HttpURLConnection.HTTP_MOVED_TEMP
                    || code == HttpURLConnection.HTTP_SEE_OTHER || code == 307 || code == 308) {
                String location = conn.getHeaderField("Location");
                conn.disconnect();
                conn = (HttpURLConnection) new URL(location).openConnection();
                conn.setInstanceFollowRedirects(true);
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(30000);
                conn.connect();
                code = conn.getResponseCode();
            }

            if (code != HttpURLConnection.HTTP_OK) {
                on_download_failed();
                return;
            }

            long total = conn.getContentLengthLong();
            long written = 0;
            int last_percent = -1;

            try (InputStream in = conn.getInputStream();
                 FileOutputStream fos = new FileOutputStream(target_file)) {
                byte[] buf = new byte[8192];
                int read;
                while ((read = in.read(buf)) != -1) {
                    if (Thread.currentThread().isInterrupted()) {
                        delete_file(target_file);
                        return;
                    }
                    fos.write(buf, 0, read);
                    written += read;
                    if (total > 0) {
                        int percent = (int) (written * 100 / total);
                        if (percent != last_percent) {
                            last_percent = percent;
                            update_notification(percent);
                            notify_progress(percent);
                        }
                    }
                }
            }

            if (expected_sha256 != null && !expected_sha256.isEmpty()) {
                if (!expected_sha256.equalsIgnoreCase(calculate_sha256(target_file))) {
                    delete_file(target_file);
                    on_download_failed();
                    return;
                }
            }

            on_download_complete(target_file, is_update);
        } catch (Exception e) {
            delete_file(target_file);
            on_download_failed();
        } finally {
            if (conn != null) conn.disconnect();
            stop_self();
        }
    }

    private void on_download_complete(File file, boolean is_update) {
        notify_complete();
        Uri file_uri = FileProvider.getUriForFile(this, getPackageName() + ".provider", file);
        Intent open_intent = new Intent(Intent.ACTION_VIEW);
        if (is_update) {
            open_intent.setDataAndType(file_uri, "application/vnd.android.package-archive");
        } else {
            open_intent.setDataAndType(file_uri, "video/*");
        }
        open_intent.setClipData(ClipData.newRawUri("", file_uri));
        open_intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);

        if (is_update) {
            try {
                startActivity(open_intent);
            } catch (Exception ignored) {
            }
        }

        PendingIntent pi = PendingIntent.getActivity(this, 0, open_intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder done_notif = new NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.download_complete))
            .setContentIntent(pi)
            .setAutoCancel(true);

        notif_manager.notify(NOTIF_ID, done_notif.build());
    }

    private void on_download_failed() {
        notify_failed();
        NotificationCompat.Builder fail_notif = new NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.download_failed))
            .setAutoCancel(true);

        notif_manager.notify(NOTIF_ID, fail_notif.build());
    }

    private void start_in_foreground() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.download_starting))
            .setProgress(100, 0, true)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel,
                getString(android.R.string.cancel), cancel_intent());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIF_ID, builder.build(), ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC);
        } else {
            startForeground(NOTIF_ID, builder.build());
        }
    }

    private void delete_file(File file) {
        if (file != null && file.exists() && !file.delete()) {
            file.deleteOnExit();
        }
    }

    private static String normalize_sha256(String hash) {
        if (hash == null) return null;
        String h = hash.trim();
        if (h.toLowerCase().startsWith("sha256:")) {
            h = h.substring(7).trim();
        } else if (h.toLowerCase().startsWith("sha-256:")) {
            h = h.substring(8).trim();
        } else if (h.toLowerCase().startsWith("sha256 ")) {
            h = h.substring(7).trim();
        } else if (h.toLowerCase().startsWith("sha-256 ")) {
            h = h.substring(8).trim();
        }
        if (!h.matches("(?i)[0-9a-f]{64}")) {
            return null;
        }
        return h.toLowerCase();
    }

    private static String calculate_sha256(File file) {
        try (InputStream in = new FileInputStream(file)) {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] buf = new byte[8192];
            int read;
            while ((read = in.read(buf)) != -1) {
                md.update(buf, 0, read);
            }
            byte[] digest = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }

    private void update_notification(int progress) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.downloading_progress, progress))
            .setProgress(100, progress, false)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel,
                getString(android.R.string.cancel), cancel_intent());

        notif_manager.notify(NOTIF_ID, builder.build());
    }

    private PendingIntent cancel_intent() {
        Intent i = new Intent(this, DownloadService.class);
        i.setAction(ACTION_CANCEL);
        return PendingIntent.getService(this, 0, i,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    private void cancel_download() {
        if (download_future != null) download_future.cancel(true);
    }

    private void stop_self() {
        stopForeground(STOP_FOREGROUND_REMOVE);
        stopSelf();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        progress_listener = null;
        executor.shutdownNow();
    }

    private static void notify_progress(int percent) {
        if (progress_listener != null) {
            main_handler.post(() -> {
                if (progress_listener != null) progress_listener.on_progress(percent);
            });
        }
    }

    private static void notify_complete() {
        if (progress_listener != null) {
            main_handler.post(() -> {
                if (progress_listener != null) progress_listener.on_complete();
            });
        }
    }

    private static void notify_failed() {
        if (progress_listener != null) {
            main_handler.post(() -> {
                if (progress_listener != null) progress_listener.on_failed();
            });
        }
    }

    public static class CacheCleanupReceiver extends android.content.BroadcastReceiver {
        @Override
        public void onReceive(Context context, android.content.Intent intent) {
            File[] files = context.getCacheDir().listFiles();
            if (files == null) return;
            for (File f : files) {
                if (f.getName().endsWith(".apk")) f.delete();
            }
        }
    }
}
