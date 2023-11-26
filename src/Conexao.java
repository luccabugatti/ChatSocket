import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Conexao implements Runnable {

    public static List<Conexao> conexoes = new CopyOnWriteArrayList<>();

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
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
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

    public void transmissaoDeMenssagem(String menssagemParaEnviar) {
        for (Conexao conexao : conexoes) {
            try {
                if (!conexao.usuarioNome.equals(usuarioNome)) {
                    conexao.bufferedWriter.write(menssagemParaEnviar);
                    conexao.bufferedWriter.newLine();
                    conexao.bufferedWriter.flush();
                }
            } catch (IOException e) {
                encerrarTudo();
            }
        }
    }

    public void removeConexao() {
        conexoes.remove(this);

        LocalDateTime agora = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
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
}
