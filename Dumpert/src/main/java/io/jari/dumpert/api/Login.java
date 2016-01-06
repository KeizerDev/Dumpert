package io.jari.dumpert.api;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import org.apache.commons.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Login (could also be called LoginAPI, or be merged to API altogether)
 * rewrote as a combination of io.jari.geenstijl.API and com.cytodev.lib.basiclogin
 *
 * Usage:
 *
 *  1. set the email and password to be sent for verification over:
 *         Login.setEmail("youremail@adress.com");
 *         Login.setPassword("yourSuperSecretPassword1234-=_+");
 *
 *  2. set cookie data to be passed along: [optional]
 *         Login.setCookies(yourCookie);
 *
 *  3. prepare the form data:
 *         Login.setFormData();
 *
 *  4. execute the login method to receive a boolean:
 *         final boolean loggedIn = Login.login(activity.this);
 *
 * Created by CytoDev on 16-12-15.
 */
public class Login {
    public static final String TAG = "DLogin";

    /**
     * the standard for data required to log in (grabbed from registratie.geenstijl.nl)
     * can be local, but declaring here gives easy access when in need of changing.
     */
    private final String sendToURL = "http://registratie.geenstijl.nl/registratie/gs_engine.php?action=login";
    private final String t         = "0";
    private final String __mode    = "";
    private final String _return   = "";
    private final String submit    = "Login";

    /**
     * actual login credentials
     */
    private String email    = null;
    private String password = null; // As plain text. Because fuck security, right GeenStijl?

    // we want to keep the form data accessible for other methods without passing it around.
    private Map<String, String> formData = null;

    // we need a cookiemonster to keep the cookies safe
    private CookieManager cookieManager = null;

    /**
     * sets the email to use while logging in.
     *
     * @param email String
     */
    public void setEmail(String email) {
        Log.d(TAG, "setting email to: " + email);

        this.email = email;
    }

    /**
     * sets the password to use while logging in.
     *
     * @param password String
     */
    public void setPassword(String password) {
        Log.d(TAG, "setting password to: " + password);

        this.password = password;
    }

    /**
     * puts the cookies retrieved from connection in the cookie jar supplied by cookieManager.
     *
     * @param connection HttpURLConnection
     */
    private void putCookiesInJar(HttpURLConnection connection) {
        Log.v(TAG, "putting new cookies in the kitchen jar");

        final Map<String, List<String>> headerFields = connection.getHeaderFields();
        final List<String> cookiesHeader = headerFields.get("Set-Cookie");

        if(cookiesHeader == null) {
            Log.w(TAG, "No cookies found");
            return;
        }

        for (String cookie : cookiesHeader) {
            Log.d(TAG, "Adding cookie: "+cookie);
            this.cookieManager.getCookieStore().add(null, HttpCookie.parse(cookie).get(0));
        }
    }

    /**
     * gets the cookies out of the jar supplied by cookieManager.
     */
    private List<HttpCookie> getCookiesFromJar() {
        Log.v(TAG, "getting the cookies from the kitchen jar");

        return this.cookieManager.getCookieStore().getCookies();
    }

    /**
     * commits the cookie (AKA session) to the shared preferences.
     *
     * @param session String
     * @param preferences SharedPreferences
     */
    private boolean setSession(String user, String session, SharedPreferences preferences) {
        Log.v(TAG, "setting session");

        return preferences.edit()
                .putString("session", session)
                .putString("username", user)
                .commit();
    }

    /**
     * sets the form data to be used while logging in.
     */
    public void setFormData() {
        Log.v(TAG, "building form data");

        this.formData = new HashMap<String, String>();

        formData.put("t", t);
        formData.put("__mode", __mode);
        formData.put("_return", _return);
        formData.put("email", email);
        formData.put("password", password);
        formData.put("submit", submit);
    }

    /**
     * if form data is set using setFormData, this method will bind it to the connection.
     *
     * @param connection HttpURLConnection passed from connect
     * @throws IOException
     */
    private void bindFormData(HttpURLConnection connection) throws IOException {
        Log.v(TAG, "binding form data");

        if(formData != null) {
            Log.v(TAG, "writing form data to connection");

            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new BufferedOutputStream(
                    connection.getOutputStream())));
            Uri.Builder builder = new Uri.Builder();
            Iterator data = formData.entrySet().iterator();

            while(data.hasNext()) {
                Map.Entry param = (Map.Entry) data.next();
                builder.appendQueryParameter(param.getKey().toString(), param.getValue().toString());
                data.remove();
            }

            bw.write(builder.build().getEncodedQuery());
            bw.flush();
            bw.close();

            Log.v(TAG, "done writing form data to connection");
        } else {
            Log.w(TAG, "formData has not been set");
        }
    }

    /**
     * universal connect method, simplified for Login.java.
     *
     * @param url String
     * @param method String
     * @param input boolean
     * @param output boolean
     * @param bindFormData boolean
     * @return HttpURLConnection
     * @throws IOException
     */
    private HttpURLConnection connect(String url, String method, boolean input, boolean output,
                                      boolean bindFormData) throws IOException {
        Log.d(TAG, "connecting to: " + url
                        + "\n" + "  using method: " + method
                        + "\n" + "  using input: " + input
                        + "\n" + "  using output: " + output
                        + "\n" + "  using bindFormData: " + bindFormData
        );

        final URL login = new URL(url);
        final HttpURLConnection connection = (HttpURLConnection) login.openConnection();
        String cookies = null;

        if(getCookiesFromJar().size() > 0) {
            cookies = TextUtils.join(";", getCookiesFromJar());
        }

        connection.setRequestMethod(method);
        connection.setInstanceFollowRedirects(false);

        if (input) connection.setDoInput(true);
        if (output) connection.setDoOutput(true);
        if(bindFormData) bindFormData(connection);

        if(cookies != null) {
            Log.d(TAG, "found cookie(s). Using " + cookies + " as cookie(s)");
            connection.setRequestProperty("Cookie", cookies);
        }

        connection.connect();

        return connection;
    }

    /**
     * universal disconnect method.
     *
     * @param connection HttpURLConnection
     */
    private void disconnect(HttpURLConnection connection) {
        Log.v(TAG, "disconnecting");

        connection.disconnect();
    }

    /**
     * retrieves the response from connect and returns it as a string.
     *
     * @param connection HttpURLConnection passed from connect
     * @return String
     * @throws IOException
     */
    private String retrieveResponse(HttpURLConnection connection) throws IOException {
        Log.v(TAG, "retrieving response from connection");

        final Charset encoding = Charset.forName((connection.getContentEncoding() != null)
                ? connection.getContentEncoding() : "UTF-8");
        final InputStream in = new BufferedInputStream(connection.getInputStream());

        return IOUtils.toString(in, encoding);
    }

    /**
     * attempts to log in using credentials passed to formData.
     * only follows redirects with GET method.
     *
     * @param context Context
     * @param redirectedURL String
     * @return boolean
     * @throws IOException
     */
    public boolean login(Context context, String redirectedURL) throws IOException {
        Log.v(TAG, "logging in");

        if(cookieManager == null) {
            Log.d(TAG, "created new cookiemonster");
            cookieManager = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        }

        final boolean redirecting = (redirectedURL != null);
        final String url = (redirecting) ? redirectedURL : sendToURL;
        final HttpURLConnection connection = connect(url, (redirecting) ? "GET" : "POST",
                !redirecting, !redirecting, !redirecting);
        final String response = retrieveResponse(connection);
        final int responseCode = connection.getResponseCode();

        putCookiesInJar(connection);

        Log.d(TAG, "got responseCode: "+responseCode);
        if(responseCode >= 300 && responseCode < 400) {
            Log.v(TAG, "redirecting");
            return login(context, connection.getHeaderField("Location"));
        }

        final boolean success = !response.contains("font color=\"red\"");

        if (success) {
            Log.v(TAG, "did not encounter red text");

            if(getCookiesFromJar().size() > 0) {
                String cname = null;
                String token = null;

                for (HttpCookie cookie : getCookiesFromJar()) {
                    switch(cookie.getName()) {
                        case "commenter_name": cname = cookie.getValue(); break;
                        case "tk_commenter":   token = cookie.getValue(); break;
                        default:
                            // not our cookie, I suppose, but let's log it anyway. To be sure.
                            Log.d(TAG, "received a cookie that is not for us? ("+cookie.getValue()+")");
                            break;
                    }
                }

                final String cookie = String.format("commenter_name=%s; tk_commenter=%s;", cname, token);

                if(setSession(cname, cookie, context.getSharedPreferences("dumpert", 0))) {
                    Log.v(TAG, "saved authentication data to local storage");
                } else {
                    Log.w(TAG, "Could not save authentication data");
                }
            } else {
                Log.w(TAG, "We didn't receive cookies. :(");
            }
        } else {
            Log.w(TAG, "The server broke. Or user is a goldfish.");
        }

        disconnect(connection);

        return success;
    }

    /**
     * destroys the "session" from SharedPreferences.
     *
     * @param context Context
     */
    public void logout(Context context) {
        Log.v(TAG, "logging out");

        if(setSession("", "", context.getSharedPreferences("dumpert", 0))) {
            Log.v(TAG, "Destroyed authentication data");
        } else {
            Log.w(TAG, "Could not destroy authentication data");
        }
    }

}
