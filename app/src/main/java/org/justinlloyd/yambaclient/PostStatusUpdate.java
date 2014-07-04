package org.justinlloyd.yambaclient;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;


public class PostStatusUpdate extends Activity {

    private EditText editTextStatusMessage;
    private TextView textViewRemainingCharacters;
    private int maximumCharacters;
    private int defaultRemainingCharactersColor;
    private int warningLength;
    private int warningRemainingCharactersColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_status_update);

        textViewRemainingCharacters = (TextView) (findViewById(R.id.textViewRemainingCharacters));
        defaultRemainingCharactersColor = textViewRemainingCharacters.getCurrentTextColor();
        warningRemainingCharactersColor = getResources().getColor(R.color.warningColor);
        maximumCharacters = getResources().getInteger(R.integer.maximumCharacters);
        warningLength = getResources().getInteger(R.integer.warningLength);
        editTextStatusMessage = (EditText) (findViewById(R.id.editTextStatusMessage));
        editTextStatusMessage.setText("You've got to know when to code it, know when to push to git, know when to load it up, know when to run.");
        editTextStatusMessage.addTextChangedListener(new TextWatcher() {
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
                    textViewRemainingCharacters.setTextColor(Color.RED);
                }
                else if (charactersRemaining < warningLength)
                {
                    Log.d(PostStatusUpdate.class.getName(), String.format("Status message has %d more characters than permitted", Math.abs(charactersRemaining)));
                    textViewRemainingCharacters.setTextColor(Color.YELLOW);
                }
                else
                {
                    textViewRemainingCharacters.setTextColor(defaultRemainingCharactersColor);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
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
    }

}
