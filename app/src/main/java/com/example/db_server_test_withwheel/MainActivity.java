package com.example.db_server_test_withwheel;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import java.net.HttpURLConnection;
import java.net.URL;


public class MainActivity extends AppCompatActivity {

    private static String IP_ADDRESS = "10.0.2.2";//다음 줄에 있는 IP 주소를 아파치 웹서버가 설치된  컴퓨터의 IP
    private static String TAG = "db_server_test_withwheel";//

    private EditText mEditTextID;
    private EditText mEditTextPassword;
    private EditText mEditTextPassword2;
    private EditText mEditTextNickname;
    private TextView mTextViewResult;

    Button loginBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mEditTextID = (EditText)findViewById(R.id.editText_main_name);
        mEditTextPassword = (EditText)findViewById(R.id.editText_main_country);
        mEditTextPassword2 = (EditText) findViewById(R.id.editText_main_country2);
        mEditTextNickname = (EditText) findViewById(R.id.editText_main_nickname);

        mTextViewResult = (TextView)findViewById(R.id.textView_main_result);

        mTextViewResult.setMovementMethod(new ScrollingMovementMethod());

        loginBtn = (Button) findViewById(R.id.loginButton);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, login.class);
                startActivity(intent);
            }
        });

        Button buttonInsert = (Button)findViewById(R.id.button_main_insert);
        buttonInsert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String userid = mEditTextID.getText().toString();
                String password = mEditTextPassword.getText().toString();
                String password2 = mEditTextPassword2.getText().toString();
                String nickname = mEditTextNickname.getText().toString();

                // 비밀번호 확인
                if(password.equals(password2)){
                    InsertData task = new InsertData();
                    task.execute("http://" + IP_ADDRESS + "/insert.php", userid, password, nickname);

                    mEditTextID.setText("");
                    mEditTextPassword.setText("");
                    mEditTextPassword2.setText("");
                    mEditTextNickname.setText("");
                }

                else {
                    Toast.makeText(MainActivity.this, "비밀번호가 다릅니다.", Toast.LENGTH_SHORT).show();
                    mEditTextPassword.setText("");
                    mEditTextPassword2.setText("");
                }

            }
        });

    }



    class InsertData extends AsyncTask<String, Void, String>{
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(MainActivity.this,
                    "Please Wait", null, true, true);
        }


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            progressDialog.dismiss();
            mTextViewResult.setText(result);
            Log.d(TAG, "POST response  - " + result);
        }


        @Override
        protected String doInBackground(String... params) {

            String userid = (String)params[1];
            String password = (String)params[2];
            String nickname = (String)params[3];

            String serverURL = (String)params[0];
            String postParameters = "userid=" + userid + "&password=" + password + "&nickname=" + nickname;


            try {

                URL url = new URL(serverURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();


                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.connect();


                OutputStream outputStream = httpURLConnection.getOutputStream();
                outputStream.write(postParameters.getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();


                int responseStatusCode = httpURLConnection.getResponseCode();
                Log.d(TAG, "POST response code - " + responseStatusCode);

                InputStream inputStream;
                if(responseStatusCode == HttpURLConnection.HTTP_OK) {
                    inputStream = httpURLConnection.getInputStream();
                }
                else{
                    inputStream = httpURLConnection.getErrorStream();
                }


                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuilder sb = new StringBuilder();
                String line = null;

                while((line = bufferedReader.readLine()) != null){
                    sb.append(line);
                }


                bufferedReader.close();


                return sb.toString();


            } catch (Exception e) {

                Log.d(TAG, "InsertData: Error ", e);

                return new String("Error: " + e.getMessage());
            }

        }
    }


}