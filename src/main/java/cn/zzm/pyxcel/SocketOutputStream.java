package cn.zzm.pyxcel;

import javax.websocket.Session;
import java.io.IOException;
import java.io.OutputStream;

public class SocketOutputStream extends OutputStream {
    private Session session;
    private static final int BUFFER_SIZE = 128*1024;
    private byte[] buffer = new byte[BUFFER_SIZE];
    private int position = 0;
    private boolean stop = false;
    public SocketOutputStream(Session session) {
        this.session = session;
        new Thread(()->{
            while(!stop && session.isOpen()){
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                this.flush();
            }
        }).start();
    }

    @Override
    public void write(int b) throws IOException {
        if(position + 1>=buffer.length){
            this.flush();
        }
        buffer[position ++ ] = (byte)b;
    }

    @Override
    public void write(byte[] b) throws IOException {
        if(position + b.length >= buffer.length){
            flush();
        }
        System.arraycopy(b, 0, buffer, position, b.length);
        position += b.length;
    }

    @Override
    public synchronized void flush(){
        if(position == 0) return;
        byte[] msgBuf = new byte[position];
        System.arraycopy(buffer, 0, msgBuf, 0, position );
        session.getAsyncRemote().sendText(new String(msgBuf));
        position = 0;
    }
}
