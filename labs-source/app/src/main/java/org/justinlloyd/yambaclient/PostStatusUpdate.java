package org.justinlloyd.yambaclient;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.marakana.android.yamba.clientlib.YambaClient;
import com.marakana.android.yamba.clientlib.YambaClientException;


public class PostStatusUpdate extends Activity {

    public static final String TAG = PostStatusUpdate.class.getName();
    private EditText editTextStatusMessage;
    private TextView textViewRemainingCharacters;
    private int maximumCharacters;
    private Button buttonPostStatus;
    private PostStatusTask postTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_status_update);

        maximumCharacters = getResources().getInteger(R.integer.maximumCharacters);
        buttonPostStatus = (Button) (findViewById(R.id.buttonPostStatus));
        buttonPostStatus.setEnabled(false);
        textViewRemainingCharacters = (TextView) (findViewById(R.id.textViewRemainingCharacters));
        editTextStatusMessage = (EditText) (findViewById(R.id.editTextStatusMessage));
        editTextStatusMessage.addTextChangedListener(new StatusMessageWatcher());
        editTextStatusMessage.setText("You've got to know when to code it, know when to push to git, know when to load it up, know when to run.");

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.post_status_update, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (postTask != null) {
            postTask.cancel(true);
        }
    }

    public void buttonPostStatus(@SuppressWarnings("UnusedParameters") View v) {
        Log.d(TAG, "Clicked the Post Status button");
        String statusMessage = editTextStatusMessage.getText().toString();
        if (statusMessage.isEmpty()) {
            Log.d(TAG, "Will not post to Yamba service - status message is empty.");
            Toast.makeText(this, "Status message cannot be empty.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (statusMessage.length() > maximumCharacters) {
            Log.d(TAG, "Will not post to Yamba service - status message is too long.");
            Toast.makeText(this, "Status message is too long.", Toast.LENGTH_SHORT).show();
            return;
        }

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if ((networkInfo == null) || (!networkInfo.isConnected())) {
            Log.d(TAG, "Status message cannot be posted, no network connectivity.");
            Toast.makeText(this, "No network connectivity.", Toast.LENGTH_SHORT).show();
            return;
        }

        postStatusMessageAsyncTask(statusMessage);
    }

    private void postStatusMessageAsyncTask(String statusMessage) {
        postTask = new PostStatusTask(this);
        postTask.execute(statusMessage);
    }

    private void postStatusMessageBackgroundThread(final String statusMessage) {

        new Thread() {
            public void run() {
                YambaClient yc = new YambaClient("student", "password");
                try {
                    long startTime = System.currentTimeMillis();
                    yc.postStatus(statusMessage);
                    long endTime = System.currentTimeMillis();
                    final long totalTime = endTime - startTime;
                    Log.d(TAG, String.format("Posted the status message in %d ms", totalTime));
                    Runnable updateUIRunnable = new Runnable() {
                        public void run() {
                            String text = String.format(getResources().getString(R.string.time_to_post), totalTime);
                            Toast.makeText(PostStatusUpdate.this, text, Toast.LENGTH_SHORT).show();
                        }
                    };

                    PostStatusUpdate.this.runOnUiThread(updateUIRunnable);
                } catch (YambaClientException e) {
                    Log.e(TAG, e.toString());
                }

            }
        }.start();

        Runnable runOnUIThread = new Runnable() {
            public void run() {
            }
        };
    }


    private class StatusMessageWatcher implements TextWatcher {
        private int defaultRemainingCharactersColor = textViewRemainingCharacters.getCurrentTextColor();
        private int warningRemainingCharactersColor = getResources().getColor(R.color.warningMessageLengthColor);
        private int errorRemainingCharactersColor = getResources().getColor(R.color.errorMessageLengthColor);
        private int warningLength = getResources().getInteger(R.integer.warningMessageLength);

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String statusMessage = s.toString();
            int messageLength = s.length();
            int charactersRemaining = maximumCharacters - messageLength;
            textViewRemainingCharacters.setText(String.valueOf(charactersRemaining));
            Log.d(TAG, String.format("Text changed (%d characters remaining): \"%s\"", charactersRemaining, statusMessage));
            if (charactersRemaining < 0) {
                Log.d(TAG, String.format("Status message has %d more characters than permitted", Math.abs(charactersRemaining)));
                textViewRemainingCharacters.setTextColor(errorRemainingCharactersColor);
            } else if (charactersRemaining < warningLength) {
                Log.d(TAG, String.format("Status message has %d more characters than permitted", Math.abs(charactersRemaining)));
                textViewRemainingCharacters.setTextColor(warningRemainingCharactersColor);
            } else {
                textViewRemainingCharacters.setTextColor(defaultRemainingCharactersColor);
            }

            buttonPostStatus.setEnabled(isValidStatusMessage(statusMessage));
        }

        @Override
        public void afterTextChanged(Editable s) {

        }

        private boolean isValidStatusMessage(String msg) {
            if (msg.isEmpty()) {
                return false;
            }

            if (msg.length() > maximumCharacters) {
                return false;
            }

            return true;
        }
    }
}
