package chatting_program;

import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.io.*;
import javax.swing.*;

public class Chat_Client extends Frame implements ActionListener {

	private JTextField idTF = null;
	private JButton login = null;
	private TextField input = null;
	private TextArea display = null;
	private CardLayout cardLayout = null;
	private BufferedReader br = null;
	private PrintWriter pw = null;
	private Socket sock = null;

	/*************************************************
	 * 
	 * 생성자에서는 접속할 ip를 인자로 전달 받음
	 * 
	 * 아이디와 문자열을 입력받기 위해,
	 * 
	 * 각 텍스트 필드 idTF와 input에 ActionListner 추가
	 * 
	 ************************************************/

	public Chat_Client(String ip) {

		// 컴포넌트 배치 부분
		super("Chat Client");
		cardLayout = new CardLayout();
		setLayout(cardLayout);
//		JPanel loginPanel = new JPanel();
        ImageIcon icon = new ImageIcon(Chat_Client.class.getResource("../img/login.png"));

		JPanel loginPanel = new JPanel() {
            public void paintComponent(Graphics g) {
                // Approach 1: Dispaly image at at full size
                g.drawImage(icon.getImage(), 0, 0, null);
                // Approach 2: Scale image to size of component
                // Dimension d = getSize();
                // g.drawImage(icon.getImage(), 0, 0, d.width, d.height, null);
                // Approach 3: Fix the image position in the scroll pane
                // Point p = scrollPane.getViewport().getViewPosition();
                // g.drawImage(icon.getImage(), p.x, p.y, null);
                setOpaque(false); //그림을 표시하게 설정,투명하게 조절
                super.paintComponent(g);
            }
        };
		
		
//		loginPanel.setLayout(new FlowLayout());
        loginPanel.setLayout(null);
		
		loginPanel.add(new Label("Ip 주소와 ID를 입력하세요"));
		
		// ID 입력 패널 
//		Panel idPanel = new Panel();
//		Label idLabel = new Label("ID : ");
		idTF = new JTextField(20);		// id 입력 
//		idLabel.setBounds(200, 30, 50, 50);
//		idTF.setBounds(260, 500, 50, 50);
//		idPanel.add(idLabel);
//		idPanel.add(idTF);
		idTF.addActionListener(this);
		
		// login 버튼 
		login = new JButton();
		login.setBounds(100, 400, 250, 70);
		login.addActionListener(this); 
		
		idTF.setBounds(150, 330, 200, 60);
		loginPanel.add(idTF);
		loginPanel.add(login);
		
		// 아이디 창 투명도, 컬러 설정 
		idTF.setOpaque(false);
		idTF.setForeground(Color.DARK_GRAY);
		idTF.setBorder(javax.swing.BorderFactory.createEmptyBorder());
		
		// login 버튼 투명도 설정 
		login.setBorderPainted(false);
		login.setFocusPainted(false);
		login.setContentAreaFilled(false);
		
		
		this.add("login", loginPanel);
		Panel main = new Panel();
		main.setLayout(new BorderLayout());
		
		input = new TextField();
		input.addActionListener(this);
		
		display = new TextArea();
		display.setEditable(false);
		main.add("Center", display);
		main.add("South", input);
		add("main", main);

		try {
			// 서버에 접속하기 위해 socket 생성 
			// outputStream은 PrintWriter, inputStream은 BufferredReader 
			sock = new Socket();
			sock.connect(new InetSocketAddress(ip, 9991));
			pw = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()));
			br = new BufferedReader(new InputStreamReader(sock.getInputStream()));

		} catch (Exception ex) {

			System.out.println("Connect Error ! ");
			System.out.println(ex);
			System.exit(1);

		}

		setSize(440, 700);

		cardLayout.show(this, "login");

		/*************************************************
		 * 
		 * 프레임에 WindowListner 추가 : 윈도우 닫힐 때 이벤트 발생
		 * 
		 *************************************************/

		// 윈도우가 종료하면 /quit 메세지를 보내고 socket 종료 
		addWindowListener(new WindowAdapter() {

			public void windowClosing(WindowEvent e) {

				pw.println("/quit");
				pw.flush();

				try {
	
					sock.close();
					
				} catch (Exception ex) {
					
				}
				System.out.println("Exit");
				System.exit(0);
			}

		});

		setVisible(true);
	}


	public static void main(String[] args) {

		if (args.length != 1) {
			
			System.out.println("사용법 : java Chat_Client ip");
			System.exit(1);

		}
		System.out.println(args[0]);
		new Chat_Client(args[0]);
	}

	/******************************************************
	 * 
	 * ActionEvent를 처리하기 위한 메소드
	 * 
	 * idTF에서 id를 입력받거나 문자열을 입력받을 경우 호출되는 메소드
	 * 
	 *****************************************************/

	public void actionPerformed(ActionEvent e) {

		// 아이디 입력 창으로부터 아이디를 입력받으면 
		// 서버로부터 문자열을 받아 TextArea에 추가하는 WinInputThread를 생성한 후 실행 
		String id = idTF.getText();
//		String ip = ipTF.getText();
		
		if (e.getSource() == idTF || e.getSource() == login) {
			if (id == null || id.trim().equals("")) {
				System.out.println("Input the Id");
				JOptionPane.showMessageDialog(null, "아이디를 입력 하셔야 됩니다.", "아이디 입력", JOptionPane.DEFAULT_OPTION);
				return;
			}

			// 서버로 입력받은 id 전송
			pw.println(id.trim());
			pw.flush();
			
			// 서버로부터 입력받은 문자열 출력하는 입력 스레드 생성 
			WinInputThread wit = new WinInputThread(sock, br);
			wit.start();

// main 컴포넌트가 보여짐 (채팅 화면이 보여진다.) 
			cardLayout.show(this, "main");
			input.requestFocus();

		} else if (e.getSource() == input) {	// TextField에 문자열이 입력되면, 저장된 문자열을 socket을 통해 전송한다.

			String msg = input.getText();
			pw.println(msg);
			pw.flush();
			if (msg.equals("/quit")) {		// 입력된 메세지가 /quit 이면, 채팅 창 종료, 소켓 종료 
				try {
					sock.close();
				} catch (Exception ex) {
					
				}
				
				System.out.println("Exit");
				System.exit(1);
			}
			input.setText("");
			input.requestFocus();
		}
	}

	/*************************************************************
	 * 
	 * 서버로부터 전달된 문자열을 모니터에 출력하는 WinInputThread 객체를 생성
	 * 
	 *************************************************************/

	class WinInputThread extends Thread {

		private Socket sock = null;
		private BufferedReader br = null;
		public WinInputThread(Socket sock, BufferedReader br) {
			this.sock = sock;
			this.br = br;
		}

		public void run() {

			try {
				String line = null;
				while ((line = br.readLine()) != null) {
					display.append(line + "\n");
				}
			} catch (Exception ex) {
				
			} finally {
				try {
					if (br != null)
						br.close();
				} catch (Exception ex) {
				}
				try {
					if (sock != null)
						sock.close();
				} catch (Exception ex) {
				}
			}
		}
	}
}
