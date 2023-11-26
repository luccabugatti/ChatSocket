// 1. Abre um socket.
// 2. Abre uma input stream e uma output stream para o socket.
// 3. Lê e escreve para a stream de acordo com o protocolo do servidor.
// 4. Fecha as streams.
// 5. Fecha o socket.

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Quando um usuário se conecta ao servidor, uma nova thread é gerada para
 * conter o usuário.
 * Desta forma multiplos usuários podem entrar e sair do servidor.
 */

// As instancias do Runnable são implementadas em uma classe onde serão
// executadas por threads.

public class Conexao implements Runnable {

    // Uma lista de Arrays que possui todas as threads que estão com usuarios para
    // que todas as mensagens sejam encaminhadas para o usuário também.

    public static ArrayList<Conexao> conexoes = new ArrayList<>();

    // Id que será incrementado a cada usuário.

    // Socket para a conexão, leitor e escritor por buffer para receber e encaminhar
    // as mensagens.

    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String usuarioNome;

    // Criando a conexão de usuário com cada socket que o servidor passa.

    public Conexao(Socket socket) {
        try {
            this.socket = socket;
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            // Quando um usuário for conectado o seu nome é apresentado.

            this.usuarioNome = bufferedReader.readLine();

            // Adiciona o novo handler de usuário ao Array, assim podendo receber as
            // mensagens dos outros usuários.

            conexoes.add(this);

            // Adiciona a funcionalidade de Hora na qual a mensagem foi encaminhada visto
            // que isso é importantissímo em um chat.

            LocalDateTime agora = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            String agoraFormatado = agora.format(formatter);
            transmissaoDeMenssagem(agoraFormatado + " SERVIDOR: " + usuarioNome + " entrou no chat.");
        } catch (IOException e) {

            // Fecha tudo.

            encerrarTudo(socket, bufferedReader, bufferedWriter);
        }
    }

    // Tudo nesta thread é processado em uma thread separada, precisamos aguardar
    // estas mensagens em uma thread separada pois ler o (bufferedReader.readLine())
    // bloqueia a operação.
    // Caso fosse bloqueada, a operação que chama ficaria aguardando a ligação que
    // atende finalizar.

    @Override
    public void run() {
        String menssagemDoUsuario;

        // Continua aguardando a mensagem enquanto o usuário permanecer conectado.

        while (socket.isConnected()) {
            try {

                // Lê o que o usuário inserir e mostra para todos os outros usuários.

                menssagemDoUsuario = bufferedReader.readLine();
                transmissaoDeMenssagem(menssagemDoUsuario);
            } catch (IOException e) {

                // Fecha tudo.

                encerrarTudo(socket, bufferedReader, bufferedWriter);
                break;
            }
        }
    }

    // Encaminha a mensagem por todas as threads transmissoras para que todos
    // recebam as mensagens.
    // Após a conexão ser estabelecida, toda mensagem que for recebida passa por um
    // loop em todas as conexões e é
    // encaminhada por elas.

    public void transmissaoDeMenssagem(String menssagemParaEnviar) {
        for (Conexao conexao : conexoes) {
            try {

                // Impede que a mensagem inserida seja apresentada para quem inseriu.

                if (!conexao.usuarioNome.equals(usuarioNome)) {
                    conexao.bufferedWriter.write(menssagemParaEnviar);
                    conexao.bufferedWriter.newLine();
                    conexao.bufferedWriter.flush();
                }
            } catch (IOException e) {

                // Fecha tudo.

                encerrarTudo(socket, bufferedReader, bufferedWriter);
            }
        }
    }

    // Caso o usuário se desconectar, remove esta ligação da lista para que a
    // mensagem não seja encaminhada a uma conexão perdida.

    public void removeConexao() {
        conexoes.remove(this);

        // Adiciona a funcionalidade de Hora na qual a mensagem foi encaminhada visto
        // que isso é importantissímo em um chat.

        LocalDateTime agora = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        String agoraFormatado = agora.format(formatter);
        transmissaoDeMenssagem(agoraFormatado + " SERVIDOR: " + usuarioNome + " saiu do chat.");
    }

    // Criação do método que fecha todos processos para que não tenham que ser
    // feitos manualmente.

    public void encerrarTudo(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {

        // Neste cenario só é necessário fechar o wrapper externo visto que todos os
        // outros serão encerrados com ele.
        // Fechar o socket também acaba com o fluxo de input e output.

        // Apaga a conexão caso o usuário seja desconectado ou algum erro tenha ocorrido
        // para remover o usuário da lista e não encaminhar nenhuma mensagem.

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
