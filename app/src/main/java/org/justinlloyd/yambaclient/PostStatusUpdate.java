package org.justinlloyd.yambaclient;

import android.app.Activity;
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

    private EditText editTextStatusMessage;
    private TextView textViewRemainingCharacters;
    private int maximumCharacters;
    private Button buttonPostStatus;

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

    public void buttonPostStatus(View v) {
        Log.d(PostStatusUpdate.class.getName(), "Clicked the Post Status button");
        String statusMessage = editTextStatusMessage.getText().toString();
        if (statusMessage.isEmpty()) {
            Log.d(PostStatusUpdate.class.getName(), "Will not post to Yamba service - status message is empty.");
            Toast.makeText(this, "Status message cannot be empty.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (statusMessage.length() > maximumCharacters) {
            Log.d(PostStatusUpdate.class.getName(), "Will not post to Yamba service - status message is too long.");
            Toast.makeText(this, "Status message is too long.", Toast.LENGTH_SHORT).show();
            return;
        }

        YambaClient yc = new YambaClient("student", "password");
        try {
            yc.postStatus(statusMessage);
        } catch (YambaClientException e) {
            e.printStackTrace();
        }
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
            Log.d(PostStatusUpdate.class.getName(), String.format("Text changed (%d characters remaining): \"%s\"", charactersRemaining, statusMessage));
            if (charactersRemaining < 0) {
                Log.d(PostStatusUpdate.class.getName(), String.format("Status message has %d more characters than permitted", Math.abs(charactersRemaining)));
                textViewRemainingCharacters.setTextColor(errorRemainingCharactersColor);
                buttonPostStatus.setEnabled(false);
            } else if (charactersRemaining < warningLength) {
                Log.d(PostStatusUpdate.class.getName(), String.format("Status message has %d more characters than permitted", Math.abs(charactersRemaining)));
                textViewRemainingCharacters.setTextColor(warningRemainingCharactersColor);
            } else {
                buttonPostStatus.setEnabled(true);
                textViewRemainingCharacters.setTextColor(defaultRemainingCharactersColor);
            }
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
