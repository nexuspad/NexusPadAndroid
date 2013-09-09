package com.nexuspad.app.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import com.edmondapps.utils.android.service.FileUploadService;
import com.nexuspad.app.Request;
import com.nexuspad.dataservice.EntryUploadService;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Author: Edmond
 */
public final class UploadService extends Service {

    public interface OnUploadCountChangeListener {
        void onUploadCountChanged(int uploadCount);
    }

    public interface OnNewRequestListener {
        void onNewRequest(Request request);
    }

    public static class UploadBinder extends Binder {
        private final WeakReference<UploadService> mService;
        private final List<OnNewRequestListener> mOnNewRequestListeners = new ArrayList<OnNewRequestListener>();

        private UploadBinder(UploadService service) {
            mService = new WeakReference<UploadService>(service);
        }

        /**
         * Remember to call {@link #removeCallback(com.nexuspad.app.service.UploadService.OnNewRequestListener)}
         */
        public void addCallback(OnNewRequestListener onNewRequestListener) {
            mOnNewRequestListeners.add(onNewRequestListener);
        }

        /**
         * @see #addCallback(com.nexuspad.app.service.UploadService.OnNewRequestListener)
         */
        public void removeCallback(OnNewRequestListener onNewRequestListener) {
            mOnNewRequestListeners.remove(onNewRequestListener);
        }

        public void addRequests(Iterable<? extends Request> requests) {
            for (Request request : requests) {
                addRequest(request);
            }
        }

        public void addRequest(Request request) {
            final UploadService service = getService();
            if (!service.mQueue.contains(request)) {
                service.mQueue.add(request);
                service.onNewRequest(request);
                for (OnNewRequestListener onNewRequestListener : mOnNewRequestListeners) {
                    onNewRequestListener.onNewRequest(request);
                }
            }
        }

        public Collection<Request> peekRequests() {
            return Collections.unmodifiableCollection(getService().mQueue);
        }

        private UploadService getService() {
            final UploadService service = mService.get();
            if (service != null) {
                return service;
            }
            throw new IllegalStateException("Service has been GCed, so should the Binder.");
        }
    }

    private static int sUploadCount;
    private static List<OnUploadCountChangeListener> sUploadCountChangeListeners = new ArrayList<OnUploadCountChangeListener>();

    public static void addOnUploadCountChangeListener(OnUploadCountChangeListener listener) {
        sUploadCountChangeListeners.add(listener);
    }

    private final List<Request> mQueue = new ArrayList<Request>();
    private final UploadBinder mBinder = new UploadBinder(this);
    private final EntryUploadService mUploadService = new EntryUploadService(this);

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    private void updateUploadCount() {
        final int oldCount = sUploadCount;
        sUploadCount = mQueue.size();
        if (oldCount != sUploadCount) {
            for (OnUploadCountChangeListener listener : sUploadCountChangeListeners) {
                listener.onUploadCountChanged(sUploadCount);
            }
        }
    }

    public void onNewRequest(Request r) {
        updateUploadCount();
        switch (r.getTarget()) {
            case FOLDER:
                mUploadService.addUploadToFolder(r.getFile(this), r.getFolder(), new CallbackWrapper(r));
                break;
            case ENTRY:
                mUploadService.addUploadToEntry(r.getFile(this), r.getNPEntry(), new CallbackWrapper(r));
                break;
        }
    }

    private class CallbackWrapper implements FileUploadService.Callback {
        private final Request mRequest;

        private CallbackWrapper(Request request) {
            mRequest = request;
        }

        @Override
        public boolean onProgress(long progress, long total) {
            final FileUploadService.Callback callback = mRequest.getCallback();
            //noinspection SimplifiableIfStatement
            if (callback != null) {
                return callback.onProgress(progress, total);
            }
            return true;
        }

        @Override
        public void onDone(boolean success) {
            mQueue.remove(mRequest);
            updateUploadCount();
            final FileUploadService.Callback callback = mRequest.getCallback();
            if (callback != null) {
                callback.onDone(success);
            }
        }
    }
}
