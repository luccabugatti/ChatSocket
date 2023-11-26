import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Usuario {

    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String usuarioNome;

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

    public void enviarMensagem() {
        LocalDateTime agora;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

        try {
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
                    agora = LocalDateTime.now();
                    String agoraFormatado = agora.format(formatter);
                    String mensagem = agoraFormatado + " " + usuarioNome + ": " + mensagemParaEnviar;
                    bufferedWriter.write(mensagem);
                    bufferedWriter.newLine();
                    bufferedWriter.flush(); 
                    adicionarAoLog(mensagem);
                }
            }
            scanner.close();
        } catch (IOException e) {
            e.printStackTrace();
            fecharTudo();
        }
    }

    public void aguardarMensagem() {
        new Thread(() -> {
            while (socket.isConnected()) {
                try {
                    String mensagemParaGrupo = bufferedReader.readLine();
                    System.out.println(mensagemParaGrupo);
                } catch (IOException e) {
                    e.printStackTrace();
                    fecharTudo();
                }
            }
        }).start();
    }

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

    private void adicionarAoLog(String mensagem) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(Conexao.ARQUIVO_LOG, true))) {
            writer.write(mensagem);
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Digite seu nome: ");
        String usuarioNome = scanner.nextLine();

        try (Socket socket = new Socket("localhost", 6666)) {
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
