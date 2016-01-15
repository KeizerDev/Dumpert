package io.jari.dumpert.dialogs;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.AppCompatImageButton;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.Serializable;
import java.util.regex.Pattern;

import io.jari.dumpert.R;
import io.jari.dumpert.api.API;
import io.jari.dumpert.api.Comment;

public class ReplyDialog extends DialogFragment {
    public static final String TAG = "ReplyDialog";

    private Activity  parent;
    private String    title;
    private String    itemID;
    private String    entryID;
    private Comment   comment;
    private ReplyTask replyTask;

    // UI references
    private TextView             reply_title;
    private EditText             reply_content;
    private AppCompatImageButton reply_send;
    private ProgressBar          reply_progress;

    public ReplyDialog() {

    }

    public static ReplyDialog newInstance(String title, String itemID, String entryID) {
        Log.v(TAG, "Creating new instance for items");

        ReplyDialog dialog = new ReplyDialog();
        Bundle args = new Bundle();

        args.putString("TITLE", title);
        args.putString("ITEMID", itemID);
        args.putString("ENTRYID", entryID);
        args.putSerializable("COMMENT", null);
        dialog.setArguments(args);

        return dialog;
    }

    public static ReplyDialog newInstance(String itemID, Comment comment) {
        Log.v(TAG, "Creating new instance for comments");

        ReplyDialog dialog = new ReplyDialog();
        Bundle args = new Bundle();

        args.putString("TITLE", comment.author);
        args.putString("ITEMID", itemID);
        args.putString("ENTRYID", comment.entry);
        args.putSerializable("COMMENT", (Serializable) comment);
        dialog.setArguments(args);

        return dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        title   =           getArguments().getString("TITLE");
        itemID  =           getArguments().getString("ITEMID");
        entryID =           getArguments().getString("ENTRYID");
        comment = (Comment) getArguments().getSerializable("COMMENT");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_reply, container);
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(parent == null) {
            Log.w(TAG, "I'm an orphan! :(");
            return;
        }

        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        reply_title    = (TextView)             view.findViewById(R.id.reply_title);
        reply_content  = (EditText)             view.findViewById(R.id.reply_content);
        reply_send     = (AppCompatImageButton) view.findViewById(R.id.reply_send);
        reply_progress = (ProgressBar)          view.findViewById(R.id.reply_progress);
        View.OnKeyListener joris = new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent event) {
                if(event.getAction() == KeyEvent.ACTION_UP) {
                    isOK(((EditText) view).getText().toString());
                }
                return false;
            }

            private void isOK(String content) {
                Pattern rxHoax = Pattern.compile("[hc]+[^a-z]*[o0]+[^a-z]*a +[^a-z]*x +");
                Pattern rxKanker = Pattern.compile("([ ^a-z0-9]|^)+(kkr ?[ ^a-z0-9]+ | kanker)");
                Pattern rxIrritant = Pattern.compile("(zakelijkthuis\\.nl|adfoc\\.us|imagetwist\\.com|urlcash\\.net|imageporter\\.com|moneymiljonair\\.nl|webs\\.com|imagecherry\\.com|gratisinzetten)");
                Pattern rxShorter = Pattern.compile("(bit\\.ly|is\\.gd|tinyurl\\.com|\\/\\/t\\.co|goo\\.gl|cli\\.gs|short\\.ie|adf\\.ly|u\\.bb|9\\.bb|j\\.gs|q\\.gs|quidlinks\\.com|tiny\\.cc|alturl\\.com|shrtlnk\\.nl|adfoc\\.us|vaax\\.ru|ow\\.ly|tr\\.im|esy\\.es|\\.ly\\/)");

                if(rxHoax.matcher(content).matches()) {
                    showRXSnack(R.string.rxHoax);
                }

                if(rxKanker.matcher(content).matches()) {
                    showRXSnack(R.string.rxKanker);
                }

                if(rxIrritant.matcher(content).matches()) {
                    showRXSnack(R.string.rxIrritant);
                }

                if(rxShorter.matcher(content).matches()) {
                    showRXSnack(R.string.rxShorter);
                }
            }

            private void showRXSnack(int rx) {
                final Snackbar snackbar = Snackbar.make(parent.findViewById(R.id.root), rx, Snackbar.LENGTH_INDEFINITE);

                snackbar.setAction(R.string.tip_close, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.v(TAG, "dismissing RX snack");
                        snackbar.dismiss();
                    }
                });

                snackbar.show();
            }
        };

        String dialogTitle;
        String dialogHint;

        if(comment == null) {
            dialogTitle = parent.getResources().getString(R.string.prompt_comment_title, title);
            dialogHint  = parent.getResources().getString(R.string.prompt_comment);
        } else {
            dialogTitle = parent.getResources().getString(R.string.prompt_reply_title, title);
            dialogHint  = parent.getResources().getString(R.string.prompt_reply);
            String quote = Html.fromHtml(comment.content)+"\n"+comment.author+" | "+comment.time + "\n";
            reply_content.setText(quote);
        }

        reply_title.setText(dialogTitle);
        reply_content.setHint(dialogHint);
        reply_content.setOnKeyListener(joris);
        reply_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgress(true);
                String message = reply_content.getText().toString();
                replyTask = new ReplyTask(parent, comment, itemID, entryID, message);
                replyTask.execute((Void) null);
            }
        });

        reply_content.requestFocus();
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    @Override
    public void onDestroyView() {
        reply_title    = null;
        reply_content  = null;
        reply_send     = null;
        reply_progress = null;

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

    private void showProgress(final boolean show) {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        reply_send.setVisibility(show ? View.GONE : View.VISIBLE);
        reply_send.animate().setDuration(shortAnimTime).alpha(show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if(reply_send != null)
                    reply_send.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        reply_progress.setVisibility(show ? View.VISIBLE : View.GONE);
        reply_progress.animate().setDuration(shortAnimTime).alpha(show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (reply_progress != null)
                    reply_progress.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

    public class ReplyTask extends AsyncTask<Void, Void, Integer> {
        private final Context context;
        private final Comment comment;
        private final String  itemID;
        private final String  entryID;
        private final String  message;

        public ReplyTask(Context context, Comment comment, String itemID, String entryID, String message) {
            this.context = context;
            this.comment = comment;
            this.itemID  = itemID;
            this.entryID = entryID;
            this.message = message;
        }

        @Override
        protected Integer doInBackground(Void... params) {
            Integer exitCode = -1;

            try {
                exitCode = API.reply(context, itemID, entryID, message);
            } catch(Exception e) {
                Log.e(TAG, e.getMessage());
            }

            return exitCode;
        }

        @Override
        protected void onPostExecute(final Integer exitCode) {
            replyTask = null;
            showProgress(false);

            switch(exitCode) {
                case -1:
                    if(comment == null) {
                        Toast.makeText(context, R.string.error_comment_not_sent, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(context, R.string.error_reply_not_sent, Toast.LENGTH_LONG).show();
                    }
                    break;
                case 0:
                    dismiss();
                    if(comment == null) {
                        Toast.makeText(context, R.string.comment_sent, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(context, R.string.reply_sent, Toast.LENGTH_LONG).show();
                    }
                    break;
                case 1:
                    if(comment == null) {
                        Toast.makeText(context, R.string.comment_sent, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(context, R.string.reply_sent, Toast.LENGTH_LONG).show();
                    }
                    break;
            }
        }

    }

}
