package org.justinlloyd.yambaclient;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.marakana.android.yamba.clientlib.YambaClient;
import com.marakana.android.yamba.clientlib.YambaClientException;

public class PostStatusTask extends AsyncTask<String, Void, Long> {

    public static final long POST_SUCCESS = 0L;
    public static final long POST_FAILED = -1L;
    public static final long POST_FAILED_USERNAME_EMPTY = -2L;
    public static final long POST_FAILED_PASSWORD_EMPTY = -3L;
    private static final String TAG = PostStatusTask.class.getName();
    private final Activity context;
    private ProgressDialog progress;

    public PostStatusTask(Activity context) {
        this.context = context;
    }

    // runs on the UI thread prior to doInBackground
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Log.d(PostStatusTask.class.getName(), "onPreExecute");
        progress = ProgressDialog.show(context, "Posting", "Please wait...");
        progress.setCancelable(true);
    }

    @Override
    protected Long doInBackground(String... params) {
        YambaClient yc = new YambaClient("student", "password");
        String username = PreferenceManager.getDefaultSharedPreferences(context).getString("username", "");
        if (username.isEmpty()) {
            Log.e(TAG, "Username preference is empty, cannot post to server.");
            return POST_FAILED_USERNAME_EMPTY;
        }

        Log.d(TAG, String.format("Username is set to: \"%s\"", username));
        String password = PreferenceManager.getDefaultSharedPreferences(context).getString("password", "");
        Log.d(TAG, String.format("Password is set to: \"%s\"", password));
        if (password.isEmpty()) {
            Log.e(TAG, "Password preference is empty, cannot post to server.");
            return POST_FAILED_PASSWORD_EMPTY;
        }

        try {
            long startTime = System.currentTimeMillis();
            yc.postStatus(params[0]);
            long endTime = System.currentTimeMillis();
            final long totalTime = endTime - startTime;
            Log.d(TAG, String.format("Posted the status message in %d ms", totalTime));
            return POST_SUCCESS;
        } catch (YambaClientException e) {
            Log.d(TAG, e.toString());
            return POST_FAILED;
        }
    }

    // runs on the UI thread after doInBackground has finished
    @Override
    protected void onPostExecute(Long result) {
        super.onPostExecute(result);
        Log.d(PostStatusTask.class.getName(), "onPostExecute");
        progress.dismiss();
        if (context != null && result != null) {
            Log.d(TAG, String.format("Post message async task completed with result code: %d", result));
            if (result == POST_SUCCESS) {
                Toast.makeText(context, "Successfully posted your status update.", Toast.LENGTH_SHORT).show();
            } else if (result == POST_FAILED) {
                Toast.makeText(context, "Failed to post to the remote server.", Toast.LENGTH_SHORT).show();
            } else if (result == POST_FAILED_USERNAME_EMPTY) {
                Toast.makeText(context, "Username is empty.", Toast.LENGTH_SHORT).show();
            } else if (result == POST_FAILED_PASSWORD_EMPTY) {
                Toast.makeText(context, "Password is empty.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
