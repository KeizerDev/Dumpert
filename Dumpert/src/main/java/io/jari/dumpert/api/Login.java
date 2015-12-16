package io.jari.dumpert.api;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.apache.commons.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

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
    private final String t         = "666";
    private final String __mode    = "handle_sign_in";
    private final String _return   = "http%3A%2F%2Fapp.steylloos.nl%2Fmt-comments.fcgi%3F__mode%3Dhandle_sign_in%26entry_id%3D4695231%26static%3Dhttp%3A%2F%2Fwww.steylloos.nl%2Fcookiesync.php%3Fsite%3DDUMP%2526return%3DaHR0cDovL3d3dy5kdW1wZXJ0Lm5sL21lZGlhYmFzZS82NzAyMzExLzA0ZGY4YTYwL2RlX2hlbGVfZ2FuemVuX3RhZy5odG1s";
    private final String submit    = "Login";

    /**
     * actual login credentials and cookie data
     */
    private String email    = null;
    private String password = null; // As plain text. Because fuck security, right GeenStijl?
    private String cookies  = null;

    // the cookie header to look for when putting the cookies in the cookie jar.
    static final String COOKIES_HEADER = "idk";

    // we want to keep the form data accessible for other methods without passing it around.
    private ContentValues formData = null;

    /**
     * sets the email to use while logging in.
     *
     * @param email String
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * sets the password to use while logging in.
     *
     * @param password String
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * sets the cookies to use when connecting.
     * if you've already got cookies new ones will NOT be explicitly requested but CAN be returned.
     *
     * @param cookies String
     */
    public void setCookies(String cookies) {
        this.cookies = cookies;
    }

    /**
     * returns the cookies Login has received.
     *
     * @return String
     */
    public String getCookies() {
        return this.cookies;
    }

    /**
     * commits the cookie (AKA session) to the shared preferences.
     *
     * @param session String
     * @param preferences SharedPreferences
     */
    private void setSession(String session, SharedPreferences preferences) {
        preferences.edit().putString("session", session).commit();
    }

    /**
     * tries to retrieve the cookie (AKA session) from the shared preferences.
     *
     * @param preferences SharedPreferences
     * @return String
     */
    private String getSession(SharedPreferences preferences) {
        return preferences.getString("session", "");
    }

    /**
     * puts the cookies retrieved from connection in the cookie jar supplied by cookieManager.
     *
     * @param connection HttpURLConnection
     * @param cookieManager CookieManager
     */
    private void putCookiesInJar(HttpURLConnection connection, CookieManager cookieManager) {
        final Map<String, List<String>> headerFields = connection.getHeaderFields();
        final List<String> cookiesHeader = headerFields.get(COOKIES_HEADER);

        for (String cookie : cookiesHeader) {
            cookieManager.getCookieStore().add(null, HttpCookie.parse(cookie).get(0));
        }
    }

    /**
     * gets the cookies out of the jar supplied by cookieManager.
     *
     * @param cookieManager CookieManager
     */
    private List<HttpCookie> getCookiesFromJar(CookieManager cookieManager) {
        return cookieManager.getCookieStore().getCookies();
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
        final URL login = new URL(url);
        final HttpURLConnection connection = (HttpURLConnection) login.openConnection();

        connection.setRequestMethod(method);
        connection.setDoInput(input);
        connection.setDoOutput(output);

        if(this.cookies != null) {
            connection.setRequestProperty("Cookie", this.cookies);
        }

        if(bindFormData) {
            bindFormData(connection);
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
        connection.disconnect();
    }

    /**
     * sets the form data to be used while logging in.
     */
    public void setFormData() {
        this.formData = new ContentValues();

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
        if(this.formData != null) {
            final OutputStream os = connection.getOutputStream();
            final BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));

            bw.write(this.formData.toString());
            bw.flush();
            bw.close();

            os.close();
        }
    }

    /**
     * retrieves the response from connect and returns it as a string.
     *
     * @param connection HttpURLConnection passed from connect
     * @return String
     * @throws IOException
     *
     * @TODO: this could also be used in the regular API to remove a lot of duplicate code.
     */
    private String retrieveResponse(HttpURLConnection connection) throws IOException {
        final Charset encoding = Charset.forName((connection.getContentEncoding() != null)
                ? connection.getContentEncoding() : "UTF-8");
        final InputStream in = new BufferedInputStream(connection.getInputStream());

        return IOUtils.toString(in, encoding);
    }

    public boolean login(Context context) throws IOException {
        final HttpURLConnection connection = connect(sendToURL, "POST", true, true, true);
        final String response = retrieveResponse(connection);
        final boolean success = !response.contains("font color=\"red\"");

        if (success) {
            final CookieManager cookieManager = new CookieManager(null, CookiePolicy.ACCEPT_ALL);

            putCookiesInJar(connection, cookieManager);

            final List<HttpCookie> collectedCookies = getCookiesFromJar(cookieManager);

            if(collectedCookies.size() > 0) {
                String cname = null;
                String token = null;

                for (HttpCookie cookie : collectedCookies) {
                    if (cookie.getName().equals("commenter_name")) {
                        cname = cookie.getValue();
                    } else if (cookie.getName().equals("tk_commenter")) {
                        token = cookie.getValue();
                    } else {
                        // not our cookie, I suppose, but let's log it anyway. To be sure.
                        Log.d(TAG, "received a cookie that is not for us? ("+cookie.getValue()+")");
                    }
                }

                final String cookie = String.format("commenter_name=%s; tk_commenter=%s;", cname, token);

                context.getSharedPreferences("dumpert", 0).edit().putString("username", cname).commit();
                setCookies(cookie);
                setSession(cookie, context.getSharedPreferences("dumpert", 0));
            } else {
                Log.w(TAG, "We didn't receive cookies. :(");
            }
        }

        disconnect(connection);

        return success;
    }

    public void logout(Context context) {
        setSession("", context.getSharedPreferences("dumpert", 0));
    }

}
