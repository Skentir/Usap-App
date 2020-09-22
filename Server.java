// Java implementation of  Server side 
// It contains two classes : Server and ClientHandler 
// Save file as Server.java 
  
import java.io.*; 
import java.util.*; 
import java.net.*; 
import java.time.*; 
import java.time.format.DateTimeFormatter; 
import java.nio.charset.*;
import java.nio.file.*;
import javax.swing.*;
  
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
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() { 
                System.out.println("[" + (LocalTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME)) + "] " + "Shutting down server!");
                log.add("[" + (LocalTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME)) + "] " + "Server shutdown.");
                System.out.println("====================");
                Scanner scan = new Scanner(System.in);
                System.out.print("Would you like to save history to a log file? (y/n) ");
                String yesno = scan.nextLine();
                if(yesno.equalsIgnoreCase("y")){
                    String currentDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH-mm-ss"));
                    try{
                        Path file = Paths.get("[" + currentDateTime + "] server log.txt");
                        Files.write(file, log, StandardCharsets.UTF_8);
                        System.out.println("Successfully wrote log file! Shutting down server for good.");
                    }catch (IOException e){
                        System.out.println("Error with creating log!");
                        e.printStackTrace();
                    }
                }
             }
         });
        ServerSocket ss = new ServerSocket(Integer.parseInt(args[0])); 
          
        Socket s; 
        
        System.out.println("Server running at port " + args[0] + ".\nUse CTRL+C to exit the server.\n====================");

        System.out.println("[" + (LocalTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME)) + "] " + "Server started! Listening at port " + args[0]);
        log.add("[" + (LocalTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME)) + "] " + "Server started. Port " + args[0]);

        // running infinite loop for getting 
        // client request 
        while (true)  
        { 
            // Accept the incoming request 
            s = ss.accept(); 
  
            //System.out.println("New client request received : " + s); 
              
            // obtain input and output streams 
            DataInputStream dis = new DataInputStream(s.getInputStream()); 
            DataOutputStream dos = new DataOutputStream(s.getOutputStream()); 
            
            String tempName = dis.readUTF();

            //System.out.println("Creating a new handler for " + tempName + "..."); 
            
            // Create a new handler object for handling this request. 
            ClientHandler mtch = new ClientHandler(s, tempName, dis, dos, log); 
  
            // Create a new Thread with this object. 
            Thread t = new Thread(mtch); 
              
            System.out.println("[" + (LocalTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME)) + "] " + tempName + " connected!"); 
  
            // add this client to active clients list 
            ar.add(mtch); 
            
            log.add("[" + (LocalTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME)) + "] " + "User " + tempName + " connected.");

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
    public ClientHandler(Socket s, String name, DataInputStream dis, DataOutputStream dos, ArrayList<String> log) { 
        this.dis = dis; 
        this.dos = dos; 
        this.name = name; 
        this.s = s; 
        this.isloggedin=true; 
        this.log = log;
    } 
  
    @Override
    public void run() { 
        
        try
        {
            for (ClientHandler mc : Server.ar)  
            { 
                // if the recipient is found, write on its 
                // output stream 
                if (!mc.name.equals(this.name) && mc.isloggedin==true)  
                { 
                    mc.dos.writeUTF(this.name + " has connected.\n"); 
                } 
            }
        } catch (Exception e){
            e.printStackTrace();
        }

        String received; 
        while (true)  
        { 
            try
            { 
                // receive the string 
                received = dis.readUTF(); 
                  
                if(received.equals("#logout")){ 
                    for (ClientHandler mc : Server.ar)  
                    { 
                        // if the recipient is found, write on its 
                        // output stream 
                        if (!mc.name.equals(this.name) && mc.isloggedin==true)  
                        { 
                            mc.dos.writeUTF(this.name + " has disconnected.\n"); 
                        } 
                    } 
                    this.isloggedin=false; 
                    this.s.close(); 
                    System.out.println("[" + (LocalTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME)) + "] " + "Client " + this.name + " disconnecting!");
                    log.add("[" + (LocalTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME)) + "] " + this.name + " disconnected from the server.");
                    break; 
                }else if(received.contains("#filesend")){
                    String[] names = received.split("#");
                    System.out.println("[" + (LocalTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME)) + "] " + "Receiving file " + names[names.length-1] + " from " + this.name);
                    log.add("[" + (LocalTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME)) + "] " + "Receiving file " + names[names.length-1] + " from " + this.name);
                    System.out.println("[" + (LocalTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME)) + "] " + "Sending " + names[names.length-1] + " to the other clients.");
                    for(ClientHandler mc: Server.ar){
                        if (!mc.name.equals(this.name) && mc.isloggedin==true)  
                        { 
                            mc.dos.writeUTF("#filesend#" + names[names.length-1] + "#" + this.name); 
                        }
                    }
                } 
                else{
                    System.out.println("[" + (LocalTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME)) + "] " + this.name + ": " + received); 
                    log.add("[" + (LocalTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME)) + "] " + this.name + ": " + received);

                    // send to all other users
                    // ar is the vector storing client of active users 
                    for (ClientHandler mc : Server.ar)  
                    { 
                        // if the recipient is found, write on its 
                        // output stream 
                        if (!mc.name.equals(this.name) && mc.isloggedin==true)  
                        { 
                            mc.dos.writeUTF(this.name+" : "+ received+"\n"); 
                        } 
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
            Server.ar.remove(this);
            Server.i--;
        }catch(IOException e){ 
            e.printStackTrace(); 
        } 
    } 
} 