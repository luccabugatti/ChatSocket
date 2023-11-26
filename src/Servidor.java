import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Servidor {

    private final ServerSocket servidorSocket;

    public Servidor(ServerSocket servidorSocket) {
        this.servidorSocket = servidorSocket;
    }

    public void iniciarServidor() {
        try {
            // Aguarda os usuarios se conectarem na porta 6666.           
            while (!servidorSocket.isClosed()) {

                // Será fechado no Usuario.
                Socket socket = servidorSocket.accept();
                LocalDateTime agora = LocalDateTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
                String agoraFormatado = agora.format(formatter);

                System.out.println(agoraFormatado + " Um novo usuário foi conectado!");

                Conexao conexao = new Conexao(socket);
                Thread thread = new Thread(conexao);

                // O metodo start começa uma nova thread.
                // Ao chamar start() o metodo run é iniciado.
                thread.start();
            }
        } catch (IOException e) {
            encerrarServidor();
        }
    }

    // Fecha o servidor
    public void encerrarServidor() {
        try {
            if (servidorSocket != null) {
                servidorSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Roda o programa
    public static void main(String[] args) throws IOException {
        try (ServerSocket servidorSocket = new ServerSocket(6666)) {
            Servidor server = new Servidor(servidorSocket);
            System.out.println("Servidor funcionando");
            server.iniciarServidor();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
