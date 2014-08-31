package com.nexuspad.app.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import com.google.common.collect.Lists;
import com.nexuspad.app.UploadRequest;
import com.nexuspad.dataservice.EntryUploadService;
import com.nexuspad.dataservice.NPUploadHelper;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Author: Edmond
 */
public final class UploadOperationUIHelper extends Service {

    public interface OnUploadCountChangeListener {
        void onUploadCountChanged(int uploadCount);
    }

    public interface OnNewRequestListener {
        void onNewRequest(UploadRequest request);
    }

    public static class UploadBinder extends Binder {
        private final WeakReference<UploadOperationUIHelper> mService;
        private final List<OnNewRequestListener> mOnNewRequestListeners = new ArrayList<OnNewRequestListener>();

        private UploadBinder(UploadOperationUIHelper service) {
            mService = new WeakReference<UploadOperationUIHelper>(service);
        }

        /**
         * Remember to call {@link #removeCallback(UploadOperationUIHelper.OnNewRequestListener)}
         */
        public void addCallback(OnNewRequestListener onNewRequestListener) {
            mOnNewRequestListeners.add(onNewRequestListener);
        }

        /**
         * @see #addCallback(UploadOperationUIHelper.OnNewRequestListener)
         */
        public void removeCallback(OnNewRequestListener onNewRequestListener) {
            mOnNewRequestListeners.remove(onNewRequestListener);
        }

        public void addRequests(Iterable<? extends UploadRequest> requests) {
            for (UploadRequest request : requests) {
                addRequest(request);
            }
        }

        public void addRequest(UploadRequest request) {
            final UploadOperationUIHelper service = getService();
            if (!service.mQueue.contains(request)) {
                service.mQueue.add(request);
                service.onNewRequest(request);
                for (OnNewRequestListener onNewRequestListener : mOnNewRequestListeners) {
                    onNewRequestListener.onNewRequest(request);
                }
            }
        }

        public Collection<UploadRequest> peekRequests() {
            return Collections.unmodifiableCollection(getService().mQueue);
        }

        private UploadOperationUIHelper getService() {
            final UploadOperationUIHelper service = mService.get();
            if (service != null) {
                return service;
            }
            throw new IllegalStateException("Service has been GCed, so should the Binder.");
        }
    }

    private static int sUploadCount;
    private static List<WeakReference<OnUploadCountChangeListener>> sUploadCountChangeListeners = Lists.newArrayList();

    /**
     * Adds a listener to be notified of upload count changes.
     * <p/>
     * Listeners are stored in a {@code WeakReference}, thus, anonymous inner class cannot be used.
     */
    public static void addOnUploadCountChangeListener(OnUploadCountChangeListener listener) {
        sUploadCountChangeListeners.add(new WeakReference<OnUploadCountChangeListener>(listener));
    }

    private final List<UploadRequest> mQueue = new ArrayList<UploadRequest>();
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
            for (WeakReference<OnUploadCountChangeListener> reference : sUploadCountChangeListeners) {
                final OnUploadCountChangeListener listener = reference.get();
                if (listener != null) {
                    listener.onUploadCountChanged(sUploadCount);
                }
            }
        }
    }

    public void onNewRequest(UploadRequest r) {
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

    private class CallbackWrapper implements NPUploadHelper.Callback {
        private final UploadRequest mRequest;

        private CallbackWrapper(UploadRequest request) {
            mRequest = request;
        }

        @Override
        public boolean onProgress(long progress, long total) {
            final NPUploadHelper.Callback callback = mRequest.getCallback();
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
            final NPUploadHelper.Callback callback = mRequest.getCallback();
            if (callback != null) {
                callback.onDone(success);
            }
        }
    }
}
