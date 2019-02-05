
package com.google.firebase.codelab.friendlychat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.appindexing.Action;
import com.google.firebase.appindexing.Indexable;
import com.google.firebase.appindexing.builders.Indexables;
import com.google.firebase.appindexing.builders.PersonBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.codelab.friendlychat.Utils.FirebaseUtils;
import com.google.firebase.codelab.friendlychat.Utils.ManageFileUtils;
import com.google.firebase.codelab.friendlychat.callback.OnMessageImageCallback;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class CustomMainActivity extends AppCompatActivity
        implements GoogleApiClient.OnConnectionFailedListener, OnMessageImageCallback {

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static final int REQUEST_CAMERA = 2;
    private static String[] PERMISSIONS_STORAGE = {
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @BindView(R2.id.adView)
    AdView mAdView;

    @BindView(R2.id.progressBar)
    ProgressBar mProgressBar;

    @BindView(R2.id.messageRecyclerView)
    RecyclerView mMessageRecyclerView;

    @BindView(R2.id.messageEditText)
    EditText mMessageEditText;

    @BindView(R2.id.sendButton)
    Button mSendButton;

    @BindView(R2.id.addMessageImageView)
    ImageView mAddMessageImageView;

    private LinearLayoutManager mLinearLayoutManager;

    private static final String ARG_IMAGE_URL = "arg_image_url";
    private static final String TAG = "MainActivity";
    public static final String MESSAGES_CHILD = "messages";
    private static final int REQUEST_INVITE = 1;
    private static final int REQUEST_IMAGE = 2;
    private static final int REQUEST_FILE_CODE = 4;
    private static final int REQUEST_CAMERA_PHOTO = 3;
    private static final String LOADING_IMAGE_URL = "https://www.google.com/images/spin-32.gif";
    public static final int DEFAULT_MSG_LENGTH_LIMIT = 10;
    public static final String ANONYMOUS = "anonymous";
    private static final String MESSAGE_SENT_EVENT = "message_sent";
    private String mUsername;
    private String mPhotoUrl;
    private String mUserType;
    private SharedPreferences mSharedPreferences;
    private FirebaseUtils firebaseUtils;
    private GoogleApiClient mGoogleApiClient;
    private static final String MESSAGE_URL = "http://friendlychat.firebase.google.com/message/";
    //File
    private File filePathImageCamera;
    private final static String PROFILE_IMAGE_FILENAME = "FriendlyChatDemo.jpg";
    private final static String FILES_DIR = "CHAT_FILES";


    // Firebase instance variables
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mFirebaseDatabaseReference;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    private FirebaseRecyclerAdapter<FriendlyMessage, CustomMessageViewHolder>
            mFirebaseAdapter;

    private ChatFirebaseAdapter chatFirebaseAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        initFirebaseAuth();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();
        initFirebaseRemoteConfig();

        //AddView
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        setMessageRecyclerViewAdapter();
        setMessageInputText();


        mSendButton.setOnClickListener(view -> {
            // Send messages on click.
            sendUserMessage();
        });

        mAddMessageImageView.setOnClickListener(view -> {
            // Select image for image message on click.
            //selectImage();
            showPhotoDialog();
        });
    }


    private void initFirebaseRemoteConfig() {
        // Initialize Firebase Remote Config.
        firebaseUtils = new FirebaseUtils(mFirebaseRemoteConfig, mMessageEditText);
        firebaseUtils.setUpFirebaseRemoteConfig();
    }

    private void initFirebaseAuth() {
        mUsername = ANONYMOUS;
        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser == null) {
            // Not signed in, launch the Sign In activity
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return;
        } else {
            mUsername = mFirebaseUser.getDisplayName();
            if (mFirebaseUser.getPhotoUrl() != null) {
                mPhotoUrl = mFirebaseUser.getPhotoUrl().toString();
            }
        }
    }

    private void setMessageRecyclerViewAdapter() {
        // Initialize ProgressBar and RecyclerView.
        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setStackFromEnd(true);
        mMessageRecyclerView.setLayoutManager(mLinearLayoutManager);

        mProgressBar.setVisibility(ProgressBar.INVISIBLE);
        // New child entries
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
        SnapshotParser<FriendlyMessage> parser = dataSnapshot -> {
            FriendlyMessage friendlyMessage = dataSnapshot.getValue(FriendlyMessage.class);
            if (friendlyMessage != null) {
                friendlyMessage.setId(dataSnapshot.getKey());
            }
            return friendlyMessage;
        };


        DatabaseReference messagesRef = mFirebaseDatabaseReference.child(MESSAGES_CHILD);


        FirebaseRecyclerOptions<FriendlyMessage> options =
                new FirebaseRecyclerOptions.Builder<FriendlyMessage>()
                        .setQuery(messagesRef, parser)
                        .build();

        chatFirebaseAdapter = new ChatFirebaseAdapter(options, mUsername, this, this);
        chatFirebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int friendlyMessageCount = chatFirebaseAdapter.getItemCount();
                int lastVisiblePosition =
                        mLinearLayoutManager.findLastCompletelyVisibleItemPosition();
                // If the recycler view is initially being loaded or the
                // user is at the bottom of the list, scroll to the bottom
                // of the list to show the newly added message.
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (friendlyMessageCount - 1) &&
                                lastVisiblePosition == (positionStart - 1))) {
                    mMessageRecyclerView.scrollToPosition(positionStart);
                }
            }
        });

        mMessageRecyclerView.setAdapter(chatFirebaseAdapter);
    }

    private void setMessageInputText() {
        mMessageEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(mSharedPreferences
                .getInt(CodelabPreferences.FRIENDLY_MSG_LENGTH, DEFAULT_MSG_LENGTH_LIMIT))});
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
    }


    @SuppressLint("TimberArgCount")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Timber.d(TAG, "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);
        switch (requestCode) {
            case RESULT_CANCELED:
                return;
            case REQUEST_IMAGE:
                performActionForSelectImage(resultCode, data);
                break;
            case REQUEST_CAMERA_PHOTO:
                performActionForUseCamera(resultCode, data);
                break;
            case REQUEST_INVITE:
                performActionForInvite(resultCode, data);
                break;
            case REQUEST_FILE_CODE:
                performActionForSelectFile(resultCode, data);
        }
    }

    private void performActionForSelectFile(int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (data != null) {
                final Uri uri = data.getData();
                Timber.d(uri.toString());

                FriendlyMessage tempMessage = new FriendlyMessage(null, mUsername, mPhotoUrl,
                        null, LOADING_IMAGE_URL);
                mFirebaseDatabaseReference.child(MESSAGES_CHILD).push()
                        .setValue(tempMessage, (databaseError, databaseReference) -> {
                            if (databaseError == null) {
                                String key = databaseReference.getKey();
                                StorageReference storageReference =
                                        FirebaseStorage.getInstance()
                                                .getReference(mFirebaseUser.getUid())
                                                .child(key)
                                                .child(uri.getLastPathSegment());

                                putFileInStorage(storageReference, uri, key);
                            } else {
                                Timber.w("Unable to write message to database. %s",
                                        databaseError);
                            }
                        });
            }
        }

    }

    private void performActionForInvite(int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            // Check how many invitations were sent and log.
            String[] ids = AppInviteInvitation.getInvitationIds(resultCode, data);
            Timber.d("Invitations sent: %d ",  ids.length);
        } else {
            // Sending failed or it was canceled, show failure message to the user
            Timber.d("Failed to send invitation.");
        }
    }


    private void performActionForSelectImage(int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (data != null) {
                final Uri uri = data.getData();
                Log.d(TAG, "Uri: " + uri.toString());

                FriendlyMessage tempMessage = new FriendlyMessage(null, mUsername, mPhotoUrl,
                        LOADING_IMAGE_URL, null);
                mFirebaseDatabaseReference.child(MESSAGES_CHILD).push()
                        .setValue(tempMessage, (databaseError, databaseReference) -> {
                            if (databaseError == null) {
                                String key = databaseReference.getKey();
                                StorageReference storageReference =
                                        FirebaseStorage.getInstance()
                                                .getReference(mFirebaseUser.getUid())
                                                .child(key)
                                                .child(uri.getLastPathSegment());

                                putImageInStorage(storageReference, uri, key);
                            } else {
                                Timber.w(databaseError.toException());
                            }
                        });
            }
        }
    }

    private void performActionForUseCamera(int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            File currentFile = ManageFileUtils.getFileFromAppExternalDirectory(PROFILE_IMAGE_FILENAME);
            if (currentFile == null) {
                return;
            }
            FriendlyMessage tempMessage = new FriendlyMessage(null, mUsername, mPhotoUrl,
                    LOADING_IMAGE_URL, null);
            mFirebaseDatabaseReference.child(MESSAGES_CHILD).push()
                    .setValue(tempMessage, (databaseError, databaseReference) -> {
                        if (databaseError == null) {
                            String key = databaseReference.getKey();
                            StorageReference storageReference =
                                    FirebaseStorage.getInstance()
                                            .getReference(mFirebaseUser.getUid())
                                            .child(key)
                                            .child(currentFile.getName());

                            putImageInStorage(storageReference, Uri.fromFile(currentFile), key);
                        } else {
                            Timber.d(databaseError.toException());
                        }
                    });
        }
    }


    private void putImageInStorage(final StorageReference storageReference, Uri uri, final String key) {
        storageReference.putFile(uri).continueWithTask(task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }
            return storageReference.getDownloadUrl();
        }).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Uri downUri = task.getResult();
                if (downUri != null) {
                    FriendlyMessage friendlyMessage =
                            new FriendlyMessage(null, mUsername, mPhotoUrl,
                                    downUri.toString(), null);
                    mFirebaseDatabaseReference.child(MESSAGES_CHILD).child(key)
                            .setValue(friendlyMessage);
                }
                Timber.d(downUri.toString());
            }
            Timber.d(task.getException());
        });


        storageReference.putFile(uri).addOnCompleteListener(CustomMainActivity.this,
                task -> {
                    if (task.isSuccessful()) {
                        FriendlyMessage friendlyMessage =
                                new FriendlyMessage(null, mUsername, mPhotoUrl,
                                        storageReference.getDownloadUrl()
                                                .toString(), null);
                        mFirebaseDatabaseReference.child(MESSAGES_CHILD).child(key)
                                .setValue(friendlyMessage);
                    } else {
                        Timber.d(task.getException());
                    }
                });
    }

    private void putFileInStorage(final StorageReference storageReference, Uri uri, final String key) {
        storageReference.putFile(uri).continueWithTask(task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }
            return storageReference.getDownloadUrl();
        }).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Uri downUri = task.getResult();
                if (downUri != null) {
                    FriendlyMessage friendlyMessage =
                            new FriendlyMessage(null, mUsername, mPhotoUrl,
                                    null, downUri.toString());
                    mFirebaseDatabaseReference.child(MESSAGES_CHILD).child(key)
                            .setValue(friendlyMessage);
                }
                Timber.d(downUri.toString());
            }
            Timber.d(task.getException());
        });


        storageReference.putFile(uri).addOnCompleteListener(CustomMainActivity.this,
                task -> {
                    if (task.isSuccessful()) {
                        FriendlyMessage friendlyMessage =
                                new FriendlyMessage(null, mUsername, mPhotoUrl,
                                        null, storageReference.getDownloadUrl()
                                        .toString());
                        mFirebaseDatabaseReference.child(MESSAGES_CHILD).child(key)
                                .setValue(friendlyMessage);
                    } else {
                        Timber.d(task.getException());
                    }
                });
    }


    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in.
        // TODO: Add code to check if user is signed in.
    }

    @Override
    public void onPause() {
        if (mAdView != null) {
            mAdView.pause();
        }
        chatFirebaseAdapter.stopListening();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        chatFirebaseAdapter.startListening();
        if (mAdView != null) {
            mAdView.resume();
        }
    }

    @Override
    public void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.crash_menu:
                Timber.d("Crashlytics Crash button clicked");
                causeCrash();
                return true;
            case R.id.fresh_config_menu:
                firebaseUtils.fetchConfig();
                return true;
            case R.id.sign_out_menu:
                mFirebaseAuth.signOut();
                Auth.GoogleSignInApi.signOut(mGoogleApiClient);
                mUsername = ANONYMOUS;
                startActivity(new Intent(this, SignInActivity.class));
                finish();
                return true;
            case R.id.invite_menu:
                sendInvitation();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Timber.d(connectionResult.toString());
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }

    private void sendUserMessage() {
        FriendlyMessage friendlyMessage = new
                FriendlyMessage(mMessageEditText.getText().toString(),
                mUsername,
                mPhotoUrl,
                null /* no image */,
                null);
        mFirebaseDatabaseReference.child(MESSAGES_CHILD)
                .push().setValue(friendlyMessage);
        mMessageEditText.setText("");
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_IMAGE);
    }

    private void selectFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, REQUEST_FILE_CODE);
    }

    private void takeImageFromCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        String photoName = DateFormat.format("yyyy-MM-dd_hhmmss", new Date()).toString();
        File capturedImageFile = ManageFileUtils.getFileFromAppExternalDirectory(PROFILE_IMAGE_FILENAME);
        if (capturedImageFile == null) {
            return;
        }

        Uri photoURI = FileProvider.getUriForFile(CustomMainActivity.this, getApplicationContext().getPackageName() + ".my.package.name.provider", capturedImageFile);
        intent = intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
        startActivityForResult(intent, REQUEST_CAMERA_PHOTO);
    }

    private void showPhotoDialog() {
        AlertDialog.Builder pictureDialog = new AlertDialog.Builder(this);
        pictureDialog.setTitle("Select Action");
        String[] pictureDialogItems = {
                "Select photo from gallery",
                "Capture photo from camera",
                "Select file"};
        pictureDialog.setItems(pictureDialogItems,
                (dialog, which) -> {
                    switch (which) {
                        case 0:
                            selectImage();
                            break;
                        case 1:
                            verifyStoragePermissions();
                            break;
                        case 2:
                            selectFile();
                    }
                });
        pictureDialog.show();
    }

    // Index messages
    private Indexable getMessageIndexable(FriendlyMessage friendlyMessage) {
        PersonBuilder sender = Indexables.personBuilder()
                .setIsSelf(mUsername.equals(friendlyMessage.getName()))
                .setName(friendlyMessage.getName())
                .setUrl(MESSAGE_URL.concat(friendlyMessage.getId() + "/sender"));

        PersonBuilder recipient = Indexables.personBuilder()
                .setName(mUsername)
                .setUrl(MESSAGE_URL.concat(friendlyMessage.getId() + "/recipient"));

        Indexable messageToIndex = Indexables.messageBuilder()
                .setName(friendlyMessage.getText())
                .setUrl(MESSAGE_URL.concat(friendlyMessage.getId()))
                .setSender(sender)
                .setRecipient(recipient)
                .build();

        return messageToIndex;
    }

    //Log view actions
    private Action getMessageViewAction(FriendlyMessage friendlyMessage) {
        return new Action.Builder(Action.Builder.VIEW_ACTION)
                .setObject(friendlyMessage.getName(), MESSAGE_URL.concat(friendlyMessage.getId()))
                .setMetadata(new Action.Metadata.Builder().setUpload(false))
                .build();
    }

    //Send invitation
    private void sendInvitation() {
        Intent intent = new AppInviteInvitation.IntentBuilder(getString(R.string.invitation_title))
                .setMessage(getString(R.string.invitation_message))
                .setCallToActionText(getString(R.string.invitation_cta))
                .build();
        startActivityForResult(intent, REQUEST_INVITE);
    }

    private void causeCrash() {
        throw new NullPointerException("Fake null pointer exception");
    }


    public void verifyStoragePermissions() {

        int permission = ActivityCompat.checkSelfPermission(CustomMainActivity.this, Manifest.permission.CAMERA);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    CustomMainActivity.this,
                    new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA);
        } else {

            // Check if we have write permission
            permission = ActivityCompat.checkSelfPermission(CustomMainActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);

            if (permission != PackageManager.PERMISSION_GRANTED) {
                // We don't have permission so prompt the user
                ActivityCompat.requestPermissions(
                        CustomMainActivity.this,
                        PERMISSIONS_STORAGE,
                        REQUEST_EXTERNAL_STORAGE);
            } else {
                // we already have permission, lets go ahead and call camera intent
                takeImageFromCamera();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_EXTERNAL_STORAGE:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                    takeImageFromCamera();
                }
                break;
        }
    }

    @Override
    public void onMessageImageClicked(String imageUrl) {
        Intent intent = new Intent(this, MessageImagePreviewActivity.class);
        Bundle passDataBundle = new Bundle();
        passDataBundle.putString(ARG_IMAGE_URL, imageUrl);
        intent.putExtras(passDataBundle);
        startActivity(intent);
    }
}
