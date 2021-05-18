package my.java.project.spotify.server;

import my.java.project.spotify.command.Command;
import my.java.project.spotify.exceptionlogger.ErrorLogger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

public class Server {

    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 7777;
    private static final int BUFFER_SIZE = 1024;

    private  boolean isOpen;
    private final Command commandExecutor;


    Server(){
        isOpen=true;
        commandExecutor=new Command();
    }

    public void start(){
        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {

            serverSocketChannel.bind(new InetSocketAddress(SERVER_HOST, SERVER_PORT));
            serverSocketChannel.configureBlocking(false);

            Selector selector = Selector.open();
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);

            while(isOpen){
                communicateWithChannels(selector, buffer);
            }
        } catch (IOException e) {
            ErrorLogger.logClientError(e);
            throw new RuntimeException("There is a problem with the server socket", e);
        }
    }
    private void manageKeys(Iterator<SelectionKey> keyIterator, ByteBuffer buffer, Selector selector) {
        try {
            SelectionKey key = keyIterator.next();
            if (key.isReadable()) {
                SocketChannel sc = (SocketChannel) key.channel();

                buffer.clear();
                int r = sc.read(buffer);
                if (r <= 0) {
                    sc.close();
                    return;
                }
                buffer.flip();
                byte[] messageBytes = new byte[buffer.remaining()];
                buffer.get(messageBytes);
                String message = new String(messageBytes, StandardCharsets.UTF_8);
                message = message.replace(System.lineSeparator(), "");

                int scHash = sc.hashCode();
                String reply = commandExecutor.execute(message,scHash);

                buffer.clear();
                buffer.put(reply.getBytes());
                buffer.flip();
                sc.write(buffer);

            } else if (key.isAcceptable()) {
                ServerSocketChannel sockChannel = (ServerSocketChannel) key.channel();
                SocketChannel accept = sockChannel.accept();
                accept.configureBlocking(false);
                accept.register(selector, SelectionKey.OP_READ);
            }

            keyIterator.remove();
        } catch (IOException e) {
            ErrorLogger.logClientError(e);
            throw new RuntimeException("There is a problem with the selection keys", e);
        }
    }

    private void communicateWithChannels(Selector selector, ByteBuffer buffer) {
        try {
            int readyChannels = selector.select();
            if (readyChannels == 0) {
                return;
            }

            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

            while (keyIterator.hasNext()) {
                manageKeys(keyIterator, buffer, selector);
            }

        } catch (IOException e) {
            ErrorLogger.logClientError(e);
            throw new RuntimeException("There is a problem with the channel communication", e);
        }
    }



    public static void main(String[] args) {

        Server spotify = new Server();
        spotify.start();
    }
}
