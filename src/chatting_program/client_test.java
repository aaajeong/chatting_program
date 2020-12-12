package chatting_program;

import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.io.*;

public class client_test {
	
	public static void main (String args[]) {
		
		String msg = "/to ahjeong 너지금뭐하니";
             
		int start = msg.indexOf(" ") + 1;
		int end = msg.indexOf(" ", start);
		if (end != -1) {
			String to = msg.substring(start, end);
			String msg2 = msg.substring(end + 1);
			
			System.out.println (start);
			System.out.println (end);
			System.out.println (to);
			System.out.println (msg2);
		}
		
	}

	
}
