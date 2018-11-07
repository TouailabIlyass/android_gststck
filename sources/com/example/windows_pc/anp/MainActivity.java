package com.example.windows_pc.anp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    Button log_btn;
    EditText name;
    EditText pass;
    ProgressDialog progressDialog;

    /* renamed from: com.example.windows_pc.anp.MainActivity$1 */
    class C03341 implements OnClickListener {
        C03341() {
        }

        public void onClick(View v) {
            new Dologin().execute(new String[0]);
        }
    }

    /* renamed from: com.example.windows_pc.anp.MainActivity$2 */
    class C03352 implements OnTouchListener {
        C03352() {
        }

        public boolean onTouch(View v, MotionEvent event) {
            return false;
        }
    }

    private class Dologin extends AsyncTask<String, String, String> {
        String em;
        boolean isSuccess;
        String namestr;
        String nm;
        String passstr;
        String password;
        /* renamed from: z */
        String f11z;

        private Dologin() {
            this.namestr = MainActivity.this.name.getText().toString();
            this.passstr = MainActivity.this.pass.getText().toString();
            this.f11z = "";
            this.isSuccess = null;
        }

        protected void onPreExecute() {
            MainActivity.this.progressDialog.setMessage("Loading...");
            MainActivity.this.progressDialog.show();
            super.onPreExecute();
        }

        protected String doInBackground(String... params) {
            if (this.namestr.trim().equals("") || this.passstr.trim().equals("")) {
                this.f11z = "Please enter all fields....";
            }
            if (1 != 2) {
                try {
                    if (AdminDB.getUser(this.namestr, this.passstr)) {
                        this.isSuccess = true;
                        this.f11z = "Login successfull";
                    } else {
                        this.isSuccess = false;
                    }
                } catch (Exception ex) {
                    this.isSuccess = false;
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("Exceptions");
                    stringBuilder.append(ex);
                    this.f11z = stringBuilder.toString();
                }
            }
            return this.f11z;
        }

        protected void onPostExecute(String s) {
            Context baseContext = MainActivity.this.getBaseContext();
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("");
            stringBuilder.append(this.f11z);
            Toast.makeText(baseContext, stringBuilder.toString(), 1).show();
            if (this.isSuccess) {
                Intent intent = new Intent(MainActivity.this, Main2Activity.class);
                intent.putExtra("name", this.namestr);
                MainActivity.this.startActivity(intent);
            }
            MainActivity.this.progressDialog.hide();
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView((int) C0336R.layout.activity_main);
        this.progressDialog = new ProgressDialog(this);
        this.log_btn = (Button) findViewById(C0336R.id.login_btn);
        this.name = (EditText) findViewById(C0336R.id.userText);
        this.pass = (EditText) findViewById(C0336R.id.passText);
        this.log_btn.setOnClickListener(new C03341());
        this.log_btn.setOnTouchListener(new C03352());
    }

    public void onResume() {
        super.onResume();
    }
}
