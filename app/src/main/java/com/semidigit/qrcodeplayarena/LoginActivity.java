package com.semidigit.qrcodeplayarena;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.semidigit.qrcodeplayarena.Utils.Constants;
import com.semidigit.qrcodeplayarena.Utils.HttpConnectionService;
import com.semidigit.qrcodeplayarena.Utils.UtilityMethods;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {

    private static final String apiPath = Constants.LOGIN_API_PATH;
    private static final String ACTIVITY_LOG_TAG = ".LoginActivity";

    private LoginActivity.UserLoginTask mAuthTask = null;
    String username, password, company_id;
    ProgressDialog pd;
    UtilityMethods utilityMethods;

    EditText et_username, et_password, et_company_id;
    Button bt_login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        utilityMethods =  new UtilityMethods(this);
        initViews();
    }

    private void initViews() {
        et_username = findViewById(R.id.et_username);
        et_password = findViewById(R.id.et_password);
        et_company_id = findViewById(R.id.et_company_id);
        bt_login = findViewById(R.id.btnLogin);
        check_existing_login();
    }

    private void check_existing_login(){
        username = PreferenceManager.getDefaultSharedPreferences(this).getString("username", null);
        if (username!=null){
            password = PreferenceManager.getDefaultSharedPreferences(this).getString("password", null);
            company_id = PreferenceManager.getDefaultSharedPreferences(this).getString("company_id", null);
            mAuthTask = new LoginActivity.UserLoginTask(this, username, password, company_id, getUniqueMachineID(), true);
            mAuthTask.execute((Void) null);
            return;
        }
    }


    public void login(View v){
        username = et_username.getText().toString().trim();
        password = et_password.getText().toString().trim();
        company_id = et_company_id.getText().toString().trim();

        if (validate() == false){
            return;
        }

        mAuthTask = new LoginActivity.UserLoginTask(this, username, password, company_id, getUniqueMachineID(), false);
        mAuthTask.execute((Void) null);
    }

    public boolean validate() {
        boolean valid = true;

        // Check for all fields are empty or not
        if (username.equals("") || username.length() == 0
                || password.equals("") || password.length() == 0 || company_id.equals("") || company_id.length() == 0) {
            Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Input all fields", Snackbar.LENGTH_LONG);
            snackbar.show();
            valid=false;
        }
        return valid;
    }

    public String getUniqueMachineID(){
        String androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        Log.d(ACTIVITY_LOG_TAG,"Device id : "+ androidId);
        return androidId;
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    class UserLoginTask extends AsyncTask<Void, Void, JSONObject> {

        String username;
        String password;
        String company_id;
        String machine_id;
        boolean manual_login;
        Context context;
        ProgressDialog pdia;

        UserLoginTask(Context context, String username, String password, String company_id, String machine_id, boolean manual_login) {
            this.context = context;
            this.username = username;
            this.password = password;
            this.company_id = company_id;
            this.machine_id = machine_id;
            this.manual_login = manual_login;
        }

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            pdia = new ProgressDialog(context);
            pdia.setMessage("Authenticating...");
            if(!((Activity) context).isFinishing())
            {
                pdia.show();
            }
        }

        @Override
        protected JSONObject doInBackground(Void... params) {

            HashMap<String, String> postDataParams = new HashMap<String, String>();
            postDataParams.put("HTTP_ACCEPT", "application/json");
            postDataParams.put("username", username);
            postDataParams.put("password", password);
            postDataParams.put("company_id", company_id);
            postDataParams.put("user_type", "staff");
            postDataParams.put("machine_id", machine_id);
            if(manual_login==true){
                postDataParams.put("save_login_entry", "0");
            }
            else{
                postDataParams.put("save_login_entry", "1");
            }

            HttpConnectionService service = new HttpConnectionService();
            String response = service.sendRequest(apiPath, postDataParams);
            JSONObject resultJsonObject = null;
            try {
                resultJsonObject = new JSONObject(response);
                return resultJsonObject;
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return resultJsonObject;
        }

        @Override
        protected void onPostExecute(final JSONObject resultJsonObject) {
            mAuthTask = null;
            pdia.dismiss();
            int responseCode=1;

            try {
                responseCode = utilityMethods.getValueOrDefaultInt(resultJsonObject.get("responseCode"),1);
            } catch (JSONException e) {
                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Unexpected response. Try again", Snackbar.LENGTH_LONG);
                snackbar.show();
            }
            if (responseCode==0){
                PreferenceManager.getDefaultSharedPreferences(context).edit().putString("username", username).apply();
                PreferenceManager.getDefaultSharedPreferences(context).edit().putString("password", password).apply();
                PreferenceManager.getDefaultSharedPreferences(context).edit().putString("company_id", company_id).apply();
                Intent intent = new Intent(context, DisplayQRActivityUSB.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
                finish();
                return;
            }
            else {
                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Invalid credentials. Try again!", Snackbar.LENGTH_LONG);
                snackbar.show();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            pdia.dismiss();
            Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Authentication cancelled by user!", Snackbar.LENGTH_LONG);
            snackbar.show();
        }

    }
}
