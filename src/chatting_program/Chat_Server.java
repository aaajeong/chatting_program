package chatting_program;

import java.io.*;
import java.net.*;
import java.awt.*;
import java.util.*;
import javax.swing.*;
import java.text.*;

public class Chat_Server extends javax.swing.JFrame {
	
	private JTextArea display = null;
	private ServerSocket serversock;
    private Socket sock = null;
    private HashMap hm = new HashMap(); 
    private JScrollPane scrollpane = null;

	public Chat_Server() {
	
        ImageIcon icon = new ImageIcon(Chat_Server.class.getResource("../img/server.png"));
		Font server_font = new Font("arian", Font.PLAIN, 16);


		JFrame frame = new JFrame();
		display = new JTextArea(10, 500) {
			public void paintComponent(Graphics g) {
                // Approach 1: Dispaly image at at full size
                g.drawImage(icon.getImage(), 0, 0, null);
                setOpaque(false); //그림을 표시하게 설정,투명하게 조절
                super.paintComponent(g);
            }
		};
        display.setFont(server_font);
        display.setForeground(new Color(9, 251, 211));
		display.setLineWrap(true);		//행 넘길 때 행의 마지막 단어가 두행에 걸쳐 나뉘지 않도록 하기
		
		scrollpane = new JScrollPane(display);
		scrollpane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollpane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
        display.setBackground(Color.darkGray);
        
		frame.setTitle("Chat Server");
//		frame.getContentPane().add(scrollpane);
		frame.add(scrollpane);
		
		display.setEditable(false);		//실행되면 수정할 수 없음.
		frame.setSize(500, 500);
		
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);	// close 버튼을 누르면 종료
		frame.setVisible(true);		//화면에 보이게 
		
		// 서버 소켓 생성하기 
		try {
			//TextArea 객체인 display에 문구를 추가합니다
            ServerSocket serversock = new ServerSocket();
            serversock.bind(new InetSocketAddress("192.168.35.97", 9991));
            display.append(" Waiting Connect...");
            display.append("\n");
            System.out.println("접속을 기다립니다.");	//콘솔창에 

            while(true){
            	
                   Socket sock = serversock.accept();
                   ChatThread chatthread = new ChatThread(sock,hm);
                   chatthread.start();
            }
		}catch(Exception ex){     
		
            System.out.println("Connect Error ! ");
            display.append(" Connect Error ! ");
            display.append("\n");
            System.out.println(ex);
            System.exit(1);
		}
	}

	public static void main(String args[]) {
		new Chat_Server();
	}	

	// ChatThread 쓰레드 
	class ChatThread extends Thread {
		private Socket sock;
		private String id;
		private BufferedReader br;
		private HashMap hm = new HashMap();
		private boolean initFlag = false;

		// 생성
		public ChatThread(Socket sock, HashMap hm) {

			// 시간 출력 
			Calendar calendar = Calendar.getInstance();
			java.util.Date date = calendar.getTime();
			String today = (new SimpleDateFormat("H:mm:ss").format(date));
//			System.out.println(today);
			
			this.sock = sock;
			this.hm = hm;
			try {
				PrintWriter pw = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()));
				br = new BufferedReader(new InputStreamReader(sock.getInputStream()));
				id = br.readLine();

				broadcast(id + "님이 접속했습니다.");
				display.append("접속한 사용자의 아이디는 " + id + "입니다.");
				display.append("\n");

				System.out.println("접속한 사용자의 아이디는 " + id + "입니다.");

				// Thread 동기화 - 한 쓰레드만이 블록 안의 코드에 접근할 수 있다.
				// 하나의 hm (HashMap)을 공유한다.
				// HashMap에 있는 자료를 삭제하거나, 수정하거나, 읽어오는 부분이 동시에 일어날 수 있기 때문이다.
				synchronized (hm) {
					// key-id, value-pw(printwriter)로 받아서 hm에 key-value 쌍 저장 
					hm.put(this.id, pw);
				}
				initFlag = true;
			} catch (Exception ex) {
				System.out.println(ex);
			}
		}

		// 스레드의 동작을 정의 
		public void run() {

			try {
				
				String line = null;

				// 소켓을 통해 한 한줄씩(readLine) 읽어온다. 
				while ((line = br.readLine()) != null) {
					if (line.equals("/quit"))			// /quit 이면 중단  
						break;
					if (line.indexOf("/to") == 0) {		// /to 이면 특정 아이디의 클라이언트에게로. 
						sendmsg(line);
					} else {							// 그 밖의 일반적인 메세지는 broadcast() 메소드를 사용해서 전송 
						broadcast(id + " : " + line);
					}
				}
			} catch (Exception e) {
				System.out.println(e);
			} finally {				// 클라이언트가 접속을 종료했을 때 
				// Thread 동기화 
				// hashmap 에서 현재 스레드에 해당하는 id 삭제 
				synchronized (hm) {
					hm.remove(id);
				}
				// 종료한 클라이언트에 대해 나머지 클라이언트에게 메세지 전송 
				broadcast(id + " 님이 접속 종료했습니다.");
				display.append(id + " 님이 접속 종료했습니다. ");
				display.append("\n");
				try {
					if (sock != null)
						sock.close();
				} catch (Exception ex) {
				}
			}
		}
		
		// sendmsg()는 위 형식의 문자열을 문자열 관련 메소드를 이용해서 분석.
		// HashMap에서 key 값이 전송 받을 ID를 찾아 PrintWriter를 이용해서 문자열을 전송한다.
		// ex) /to [전송받을 ID] [문자열]
		// start: id 시작 인덱스, end: id 끝 인덱스, to: 전송 받을 id, msg2: 전송 할 메세지 
		public void sendmsg(String msg) {
			
			int start = msg.indexOf(" ") + 1;
			int end = msg.indexOf(" ", start);

			if (end != -1) {
				String to = msg.substring(start, end);
				String msg2 = msg.substring(end + 1);
				Object obj = hm.get(to);

				if (obj != null) {
					PrintWriter pw = (PrintWriter) obj;
					pw.println(id + "님이 다음의 귓속말을 보내셨습니다. :" + msg2);
					pw.flush();
				}
			}
		}
		
		// 접속한 모든 클라이언트에게 메세지 전송  
		public void broadcast(String msg){

            synchronized(hm){
                  Collection collection = hm.values();
                  Iterator iter = collection.iterator();

                  while(iter.hasNext()){
                         PrintWriter pw = (PrintWriter)iter.next();
                         pw.println(msg);
                         pw.flush();
                  }
            }
		}
	}


	// Variables declaration - do not modify
//	private javax.swing.JScrollPane jScrollPane1;
//	private javax.swing.JButton msg_send;
//	private javax.swing.JTextField msg_text;
	// End of variables declaration
}
