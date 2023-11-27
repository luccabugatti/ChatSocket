import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Conexao implements Runnable {

    // Lista de conexões usando CopyOnWriteArrayList para evitar ConcurrentModificationException
    public static List<Conexao> conexoes = new CopyOnWriteArrayList<>();

    // Constantes para o arquivo de log e o padrão de formatação de data e hora
    static final String ARQUIVO_LOG = "historico.txt";
    static final String pattern = "HH:mm";

    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String usuarioNome;
    private List<String> usuariosBloqueados = new ArrayList<>();

    // Construtor que recebe um Socket para inicializar a conexão
    public Conexao(Socket socket) {
        try {
            this.socket = socket;
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.usuarioNome = bufferedReader.readLine();

            // Adiciona a conexão à lista global de conexões
            conexoes.add(this);

            LocalDateTime agora = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
            String agoraFormatado = agora.format(formatter);
            transmissaoDeMenssagem(agoraFormatado + " SERVIDOR: " + usuarioNome + " entrou no chat.");
        } catch (IOException e) {
            encerrarTudo();
        }
    }

    // Método run da interface Runnable, responsável por receber e processar mensagens do usuário
    @Override
    public void run() {
        String mensagemDoUsuario;

        while (socket.isConnected()) {
            try {
                // Lê a mensagem do usuário
                mensagemDoUsuario = bufferedReader.readLine();

                // Processa comandos de bloqueio e desbloqueio ou envia a mensagem ao grupo
                if (mensagemDoUsuario.trim().toLowerCase().startsWith("/bloquear")) {
                    usuariosBloqueados.add(mensagemDoUsuario.substring("/bloquear ".length()));
                } else if(mensagemDoUsuario.trim().toLowerCase().startsWith("/desbloquear")){
                    usuariosBloqueados.remove(mensagemDoUsuario.substring("/desbloquear ".length()));
                } else {
                    transmissaoDeMenssagem(mensagemDoUsuario);
                }
            } catch (IOException e) {
                encerrarTudo();
                break;
            }
        }
    }

    // Método para transmitir mensagens a todos os usuários, incluindo tratamento de mensagens privadas
    public void transmissaoDeMenssagem(String mensagemParaEnviar) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);

        for (Conexao conexao : conexoes) {
            try {
                // Trata mensagens privadas
                if(mensagemParaEnviar.trim().toLowerCase().startsWith("/privado")){
                    String[] partes = mensagemParaEnviar.split(" ", 3);
                    String destinatario = partes[1];
                    String mensagemPrivada = partes[2];

                    LocalDateTime agora = LocalDateTime.now();
                    String agoraFormatado = agora.format(formatter);
                    String mensagem = agoraFormatado + " (Privado para " + destinatario + ") " + usuarioNome + ": " + mensagemPrivada;

                    // Envia a mensagem privada apenas para o destinatário
                    if (!usuariosBloqueados.contains(destinatario) && !conexao.usuariosBloqueados.contains(usuarioNome) && conexao.usuarioNome.equals(destinatario)) {
                        conexao.bufferedWriter.write(mensagem);
                        conexao.bufferedWriter.newLine();
                        conexao.bufferedWriter.flush();
                        adicionarAoLog(mensagem);
                    }   
                } else if (!usuariosBloqueados.contains(conexao.usuarioNome) && !conexao.usuariosBloqueados.contains(usuarioNome)){
                    // Envia mensagens normais a todos os usuários, exceto ao remetente
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

    // Método para remover a conexão da lista global e enviar mensagem de saída
    public void removeConexao() {
        conexoes.remove(this);

        LocalDateTime agora = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        String agoraFormatado = agora.format(formatter);
        transmissaoDeMenssagem(agoraFormatado + " SERVIDOR: " + usuarioNome + " saiu do chat.");
    }

    // Método para encerrar tudo, chamando o método de remoção e fechando os recursos
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

    // Método privado e sincronizado para adicionar mensagens ao log de forma segura
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
