
// Server class 
public class Server extends Thread 
{ 
     private static ServerSocket ss; 
     private String nome; 
     private Socket s;
     private DataInputStream dis; 
     private BufferedReader bfr;
     // Store clients
     private static ArrayList<BufferedWriter> clients;   

    // Constructor
    public Server (Socket s) {
        this.s = s;
        try {
            dis = new DataInputStream(s.getInputStream());
            bfr = new BufferedReader(dis); 
        } catch (IOException e) {
            e.printStackTrace()
        }
    }

    // Run - Everytime a client connects, a new thread is allocated
    public void run() {
        try {
            String msg;
            DataOutputStream dos = this.s.getOutputStream();
            BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(dos));
            clients.add(wr); 
            
            while(!"logout".equalsIgnoreCase(msg)) {
                msg = wr.readLine()
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException  
    { 
        // server is listening on port 1234 
        ServerSocket ss = new ServerSocket(1234); 
          
        Socket s; 
          
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
              
            System.out.println("Creating a new handler for this client..."); 
  
            // Create a new handler object for handling this request. 
            ClientHandler mtch = new ClientHandler(s,"client " + i, dis, dos); 
  
            // Create a new Thread with this object. 
            Thread t = new Thread(mtch); 
              
            System.out.println("Adding this client to active client list"); 
  
            // add this client to active clients list 
            ar.add(mtch); 
  
            // start the thread. 
            t.start(); 
  
            // increment i for new client. 
            // i is used for naming only, and can be replaced 
            // by any naming scheme 
            i++; 
  
        } 
    } 
} 