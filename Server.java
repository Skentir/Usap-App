import java.io.*; 
import java.net.*; 
import java.util.*; 
  
  
// Server class 
public class Server extends Thread 
{ 
	private static ArrayList<BufferedWriter> clients;           
	private static ServerSocket server; 
	private String nome;
	private Socket con;
	private InputStream in;  
	private InputStreamReader inr;  
	private BufferedReader bfr; 

    // Constructor
    public Server(Socket con){
		this.con = con;
		try {
			in  = con.getInputStream();
			inr = new InputStreamReader(in);
			bfr = new BufferedReader(inr);
		} catch (IOException e) {
			  e.printStackTrace();
		}                          
	}       

    // Run - Everytime a client connects, a new thread is allocated
    public void run() {
		try{								 
			String msg;
			OutputStream ou =  this.con.getOutputStream();
			Writer ouw = new OutputStreamWriter(ou);
			BufferedWriter bfw = new BufferedWriter(ouw); 
			clients.add(bfw);
			nome = msg = bfr.readLine();
				  
			while(!"Logout".equalsIgnoreCase(msg) && msg != null)
			{           
				msg = bfr.readLine();
				sendToAll(bfw, msg);
				System.out.println(msg);                                              
			}										 
		}catch (Exception e) {
				e.printStackTrace();

		} 
    }

	// sendToAll - when the server receives a message, it sends it to all other connected clients
	public void sendToAll(BufferedWriter bwOutput, String msg) throws  IOException 
	{
		BufferedWriter bwS;

		for(BufferedWriter bw :  clients){
			bwS = (BufferedWriter)bw;
			if(!(bwOutput == bwS)){
				bw.write(nome + " -> " + msg+"\r\n");
				bw.flush(); 
			}
		}          
	}
	
	// main
    public static void main(String[] args) throws IOException  
    { 
		try{
			//Cria os objetos necessário para instânciar o server
			/*JLabel lblMessage = new JLabel("Server Port:");
			JTextField txtPort = new JTextField("12345");
			Object[] texts = {lblMessage, txtPort };  
			JOptionPane.showMessageDialog(null, texts);*/
			server = new ServerSocket(Integer.parseInt(args[0]));
			clients = new ArrayList<BufferedWriter>();
			//JOptionPane.showMessageDialog(null,"Active Server at Port: "+ txtPort.getText());
			System.out.println("Server listening at Port " + args[0]);

			while(true){
				System.out.println("Waiting for connection...");
				Socket con = server.accept();
				System.out.println("Client connected...");
				Thread t = new Server(con);
				t.start();   
			}
								 
		}catch (Exception e) {
			e.printStackTrace();
		} 
    } 
} 