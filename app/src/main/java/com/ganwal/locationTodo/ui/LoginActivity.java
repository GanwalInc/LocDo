package com.ganwal.locationTodo.ui;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.ganwal.locationTodo.R;
import com.ganwal.locationTodo.db.ContentProviderContract;
import com.ganwal.locationTodo.db.LocationTodo;
import com.ganwal.locationTodo.db.User;
import com.ganwal.locationTodo.service.HelperUtility;
import com.ganwal.locationTodo.service.LocationTODOServiceInterface;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;

import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity
        implements GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = LoginActivity.class.getSimpleName();

    public static final String EXTRA_USER_ID = "com.ganwal.locationTodo.user_id";
    public static final String ACCOUNT_TYPE = "com.google";
    public static final String ACCOUNT = "sync_account";
    public static final int PERMISSIONS_GET_ACCOUNTS_CODE = 200;
    public static final String PREF_TOKEN = "token";

    private Account mAccount;
    private Context mContext;

    private SignInButton mSignInButton;
    private TextView mLoginNameText;
    private GoogleApiClient mGoogleApiClient;
    private GoogleSignInOptions mGoogleSignInOptions;
    GoogleSignInAccount mSignInAccount;
    LocationTODOServiceInterface mServiceEndpoint;
    User mCurrentUser;
    private ProgressDialog mWaitDialog;

    private static int SIGN_REQUEST_CODE = 100;
    private static int SIGN_TOKEN_CODE = 200;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_login);

        String clientID = getString(R.string.google_client_id);

        mAccount = CreateSyncAccount(this);

        mGoogleSignInOptions = new GoogleSignInOptions.
                Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).
                requestEmail().
                requestIdToken(clientID).
                build();
        mGoogleApiClient = new GoogleApiClient.Builder(this).
                enableAutoManage(this, this).
                addApi(Auth.GOOGLE_SIGN_IN_API, mGoogleSignInOptions).
                build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(this.getString(R.string.locdo_webservice_base_url))
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        mServiceEndpoint = retrofit.create(LocationTODOServiceInterface.class);

        boolean signInSilently = signInSilently();
        Log.d(TAG, "onCreate: Logging in User signInSilently:"+signInSilently);
        if(!signInSilently) {
            Log.d(TAG, "onCreate: Showing Login button");
            mSignInButton = (SignInButton) findViewById(R.id.loginButton);
            mSignInButton.setSize(SignInButton.SIZE_WIDE);
            mSignInButton.setScopes(mGoogleSignInOptions.getScopeArray());
            mSignInButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "onClick: in button onClick:");
                    Intent signinIntent = Auth.GoogleSignInApi.
                            getSignInIntent(mGoogleApiClient);
                    startActivityForResult(signinIntent, SIGN_TOKEN_CODE);
                    showWaitDialog();
                }
            });
            mLoginNameText = (TextView) findViewById(R.id.login_name);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SIGN_TOKEN_CODE) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            Log.d(TAG, "onActivityResult: returned result:"+result);
            Log.d(TAG, "onActivityResult: returned result success:"+result.isSuccess());
            if (result.isSuccess()) {
                showAppUserInfo(result);
            } else {
                Log.d(TAG, "onActivityResult: Sign-in FAILURE");
            }
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        System.out.print("@@@ onConnectionFailed");
    }

    @Override
    protected void onDestroy() {
        if (mWaitDialog != null) {
            mWaitDialog.dismiss();
        }
        super.onDestroy();
    }

    /**
     * Using Google account for sync adapter
     *
     * @param context The application context
     */
    public Account CreateSyncAccount(Context context) {
        //Account newAccount = new Account(ACCOUNT, ACCOUNT_TYPE);

        //check api level here
        if(hasGetAccountsPermission()) {
            createSyncAccount(context);
        }
        return null;
    }

    private void showAppUserInfo(GoogleSignInResult result) {
        Log.d(TAG, "onActivityResult: Sign-in SUCCESS");
        mSignInAccount = result.getSignInAccount();
        String idToken = mSignInAccount.getIdToken();
        Log.d(TAG, "showAppUserInfo: idToken:"+idToken);
        saveTokenPreference(idToken);

        String googleId = mSignInAccount.getId();
        Log.d(TAG, "onActivityResult:sign-in ID:" + googleId);
        if (googleId == null) {
            Log.e(TAG, "Error getting Google ID from signed in account:googleId"+googleId);
            return;
        }
        Log.d(TAG, "onActivityResult:email:" + mSignInAccount.getEmail());
        Uri uri = ContentProviderContract.UserEntry.CONTENT_URI;
        Log.d(TAG, "onActivityResult:sign-in ID uri:" + uri);

        AccountManager am = AccountManager.get(this);
        Account[] accounts = am.getAccounts();
        Log.d(TAG, "CreateSyncAccount: accounts:"+accounts);
        Account gAcct = null;
        for (Account acc : accounts) {
            Log.d(TAG, "CreateSyncAccount: acc:"+acc);
            if(acc.type.equals(ACCOUNT_TYPE)){
                gAcct = acc;
                Log.d(TAG, "CreateSyncAccount: gAcct:"+gAcct);
                Log.d(TAG, "CALLING ----- Sync now");
                ContentResolver.requestSync(mAccount,
                        this.getString(R.string.content_authority),
                        Bundle.EMPTY);
                //setup automatic sync
                /*ContentResolver.setSyncAutomatically(gAcct,
                        this.getString(R.string.content_authority), true);*/
            }
        }
        if(gAcct == null){
            Log.e(TAG, "CreateSyncAccount: Error Sync Account not found");
            //throw new InvalidParameterException("Account not found");
        }

        //find the user from the db, if not present in local db then query the webservice if still not found create new one
        Cursor cursor = getContentResolver().query(uri,
                ContentProviderContract.UserEntry.projections,
                ContentProviderContract.UserEntry.COL_GOOGLE_ID + " = ? and " +
                        ContentProviderContract.UserEntry.COL_DELETED+ " = ?",
                new String[]{googleId + "", "0"},
                null);

        if (cursor != null && cursor.getCount() > 0) {
            Log.d(TAG, "onActivityResult: Found user in db");
            cursor.moveToFirst();
            mCurrentUser = ContentProviderContract.UserEntry.cursorToUser(cursor);
            long currentTimestamp = new Date().getTime();
            mCurrentUser.setLastLoginDate(currentTimestamp);
            mCurrentUser.setLastUpdateDate(currentTimestamp);
            saveUser(mCurrentUser);
            //Put the current logged in user's userId in the prefs
            saveUserIdPreference();

            startListActivity();
        } else {
            Log.d(TAG, "onActivityResult: User not found in db");
            Call<List<User>> call = mServiceEndpoint.findUsers(HelperUtility.AUTH_HEADER_PREFIX + idToken, googleId);
            Log.d(TAG, "onActivityResult: Webservice call executed call:" + call);
            call.enqueue(new UserCallBack());
            showWaitDialog();
        }
        Log.d(TAG, "onActivityResult: currentUser:"+mCurrentUser);
        cursor.close();
    }

    private void saveUserIdPreference() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(AppSettingsActivity.PREF_CURRENT_USER_ID, mCurrentUser.getId());
        editor.apply();
    }

    /**
     * TODO - Using this temporarily - find better way
     * @param token
     */
    private void saveTokenPreference(String token) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PREF_TOKEN, token);
        editor.apply();
    }
    private void showWaitDialog() {
        mWaitDialog = new ProgressDialog(this);
        mWaitDialog.setTitle(R.string.login_wait_dailog_title);
        mWaitDialog.setMessage(getString(R.string.login_wait_dailog_text));
        mWaitDialog.setCancelable(false);
        mWaitDialog.setIndeterminate(true);
        mWaitDialog.show();
    }


    private User saveUser(User user){
        Log.d(TAG, "saveUser: user:"+user);
        //insert
        if (user.getId() <= 0) {
            Uri createdUserUri = getContentResolver().insert(
                    ContentProviderContract.UserEntry.CONTENT_URI,
                    ContentProviderContract.UserEntry.loadContentValues(user));
            Log.d(TAG, "saveUser: createdUserUri:" + createdUserUri);
            user.setId(ContentUris.parseId(createdUserUri));
        } else {
            //update
            int noOfRowsUpdated = getContentResolver().update(
                            ContentProviderContract.UserEntry.getUserUri(user.getId()),
                            ContentProviderContract.UserEntry.loadContentValues(user),
                            ContentProviderContract.UserEntry._ID+ " = ?",
                            new String[]{user.getId()+""});
            Log.d(TAG, "Updated User:" + user);
            if (noOfRowsUpdated > 0) {
                Log.d(TAG, "Saved User:" + user);
            } else {
                Log.e(TAG, "Error Saving Used:" + user);
            }
        }
        return user;
    }
    private void saveTodo(LocationTodo todo) {
        Uri createdTodoUri = getContentResolver().insert(
                ContentProviderContract.LocationTodoEntry.CONTENT_URI,
                ContentProviderContract.LocationTodoEntry.loadContentValues(todo));
        if(createdTodoUri == null || ContentUris.parseId(createdTodoUri) <= 0) {
            Log.e(TAG, "saveTodo: Error inserting TODO in the DB. Returned URI:"+createdTodoUri);
        }
    }

    class UserCallBack implements  Callback<List<User>> {
        @Override
        public void onResponse(Call<List<User>> call, Response<List<User>> response) {
            if(mWaitDialog != null) {
                mWaitDialog.dismiss();
            }
            if(response.isSuccessful()) {
                List<User> returnedUsers = response.body();
                Log.d(TAG, "onResponse: returnedUsers:"+returnedUsers);
                if (returnedUsers != null && returnedUsers.size() > 0) {
                    updateLocalUserWithCloud(returnedUsers);
                } else {
                    //create a new user
                    if (mCurrentUser == null) {
                        //this is really the first time user is logging in create a brand new user
                        createNewUser();
                    }
                }
            } else {
                Log.d(TAG, "onResponse: response:"+response);
                if(response.code() == 404 ) {
                    Log.d(TAG, "onResponse: User not found in cloud, creating new one in db");
                    createNewUser();
                }
            }
            startListActivity();
        }

        @Override
        public void onFailure(Call<List<User>> call, Throwable t) {
            Log.e(TAG, "onFailure: Error response returned from the cloud",t.getCause());
            //TODO - tell user that login failed
        }
    }

    private void updateLocalUserWithCloud(List<User> returnedUsers) {
        //found user in the cloud db, use it to create local user
        Log.d(TAG, "onActivityResult: User received from server and saved in local db");
        User currentUser = new User();
        //need only the first item from the list, there should not be more than one
        User retUser = returnedUsers.get(0);
        Log.d(TAG, "updateLocalUserWithCloud: retUser:" + retUser);
        currentUser.setName(retUser.getName());
        currentUser.setGoogleId(retUser.getGoogleId());
        currentUser.setEmail(retUser.getEmail());
        currentUser.setCloudId(retUser.getCloudId());
        long currentTimestamp = new Date().getTime();
        currentUser.setLastLoginDate(currentTimestamp);
        currentUser.setLastUpdateDate(currentTimestamp);
        currentUser.setUpdated(false);
        currentUser.setDeleted(false);
        mCurrentUser = saveUser(currentUser);
        saveUserIdPreference();
        List<LocationTodo> retUserTodos = retUser.getUserTodos();
        Log.d(TAG, "updateLocalUserWithCloud: retUserTodos:" + retUserTodos);
        if (retUserTodos != null
                && retUserTodos.size() > 0) {
            updateLocalTodosWithCloud(retUserTodos);
        }
    }

    private void updateLocalTodosWithCloud(List<LocationTodo> retUserTodos) {

        Log.d(TAG, "onActivityResult: On server found todos for current user");
        //TODO - Insert multiple todos once instead of doing one by one
        for (LocationTodo retTodo : retUserTodos) {
            LocationTodo locTodo = new LocationTodo();
            locTodo.setCloudId(retTodo.getCloudId());
            locTodo.setName(retTodo.getName());
            locTodo.setSummary(retTodo.getSummary());
            locTodo.setLocationAlert(retTodo.getLocationAlert());
            locTodo.setGeofenceID(retTodo.getGeofenceID());
            locTodo.setLatitude(retTodo.getLatitude());
            locTodo.setLongitude(retTodo.getLongitude());
            locTodo.setLocationDescr(retTodo.getLocationDescr());
            locTodo.setRadius(retTodo.getRadius());
            locTodo.setPriority(retTodo.getPriority());
            locTodo.setDueDate(retTodo.getDueDate());
            long currentTimestamp = new Date().getTime();
            locTodo.setCreateDate(currentTimestamp);
            locTodo.setLastUpdateDate(currentTimestamp);
            locTodo.setUpdated(false);
            locTodo.setDeleted(false);
            locTodo.setUserId(mCurrentUser.getId());
            saveTodo(locTodo);
        }
    }

    private void createNewUser() {
        Log.d(TAG, "onActivityResult: Creating new user");
        mCurrentUser = new User();
        mCurrentUser.setName(mSignInAccount.getDisplayName());
        mCurrentUser.setGoogleId(mSignInAccount.getId());
        mCurrentUser.setEmail(mSignInAccount.getEmail());
        long currentTimestamp = new Date().getTime();
        mCurrentUser.setCreateDate(currentTimestamp);
        mCurrentUser.setLastLoginDate(currentTimestamp);
        mCurrentUser.setLastUpdateDate(currentTimestamp);
        mCurrentUser.setUpdated(true);
        mCurrentUser.setDeleted(false);
        mCurrentUser = saveUser(mCurrentUser);
        //Put the current logged in user's userId in the prefs
        saveUserIdPreference();
    }

    private void startListActivity() {
        if(mCurrentUser != null) {
            //got user now show them the task screen
            Intent i = new Intent(mContext, ListActivity.class);
            i.putExtra(EXTRA_USER_ID, mCurrentUser.getId());
            startActivity(i);
        } else {
            Log.e(TAG, "startListActivity: Unable find the logged in User, can't move forward");
            return;
        }
    }

    private boolean  signInSilently() {
        boolean signInSilently = false;
        Log.d(TAG, "signInSilently: at 1");
        OptionalPendingResult<GoogleSignInResult> pendingResult =
                Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
        if (pendingResult.isDone()) {
            Log.d(TAG, "signInSilently: at 2");
            // There's immediate result available.
            GoogleSignInResult result = pendingResult.get();
            if(result != null && result.isSuccess()) {
                signInSilently = true;
                showAppUserInfo(result);
            }
        } else {
            Log.d(TAG, "signInSilently: at 3");
            // There's no immediate result ready, displays some progress indicator and waits for the
            // async callback.
            showWaitDialog();
            pendingResult.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(@NonNull GoogleSignInResult result) {
                    Log.d(TAG, "signInSilently: at 4");
                    if (mWaitDialog != null) {
                        mWaitDialog.dismiss();
                    }
                    if(result != null && result.isSuccess()) {
                        Log.d(TAG, "signInSilently: at 5");
                        showAppUserInfo(result);
                    } else {
                        Log.e(TAG, "signInSilently: FAILURE - Google Login, can't move forward");
                        return;
                    }
                }
            });
        }
        return signInSilently;
    }

    @TargetApi(23)
    private boolean hasGetAccountsPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.GET_ACCOUNTS)
                != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Don't have permission to use google accounts");
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                Log.d(TAG, "Don't have permission to use google accounts, user has denied permission in past");
                //TODO - how should I handle this?
                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {
                Log.d(TAG, "Asking user for permission");
                // No explanation needed, we can request the permission.
                requestPermissions(new String[]{Manifest.permission.GET_ACCOUNTS},
                        PERMISSIONS_GET_ACCOUNTS_CODE);

            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[],
                                           int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_GET_ACCOUNTS_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "permission was granted by user to get accounts");
                    createSyncAccount(this);
                } else {
                    Log.e(TAG, "onRequestPermissionsResult: USER DENIED PERMISSION " +
                            "TO APP ACCESS_FINE_LOCATION");
                }
            }
        }
    }

    private void createSyncAccount(Context context) {
        Account[] accounts = AccountManager.get(context).getAccounts();
        Log.d(TAG, "CreateSyncAccount: accounts:"+accounts);
        Account gAcct = null;
        for (Account acc : accounts) {
            Log.d(TAG, "CreateSyncAccount: acc:"+acc);
            if(acc.type.equals(ACCOUNT_TYPE)){
                gAcct = acc;
                Log.d(TAG, "CreateSyncAccount: gAcct:"+gAcct);
                //setup automatic sync
                ContentResolver.setSyncAutomatically(gAcct,
                        context.getString(R.string.content_authority), true);
            }
        }
        if(gAcct == null){
            Log.e(TAG, "CreateSyncAccount: Error Sync Account not found");
            //throw new InvalidParameterException("Account not found");
        }
    }
}

