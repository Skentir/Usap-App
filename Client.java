// Java implementation for multithreaded chat client 
// Save file as Client.java 
  
import java.io.*; 
import java.net.*; 
import java.util.*; 
import javax.swing.*;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
  
public class Client extends JFrame 
{ 
    private DataInputStream dis;
    private DataOutputStream dos;
    private Socket s;
    private String uname;

    //For testing
    private Scanner scn;
    final static int ServerPort = 12345; 
    
    private JPanel pnlContent;
    private CardLayout cardlayout;
    private LoginPanel login;
    private MessengerPanel messenger;

    private ArrayList<String> log;

    public Client() throws IOException {
        cardlayout = new CardLayout();
        pnlContent = new JPanel();
        
        login = new LoginPanel(cardlayout,pnlContent,this);
        messenger = new MessengerPanel(cardlayout,pnlContent);

        pnlContent.setLayout(cardlayout);
        pnlContent.add(login, "login");
        pnlContent.add(messenger,"messenger");

        setContentPane(pnlContent);
        setLocationRelativeTo(null);
        setResizable(false);
        setSize(250,300);
        setVisible(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        log = new ArrayList<>();
    }

    public void connect() throws IOException {
           // establish the connection 
           s = new Socket(login.getIP(), login.getPort());
             
           // obtaining input and out streams 
           dis = new DataInputStream(s.getInputStream()); 
           dos = new DataOutputStream(s.getOutputStream()); 
           uname = login.getName();
           System.out.println(login.getIP() + " -> " + login.getPort() + " -> " + login.getName());
           dos.writeUTF(uname);   
           log.add(uname + " connecting to the server.");
    }
 
    public void sendMsg() throws IOException {
        // sendMessage thread 
        String name = this.uname;
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
                        log.add(name + ": " + msg);
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
        //client.connect(args[0],Integer.parseInt(args[1]), args[2]);
        client.connect();
        
        // sendMessage thread
        client.sendMsg();
          
        // readMessage thread 
        client.listen();
  
    } 
} 

class LoginPanel extends JPanel implements ActionListener {
    JTextField txtIP;
    JTextField txtPort;
    JTextField txtName;
    JButton btnEnter; 

    CardLayout card;
    JPanel pnl;
    Client cl; 
    public LoginPanel(CardLayout card, JPanel pnl, Client cl) {
        this.card = card;
        this.pnl = pnl;
        this.cl = cl;
        txtIP = new JTextField(20);
        JLabel lblIP = new JLabel("IP Address");
        txtPort = new JTextField(20);
        JLabel lblPort = new JLabel("Port");
        txtName = new JTextField(20);
        JLabel lblName = new JLabel("Name");
        btnEnter = new JButton("Enter");
        btnEnter.addActionListener (this);
        
        add(lblIP);
        add(txtIP);
        add(lblPort);
        add(txtPort);
        add(lblName);
        add(txtName);
        add(btnEnter);
        setBackground(Color.red);
    }
    @Override
    public void actionPerformed (ActionEvent e) {
        try {
            loginServer();
            card.next(pnl);
        } catch(IOException err) {
            System.out.println("Can't connect!");
        }
    }
    public void loginServer() throws IOException {
        cl.connect();
    }
    public String getIP() {
        return txtIP.getText();
    }
    public Integer getPort() {
        return Integer.parseInt(txtPort.getText());
    }
    public String getName() {
        return txtName.getText();
    }
}

class MessengerPanel extends JPanel implements ActionListener {
    JTextArea text;
    JTextField txtMsg;
    JButton btnSend;
    JButton btnLogout;
    
    CardLayout card;
    JPanel pnl;

    public MessengerPanel(CardLayout card, JPanel pnl) {
        this.card = card;
        this.pnl = pnl;

        text = new JTextArea(10,20);
        text.setEditable(false);

        txtMsg = new JTextField(20);
        JLabel lblMsg = new JLabel("Message");
        btnSend = new JButton("Send");
        btnLogout = new JButton("Logout");
        btnLogout.addActionListener(this);

        JScrollPane scroll = new JScrollPane(text);
        add(scroll);
        add(lblMsg);
        add(txtMsg);
        add(btnSend);
        add(btnLogout);
        setBackground(Color.blue);
    }   
    @Override
    public void actionPerformed(ActionEvent e) {
        card.next(pnl);
    }
}