package org.techtown.doorlock;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Instrumentation;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.material.snackbar.Snackbar;
import com.pedro.library.AutoPermissions;
import com.pedro.library.AutoPermissionsListener;


//메인 화면과 합치기위해 임시로 로그인 화면을 만들었습니다.
//프래그먼트를 만들기 위해 모양만 맞추었습니다.
public class MainActivity extends AppCompatActivity implements AutoPermissionsListener {
    static final int REQUEST_CODE = 102; //로그인 요청
    EditText editText;
    EditText editText2;

    //아이디와 비밀번호
    private String id;
    private String pw;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText = findViewById(R.id.editText);
        editText2 = findViewById(R.id.editText2);
        Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                id = editText.getText().toString();
                pw = editText2.getText().toString();
                Intent intent = new Intent(getApplicationContext(),MenuActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("id",id);
                intent.putExtra("pw",pw);
                startActivityForResult(intent,REQUEST_CODE);

            }
        });

        AutoPermissions.Companion.loadAllPermissions(this,101);
    }

    //로그아웃시
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);

        if(requestCode == REQUEST_CODE){
            if(resultCode == RESULT_OK){
                editText.setText(id);
                Snackbar.make(this.getWindow().getDecorView(),"로그아웃 완료",Snackbar.LENGTH_SHORT).show();
            }
        }

    }


    //위험권한 요청
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults){
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        AutoPermissions.Companion.parsePermissions(this,requestCode,permissions,this);
    }

    public void onGranted(int requestCode,String[] permissions){

    }

    public void onDenied(int requestCode,String[] permissions){

    }
}