import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Servidor {

    private final ServerSocket servidorSocket;

    // Construtor que recebe um ServerSocket para inicializar o servidor
    public Servidor(ServerSocket servidorSocket) {
        this.servidorSocket = servidorSocket;
    }

    // Método para iniciar o servidor e aguardar conexões de usuários
    public void iniciarServidor() {
        try {
            // Aguarda os usuários se conectarem na porta 6666.           
            while (!servidorSocket.isClosed()) {

                // Aceita a conexão do cliente
                Socket socket = servidorSocket.accept();
                LocalDateTime agora = LocalDateTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
                String agoraFormatado = agora.format(formatter);

                System.out.println(agoraFormatado + " Um novo usuário foi conectado!");

                // Cria uma instância de Conexao para lidar com a conexão do usuário
                Conexao conexao = new Conexao(socket);
                Thread thread = new Thread(conexao);

                // Inicia uma nova thread para a conexão do usuário
                thread.start();
            }
        } catch (IOException e) {
            encerrarServidor();
        }
    }

    // Método para encerrar o servidor
    public void encerrarServidor() {
        try {
            if (servidorSocket != null) {
                servidorSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Método principal para iniciar o servidor
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
