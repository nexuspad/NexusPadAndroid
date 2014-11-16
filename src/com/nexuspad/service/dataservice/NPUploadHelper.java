/*
 * Copyright 2013 Edmond Chui
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nexuspad.service.dataservice;

import android.os.AsyncTask;
import com.nexuspad.service.util.IoUtils;
import com.nexuspad.service.util.IoUtils.ProgressCallback;
import com.nexuspad.service.util.Logs;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Ren
 */
public abstract class NPUploadHelper {
	public static final String TAG = NPUploadHelper.class.getName();

	// Line separator required by multipart/form-data.
	private static final String CRLF = "\r\n";
	private static final String HEADER_NAME = "Upload_0";

	public interface Callback {
		/**
		 * Called when progress is made.
		 *
		 * @return if the operation should continue (false to terminate)
		 */
		boolean onProgress(long progress, long total);

		/**
		 * Called when the task is finished or canceled.
		 *
		 * @param success if the operation was finished successfully
		 */
		void onDone(boolean success);
	}

	/**
	 * Uploads a {@link java.io.File} synchronously.
	 *
	 * @param file  the file to be uploaded
	 * @param where the destination url
	 * @return if the upload was successful
	 */
	public static boolean uploadFile(File file, String where) {
		return uploadFile(file, where, null);
	}

	/**
	 * Uploads a {@link java.io.File} asynchronously.
	 *
	 * @param file     the file to be uploaded
	 * @param where    the destination url
	 * @param callback the callback (invoked in the same thread as the one calling
	 *                 this method)
	 * @return an {@link android.os.AsyncTask} reference, you may cancel the task with this
	 * reference
	 */
	public static AsyncTask<?, ?, ?> uploadFileAsync(File file, String where, Callback callback) {
		return new FileUploadTask(file, where, callback).execute((Void[]) null);
	}

	/*
	 * Ref:
	 * http://stackoverflow.com/questions/2793150/how-to-use-java-net-urlconnection-to-fire-and-handle-http-requests/2793153#2793153
	 */
	private static boolean uploadFile(File file, String urlStr, ProgressCallback callback) {
		HttpURLConnection conn = null;
		InputStream inputStream = null;
		OutputStream outputStream = null;
		PrintWriter writer = null;

		// unique string
		String boundary = "abcde" + Long.toHexString(System.currentTimeMillis()) + "12345";

		try {
			URL uploadUrl = new URL(urlStr);
			conn = (HttpURLConnection) uploadUrl.openConnection();

			conn.setRequestMethod("POST");
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setUseCaches(false);
			conn.setChunkedStreamingMode(1024);

			conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

			inputStream = new FileInputStream(file);
			outputStream = conn.getOutputStream();
			writer = new PrintWriter(new OutputStreamWriter(outputStream), true);

			// start of binary boundary.
			writer.append("--").append(boundary).append(CRLF);
			writer.append("Content-Disposition: form-data;").append("filename=\"").append(file.getName()).append("\"; name=\"filename\"").append(CRLF);
			writer.append("Content-Type: ").append(URLConnection.guessContentTypeFromName(file.getName())).append(CRLF);
			writer.append("Content-Transfer-Encoding: binary").append(CRLF);
			writer.append(CRLF).flush();

			byte[] buffer = new byte[1024];
			long progress = 0;
			for (int read = 0; (read = inputStream.read(buffer)) != -1; ) {
				outputStream.write(buffer, 0, read);
				progress += read;
				if (callback != null) {
					if (!callback.onProgress(progress)) {
					}
				}
			}

			outputStream.flush();

			// end of binary boundary.
			writer.append(CRLF).flush();

			// End of multipart/form-data.
			writer.append("--").append(boundary).append("--").append(CRLF);
			writer.flush();

			List<String> response = new ArrayList<String>();

			int status = conn.getResponseCode();

			if (status == HttpURLConnection.HTTP_OK) {
				BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				String line = null;
				while ((line = reader.readLine()) != null) {
					response.add(line);
				}
				reader.close();

			} else {
				throw new IOException("Server returned non-OK status: " + status);
			}

			return status == HttpURLConnection.HTTP_OK;

		} catch (IOException e) {
			Logs.e(TAG, e);
			return false;

		} finally {
			IoUtils.quietDisconnet(conn);
			IoUtils.quietClose(inputStream);
			IoUtils.quietClose(outputStream);
			IoUtils.quietClose(writer);
		}
	}

	private static void copyInputToOutputStream(InputStream source, OutputStream target, ProgressCallback callback) throws IOException {
		byte[] buffer = new byte[1024];
		long progress = 0;
		for (int read = 0; (read = source.read(buffer)) != -1; ) {
			target.write(buffer, 0, read);
			progress += read;
			if (callback != null) {
				if (!callback.onProgress(progress)) {
					return;
				}
			}
		}
	}

	private static class FileUploadTask extends AsyncTask<Void, Long, Boolean> {
		private final File mFile;
		private final String mWhere;
		private final Callback mCallback;
		private final long mFileLength;

		private FileUploadTask(File file, String where, Callback callback) {
			mFile = file;
			mFileLength = file.length();
			mWhere = where;
			mCallback = callback;
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			return uploadFile(mFile, mWhere, new ProgressCallback() {
				@Override
				public boolean onProgress(long progress) {
					publishProgress(progress, mFileLength);
					return !isCancelled();
				}
			});
		}

		@Override
		protected void onProgressUpdate(Long... values) {
			super.onProgressUpdate(values);
			if (mCallback != null && !isCancelled()) {
				if (!mCallback.onProgress(values[0], values[1])) {
					cancel(false);
				}
			}
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			if (mCallback != null) {
				mCallback.onDone(result);
			}
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			if (mCallback != null) {
				mCallback.onDone(false);
			}
		}
	}
}
