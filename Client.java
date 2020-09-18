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
    
    private JPanel pnlContent;
    private CardLayout cardlayout;
    private LoginPanel login;
    private MessengerPanel messenger;

    private ArrayList<String> log;

    public Client() throws IOException {
        cardlayout = new CardLayout();
        pnlContent = new JPanel();
        
        login = new LoginPanel(cardlayout,pnlContent,this);
        messenger = new MessengerPanel(cardlayout,pnlContent,this);

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
        Thread sendMessage = new Thread(new Runnable()  
        { 
            @Override
            public void run() { 
                while (true) { 
                    //String msg = scn.nextLine(); 
                    messenger.btnSend.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            try { 
                                 // read the message to deliver. 
                                String msg = messenger.getMessage();
                                // write on the output stream 
                                dos.writeUTF(msg); 
                                log.add(uname + ": " + msg);
                            } catch (IOException err) { 
                                err.printStackTrace(); 
                            } 
                        }
                    });                   
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
                        login.scroll.addText(msg);
                    } catch (IOException e) { 
                        e.printStackTrace(); 
                    } 
                } 
            } 
        }); 
        readMessage.start(); 
    }

    public void terminate() throws IOException {
        dis.close();
        dos.close();
        s.close();
    }
  
    public static void main(String args[]) throws IOException
    { 
       
        Client client = new Client();
        //Scanner scn = new Scanner(System.in);
       // client.scn = scn; 
        // getting localhost ip 
        //InetAddress ip = InetAddress.getByName("localhost"); 
        
        // sendMessage thread
        //client.sendMsg();
          
        // readMessage thread 
        //client.listen();
  
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
            // establish the connection given the credentials
            loginServer();
            card.next(pnl);
            startThread();
        } catch(IOException err) {
            System.out.println("Can't connect!");
        }
    }
    public void loginServer() throws IOException {
        cl.connect();
    }
    public void startThread() throws IOException {
        cl.sendMsg();
        cl.listen();
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
    JScrollPane scroll;

    CardLayout card;
    JPanel pnl;
    Client cl;

    public MessengerPanel(CardLayout card, JPanel pnl, Client cl) {
        this.card = card;
        this.pnl = pnl;
        this.cl = cl;

        text = new JTextArea(10,20);
        text.setEditable(false);

        txtMsg = new JTextField(20);
        JLabel lblMsg = new JLabel("Message");
        btnSend = new JButton("Send");
        btnLogout = new JButton("Logout");
        btnLogout.addActionListener(this);

        scroll = new JScrollPane(text);
        add(scroll);
        add(lblMsg);
        add(txtMsg);
        add(btnSend);
        add(btnLogout);
        setBackground(Color.blue);
    }   
    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            exit();
            card.next(pnl);
        } catch (IOException err) {
            System.out.println("Cannot exit!");
        }
    }
    public void exit() throws IOException {
        cl.terminate();
    }
    public String getMessage() {
        return txtMsg.getText();
    }
}