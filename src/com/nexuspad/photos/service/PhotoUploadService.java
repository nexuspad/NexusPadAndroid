package com.nexuspad.photos.service;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import com.edmondapps.utils.android.service.FileUploadService;
import com.nexuspad.datamodel.Folder;
import com.nexuspad.dataservice.UploadService;
import com.nexuspad.photos.Request;

import java.lang.ref.WeakReference;
import java.util.*;

/**
 * Author: Edmond
 */
public class PhotoUploadService extends Service {

    public interface Callback {
        void onNewRequest(Request request);
    }

    public static class PhotosUploadBinder extends Binder {
        private final WeakReference<PhotoUploadService> mService;
        private final List<Callback> mCallbacks = new ArrayList<Callback>();

        private PhotosUploadBinder(PhotoUploadService service) {
            mService = new WeakReference<PhotoUploadService>(service);
        }

        /**
         * Remember to call {@link #removeCallback(Callback)}
         */
        public void addCallback(Callback callback) {
            mCallbacks.add(callback);
        }

        /**
         * @see #addCallback(Callback)
         */
        public void removeCallback(Callback callback) {
            mCallbacks.remove(callback);
        }

        public void addRequests(Iterable<? extends Request> requests) {
            for (Request request : requests) {
                addRequest(request);
            }
        }

        public void addRequest(Request request) {
            final PhotoUploadService service = getService();
            if (!service.mQueue.contains(request)) {
                service.mQueue.add(request);
                service.onNewRequest(request);
                for (Callback callback : mCallbacks) {
                    callback.onNewRequest(request);
                }
            }
        }

        public Collection<Request> peekRequests() {
            return Collections.unmodifiableCollection(getService().mQueue);
        }

        private PhotoUploadService getService() {
            final PhotoUploadService service = mService.get();
            if (service != null) {
                return service;
            }
            throw new IllegalStateException("Service has been GCed, so should the Binder.");
        }
    }

    private final List<Request> mQueue = new ArrayList<Request>();
    private final PhotosUploadBinder mBinder = new PhotosUploadBinder(this);
    private final UploadService mUploadService = new UploadService(this);

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    public void onNewRequest(Request r) {
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
            if (callback != null) {
                return callback.onProgress(progress, total);
            }
            return true;
        }

        @Override
        public void onDone(boolean success) {
            mQueue.remove(mRequest);
            final FileUploadService.Callback callback = mRequest.getCallback();
            if (callback != null) {
                callback.onDone(success);
            }
        }
    }
}
