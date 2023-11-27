import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Conexao implements Runnable {

    public static List<Conexao> conexoes = new CopyOnWriteArrayList<>();
    static final String ARQUIVO_LOG = "historico.txt";
    static final String pattern = "HH:mm";

    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String usuarioNome;

    public Conexao(Socket socket) {
        try {
            this.socket = socket;
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.usuarioNome = bufferedReader.readLine();

            conexoes.add(this);

            LocalDateTime agora = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
            String agoraFormatado = agora.format(formatter);
            transmissaoDeMenssagem(agoraFormatado + " SERVIDOR: " + usuarioNome + " entrou no chat.");
        } catch (IOException e) {
            encerrarTudo();
        }
    }

    @Override
    public void run() {
        String menssagemDoUsuario;

        while (socket.isConnected()) {
            try {
                menssagemDoUsuario = bufferedReader.readLine();
                transmissaoDeMenssagem(menssagemDoUsuario);
            } catch (IOException e) {
                encerrarTudo();
                break;
            }
        }
    }

    public void transmissaoDeMenssagem(String mensagemParaEnviar) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);

        for (Conexao conexao : conexoes) {
            try {
                if(mensagemParaEnviar.trim().toLowerCase().startsWith("/privado")){
                    String[] partes = mensagemParaEnviar.split(" ", 3);
                    String destinatario = partes[1];
                    String mensagemPrivada = partes[2];

                    LocalDateTime agora = LocalDateTime.now();
                    String agoraFormatado = agora.format(formatter);
                    String mensagem = agoraFormatado + " (Privado para " + destinatario + ") " + usuarioNome + ": " + mensagemPrivada;

                    if (conexao.usuarioNome.equals(destinatario)) {
                        conexao.bufferedWriter.write(mensagem);
                        conexao.bufferedWriter.newLine();
                        conexao.bufferedWriter.flush();
                        adicionarAoLog(mensagem);
                    }   
                }else{
                    LocalDateTime agora = LocalDateTime.now();
                    String agoraFormatado = agora.format(formatter);
                    String mensagem = agoraFormatado + " " + usuarioNome + ": " + mensagemParaEnviar;

                    if (!conexao.usuarioNome.equals(usuarioNome)) {
                        conexao.bufferedWriter.write(mensagem);
                        conexao.bufferedWriter.newLine();
                        conexao.bufferedWriter.flush();
                        adicionarAoLog(mensagem);
                    }
                }               
            } catch (IOException e) {
                encerrarTudo();
            }
        }
    }

    public void removeConexao() {
        conexoes.remove(this);

        LocalDateTime agora = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        String agoraFormatado = agora.format(formatter);
        transmissaoDeMenssagem(agoraFormatado + " SERVIDOR: " + usuarioNome + " saiu do chat.");
    }

    public void encerrarTudo() {
        removeConexao();
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

    private static synchronized void adicionarAoLog(String mensagem) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(ARQUIVO_LOG, true))) {
            writer.write(mensagem);
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
