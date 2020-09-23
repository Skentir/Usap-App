// Java implementation for multithreaded chat client 
// Save file as Client.java 
  
import java.io.*; 
import java.net.*; 
import java.util.*; 
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.*; 
import java.time.format.DateTimeFormatter; 
import java.nio.charset.*;
import java.nio.file.*;
import java.util.regex.*;
  
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
        setSize(300,400);
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
           this.messenger.text.append("[" + (LocalTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME)) + "] " + uname + " connecting to the server.\n");
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
                            String finalOutput;

                            //receiving a file
                            if(msg.contains("#filesend")){
                                String[] tokens = msg.split("#");
                                finalOutput = "[" + (LocalTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME)) + "] " + "Receiving file " + tokens[2] + " from " + tokens[3];
                                System.out.println(finalOutput);
                                messenger.text.append(finalOutput + "\n");
                                log.add(finalOutput);
                                String savedir = messenger.getSaveDirectory(tokens[2]);
                                FileOutputStream fos = new FileOutputStream(savedir);
                                copy(dis,fos);
                                fos.close();
                            }
                            else{
                                finalOutput = "[" + (LocalTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME)) + "] " + msg;
                                System.out.println(finalOutput); 
                                messenger.text.append(finalOutput);
                                log.add(finalOutput);
                            }
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

    public void sendFile(String filepath) throws FileNotFoundException, IOException{
        String[] path = filepath.split(Pattern.quote(File.separator));
        FileInputStream fis = new FileInputStream(new File(filepath));
        long size = new File(filepath).length();
        dos.writeUTF("#filesend#" + path[path.length-1] + "#" + size);
        messenger.text.append("[" + (LocalTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME)) + "] " + "Sending file " + path[path.length-1] + "\n");
        copy(fis,dos);
        dos.flush();
        dos.writeUTF("#EOF!");
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

    public int copy(InputStream in, OutputStream out) throws IOException {
		byte[] buf = new byte[2048];
		int bytesRead = 0;
		int totalBytes = 0;
		while((bytesRead = in.read(buf)) != -1) {
		  totalBytes += bytesRead;
		  out.write(buf, 0, bytesRead);
		}
		return totalBytes;
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
        
        this.setLayout(new BorderLayout());
        JPanel tempPanel = new JPanel();
        tempPanel.setBackground(new Color(255,120,120));
        this.add(tempPanel, BorderLayout.NORTH);
        tempPanel = new JPanel();
        tempPanel.setBackground(new Color(255,120,120));
        this.add(tempPanel, BorderLayout.SOUTH);
        tempPanel = new JPanel();
        tempPanel.setBackground(new Color(255,120,120));
        this.add(tempPanel, BorderLayout.WEST);
        tempPanel = new JPanel();
        tempPanel.setBackground(new Color(255,120,120));
		this.add(tempPanel, BorderLayout.EAST);

        tempPanel = new JPanel();
        tempPanel.setLayout(new GridLayout(10,1));
        tempPanel.setBackground(new Color(255,120,120));
        tempPanel.add(lblIP);
        tempPanel.add(txtIP);
        tempPanel.add(lblPort);
        tempPanel.add(txtPort);
        tempPanel.add(lblName);
        tempPanel.add(txtName);
        JPanel tempPanel2 = new JPanel();
        tempPanel2.setBackground(new Color(255,120,120));
        tempPanel.add(tempPanel2);
        tempPanel.add(btnEnter);
        this.add(tempPanel, BorderLayout.CENTER);
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

        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();

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
                    fc.setDialogTitle("Choose a file to send");
                    int state = fc.showOpenDialog(cl);
                    if(state == JFileChooser.APPROVE_OPTION){
                        cl.sendFile(fc.getSelectedFile().getAbsolutePath());
                    }
                } catch (Exception err){
                    err.printStackTrace();
                }
            };
        });

        btnLogout.addActionListener(this);

        this.setLayout(new BorderLayout());

        JPanel tempPanel = new JPanel();
        tempPanel.setBackground(new Color(120,120,255));
        this.add(tempPanel, BorderLayout.NORTH);
        tempPanel = new JPanel();
        tempPanel.setBackground(new Color(120,120,255));
        this.add(tempPanel, BorderLayout.SOUTH);
        tempPanel = new JPanel();
        tempPanel.setBackground(new Color(120,120,255));
        this.add(tempPanel, BorderLayout.WEST);
        tempPanel = new JPanel();
        tempPanel.setBackground(new Color(120,120,255));
		this.add(tempPanel, BorderLayout.EAST);

        tempPanel = new JPanel();
        tempPanel.setLayout(gbl);
        tempPanel.setBackground(new Color(120,120,255));

        
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 0.5;
        gbc.gridheight = 1;
        gbc.gridwidth = 3;
        gbc.gridx = 0;
        gbc.gridy = 0;
        tempPanel.add(lblName,gbc);
        gbc.gridheight = 5;
        gbc.gridy = 1;
        gbc.weighty = 1;
        scroll = new JScrollPane(text);
        tempPanel.add(scroll, gbc);
        gbc.weighty = 0;
        gbc.gridheight = 1;
        gbc.gridy = 7;
        tempPanel.add(lblMsg,gbc);
        gbc.gridy = 8;
        tempPanel.add(txtMsg,gbc);
        gbc.gridwidth = 1;
        gbc.gridy = 9;
        gbc.gridx = 0;
        tempPanel.add(btnSend,gbc);
        gbc.gridx = 1;
        tempPanel.add(btnLogout,gbc);
        gbc.gridx = 2;
        tempPanel.add(btnSendFile,gbc);
        this.add(tempPanel, BorderLayout.CENTER);
        
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

    public String getSaveDirectory(String defaultFileName){
        String choice;
        fc.setSelectedFile(new File(defaultFileName));
        fc.setDialogTitle("Specify a file to save.");
        int state = fc.showOpenDialog(cl);
        if(state == JFileChooser.APPROVE_OPTION){
            choice = fc.getSelectedFile().getAbsolutePath();
            fc.setSelectedFile(new File(""));
            fc.setCurrentDirectory(new File(System.getProperty("user.dir")));
            return choice;
        }
        else{
            choice = System.getProperty("user.dir") + "\\" + defaultFileName;
            fc.setSelectedFile(new File(""));
            fc.setCurrentDirectory(new File(System.getProperty("user.dir")));
            return choice;
        }
    }
}
