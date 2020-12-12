//package chatting_program;
//
//import java.awt.*;
//import java.awt.Graphics;
//import java.awt.Image;
//import javax.swing.ImageIcon;
//import javax.swing.JFrame;
//import javax.swing.JPanel;
//import javax.swing.*;
//
//public class img_test extends JFrame{
//	JButton button = new JButton("버튼");
//	static JPanel page1=new JPanel() {
//		Image background=new ImageIcon(img_test.class.getResource("../img/layout.png")).getImage();
//		public void paint(Graphics g) {//그리는 함수
//				g.drawImage(background, 0, 0, null);//background를 그려줌		
//		}
//	};
//	public img_test() {
//		homeframe();
//	}
//	public void homeframe() {
//		setTitle("1");
//		setSize(430,700);//프레임의 크기
//		setResizable(false);//창의 크기를 변경하지 못하게
//		setLocationRelativeTo(null);//창이 가운데 나오게
//		setLayout(null);
//		page1.setLayout(new BorderLayout());
//		page1.setBounds(0, 0, 430, 700);
////		button.setBounds(0, 0, 300, 300);
//		page1.add("Center", button);
//
//		add(page1);
//		setVisible(true);//창이 보이게	
//		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);//JFrame이 정상적으로 종료되게
//		
//	}
//	public static void main(String[] args){
//		new img_test();
//	}
//}
package chatting_program;
import java.awt.event.*;
import javax.swing.*;
import java.awt.*;
 
public class img_test extends JFrame implements ActionListener{
	
	private CardLayout cards = new CardLayout();
	JPanel panel;
    JScrollPane scrollPane;
    ImageIcon icon;
    JButton button;
 
    public img_test() {
    	getContentPane().setLayout(cards);

        icon = new ImageIcon(img_test.class.getResource("../img/layout.png"));
       
        //배경 Panel 생성후 컨텐츠페인으로 지정      
        JPanel background = new JPanel() {
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
        
        JPanel panel = new JPanel();
        JLabel label = new JLabel("after");
        panel.add(label);
        
       
       
        button = new JButton("버튼");
        button.addActionListener(this);


        background.add(button);
        scrollPane = new JScrollPane(background);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

//        setContentPane(scrollPane);
        getContentPane().add("One", scrollPane);
        getContentPane().add("panel", panel);
        
        

    }
 
    public static void main(String[] args) {
        img_test frame = new img_test();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(440, 700);
        frame.setVisible(true);
        frame.setTitle("Chat_Client");
    }
    
    public void actionPerformed(ActionEvent e) {
		
		if (e.getSource() == button) {
			System.out.println("dd");
			cards.next(panel);

		} 
	}
}