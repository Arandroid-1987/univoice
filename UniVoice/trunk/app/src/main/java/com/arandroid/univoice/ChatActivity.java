package com.arandroid.univoice;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.transition.Transition;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.arandroid.univoice.firebase.MessageManager;
import com.arandroid.univoice.model.Message;
import com.arandroid.univoice.model.User;
import com.arandroid.univoice.ui.adapter.LiveListRecyclerAdapter;
import com.arandroid.univoice.ui.viewholder.MessageViewHolder;
import com.arandroid.univoice.utils.GenericConstants;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NavUtils;
import androidx.core.app.TaskStackBuilder;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ChatActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {
    private static final String TAG = "ChatActivity";
    private static final int REQUEST_PERMISSIONS = 0;
    private static final int REQ_CODE_SPEECH_INPUT = 100;
    private static final String BASE_URL_TRANSLATE = "http://18.217.136.109:3000/translate?";
    private static final String UTTERANCE_ID = "com.arandroid.univoice";

    public static final String USER_EXTRA = "USER_EXTRA";

    private TextToSpeech tts;
    private FirebaseAuth mAuth;
    private User receiver;
    private FloatingActionButton actionPlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_chat);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        receiver = (User) getIntent().getSerializableExtra(USER_EXTRA);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            if (receiver.getName() != null) {
                actionBar.setTitle(receiver.getName());
            } else {
                actionBar.setTitle(receiver.getPhoneNumber());
            }
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            addTransitionListener();
        } else {
            init();
        }
    }

    private void init() {
        mAuth = FirebaseAuth.getInstance();
        tts = new TextToSpeech(this, this);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> checkPermissions());

        Query query = FirebaseFirestore.getInstance().collection(MessageManager.getSenderCollection(mAuth.getUid(), receiver.getUid())).orderBy(Message.DATE_KEY, Query.Direction.DESCENDING);
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        LiveListRecyclerAdapter<Message, MessageViewHolder> adapter = new LiveListRecyclerAdapter<>(this, Message.class, MessageViewHolder.class, R.layout.item_message, query);
        adapter.setItemClickListener(view -> {
            Message message = (Message) view.getTag();
            translate(message.getMessage(), message.getOriginalLocale(), Locale.getDefault().getLanguage());
            if (view instanceof FloatingActionButton) {
                actionPlay = (FloatingActionButton) view;
            } else {
                actionPlay = view.findViewById(R.id.action_play);
            }
            actionPlay.setImageResource(R.drawable.baseline_pause_white_48);
        });
        adapter.startListening();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private boolean addTransitionListener() {
        final Transition transition = getWindow().getEnterTransition();

        if (transition != null) {
            // There is an entering shared element transition so add a listener to it
            transition.addListener(new Transition.TransitionListener() {
                @Override
                public void onTransitionEnd(Transition transition) {
                    // As the transition has ended, we can now load the full-size image
                    init();

                    // Make sure we remove ourselves as a listener
                    transition.removeListener(this);
                }

                @Override
                public void onTransitionStart(Transition transition) {
                    // No-op
                }

                @Override
                public void onTransitionCancel(Transition transition) {
                    // Make sure we remove ourselves as a listener
                    transition.removeListener(this);
                }

                @Override
                public void onTransitionPause(Transition transition) {
                    // No-op
                }

                @Override
                public void onTransitionResume(Transition transition) {
                    // No-op
                }
            });
            return true;
        }

        // If we reach here then we have not added a listener
        return false;
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            promptSpeechInput();
        } else {
            requestNeededPermissions();
        }
    }

    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.speech_prompt));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(), getString(R.string.speech_not_supported), Toast.LENGTH_SHORT).show();
        }
    }

    private void requestNeededPermissions() {
        List<String> neededPermissions = new LinkedList<>();
        String storagePermission = android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
        String recordPermission = Manifest.permission.RECORD_AUDIO;

        if (ContextCompat.checkSelfPermission(this, storagePermission) != PackageManager.PERMISSION_GRANTED) {
            neededPermissions.add(storagePermission);
        }
        if (ContextCompat.checkSelfPermission(this, recordPermission) != PackageManager.PERMISSION_GRANTED) {
            neededPermissions.add(recordPermission);
        }

        if (neededPermissions.size() > 0) {
            ActivityCompat.requestPermissions(this, neededPermissions.toArray(new String[0]), REQUEST_PERMISSIONS);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_CODE_SPEECH_INPUT) {
            if (resultCode == RESULT_OK && null != data) {
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                String sourceText = result.get(0);
                Message message = new Message();
                message.setDate(GenericConstants.FIREBASE_DATE_FORMATTER.format(new Date()));
                message.setMessage(sourceText);
                message.setOriginalLocale(Locale.getDefault().getLanguage());
                message.setSenderUid(mAuth.getUid());
                message.setReceiverUid(receiver.getUid());
                MessageManager.getInstance().writeMessage(message);
            }
        }
    }

    private void translate(String sourceText, String fromLang, String toLang) {
        toLang = "en";
        String url = BASE_URL_TRANSLATE + "sl=" + fromLang + "&tl=" + toLang + "&q=" + sourceText.replaceAll(" ", "+");
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    Log.d(TAG, response);
                    read(response);
                }, error -> Log.e(TAG, error.toString()));

        queue.add(stringRequest);
    }

    private void read(String text) {
        HashMap<String, String> params = new HashMap<>();
        params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, UTTERANCE_ID);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, new Bundle(), UTTERANCE_ID);
        } else {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, params);
        }
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {

            int result = tts.setLanguage(Locale.ENGLISH);

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
            }

            tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override
                public void onStart(String s) {

                }

                @Override
                public void onDone(String s) {
                    runOnUiThread(() -> {
                        if (actionPlay != null) {
                            actionPlay.setImageResource(R.drawable.baseline_play_arrow_white_48);
                        }
                    });
                }

                @Override
                public void onError(String s) {

                }
            });

        } else {
            Log.e("TTS", "Initilization Failed!");
        }
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        boolean res = false;
        int id = item.getItemId();
        if (id == android.R.id.home) {
            Intent upIntent = NavUtils.getParentActivityIntent(this);
            if (upIntent != null && NavUtils.shouldUpRecreateTask(this, upIntent)) {
                TaskStackBuilder.create(this)
                        .addNextIntentWithParentStack(upIntent)
                        .startActivities();
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    finishAfterTransition();
                } else {
                    finish();
                }
            }
            res = true;
        }
        return res;
    }

}
