import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.Scanner;

public class Server {
    public Server() throws Exception
    {
        ServerSocket server = new ServerSocket(2021);
        while (true)
        {
            Socket socket = server.accept(); // to accept incoming connections.
            ServerThread serverThread = new ServerThread(socket,this);
            Thread thread = new Thread(serverThread);
            thread.start(); //start a thread for each incoming connection.
        }
    }

    public static void main(String[] args) //Opening server at port 2021.
    {
        try {
            new Server();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}

class FindFile { //FindFile class that is responsible for finding the file requested in the project directory.
    private String file_path = "not found";
    //String function that compares all file in directory with the filename requested and then return it.
    public String check_through(String path, String filename)
    {
        File direct = new File(path);
        //list of files and folders in directory.
        File[] list = direct.listFiles();

        //loop through files and after it finishes its returns targeted file path.
        for (File f : list) {
            if (!file_path.equals("not found"))
                break;
            if (f.isDirectory()) {
                check_through(f.getAbsolutePath(), filename);
            }
            else {
                if(f.getName().contains(filename)) {
                    file_path = f.getAbsolutePath();
                }
            }
        }
        return file_path;
    }
}

class ServerThread implements Runnable { //Server threading to allow multiple clients to connect at the same time.
    private Socket socket;
    private Server server;

    public ServerThread(Socket socket,Server server){
        this.socket=socket;
        this.server=server;
    }

    @Override
    public void run(){
        try {
            //buffers for reading client messages and responding to it.
            BufferedReader incoming_socket = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter outgoing_socket = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);

            outgoing_socket.println("Please Enter the name of the required file");
            String message = incoming_socket.readLine();
            System.out.println("Request received");
            //FindFiles class which goes through all project folders starting with the main folder to find targeted file.
            FindFile myFile = new FindFile();
            //recording the path returned by FindFiles class and then splitting its folders in a string array.
            String path = myFile.check_through("..", message);
            String[] arr = path.split("\\\\");

            //path of file is split to get folder(host) and subfolder paths containing the file.
            if(arr.length>=3) { //if array length is >=3 this means the file is found within a host in the same sequence requested.
                System.out.println("GET/" + arr[arr.length - 2] + "/" + message + " HTTP/1.1\nHost: " + arr[arr.length - 3]);
            }else{ //means the file is not found as there is no host or subfolder directory provided.
                System.out.println("File requested by client couldn't be found or has an invalid Path!");
            }

            //File object initialized with the object path of the searched file.
            File resFile = new File(path);
            //check for file existence.
            if(resFile.exists()) //if the project exists if will return status code 200, date, time and File requested.
            {
                outgoing_socket.println("HTTP/1.1 200 OK");
                Date date = new Date();
                outgoing_socket.println(date.toString());
                Scanner fileReader = new Scanner(resFile); //Scanner object that reads file contents and a while loop to send the contents to client side.
                while (fileReader.hasNextLine()) {
                    String data = fileReader.nextLine();
                    outgoing_socket.println(data);
                }
                //to mark end of file we send EOF string which is catched by the other side and it indicates that no more data will be sent.
                outgoing_socket.println("EOF");
                fileReader.close();

            }else{ //if file was not found it will send status code 404, date and time only.
                outgoing_socket.println("HTTP/1.1 404 Not Found");
                Date date = new Date();
                outgoing_socket.println(date.toString());
            }
            socket.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}