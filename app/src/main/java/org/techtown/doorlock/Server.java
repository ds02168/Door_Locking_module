package org.techtown.doorlock;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Server extends Service {
    public static final int DOOR_OPEN=1; //열린 상태
    public static final int DOOR_CLOSE=2; //닫힌 상태
    public static final int DOOR_LOCK=3; //잠긴 상태
    public static final int DOOR_NOPE=13; //문이 열린(닫히지않은) 상태에서 잠그려 할때
    public static final int DOOR_CCLOSE=22; //문이 이미 잠겨있지 않을때 풀려 할때
    public static final int DOOR_LLOCK=33; //문이 잠겨 있을때 잠그려 할때

    //서버 스레드
    SocketServerThread thread;


    public Server() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        thread = new SocketServerThread();
        thread.start();
    }

    //도어락 수동 조종(임시)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int status = intent.getIntExtra("door",DOOR_CLOSE);
        thread.door = status;

        //열고, 닫고, 잠그고 도어락 수동 조종의 경우
        switch(status){
            case DOOR_OPEN:
                Log.d("Server", "누군가 문을 열었습니다.");
                break;
            case DOOR_CLOSE:
                Log.d("Server", "누군가 문을 닫았습니다.");
                break;
            case DOOR_LOCK:
                Log.d("Server", "누군가 문을 잠궜습니다.");
                break;
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }


    //서버 구동(임시)
    class SocketServerThread extends Thread{
        //DB 부분
        int door = DOOR_CLOSE;

        //WAS 부분
        public void run(){
            try{
                int portNumber = 5001;
                ServerSocket server = new ServerSocket(portNumber);
                Log.d("Server","서버 구축");

                while(true){
                    //연결
                    Socket sock = server.accept();
                    //입력
                    ObjectInputStream instream = new ObjectInputStream(sock.getInputStream());
                    int requestDoor = Integer.parseInt(instream.readObject().toString());

                    //출력
                    ObjectOutputStream outstream = new ObjectOutputStream(sock.getOutputStream());


                    if(door == DOOR_OPEN && requestDoor == DOOR_LOCK) { //문을 연 상태로 닫을 수 없음
                        door = DOOR_NOPE; //닫기 불가 상태
                        outstream.writeObject(door);
                        door = DOOR_OPEN; //열림 으로 변경
                    }
                    else if(door == DOOR_OPEN && requestDoor == DOOR_CLOSE){ //잠금 해제 중복
                        door = DOOR_CCLOSE;
                        outstream.writeObject(door);
                        door = DOOR_OPEN;
                    }
                    else if(door == DOOR_CLOSE && requestDoor == DOOR_CLOSE){ // 잠금 해제 중복
                        door = DOOR_CCLOSE;
                        outstream.writeObject(door);
                        door = DOOR_CLOSE;
                    }
                    else if(door == DOOR_LOCK && requestDoor == DOOR_LOCK){ // 잠금 설정 중복
                        door = DOOR_LLOCK;
                        outstream.writeObject(door);
                        door = DOOR_LOCK;
                    }
                    else {  //이 외 상황은 가능
                        door = requestDoor;
                        outstream.writeObject(door);
                    }
                    outstream.flush();

                    //연결 종료
                    sock.close();
                }
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }
}