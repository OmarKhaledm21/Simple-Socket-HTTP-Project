import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;


public class Client {
    private Socket socket;
    public Client() throws Exception {
        Scanner input = new Scanner(System.in);
        InetAddress clientADDRESS = InetAddress.getLocalHost();
        socket = new Socket(clientADDRESS, 2021); //start TCP connection to server.

        // buffers for reading server responses and sending requests to it.
        BufferedReader _incoming_socket = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter _outgoing_socket = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
        String status = "";
        //block of exchanges between server and client sides where client requests a file and gets responses from server.
        String exchanges = _incoming_socket.readLine();
        System.out.println(exchanges);
        exchanges = input.nextLine();
        _outgoing_socket.println(exchanges);
        exchanges = _incoming_socket.readLine();
        System.out.println("\nReplay received\n" + exchanges);
        status =exchanges;
        exchanges = _incoming_socket.readLine();
        System.out.println(exchanges);

        if(status.contains("200")) { //checks if the status code is 200 to make sure that file is found and will be sent.
            while (true) {
                exchanges = _incoming_socket.readLine();
                if (exchanges.equals("EOF")) { //EOF indicating end-of-file so that it closes loop and connection as file is finished.
                    break;
                }
                System.out.println(exchanges);
            }
        }
        socket.close();
    }

    public static void main(String[] args) {
        try {
            new Client();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}