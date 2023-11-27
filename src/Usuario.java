import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Usuario {

    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String usuarioNome;

    // Construtor que inicializa a conexão do usuário
    public Usuario(Socket socket, String usuarioNome) {
        try {
            this.socket = socket;
            this.usuarioNome = usuarioNome;
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        } catch (IOException e) {
            e.printStackTrace();
            fecharTudo();
        }
    }

    // Método para enviar mensagens
    public void enviarMensagem() {
        try {
            // Envia o nome de usuário ao servidor
            bufferedWriter.write(usuarioNome);
            bufferedWriter.newLine();
            bufferedWriter.flush();

            Scanner scanner = new Scanner(System.in);
            while (socket.isConnected()) {
                String mensagemParaEnviar = scanner.nextLine();
                if("/sair".equalsIgnoreCase(mensagemParaEnviar)){
                    System.exit(0);
                }else if("/limpar".equalsIgnoreCase(mensagemParaEnviar)){
                   limparTerminal(); 
                }else{
                    // Envia a mensagem ao servidor
                    bufferedWriter.write(mensagemParaEnviar);
                    bufferedWriter.newLine();
                    bufferedWriter.flush(); 
                }
            }
            scanner.close();
        } catch (IOException e) {
            e.printStackTrace();
            fecharTudo();
        }
    }

    // Método para aguardar mensagens do servidor em uma thread separada
    public void aguardarMensagem() {
        new Thread(() -> {
            while (socket.isConnected()) {
                try {
                    // Lê mensagens do servidor e imprime no console
                    String mensagemParaGrupo = bufferedReader.readLine();
                    System.out.println(mensagemParaGrupo);
                } catch (IOException e) {
                    e.printStackTrace();
                    fecharTudo();
                }
            }
        }).start();
    }

    // Método para fechar todos os recursos associados ao usuário
    public void fecharTudo() {
        try {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Método estático para limpar o terminal
    public static void limparTerminal(){    
        try {
            final String os = System.getProperty("os.name");

            if (os.contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                System.out.print("\033[H\033[2J");
                System.out.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }   

    // Método principal para iniciar a aplicação do usuário
    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Digite seu nome: ");
        String usuarioNome = scanner.nextLine();

        try (Socket socket = new Socket("localhost", 6666)) {
             // Cria uma instância de usuário e inicia as operações de envio e recebimento de mensagens
            Usuario usuario = new Usuario(socket, usuarioNome);
            usuario.aguardarMensagem();
            usuario.enviarMensagem();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }
}
