package io.jari.dumpert.dialogs;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import io.jari.dumpert.R;
import io.jari.dumpert.activities.MainActivity;
import io.jari.dumpert.api.Login;


/**
 * A login screen that offers login via email/password.
 */
public class LoginDialog extends DialogFragment {
    public final String TAG = "LoginDialog";

    /**
     * can be local, but declaring here gives easy access when in need of changing.
     */
    @SuppressWarnings("FieldCanBeLocal")
    private final String registerURL = "http://registratie.geenstijl.nl/registratie/index.php?view=";
    private final String goldfishURL = "http://registratie.geenstijl.nl/registratie/index.php?view=lost_pw";

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    private Activity parent;

    // UI references.
    private View     mLoginFormView;
    private View     mProgressView;
    private TextView mError;
    private EditText mEmailView;
    private EditText mPasswordView;

    public LoginDialog() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_login, container);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        // Set up the login form.
        mLoginFormView = view.findViewById(R.id.email_login_form);
        mProgressView = view.findViewById(R.id.login_progress);
        mError = (TextView) view.findViewById(R.id.error);
        mEmailView = (EditText) view.findViewById(R.id.email);
        mPasswordView = (EditText) view.findViewById(R.id.password);

        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.action_signin || id == EditorInfo.IME_ACTION_SEND) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button actionSignin = (Button) view.findViewById(R.id.action_signin);
        TextView actionRegister = (TextView) view.findViewById(R.id.action_register);
        TextView actionGoldfish = (TextView) view.findViewById(R.id.action_goldfish);

        actionSignin.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });
        actionRegister.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent registerIntent = new Intent(Intent.ACTION_VIEW);
                registerIntent.setData(Uri.parse(registerURL));
                startActivity(registerIntent);
            }
        });
        actionGoldfish.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent goldfishIntent = new Intent(Intent.ACTION_VIEW);
                goldfishIntent.setData(Uri.parse(goldfishURL));
                startActivity(goldfishIntent);
            }
        });

        mLoginFormView.requestFocus();
        getDialog().getWindow().setSoftInputMode(WindowManager
                .LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }


    @Override
    public void onDestroyView() {
        mLoginFormView = null;
        mProgressView = null;
        mError = null;
        mEmailView = null;
        mPasswordView = null;

        super.onDestroyView();
    }

    @Override
    public void onAttach(Activity activity) {
        parent = activity;
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        parent = null;
        super.onDetach();
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        showProgress(true);
        mAuthTask = new UserLoginTask(parent.getBaseContext(), email, password);
        mAuthTask.execute((Void) null);
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    private void showProgress(final boolean show) {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if(mLoginFormView != null)
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        mProgressView.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if(mProgressView != null)
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final Context mContext;
        private final String mEmail;
        private final String mPassword;

        UserLoginTask(Context context, String email, String password) {
            mContext = context;
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean success = false;

            try {
                Login account = new Login();
                account.setEmail(mEmail);
                account.setPassword(mPassword);
                account.setFormData();

                success = account.login(mContext, null);
            } catch(Exception e) {
                Log.e(TAG, e.getMessage());
            }

            return success;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                dismiss();
                MainActivity.notifyAccountChanged();
            } else {
                mError.setVisibility(View.VISIBLE);
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

}
