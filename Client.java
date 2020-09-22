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
import java.time.*; 
import java.time.format.DateTimeFormatter; 
import java.nio.charset.*;
import java.nio.file.*;
  
public class Client extends JFrame 
{ 
    private DataInputStream dis;
    private DataOutputStream dos;
    private Socket s;
    private String uname;

    //For testing
    //private Scanner scn;
    
    private JPanel pnlContent;
    private CardLayout cardlayout;
    private LoginPanel login;
    private MessengerPanel messenger;

    private ArrayList<String> log;
    private Boolean status;

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
        setSize(250,320);
        setVisible(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        log = new ArrayList<>();
        status = false;
    }

    public void connect() throws IOException {
           // establish the connection 
           s = new Socket(login.getIP(), login.getPort());
             
           // obtaining input and out streams 
           dis = new DataInputStream(s.getInputStream()); 
           dos = new DataOutputStream(s.getOutputStream()); 
           uname = login.getName();
           System.out.println("[" + (LocalTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME)) + "] " + login.getIP() + " -> " + login.getPort() + " -> " + login.getName());
           dos.writeUTF(uname);   
           log.add("[" + (LocalTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME)) + "] " + uname + " connecting to the server.");
           status = true;
           setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    }

    public void listen() throws IOException {
        Thread readMessage = new Thread(new Runnable()  
        { 
            @Override
            public void run() { 
  
                while (true) { 
                    if(status)
                    {
                        try { 
                            // read the message sent to this client 
                            String msg = dis.readUTF(); 
                            System.out.println("[" + (LocalTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME)) + "] " + msg); 
                            messenger.text.append("[" + (LocalTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME)) + "] " + msg);
                            log.add("[" + (LocalTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME)) + "] " + msg);
                        } catch (IOException e) { 
                            e.printStackTrace(); 
                        }
                    } 
                } 
            } 
        }); 
        readMessage.start(); 
    }

    public void terminate() throws IOException {
        dos.writeUTF("#logout"); 
        dis.close();
        dos.close();
        s.close();
        status = false;
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    public void send(String message) throws IOException {
        if(message != "#logout") {
            System.out.println("[" + (LocalTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME)) + "] " + this.uname + ": " + message + "\n");
            dos.writeUTF(message); 
            messenger.text.append("[" + (LocalTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME)) + "] " + this.uname + ": " + message + "\n");
            log.add("[" + (LocalTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME)) + "] " + this.uname + ": " + message + "\n");
        } else
            messenger.exit();
    }

    public void setName() {
        messenger.lblName.setText(login.getName());
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
    
    public void addToLog(String s){
        log.add(s);
    }

    public String getName(){
        return this.uname;
    }

    public ArrayList<String> getLog(){
        return this.log;
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
        setBackground(new Color(255,120,120));
    }
    @Override
    public void actionPerformed (ActionEvent e) {
        try {
            // establish the connection given the credentials
            loginServer();
            cl.setName();
            card.next(pnl);
            // clear text fields
            txtIP.setText(null);
            txtPort.setText(null);
            txtName.setText(null);
            startThread();
        } catch(IOException err) {
            System.out.println("Can't connect!");
        }
    }
    public void loginServer() throws IOException {
        cl.connect();
    }
    public void startThread() throws IOException {
      //  cl.sendMsg();
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
    JLabel lblName;
    JTextArea text;
    JTextField txtMsg;
    JButton btnSend;
    JButton btnLogout;
    JButton btnSendFile;
    JScrollPane scroll;
    JFileChooser fc;

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
        lblName = new JLabel();
        JLabel lblMsg = new JLabel("Message");
        btnSend = new JButton("Send");
        btnLogout = new JButton("Logout");
        btnSendFile = new JButton("Send File");
        fc = new JFileChooser(System.getProperty("user.dir"));

        btnSend.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try { 
                     // read the message to deliver. 
                    String msg = txtMsg.getText();
                    if(!msg.equals("")){
                        cl.send(msg);
                        txtMsg.setText("");
                    }
                } catch (IOException err) { 
                    err.printStackTrace(); 
                } 
            }
        });

        btnSendFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e){
                try{
                    int state = fc.showOpenDialog(cl);
                    System.out.println(state);
                } catch (Exception err){
                    err.printStackTrace();
                }
            };
        });

        btnLogout.addActionListener(this);

        scroll = new JScrollPane(text);
        add(lblName);
        add(scroll);
        add(lblMsg);
        add(txtMsg);
        add(btnSend);
        add(btnLogout);
        add(btnSendFile);
        setBackground(new Color(120,120,255));
    } 
      
    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getActionCommand().equals("Logout")){
            try {
                int dialogResult = JOptionPane.showConfirmDialog (this, "Would you like to save the message history to a log file?","Save log?",JOptionPane.YES_NO_OPTION);
                if(dialogResult == JOptionPane.YES_OPTION){
                    cl.addToLog("[" + (LocalTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME)) + "] " + "Disconnected from server.");
                    String currentDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH-mm-ss"));
                    try{
                        Path file = Paths.get("[" + currentDateTime + "] " + cl.getName() + " log.txt");
                        Files.write(file, cl.getLog(), StandardCharsets.UTF_8);
                        JOptionPane.showMessageDialog(this, "Successfully saved log! Check the working directory of this program.\n Filename: " + "[" + currentDateTime + "] " + cl.getName() + " log.txt");
                    }catch (IOException error){
                        JOptionPane.showMessageDialog(this, "Error with creating log! Proceeding with logout without creating log.");
                        error.printStackTrace();
                    }
                }
                exit();
                card.next(pnl);
            } catch (IOException err) {
                System.out.println("Cannot exit!");
                System.out.println(err);
            }
        }
    }
    public void exit() throws IOException {
        cl.terminate();
        text.setText(null);
        txtMsg.setText(null);
    }
    public String getMessage() {
        return txtMsg.getText();
    }

}
