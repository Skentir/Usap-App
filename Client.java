// Java implementation for multithreaded chat client 
// Save file as Client.java 
  
import java.io.*; 
import java.net.*; 
import java.util.*; 
import javax.swing.*;
  
public class Client extends JFrame 
{ 
    private DataInputStream dis;
    private DataOutputStream dos;
    private Socket s;
    private String uname;

    //For testing
    private Scanner scn;

    private JTextArea text; 
    private JTextField txtMsg; 
    private JTextField txtIP; 
    private JTextField txtPort; 
    private JTextField txtName; 
    private JButton btnSend; 
    private JButton btnLogout;
    private JLabel lblMsg; 
    private JPanel pnlContent;
    
    final static int ServerPort = 1234; 

    public Client() throws IOException {
        pnlContent = new JPanel();
        text = new JTextArea(10,20);
        text.setEditable(false);

        txtMsg = new JTextField(20);
        lblMsg = new JLabel("Message");
        btnSend = new JButton("Send");

        JScrollPane scroll = new JScrollPane(text);
        pnlContent.add(scroll);
        pnlContent.add(lblMsg);
        pnlContent.add(txtMsg);
        pnlContent.add(btnSend);

        setContentPane(pnlContent);
        setLocationRelativeTo(null);
        setResizable(false);
        setSize(250,300);
        setVisible(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    public void connect(String ip, Integer port, String name) throws IOException {
           // establish the connection 
           s = new Socket(ip, port);
             
           // obtaining input and out streams 
           dis = new DataInputStream(s.getInputStream()); 
           dos = new DataOutputStream(s.getOutputStream()); 
           uname = name;

           dos.writeUTF(uname);   
    }
 
    public void sendMsg() throws IOException {
        // sendMessage thread 
        Thread sendMessage = new Thread(new Runnable()  
        { 
            @Override
            public void run() { 
                while (true) { 
  
                    // read the message to deliver. 
                    String msg = scn.nextLine(); 
                      
                    try { 
                        // write on the output stream 
                        dos.writeUTF(msg); 
                    } catch (IOException e) { 
                        e.printStackTrace(); 
                    } 
                } 
            } 
        }); 

        sendMessage.start(); 
    }

    public void listen() throws IOException {
        Thread readMessage = new Thread(new Runnable()  
        { 
            @Override
            public void run() { 
  
                while (true) { 
                    try { 
                        // read the message sent to this client 
                        String msg = dis.readUTF(); 
                        System.out.println(msg); 
                    } catch (IOException e) { 
  
                        e.printStackTrace(); 
                    } 
                } 
            } 
        }); 
        readMessage.start(); 
    }
  
    public static void main(String args[]) throws IOException
    { 
        
        /*txtIP = new JTextField("127.0.0.1"); 
        txtPort = new JTextField("12345"); 
        txtName = new JTextField("hayabusa");

        pnlContent = new JPanel(); */

        Client client = new Client();
        Scanner scn = new Scanner(System.in);
        client.scn = scn; 
        // getting localhost ip 
        //InetAddress ip = InetAddress.getByName("localhost"); 
        
        // establish the connection 
        client.connect(args[0],Integer.parseInt(args[1]), args[2]);
        
        // sendMessage thread
        client.sendMsg();
          
        // readMessage thread 
        client.listen();
  
    } 
} 