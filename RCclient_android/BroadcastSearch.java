package broadcast;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class BroadcastSearch {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			int port = 5001;
	
		    byte[] message = "Java Source and Support".getBytes();
		    
		    // Get the internet address of the specified host
		    InetAddress address = InetAddress.getByName("10.10.10.255");
		    
		    // Initialize a datagram packet with data and address
		    DatagramPacket packet = new DatagramPacket(message, message.length, address, port);
		    // Create a datagram socket, send the packet through it, close it.
		    DatagramSocket dsocket = new DatagramSocket();
		    dsocket.send(packet);
		    dsocket.close();
		    
		    //receive
		    // Create a socket to listen on the port.
		    DatagramSocket rsocket = new DatagramSocket(port);
		    
		    // Create a buffer to read datagrams into. If a
		    // packet is larger than this buffer, the
		    // excess will simply be discarded!
		    byte[] buffer = new byte[2048];
		    
		    // Create a packet to receive data into the buffer
		    packet = new DatagramPacket(buffer, buffer.length);
		    rsocket.setSoTimeout(10000);
		    
	        // Wait to receive a datagram
	        rsocket.receive(packet);

	        // Convert the contents to a string, and display them
	        String msg = new String(buffer, 0, packet.getLength());
	        System.out.println(packet.getAddress().getHostName() + ":" + packet.getPort() + " "
	            + msg);

	        // Reset the length of the packet before reusing it.
	        packet.setLength(buffer.length);
	  
	        
		    

		} catch (Exception e) {
		    System.err.println(e);
		}

	}

}
