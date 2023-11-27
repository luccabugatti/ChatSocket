

# Chat utilizando Socket em Java

Este projeto de chat em Java com Socket oferece uma plataforma de comunicação em tempo real, permitindo que usuários se conectem e interajam em um ambiente de chat dinâmico. Desenvolvido em Java, o projeto utiliza o conceito de sockets para facilitar a comunicação entre o servidor e os clientes, proporcionando uma experiência interativa e colaborativa.

![Exemplo de vários usuarios ao mesmo tempo](https://github.com/luccabugatti/ChatSocket/blob/main/assets/Exemplo.png)

### __Manual de utilização__

Para utilizar o chat tem duas maneiras, diretamente via cmd ou em uma IDE como o VSCode.

#### VSCode 

* Abra o Projeto no VSCode e de run no projeto do Servidor primeiro. Você vai saber que o servidor está ativo quando aparecer a mensagem “Servidor funcionando”

* Após isso os usuários podem se conectar, clique em run no arquivo Usuario e escreva seu nome.

* Você pode rodar várias vezes para abrir mais terminais de usuários

#### CMD

* Compile os arquivos .java na pasta src utilizando o comando "javac NomeDoArquivo.java"

* Agora rode os arquivos .class na pasta bin começando pelo Servidor ex."java Servidor"

*  Depois do servidor iniciar você pode executar os usuarios quantas vezes quiser 

### __Recursos__

**Histórico de Mensagens**:  
  
Todas as mensagens enviadas no chat são registradas em um arquivo de histórico, permitindo a revisão e recuperação de conversas anteriores.  

**Mensagens Privadas**:   
  
Os usuários podem enviar mensagens privadas diretamente uns aos outros, garantindo uma comunicação mais seletiva e personalizada.  

**Bloqueio de Usuários**:   
  
A implementação de um sistema de bloqueio permite que os usuários controlem quem podem ou não interagir com eles no chat.  

**Nomes de Usuários Únicos**:   
  
Um mecanismo de verificação de nomes de usuário assegura que cada participante tenha um nome único, mesmo considerando variações de maiúsculas e minúsculas.  

**Integração com Comandos Adicionais**:  
  
Funcionalidades adicionais, como comandos para limpar o terminal (/limpar) e sair do chat (/sair), complementam a experiência do usuário ao proporcionar opções práticas e úteis durante a interação no chat.  

### __Lista de comandos no chat__

- **/limpar:** Limpa a tela do terminal local do usuário, proporcionando uma interface mais organizada. 

- **/sair:** Permite que o usuário encerre a aplicação de forma controlada, facilitando a saída segura do chat e desconexão do servidor. 

- **/privado <destinatário> <mensagem>**: Envia uma mensagem privada para o usuário especificado. Exemplo: /privado Caio Olá, como está? 

- **/bloquear <usuário>**: Bloqueia as mensagens do usuário especificado, impedindo que suas mensagens sejam recebidas. 

- **/desbloquear <usuário>**: Remove o bloqueio das mensagens do usuário especificado, permitindo que suas mensagens sejam recebidas novamente.


