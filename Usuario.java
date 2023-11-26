import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// Um usuário encaminha uma mensagem ao servidor, o servidor abre uma thread para se comunicar com o usuário.
// Toda conexao com usuário é adicionada á uma lista de array, assim toda mensagem chega a todos os usuário realizando um loop entre eles.

public class Usuario {

    // Agora o usuário possiu um socket para se conectar ao servidor, um scanner e
    // escritor para receber e encaminhar as mensagens.
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

            // Fecha tudo.

            fecharTudo(socket, bufferedReader, bufferedWriter);
        }
    }

    // O processo de encaminhar uma mensagem não bloqueia a thread (ficar aguardando
    // por outra) e também não fica gerando novas threads todas as vezes.

    public void enviarMensagem() {

        // Adiciona a funcionalidade de Hora na qual a mensagem foi encaminhada visto
        // que isso é importantissímo em um chat.

        LocalDateTime agora = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        String agoraFormatado = agora.format(formatter);

        try {

            // Encaminha o nome do usuário.
            bufferedWriter.write(usuarioNome);
            bufferedWriter.newLine();
            bufferedWriter.flush();

            // Cria o scanner para que o nome seja introduzido.
            Scanner scanner = new Scanner(System.in);

            // Enquanto existir uma conexão ativa com o servidor ele permanecerá buscando
            // por novas mensagens na thread separada.
            while (socket.isConnected()) {
                String menssagemParaEnviar = scanner.nextLine();
                if("/sair".equalsIgnoreCase(menssagemParaEnviar)){
                    System.exit(0);
                }else if("/limpar".equalsIgnoreCase(menssagemParaEnviar)){
                   limparTerminal(); 
                }else{
                    bufferedWriter.write(agoraFormatado + " " + usuarioNome + ": " + menssagemParaEnviar);
                    bufferedWriter.newLine();
                    bufferedWriter.flush(); 
                }
            }
            scanner.close();
        } catch (IOException e) {

            // Fecha tudo.
            fecharTudo(socket, bufferedReader, bufferedWriter);
        }
    }

    // O ato de buscar mensagens bloqueia a thread portando foi necessário gerar uma
    // nova thread para isso.
    public void aguardarMensagem() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String menssagemParaGrupo;

                // Enquanto existir uma conexão ativa com o servidor ele permanecerá buscando
                // por novas mensagens na thread separada.
                while (socket.isConnected()) {
                    try {

                        // Recebe as mensagens encaminhadas pelos usuários e printa no console.
                        menssagemParaGrupo = bufferedReader.readLine();
                        System.out.println(menssagemParaGrupo);
                    } catch (IOException e) {

                        // Fechar todos os processos.
                        fecharTudo(socket, bufferedReader, bufferedWriter);
                    }
                }
            }
        }).start();
    }

    // Criação do método que fecha todos processos para que não tenham que ser
    // feitos manualmente.

    public void fecharTudo(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {

        // Neste cenario só é necessário fechar o wrapper externo visto que todos os
        // outros serão encerrados com ele.
        // Fechar o socket também acaba com o fluxo de input e output.
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

    // Inicia o programa
    public static void main(String[] args) throws IOException {

        // Atribui um nome e um socket para o usuário/conexão.
        Scanner scanner = new Scanner(System.in);
        System.out.print("Digite seu nome: ");
        String usuarioNome = scanner.nextLine();

        // Cria um socket para se conectar ao servidor.
        Socket socket = new Socket("localhost", 6666);

        // Passa o socket e atribui ao usuario a usuarioNome.
        Usuario usuario = new Usuario(socket, usuarioNome);

        // Loop infinito para ler e escrever mensagens.
        usuario.aguardarMensagem();
        usuario.enviarMensagem();

        scanner.close();
    }

    public final static void limparTerminal(){    
        try {
            final String os = System.getProperty("os.name");

            if (os.contains("Windows")) {
                // Se o sistema operacional for Windows, usa o comando "cls"
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                // Se for um sistema operacional baseado em Unix, usa ANSI escape codes
                System.out.print("\033[H\033[2J");
                System.out.flush();
            }
        } catch (Exception e) {
            // Lidar com exceções, se necessário
            e.printStackTrace();
        }
    }   
}
