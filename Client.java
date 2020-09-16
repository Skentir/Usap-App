import java.io.*; 
import java.net.*; 
import java.util.*; 
  
public class Client  
{ 
    private Socket s;
    private OutputStreamWriter dos;
    private BufferedWriter wr;
    private String name;
    // Connect - connect client to a server socket
    public void connect(String ip, String port, String username) throws IOException {
        s = new Socket(ip, Integer.parseInt(port));
        dos = new OutputStreamWriter(s.getOutputStream());
        wr = new BufferedWriter(dos);
        name = username;
        // Name of client
        wr.write(this.name);
        wr.flush();
    }

    public void sendMsg(String msg) throws IOException {
        if (msg.equals("logout")) {
            wr.write("Disconnected");
        } else {
            wr.write(this.name + " : " + msg);
        }
        wr.flush();
    }

    public void listen() throws IOException {
        InputStreamReader dis = new InputStreamReader(s.getInputStream());
        BufferedReader br = new BufferedReader(dis);

        String msg = "";
        while (msg != "logout")  {
            if(br.ready())
                msg = br.readLine();
        }
    }

    public static void main(String []args) throws IOException { 
        Client app = new Client(); 
        app.connect(args[0], args[1], args[2]); 
        app.listen(); 
    }

} 