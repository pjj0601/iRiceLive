package test.com.livetest;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{
    private EditText account;
    private EditText password;
    private Switch aSwitch;
    public int isnot = 0;
    private Button login;
    private Button register;
//    private Button btn_test;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        account = (EditText) findViewById(R.id.account);
        password = (EditText) findViewById(R.id.password);
        aSwitch = (Switch) findViewById(R.id.switch1);
        login = (Button) findViewById(R.id.login);
        register = (Button) findViewById(R.id.register);
//        btn_test = (Button) findViewById(R.id.test);
//        btn_test.setOnClickListener(this);
        login.setOnClickListener(this);
        register.setOnClickListener(this);
        aSwitch.setChecked(false);
        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                    isnot = 1;
                else
                    isnot = 0;
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login:
                if (isnot==1){
                    if (account.getText().toString().equals("pande")||password.getText().toString().equals("pande")){
                        startActivity(new Intent(this, CameraActivity.class));
//                        Toast.makeText(LoginActivity.this,"test",Toast.LENGTH_LONG).show();
                    }

                }else {
                        if (account.getText().toString().equals("pande")||password.getText().toString().equals("pande")) {
//                            startActivity(new Intent(this, PlayerActivity.class));
                            startActivity(new Intent(this,MenuActivity.class));
                        } else {
                            Toast.makeText(LoginActivity.this,"账号密码错误",Toast.LENGTH_LONG).show();
                        }

                }

                break;
            case R.id.register:
//                startActivity(new Intent(this,PlayerActivity.class));
                break;
//            case R.id.test:
//                startActivity(new Intent(this,MenuActivity.class));
//                break;
        }
    }
}
