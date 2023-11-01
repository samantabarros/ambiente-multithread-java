import java.io.*;
import java.net.*;
import java.util.*;

public class Multithread implements Runnable {
    private int port;
    private List<String> allAddresses;
    private ServerSocket serverSocket;

    public Multithread(int port, List<String> allAddresses) {
        this.port = port;
        this.allAddresses = allAddresses;
    }

    // Função para enviar mensagem para todos os outros Multithreads
    public void broadcastMessage(String message) {
        for (String address : allAddresses) {
            if (!address.equals("localhost:" + port)) { // Não envia para si mesmo
                try (Socket socket = new Socket("localhost", Integer.parseInt(address.split(":")[1]))) {
                    PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
                    writer.println(message);
                } catch (IOException e) {
                    System.err.println("Falha ao enviar mensagem para " + address);
                }
            }
        }
    }

    // Função para lidar com conexões recebidas
    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Multithread iniciado na porta " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(() -> {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
                        String receivedMessage = reader.readLine();
                        System.out.println(port + " recebeu: " + receivedMessage);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        List<String> addresses = Arrays.asList("localhost:8080", "localhost:8081", "localhost:8082"); // Lista de endereços de todos os Multithreads

        // Iniciar todos os Multithreads
        for (String address : addresses) {
            int port = Integer.parseInt(address.split(":")[1]);
            Multithread Multithread = new Multithread(port, addresses);
            new Thread(Multithread).start();
        }

        // Pausa para garantir que todos os servidores estejam em execução
        Thread.sleep(2000);

        // Simulação: Cada Multithread envia uma mensagem para todos os outros
        for (String address : addresses) {
            int port = Integer.parseInt(address.split(":")[1]);
            Multithread clientMultithread = new Multithread(port, addresses);
            clientMultithread.broadcastMessage("Olá do Multithread na porta " + port);
        }
    }
}
