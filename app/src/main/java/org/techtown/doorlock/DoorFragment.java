package org.techtown.doorlock;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class DoorFragment extends Fragment {
    /*
    * 1. 메인화면과 합치기 위하여 프래그먼트로 작성
    * 2. 하단과 상단의 뷰들은 확인을 위해 임시로 생성
    * 3. 서버 클라이언트간 Log의 Tag는 "Server"로 통일
    * */

    //문이 열림, 닫힘, 잠김 상황을 상수로 표현
    public static final int DOOR_OPEN=1; //열린 상태
    public static final int DOOR_CLOSE=2; //닫힌 상태
    public static final int DOOR_LOCK=3; //잠긴 상태
    public static final int DOOR_NOPE=13; //문이 열린(닫히지않은) 상태에서 잠그려 할때
    public static final int DOOR_CCLOSE=22; //문이 이미 잠겨있지 않을때 풀려 할때
    public static final int DOOR_LLOCK=33; //문이 잠겨 있을때 잠그려 할때

    TextView textView; //도어락 상태 확인을 위한 텍스트뷰
    TextView textView2; //연결 상태 확인을 위한 텍스트뷰

    MenuActivity activity;//연결 액티비티

    //아이디와 비밀번호
    private String id;
    private String pw;

    //소켓 통신을 위한 핸들러(임시) -> 서버 구축시 HTTP 통신
    Handler handler = new Handler();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // 인플레이션
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_door, container, false);

        //메뉴 엑티비티 로드
        activity = (MenuActivity)getActivity();

        //상단의 상태 조작 버튼과 텍스트 뷰(임시)
        textView  = rootView.findViewById(R.id.check_status);
        Button open = rootView.findViewById(R.id.open);
        Button close = rootView.findViewById(R.id.close);
        Button lock = rootView.findViewById(R.id.lock);

        //문을 조작하는 버튼
        ImageButton door_open = rootView.findViewById(R.id.door_open);
        ImageButton door_lock = rootView.findViewById(R.id.door_lock);

        //하단의 연결 상태 확인 텍스트뷰
        textView2 = rootView.findViewById(R.id.link_status);

        //MenuActivity에서 로그인 정보 받기
        id = getArguments().getString("id");
        pw = getArguments().getString("pw");
        println("아이디 : " + id + ", 비밀번호 : " + pw);


        //상단의 문상태 조정 버튼(임시) DoorFragment-MenuActivity-Server 메서드로 구현
        open.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.changeDoor(DOOR_OPEN);
            }
        });
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.changeDoor(DOOR_CLOSE);
            }
        });
        lock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.changeDoor(DOOR_LOCK);
            }
        });

        //문 조작 버튼 DoorFragment-Server 소켓 통신 구현
        door_open.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = "문을 여시겠습니까?";
                requestDialog(v,message,DOOR_CLOSE);
            }
        });
        door_lock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = "문을 잠그시겠습니까?";
                requestDialog(v,message,DOOR_LOCK);
            }
        });

        return rootView;
    }

    //다이얼로그 출력
    public void requestDialog(View v, String message, int door){
        String title="확인 메시지";
        String titleButtonYes = "예";
        String titleButtonNo = "아니오";
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(titleButtonYes,new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialogInterface,int i){
                //클라이언트 쓰레드 생성
                new Thread(new Runnable(){
                    public void run(){
                        send(door);
                    }
                }).start();
            }
        });
        builder.setNegativeButton(titleButtonNo,new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialogInterface,int i){ }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }



    //클라이언트 쓰레드
    public void send(int door){
        try{
            int portNumber = 5001;
            Socket sock = new Socket("localhost",portNumber);

            ObjectOutputStream outstream = new ObjectOutputStream(sock.getOutputStream());
            outstream.writeObject(door);
            outstream.flush();

            ObjectInputStream instream = new ObjectInputStream(sock.getInputStream());
            int status = Integer.parseInt(instream.readObject().toString());

            //각각의 상태 변화에 따라 화면에 출력(임시)
            switch(status){
                case DOOR_CLOSE: //잠금 해제
                    println("잠금이 해제 되었 습니다.");
                    break;
                case DOOR_LOCK: //잠금 설정
                    println("잠금이 설정 되었 습니다.");
                    break;
                case DOOR_NOPE: //연 상태로 잠금
                    println("문을 닫고 잠궈 주세요!");
                    break;
                case DOOR_CCLOSE: //해제 중복
                    println("잠금이 이미 해제 되어 있습니다");
                    break;
                case DOOR_LLOCK: //잠금 중복
                    println("잠금이 이미 설정 되어 있습니다.");
                    break;
            }

            sock.close();

        }catch(Exception e){
            e.printStackTrace();
        }

    }
    //연결 상태 로그 추가
    public void println(String data){

        handler.post(new Runnable(){
            public void run(){
                textView2.append(data + "\n");
            }
        });
    }

}