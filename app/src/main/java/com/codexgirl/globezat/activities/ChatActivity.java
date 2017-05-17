package com.codexgirl.globezat.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v4.app.NavUtils;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.codexgirl.globezat.message.FriendlyMessage;
import com.codexgirl.globezat.R;
import com.codexgirl.globezat.models.User;
import com.codexgirl.globezat.utils.Utils;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;

import javax.net.ssl.HttpsURLConnection;

import de.hdodenhof.circleimageview.CircleImageView;

/*chatbox implementation starts now */
public class ChatActivity extends AppCompatActivity {

    private static final String GLOBEZAT_SHARE_HASHTAG = " #GlobezatApp";
    private static final String TAG = "ChatActivity";
    private static final String KEY = "AIzaSyCMaUkmY08uucEBzU-o_RWxlQCPqCGJ8Pg";
    public String MESSAGES_CHILD = "";

    private Button mSendButton;
    private RecyclerView mMessageRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private FirebaseRecyclerAdapter<FriendlyMessage, MessageViewHolder> mFirebaseAdapter;
    private DatabaseReference mFirebaseDatabaseReference;
    private EditText mMessageEditText;

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageTextView;
        TextView messengerTextView;
        CircleImageView messengerImageView;

        public MessageViewHolder(View v) {
            super(v);
            messageTextView = (TextView) itemView.findViewById(R.id.messageTextView);
            messengerTextView = (TextView) itemView.findViewById(R.id.messengerTextView);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        User user = getIntent().getParcelableExtra("CHAT");
        String[] emails = {user.getEmail(), Utils.readPreferenceData(getApplicationContext(), "EMAIL", "")};
        Arrays.sort(emails);
        MESSAGES_CHILD = dbCreate(emails);

        mMessageRecyclerView = (RecyclerView) findViewById(R.id.messageRecyclerView);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setStackFromEnd(true);

        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mFirebaseAdapter = new FirebaseRecyclerAdapter<FriendlyMessage, MessageViewHolder>(
                FriendlyMessage.class,
                R.layout.item_layout,
                MessageViewHolder.class,
                mFirebaseDatabaseReference.child("messages").child(MESSAGES_CHILD)) {

            @Override
            protected FriendlyMessage parseSnapshot(DataSnapshot snapshot) {
                FriendlyMessage friendlyMessage = super.parseSnapshot(snapshot);
                return friendlyMessage;
            }

            @Override
            protected void populateViewHolder(final MessageViewHolder viewHolder,
                                              FriendlyMessage friendlyMessage, int position) {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(ChatActivity.this);

                if (friendlyMessage.getMessage() != null) {
                    /**
                     * it is AsyncTask for language translation
                     * TranslateTask ctor MessageViewHolder object to set text after convert
                     * execute() method take 3 parameter
                     * 1. message - which u want to convert
                     * 2. to - in which lang u want to convert this text
                     * 3. from - current lang of text
                     *
                     */
                    new TranslateTask(viewHolder).execute(friendlyMessage.getMessage(), sharedPreferences.getString(getString(R.string.pref_language_key),getString(R.string.pref_lang_hindi_value)), friendlyMessage.getLanguage());
                }

                viewHolder.messengerTextView.setText(friendlyMessage.getLanguage());
            }
        };

        mFirebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int friendlyMessageCount = mFirebaseAdapter.getItemCount();
                int lastVisiblePosition = mLinearLayoutManager.findLastCompletelyVisibleItemPosition();
                // If the recycler view is initially being loaded or the user is at the bottom of the list, scroll
                // to the bottom of the list to show the newly added message.
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (friendlyMessageCount - 1) && lastVisiblePosition == (positionStart - 1))) {
                    mMessageRecyclerView.scrollToPosition(positionStart);
                }
            }
        });

        mMessageRecyclerView.setLayoutManager(mLinearLayoutManager);
        mMessageRecyclerView.setAdapter(mFirebaseAdapter);


        mMessageEditText = (EditText) findViewById(R.id.messageEditText);
        mMessageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    mSendButton.setEnabled(true);
                } else {
                    mSendButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });


        mSendButton = (Button) findViewById(R.id.sendButton);
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(ChatActivity.this);
                FriendlyMessage friendlyMessage = new FriendlyMessage(mMessageEditText.getText().toString(),sharedPreferences.getString(getString(R.string.pref_language_key),getString(R.string.pref_lang_hindi_value)), Utils.readPreferenceData(getApplicationContext(), "EMAIL", ""));
                mFirebaseDatabaseReference.child("messages").child(MESSAGES_CHILD).push().setValue(friendlyMessage);
                mMessageEditText.setText("");
            }
        });
    }

    private String dbCreate(String... strings) {
        strings[0] = strings[0].replace("@", "");
        strings[0] = strings[0].replace(".", "");
        strings[1] = strings[1].replace("@", "");
        strings[1] = strings[1].replace(".", "");
        return strings[0] + strings[1];
    }

    class TranslateTask extends AsyncTask<String, String, String> {

        private MessageViewHolder messageViewHolder;

        public TranslateTask(MessageViewHolder messageViewHolder) {
            this.messageViewHolder = messageViewHolder;
        }

        @Override
        protected String doInBackground(String... data) {
            String text = data[0];
            String to = data[1];
            String from = data[2];

            StringBuilder result = new StringBuilder();
            try {
                String encodedText = URLEncoder.encode(text, "UTF-8");
                String urlStr = "https://www.googleapis.com/language/translate/v2?key=" + KEY + "&q=" + encodedText + "&target=" + to + "&source=" + from;

                URL url = new URL(urlStr);

                HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                InputStream stream;
                if (conn.getResponseCode() == 200) //success
                {
                    stream = conn.getInputStream();
                } else
                    stream = conn.getErrorStream();

                BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

                JsonParser parser = new JsonParser();

                JsonElement element = parser.parse(result.toString());

                if (element.isJsonObject()) {
                    JsonObject obj = element.getAsJsonObject();
                    if (obj.get("error") == null) {
                        String translatedText = obj.get("data").getAsJsonObject().
                                get("translations").getAsJsonArray().
                                get(0).getAsJsonObject().
                                get("translatedText").getAsString();
                        return translatedText;

                    }
                }

                if (conn.getResponseCode() != 200) {
                    System.err.println(result);
                }

            } catch (IOException | JsonSyntaxException ex) {
                System.err.println(ex.getMessage());
            }

            return null;

        }

        @Override
        protected void onPostExecute(String s) {
            if(s != null) {
                messageViewHolder.messageTextView.setText(s);
            }
        }
    }

    private Intent createShareForecastIntent()
    {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(ChatActivity.this);
        FriendlyMessage friendlyMessage = new FriendlyMessage(mMessageEditText.getText().toString(),sharedPreferences.getString(getString(R.string.pref_language_key),getString(R.string.pref_lang_hindi_value)), Utils.readPreferenceData(getApplicationContext(), "EMAIL", ""));
        mFirebaseDatabaseReference.child("messages").child(MESSAGES_CHILD).push().setValue(friendlyMessage);
        mMessageEditText.setText("");
        Intent shareIntent = ShareCompat.IntentBuilder.from(this)
                .setType("text/plain")
                .setText("Hey, Brick the barrier and download the app!!" + GLOBEZAT_SHARE_HASHTAG)
                .getIntent();
        return shareIntent;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat_menu,menu);
        MenuItem menuItem = menu.findItem(R.id.action_share);
        menuItem.setIntent(createShareForecastIntent());
        return true;
    }

}
