package id.hnslabs.loginnregister;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends AppCompatActivity {
    private Button btnLogin;
    private Button btnLinkToRegister;
    private EditText inputEmail;
    private EditText inputPassword;
    private ProgressDialog pDialog;
    private SessionManager session;

    private HttpURLConnection c;
    private JSONObject jObj = null;
    private String json = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        inputEmail = (EditText) findViewById(R.id.email);
        inputPassword = (EditText) findViewById(R.id.password);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnLinkToRegister = (Button) findViewById(R.id.btnLinkToRegisterScreen);

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        // SQLite database handler
        //db = new SQLiteHandler(getApplicationContext());

        // Session manager
        session = new SessionManager(getApplicationContext());

        // Check if user is already logged in or not
        if (session.isLoggedIn()) {
            // User is already logged in. Take him to main activity
            //Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            //startActivity(intent);
            //finish();
            Log.i("Status","You already login");
        }

        // Login button Click Event
        btnLogin.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                String email = inputEmail.getText().toString().trim();
                String password = inputPassword.getText().toString().trim();

                // Check for empty data in the form
                if (!email.isEmpty() && !password.isEmpty()) {
                    // login user
                    checkLogin("http://10.170.16.45/loginregister/login.php", email, password);
                } else {
                    // Prompt user to enter credentials
                    Toast.makeText(getApplicationContext(),
                            "Please enter the credentials!", Toast.LENGTH_LONG)
                            .show();
                }
            }

        });

        // Link to Register Screen
        btnLinkToRegister.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(i);
                finish();
            }
        });

    }

    public void checkLogin(String url, String email, String password) {
        new doAsyntask(url, email, password).execute();
    }

    private class doAsyntask extends AsyncTask<Void, Void, String>{
        private String email, password, url;

        public doAsyntask(String url, String email, String password){
            this.email = email;
            this.password = password;
            this.url = url;
        }

        @Override
        protected String doInBackground(Void... params) {
            try{
                URL u = new URL(url);
                c = (HttpURLConnection) u.openConnection();

                c.setRequestMethod("POST");
                c.setUseCaches(false);
                c.setDoInput(true);
                c.setDoOutput(true);

                List<Pair<String, String>> param = new ArrayList<>();
                param.add(new Pair<>("email", email));
                param.add(new Pair<>("password", password));

                OutputStream os = c.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(getQuery(param));
                writer.flush();
                writer.close();
                os.close();

                c.connect();

                int status = c.getResponseCode();
                switch (status){
                    case 200:
                    case 201:
                        BufferedReader br = new BufferedReader(new InputStreamReader(c.getInputStream()));
                        StringBuilder sb = new StringBuilder();
                        String line = null;

                        while ((line = br.readLine()) != null) {
                            sb.append(line+"\n");
                        }

                        br.close();
                        json = sb.toString();
                        Log.i("Json",json.toString());
                }

            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } finally {
                if (c != null) {
                    try {
                        c.disconnect();
                        jObj = new JSONObject(json);
                        Log.i("JsonObject",jObj.toString());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
            return null;
        }
    }

    private String getQuery(List<Pair<String, String>> params) throws UnsupportedEncodingException
    {
        StringBuilder result = new StringBuilder();
        boolean first = true;

        for (Pair<String, String> pair : params)
        {
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(pair.first, "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(pair.second, "UTF-8"));
        }

        return result.toString();
    }

}
