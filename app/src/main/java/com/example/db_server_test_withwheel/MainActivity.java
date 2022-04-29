package com.example.db_server_test_withwheel;

import android.app.ProgressDialog;
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
    private TextView mTextViewResult;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mEditTextID = (EditText)findViewById(R.id.editText_main_name);
        mEditTextPassword = (EditText)findViewById(R.id.editText_main_country);
        mEditTextPassword2 = (EditText) findViewById(R.id.editText_main_country2);

        mTextViewResult = (TextView)findViewById(R.id.textView_main_result);

        mTextViewResult.setMovementMethod(new ScrollingMovementMethod());


        Button buttonInsert = (Button)findViewById(R.id.button_main_insert);
        buttonInsert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String name = mEditTextID.getText().toString();
                String country = mEditTextPassword.getText().toString();
                String country2 = mEditTextPassword2.getText().toString();

                // 비밀번호 확인
                if(country.equals(country2)){
                    InsertData task = new InsertData();
                    task.execute("http://" + IP_ADDRESS + "/insert.php", name, country);

                    mEditTextID.setText("");
                    mEditTextPassword.setText("");
                    mEditTextPassword2.setText("");
                }

                else {
                    Toast.makeText(MainActivity.this, "비밀번호가 다릅니다.", Toast.LENGTH_SHORT).show();
                    mEditTextID.setText("");
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

            String name = (String)params[1];
            String country = (String)params[2];

            String serverURL = (String)params[0];
            String postParameters = "name=" + name + "&country=" + country;


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