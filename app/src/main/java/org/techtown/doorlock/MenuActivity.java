package org.techtown.doorlock;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.material.snackbar.Snackbar;

public class MenuActivity extends AppCompatActivity {
    //프래그먼트
    DoorFragment doorFragment;

    //아이디와 비밀번호
    private String id;
    private String pw;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        doorFragment = new DoorFragment();
        FragmentManager manager = getSupportFragmentManager();

        //로그인 화면에서 아이디, 비밀번호 받기
        Intent intent = getIntent();
        processIntent(intent);

        //서버 시작(임시)
        Intent serverIntent = new Intent(getApplicationContext(),Server.class);
        startService(serverIntent);

        //화면에 프래그먼트 출력
        manager.beginTransaction().replace(R.id.container,doorFragment).commit();
    }

    //첫 화면에서 받아온 인텐트 처리하는 함수
    private void processIntent(Intent intent){
        Bundle bundle = new Bundle();

        if(intent != null){
            //MainActivity에서 정보 받기
            id = intent.getStringExtra("id");
            pw = intent.getStringExtra("pw");
            //DoorFragment에 정보 넘기기
            bundle.putString("id",id);
            bundle.putString("pw",pw);
            doorFragment.setArguments(bundle);
            Snackbar.make(this.getWindow().getDecorView(),id+"님, 로그인 환영합니다.",Snackbar.LENGTH_SHORT).show();
        }
    }

    //상단 탭 에서 서비스로 상태 전송(임시)
    public void changeDoor(int status){
        Intent changeIntent = new Intent(getApplicationContext(),Server.class);
        changeIntent.putExtra("door",status);
        startService(changeIntent);
    }
}