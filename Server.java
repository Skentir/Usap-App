// Java implementation of  Server side 
// It contains two classes : Server and ClientHandler 
// Save file as Server.java 
  
import java.io.*; 
import java.util.*; 
import java.net.*; 
  
// Server class 
public class Server
{ 
  
    // Vector to store active clients 
    static Vector<ClientHandler> ar = new Vector<>(); 
    
    // ArrayList for log
    static ArrayList<String> log = new ArrayList<>();

    // counter for clients 
    static int i = 0; 
  
    public static void main(String[] args) throws IOException  
    { 
        ServerSocket ss = new ServerSocket(Integer.parseInt(args[0])); 
          
        Socket s; 
        
        System.out.println("Server started! Listening at port " + args[0]);
        log.add("Server started. Port " + args[0]);

        // running infinite loop for getting 
        // client request 
        while (true)  
        { 
            // Accept the incoming request 
            s = ss.accept(); 
  
            System.out.println("New client request received : " + s); 
              
            // obtain input and output streams 
            DataInputStream dis = new DataInputStream(s.getInputStream()); 
            DataOutputStream dos = new DataOutputStream(s.getOutputStream()); 
            
            String tempName = dis.readUTF();

            System.out.println("Creating a new handler for " + tempName + "..."); 
            
            // Create a new handler object for handling this request. 
            ClientHandler mtch = new ClientHandler(s, tempName, dis, dos, log); 
  
            // Create a new Thread with this object. 
            Thread t = new Thread(mtch); 
              
            System.out.println("Adding this client to active client list"); 
  
            // add this client to active clients list 
            ar.add(mtch); 
            
            log.add("User " + tempName + " connected.");

            // start the thread. 
            t.start(); 
  
            // increment i for new client. 
            // i is used for naming only, and can be replaced 
            // by any naming scheme 
            i++; 
  
        } 
    } 
} 
  
// ClientHandler class 
class ClientHandler implements Runnable  
{ 
    Scanner scn = new Scanner(System.in); 
    private String name; 
    final DataInputStream dis; 
    final DataOutputStream dos; 
    Socket s; 
    boolean isloggedin; 
    ArrayList<String> log;

    // constructor 
    public ClientHandler(Socket s, String name, 
                            DataInputStream dis, DataOutputStream dos, ArrayList<String> log) { 
        this.dis = dis; 
        this.dos = dos; 
        this.name = name; 
        this.s = s; 
        this.isloggedin=true; 
        this.log = log;
    } 
  
    @Override
    public void run() { 
  
        String received; 
        while (true)  
        { 
            try
            { 
                // receive the string 
                received = dis.readUTF(); 
                  
                System.out.println(this.name + ":" + received); 
                  
                if(received.equals("logout")){ 
                    this.isloggedin=false; 
                    this.s.close(); 
                    System.out.println("Client disconnecting!");
                    log.add(this.name + " disconnected from the server.");
                    break; 
                } 
                
                log.add(this.name + ": " + received);

                // send to all other users
                // ar is the vector storing client of active users 
                for (ClientHandler mc : Server.ar)  
                { 
                    // if the recipient is found, write on its 
                    // output stream 
                    if (!mc.name.equals(this.name) && mc.isloggedin==true)  
                    { 
                        mc.dos.writeUTF(this.name+" : "+ received); 
                    } 
                } 
            } catch (IOException e) { 
                  
                e.printStackTrace(); 
            } 
              
        } 
        try
        { 
            // closing resources 
            this.dis.close(); 
            this.dos.close(); 
              
        }catch(IOException e){ 
            e.printStackTrace(); 
        } 
    } 
} 