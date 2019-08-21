package com.arandroid.univoice;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.arandroid.univoice.io.SharedPreferencesManager;
import com.arandroid.univoice.model.User;
import com.arandroid.univoice.ui.adapter.LiveListRecyclerAdapter;
import com.arandroid.univoice.ui.viewholder.UserViewHolder;
import com.arandroid.univoice.utils.GenericConstants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int REQUEST_PERMISSIONS = 0;

    private FirebaseAuth mAuth;
    private List<User> users;
    private ArrayList<User> contacts;
    private LiveListRecyclerAdapter<User, UserViewHolder> adapter;
    private SharedPreferencesManager preferencesManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.bottom_bar);
        setSupportActionBar(toolbar);

        mAuth = FirebaseAuth.getInstance();
        preferencesManager = SharedPreferencesManager.getInstance(this);
        contacts = preferencesManager.read(GenericConstants.CACHED_CONTACTS, new TypeToken<ArrayList<User>>() {
        }.getType());
        if (contacts == null) {
            contacts = new ArrayList<>();
        }

        Query query = FirebaseFirestore.getInstance().collection(GenericConstants.FIREBASE_TABLE_USERS);
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        adapter = new LiveListRecyclerAdapter<>(this, User.class, UserViewHolder.class, R.layout.item_user, query);
        adapter.setItemClickListener(view -> {
            User receiver = (User) view.getTag();
            Intent intent = new Intent(MainActivity.this, ChatActivity.class);
            intent.putExtra(ChatActivity.USER_EXTRA, receiver);

            @SuppressWarnings("unchecked")
            Bundle options = ActivityOptionsCompat.makeSceneTransitionAnimation(MainActivity.this).toBundle();
            ActivityCompat.startActivity(MainActivity.this, intent, options);
        });
        users = adapter.getCurrentList();

        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                checkPermissions();
                super.onChanged();
            }
        });

        adapter.startListening();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            readContactList();
        } else {
            requestNeededPermissions();
        }
    }

    private void requestNeededPermissions() {
        List<String> neededPermissions = new LinkedList<>();
        String contactPermission = Manifest.permission.READ_CONTACTS;

        if (ContextCompat.checkSelfPermission(this, contactPermission) != PackageManager.PERMISSION_GRANTED) {
            neededPermissions.add(contactPermission);
        }

        if (neededPermissions.size() > 0) {
            ActivityCompat.requestPermissions(this, neededPermissions.toArray(new String[0]), REQUEST_PERMISSIONS);
        }
    }

    private void readContactList() {
        if (contacts.isEmpty()) {
            ContentResolver cr = getContentResolver();
            Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                    null, null, null, null);

            if ((cur != null ? cur.getCount() : 0) > 0) {
                while (cur.moveToNext()) {
                    String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                    String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                    if (cur.getInt(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                        Cursor pCur = cr.query(
                                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                                new String[]{id}, null);
                        if (pCur != null) {
                            while (pCur.moveToNext()) {
                                String phoneNo = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                                User mUser = new User();
                                mUser.setName(name);
                                mUser.setPhoneNumber(phoneNo);
                                contacts.add(mUser);
                            }
                            pCur.close();
                        }
                    }
                }
            }
            if (cur != null) {
                cur.close();
            }
            preferencesManager.write(GenericConstants.CACHED_CONTACTS, contacts);
            adapter.setFilter("");
            adapter.notifyDataSetChanged();
        }
        for (User contact : contacts) {
            for (User user : users) {
                if (user.equals(contact)) {
                    user.setName(contact.getName());
                    break;
                }
            }
        }
    }

    public Uri getPhotoUri(long contactId) {
        ContentResolver contentResolver = getContentResolver();
        Uri result = null;
        boolean ok = true;

        try {
            Cursor cursor = contentResolver.query(ContactsContract.Data.CONTENT_URI,
                    null,
                    ContactsContract.Data.CONTACT_ID
                            + "="
                            + contactId
                            + " AND "

                            + ContactsContract.Data.MIMETYPE
                            + "='"
                            + ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE
                            + "'", null, null);

            if (cursor != null) {
                if (!cursor.moveToFirst()) {
                    ok = false; // no photo
                }
                cursor.close();
            } else {
                ok = false; // error in cursor process
            }
        } catch (Exception e) {
            e.printStackTrace();
            result = null;
            ok = false;
        }

        if (ok) {
            Uri person = ContentUris.withAppendedId(
                    ContactsContract.Contacts.CONTENT_URI, contactId);
            result = Uri.withAppendedPath(person,
                    ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
        }

        return result;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:
                break;
            case R.id.action_sign_out:
                mAuth.signOut();
                backToSignIn();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSIONS) {
            checkPermissions();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void backToSignIn() {
        Intent intent = new Intent(this, SignInActivity.class);
        startActivity(intent);
        finish();
    }

}
